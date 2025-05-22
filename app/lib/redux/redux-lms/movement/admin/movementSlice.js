import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchMovementRequests = createAsyncThunk(
    'movement/fetchMovementRequests',
    async ({ page = 0, size = 10 }, { rejectWithValue }) => {
        try {
            // Get userId from session storage
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                throw new Error('User ID not found in session storage');
            }

            // Use the URL with the userId from session storage
            const response = await fetch(`http://localhost:8080/lms/movement/admin/${storedUserId}/${empId}?page=${page}&size=${size}`, {
                method: 'GET',
                credentials: 'include', // This will send cookies with the request
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();

            // Filter out null values from the content array
            const filteredContent = data.content.filter(item => item !== null);

            return {
                content: filteredContent,
                pagination: {
                    currentPage: data.number,
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                    pageSize: data.pageable.pageSize
                }
            };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Async thunk to process movement requests (approve/reject)
export const processMovementRequest = createAsyncThunk(
    'movement/processMovementRequest',
    async ({ movementId, approved }, { rejectWithValue }) => {
        try {
            // Get userId from session storage
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                throw new Error('User ID not found in session storage');
            }

            const endpoint = approved
                ? `http://localhost:8080/lms/movement/process/${movementId}/${storedUserId}/${empId}`
                : `http://localhost:8080/lms/movement/reject/${movementId}/${storedUserId}/${empId}`;

            // Send the approval/rejection request
            const response = await fetch(endpoint, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    approved: approved,
                    userId: storedUserId
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            return { movementId, approved };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const processBulkMovementRequests = createAsyncThunk(
    'movement/processBulkMovementRequests',
    async ({ movementIds, approved }, { getState, rejectWithValue }) => {
        try {
            // Get userId from session storage
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                throw new Error('User ID not found in session storage');
            }

            // Get the movement requests from the state
            const state = getState();
            const movementRequests = state.movement.requests;

            // Extract employee IDs from the selected movement requests
            const approvedEmployeesToday = [];

            movementRequests.forEach(request => {
                if (request && request.publicId && movementIds.includes(request.publicId) && request.employeeID) {
                    approvedEmployeesToday.push(request.employeeID);
                }
            });

            // Create the request body
            const requestBody = {
                approvedEmployeesToday,
                approvedIds: movementIds
            };


            const endpoint = approved
                ? `http://localhost:8080/lms/bulk/approved/movement/${empId}`
                : `http://localhost:8080/lms/bulk/reject/movement/${empId}`;

            const response = await fetch(
                endpoint,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(requestBody),
                    credentials: 'include'
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();

            return {
                movementIds,
                approvedEmployeesToday,
                approved,
                response: data
            };
        } catch (error) {
            return rejectWithValue(error.message || 'An unknown error occurred');
        }
    }
);

// Create the movement slice
const movementSlice = createSlice({
    name: 'movement',
    initialState: {
        requests: [],
        selected: [],
        pagination: {
            currentPage: 0,
            totalPages: 0,
            totalElements: 0,
            pageSize: 10
        },
        loading: false,
        error: null
    },
    reducers: {
        // Handle selection of a single row
        selectMovementRequest: (state, action) => {
            const id = action.payload;
            if (state.selected.includes(id)) {
                state.selected = state.selected.filter(item => item !== id);
            } else {
                state.selected.push(id);
            }
        },

        // Handle "Select All" functionality
        selectAllMovementRequests: (state) => {
            if (state.selected.length === state.requests.length) {
                state.selected = [];
            } else {
                state.selected = state.requests.map(request => request.publicId);
            }
        },

        // Clear selected items
        clearSelectedMovementRequests: (state) => {
            state.selected = [];
        },

        // Set page size
        setPageSize: (state, action) => {
            state.pagination.pageSize = action.payload;
        }
    },
    extraReducers: (builder) => {
        builder
            // Handle fetchMovementRequests
            .addCase(fetchMovementRequests.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchMovementRequests.fulfilled, (state, action) => {
                state.loading = false;
                state.requests = action.payload.content;
                state.pagination = action.payload.pagination;
            })
            .addCase(fetchMovementRequests.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })

            // Handle processMovementRequest
            .addCase(processMovementRequest.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(processMovementRequest.fulfilled, (state, action) => {
                state.loading = false;
                // Update the status of the processed request in the state
                const index = state.requests.findIndex(request => request.publicId === action.payload.movementId);
                if (index !== -1) {
                    if (action.payload.approved) {
                        state.requests[index].accepted = true;
                        state.requests[index].pending = false;
                    } else {
                        state.requests[index].unAuthorized = true;
                        state.requests[index].pending = false;
                    }
                }
            })
            .addCase(processMovementRequest.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })

            // Handle processBulkMovementRequests
            .addCase(processBulkMovementRequests.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(processBulkMovementRequests.fulfilled, (state, action) => {
                state.loading = false;
                // For each ID in the payload, update the corresponding request
                action.payload.movementIds.forEach(id => {
                    const index = state.requests.findIndex(request => request.publicId === id);
                    if (index !== -1) {
                        if (action.payload.approved) {
                            state.requests[index].accepted = true;
                            state.requests[index].pending = false;
                        } else {
                            state.requests[index].unAuthorized = true;
                            state.requests[index].pending = false;
                        }
                    }
                });
                // Clear selection after bulk processing
                state.selected = [];
            })
            .addCase(processBulkMovementRequests.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

// Export actions
export const {
    selectMovementRequest,
    selectAllMovementRequests,
    clearSelectedMovementRequests,
    setPageSize
} = movementSlice.actions;

// Export reducer
export default movementSlice.reducer;