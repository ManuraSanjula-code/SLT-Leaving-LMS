import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchLeaveRequests = createAsyncThunk(
    'leave/fetchLeaveRequests',
    async ({ page = 0, size = 10 }, { rejectWithValue }) => {
        try {
            // Get userId from session storage
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                return rejectWithValue('User ID not found in session storage');
            }

            // Make API call to fetch leave requests
            const response = await fetch(`http://localhost:8080/lms/leave/admin/${storedUserId}/${empId}?page=${page}&size=${size}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            return {
                content: data.content || [], // Ensure we always have an array even if content is null
                pagination: {
                    currentPage: data.number,
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                    pageSize: data.pageable?.pageSize || size
                }
            };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const processLeaveRequest = createAsyncThunk(
    'leave/processLeaveRequest',
    async ({ publicId, approved }, { rejectWithValue }) => {
        try {
            // Get userId from session storage
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                return rejectWithValue('User ID not found in session storage');
            }

            const endpoint = approved
                ? `http://localhost:8080/lms/leave/process/${publicId}/${storedUserId}/${empId}`
                : `http://localhost:8080/lms/leave/reject/${publicId}/${storedUserId}/${empId}`;

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
            console.log(response)

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            return { publicId, approved };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const processBulkLeaveRequests = createAsyncThunk(
    'leave/processBulkLeaveRequests',
    async ({ leaveIds, approved }, { getState, rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            const state = getState();
            const leaveRequests = state.leave.requests;

            // Extract employee IDs from the selected leave requests
            const approvedEmployeesToday = [];

            leaveRequests.forEach(request => {
                if (request && request.publicId && leaveIds.includes(request.publicId) && request.employeeID) {
                    approvedEmployeesToday.push(request.employeeID);
                }
            });

            const requestBody = {
                approvedEmployeesToday,
                approvedIds: leaveIds
            };

            console.log('Sending request:', requestBody);

            const endpoint = approved
                ? `http://localhost:8080/lms/bulk/approved/leave/${empId}`
                : `http://localhost:8080/lms/bulk/reject/leave/${empId}`;

            const response = await fetch(
                endpoint,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include',
                    body: JSON.stringify(requestBody)
                }
            );

            console.log(response);

            // Check if response is ok
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            // Handle void response - API doesn't return data
            // We'll return the data we need for the fulfilled case
            return {
                leaveIds,
                approvedEmployeesToday,
                approved,
            };
        } catch (error) {
            return rejectWithValue(error.message || 'An unknown error occurred');
        }
    }
);

// Create the leave slice
const leaveSlice = createSlice({
    name: 'leave',
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
        error: null,
        notification: {
            open: false,
            message: '',
            severity: 'info'
        }
    },
    reducers: {
        // Select a single leave request
        selectLeaveRequest: (state, action) => {
            const id = action.payload;
            if (state.selected.includes(id)) {
                state.selected = state.selected.filter(item => item !== id);
            } else {
                state.selected.push(id);
            }
        },

        // Select all leave requests
        selectAllLeaveRequests: (state) => {
            if (state.selected.length === state.requests.filter(req => req && req.publicId).length) {
                state.selected = [];
            } else {
                state.selected = state.requests.filter(req => req && req.publicId).map(req => req.publicId);
            }
        },

        // Clear selected leave requests
        clearSelectedLeaveRequests: (state) => {
            state.selected = [];
        },

        // Set notification state
        setNotification: (state, action) => {
            state.notification = action.payload;
        },

        // Clear notification
        clearNotification: (state) => {
            state.notification = {
                open: false,
                message: '',
                severity: 'info'
            };
        },

        // Set page size
        setPageSize: (state, action) => {
            state.pagination.pageSize = action.payload;
        }
    },
    extraReducers: (builder) => {
        builder
            // Handle fetchLeaveRequests
            .addCase(fetchLeaveRequests.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchLeaveRequests.fulfilled, (state, action) => {
                state.loading = false;
                state.requests = action.payload.content;
                state.pagination = action.payload.pagination;
            })
            .addCase(fetchLeaveRequests.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                // Set notification for missing user ID
                if (action.payload === 'User ID not found in session storage') {
                    state.notification = {
                        open: true,
                        message: 'User ID not found. Please log in again.',
                        severity: 'error'
                    };
                }
            })

            // Handle processLeaveRequest
            .addCase(processLeaveRequest.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(processLeaveRequest.fulfilled, (state, action) => {
                state.loading = false;
                // Update the status of the processed request in the state
                const index = state.requests.findIndex(req => req && req.publicId === action.payload.publicId);
                if (index !== -1) {
                    if (action.payload.approved) {
                        state.requests[index].accepted = true;
                        state.requests[index].pending = false;
                        state.requests[index].reject = false;
                    } else {
                        // When rejected, set reject to true and pending to false
                        state.requests[index].reject = true;
                        state.requests[index].pending = false;
                        state.requests[index].accepted = false;
                    }
                }
                // Set success notification
                state.notification = {
                    open: true,
                    message: `Request ${action.payload.approved ? 'approved' : 'rejected'} successfully`,
                    severity: 'success'
                };
            })
            .addCase(processLeaveRequest.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                // Set error notification
                state.notification = {
                    open: true,
                    message: `Failed to process request: ${action.payload}`,
                    severity: 'error'
                };
            })

            // Handle processBulkLeaveRequests
            .addCase(processBulkLeaveRequests.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(processBulkLeaveRequests.fulfilled, (state, action) => {
                state.loading = false;
                // Update status for all processed requests
                action.payload.leaveIds.forEach(id => {
                    const index = state.requests.findIndex(req => req && req.publicId === id);
                    if (index !== -1) {
                        if (action.payload.approved) {
                            state.requests[index].accepted = true;
                            state.requests[index].pending = false;
                            state.requests[index].reject = false;
                        } else {
                            // When rejected, set reject to true and pending to false
                            state.requests[index].reject = true;
                            state.requests[index].pending = false;
                            state.requests[index].accepted = false;
                        }
                    }
                });
                // Clear selection after bulk processing
                state.selected = [];
                // Set success notification
                state.notification = {
                    open: true,
                    message: `${action.payload.leaveIds.length} requests ${action.payload.approved ? 'approved' : 'rejected'} successfully`,
                    severity: 'success'
                };
            })
            .addCase(processBulkLeaveRequests.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                // Set error notification
                state.notification = {
                    open: true,
                    message: `Failed to process requests: ${action.payload}`,
                    severity: 'error'
                };
            });
    }
});

// Export actions
export const {
    selectLeaveRequest,
    selectAllLeaveRequests,
    clearSelectedLeaveRequests,
    setNotification,
    clearNotification,
    setPageSize
} = leaveSlice.actions;

// Export reducer
export default leaveSlice.reducer;