import {createSlice, createAsyncThunk} from '@reduxjs/toolkit';

export const fetchMovementRequests = createAsyncThunk(
    'movement/fetchMovementRequests',
    async ({isAdmin, userId, page, size}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(
                `http://localhost:8080/lms/${isAdmin ? 'movement/all' : `movement/${userId}`}/${empId}?page=${page}&size=${size}&isAdmin=${isAdmin}`,
                {credentials: 'include'}
            );

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            return data;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const deleteMovementRequest = createAsyncThunk(
    'movement/deleteMovementRequest',
    async (publicId, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://localhost:8080/lms/movement/${publicId}/${empId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            return {publicId};
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

// Updated to send all movement fields with proper boolean handling
export const updateMovementRequest = createAsyncThunk(
    'movement/updateMovementRequest',
    async ({updatePayload, isAdmin}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            // Clean payload and ensure boolean values are properly handled
            const cleanPayload = {};

            // Handle each field appropriately
            Object.entries(updatePayload).forEach(([key, value]) => {
                if (key !== 'publicId' && value !== undefined && value !== null) {
                    // For boolean fields, ensure they are actual booleans
                    if (typeof value === 'boolean' ||
                        ['unAuthorized', 'accepted', 'pending', 'reject', 'halfDay', 'absent',
                            'isAbsent', 'isUnSuccessfulAttdate', 'isHalfDay', 'isLate', 'isLateCover'].includes(key)) {
                        cleanPayload[key] = Boolean(value);
                    }
                    // For string fields, don't include empty strings unless they're meaningful
                    else if (typeof value === 'string' && value.trim() !== '') {
                        cleanPayload[key] = value;
                    }
                    // For numbers
                    else if (typeof value === 'number') {
                        cleanPayload[key] = value;
                    }
                    // For dates (should already be ISO strings at this point)
                    else if (value instanceof Date || (typeof value === 'string' && value.includes('T'))) {
                        cleanPayload[key] = value;
                    }
                }
            });

            if (isAdmin) {
                let userInput = prompt("Enter your comment:");
                if (userInput == null) return rejectWithValue('Comment is Required');
                cleanPayload.adminId = empId;
                cleanPayload.adminComment = userInput;
            }

            console.log("Clean payload being sent:", cleanPayload);

            const response = await fetch(`http://localhost:8080/lms/management/movement/${updatePayload.publicId}/${empId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(cleanPayload)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP error! Status: ${response.status}, Message: ${errorText}`);
            }

            return updatePayload;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const fetchInOutData = createAsyncThunk(
    'movement/fetchInOutData',
    async ({userId, happenDate}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const dateObj = new Date(happenDate);
            dateObj.setDate(dateObj.getDate() + 1);
            const adjustedDate = dateObj.toISOString().split('T')[0];

            const response = await fetch(`http://localhost:8080/lms/in-out/${adjustedDate}/earliest/${userId}/${empId}`, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            return await response.json();
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

// Helper function to safely convert to boolean
const safeBooleanConvert = (value) => {
    if (typeof value === 'boolean') return value;
    if (typeof value === 'string') return value.toLowerCase() === 'true';
    if (typeof value === 'number') return value === 1;
    return Boolean(value);
};

const movementSlice = createSlice({
    name: 'movement',
    initialState: {
        requests: [],
        selected: [],
        searchQuery: "",
        statusFilter: "All",
        typeFilter: "All",
        startDateFilter: "",
        endDateFilter: "",
        loading: false,
        error: null,
        pagination: {
            totalPages: 0,
            totalElements: 0,
            currentPage: 0,
            pageSize: 10
        },
        inOutData: null,
        loadingInOutData: false
    },
    reducers: {
        setSelected: (state, action) => {
            state.selected = action.payload;
        },
        setSearchQuery: (state, action) => {
            state.searchQuery = action.payload;
        },
        setStatusFilter: (state, action) => {
            state.statusFilter = action.payload;
        },
        setTypeFilter: (state, action) => {
            state.typeFilter = action.payload;
        },
        setStartDateFilter: (state, action) => {
            state.startDateFilter = action.payload;
        },
        setEndDateFilter: (state, action) => {
            state.endDateFilter = action.payload;
        },
        setPageSize: (state, action) => {
            state.pagination.pageSize = action.payload;
            state.pagination.currentPage = 0;
        },
        setCurrentPage: (state, action) => {
            state.pagination.currentPage = action.payload;
        },
        resetFilters: (state) => {
            state.searchQuery = "";
            state.statusFilter = "All";
            state.typeFilter = "All";
            state.startDateFilter = "";
            state.endDateFilter = "";
            state.pagination.currentPage = 0;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchMovementRequests.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchMovementRequests.fulfilled, (state, action) => {
                state.loading = false;
                const data = action.payload;

                if (!data || !data.content) {
                    state.requests = [];
                    state.pagination = {
                        ...state.pagination,
                        totalPages: 0,
                        totalElements: 0
                    };
                } else {
                    state.requests = data.content.map(item => ({
                        id: item.id || 0,
                        publicId: item.publicId || "",
                        employeeId: item.employeeId || "",
                        userId: item.userId || "",
                        type: item.movementType || "Unknown",
                        startDate: item.happenDate ? new Date(item.happenDate).toISOString().split('T')[0] : "",
                        endDate: item.reqDate ? new Date(item.reqDate).toISOString().split('T')[0] : "",
                        status: getStatus(item),
                        inTime: item.inTime || "00:00:00",
                        outTime: item.outTime || "00:00:00",
                        logTime: item.logTime ? new Date(item.logTime).toISOString() : "",
                        comment: item.comment || "",
                        destination: item.destination || "",
                        category: item.category || "",

                        // Boolean fields - properly converted
                        unAuthorized: safeBooleanConvert(item.unAuthorized),
                        accepted: safeBooleanConvert(item.accepted),
                        pending: safeBooleanConvert(item.pending),
                        reject: safeBooleanConvert(item.reject),
                        halfDay: safeBooleanConvert(item.halfDay),
                        absent: safeBooleanConvert(item.absent),

                        // Additional boolean fields from MovementReq
                        isAbsent: safeBooleanConvert(item.isAbsent),
                        isUnSuccessfulAttdate: safeBooleanConvert(item.unSuccessfulAttdate || item.isUnSuccessfulAttdate),
                        isHalfDay: safeBooleanConvert(item.isHalfDay),
                        isLate: safeBooleanConvert(item.isLate),
                        isLateCover: safeBooleanConvert(item.isLateCover),

                        // Original dates for editing
                        happenDate: item.happenDate || "",
                        reqDate: item.reqDate || "",
                        movementType: item.movementType || "",
                        adminsTra: item.adminsTra || [],
                        editedByDTOs: item.editedByDTOs || [],

                        // Additional fields
                        attSync: item.attSync || 0,
                        attendance: item.attendance || "",
                        unSuccessfulAttdate: safeBooleanConvert(item.unSuccessfulAttdate)
                    }));

                    state.pagination = {
                        totalPages: data.totalPages || 0,
                        totalElements: data.totalElements || 0,
                        currentPage: data.number || 0,
                        pageSize: data.pageable?.pageSize || state.pagination.pageSize
                    };
                }
            })
            .addCase(fetchMovementRequests.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.requests = [];
            })
            .addCase(deleteMovementRequest.fulfilled, (state, action) => {
                state.requests = state.requests.filter(req => req.publicId !== action.payload.publicId);
                state.selected = state.selected.filter(id => {
                    const request = state.requests.find(req => req.id === id);
                    return request && request.publicId !== action.payload.publicId;
                });
            })
            .addCase(updateMovementRequest.pending, (state) => {
                state.loading = true;
            })
            .addCase(updateMovementRequest.fulfilled, (state, action) => {
                state.loading = false;
                // The data will be refreshed by the component, so we don't need to update here
                // This prevents stale data issues
            })
            .addCase(updateMovementRequest.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(fetchInOutData.pending, (state) => {
                state.loadingInOutData = true;
                state.inOutData = null;
            })
            .addCase(fetchInOutData.fulfilled, (state, action) => {
                state.loadingInOutData = false;
                state.inOutData = action.payload;
            })
            .addCase(fetchInOutData.rejected, (state, action) => {
                state.loadingInOutData = false;
                state.error = action.payload;
            });
    }
});

// Helper function to determine status with proper boolean checking
const getStatus = (item) => {
    if (safeBooleanConvert(item.pending)) return "Pending";
    if (safeBooleanConvert(item.accepted)) return "Approved";
    if (safeBooleanConvert(item.reject)) return "Rejected";
    if (safeBooleanConvert(item.unAuthorized)) return "Unauthorized";
    if (safeBooleanConvert(item.absent) || safeBooleanConvert(item.isAbsent)) return "Absent";
    if (safeBooleanConvert(item.halfDay) || safeBooleanConvert(item.isHalfDay)) return "Half Day";
    return "Unknown";
};

export const {
    setSelected,
    setSearchQuery,
    setStatusFilter,
    setTypeFilter,
    setStartDateFilter,
    setEndDateFilter,
    setPageSize,
    setCurrentPage,
    resetFilters
} = movementSlice.actions;

export default movementSlice.reducer;