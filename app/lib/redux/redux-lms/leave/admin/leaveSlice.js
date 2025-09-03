import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchLeaveRequests = createAsyncThunk(
    'leave/fetchLeaveRequests',
    async ({ page = 0, size = 10 }, { rejectWithValue }) => {
        try {
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                return rejectWithValue('User ID not found in session storage');
            }

            const response = await fetch(`http://192.168.3.20:8080/lms/leave/admin/${storedUserId}/${empId}?page=${page}&size=${size}`, {
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
            console.log(data)
            return {
                content: data.content || [],
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

export const updateLeaveRequest = createAsyncThunk(
    'leave/updateLeaveRequest',
    async ({ updatePayload, userAdmin, isAdmin }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) return rejectWithValue('Employee ID not found');

            const cleanPayload = {
                fromDate: updatePayload.fromDate,
                toDate: updatePayload.toDate,
                leaveType: updatePayload.leaveType,
                description: updatePayload.description,
                numOfDays: updatePayload.numOfDays,
                happenDate: updatePayload.happenDate,
                userId: updatePayload.userId,
                employeeID: updatePayload.employeeID,
                componentBehavior: updatePayload.componentBehavior,
                requestStatus: updatePayload.requestStatus,
                notUsed: updatePayload.notUsed || false,
                isManualRequest: updatePayload.isManualRequest || false,
                isEdited: updatePayload.isEdited || false
            };

            Object.keys(cleanPayload).forEach(key => {
                if (cleanPayload[key] === undefined || cleanPayload[key] === null || cleanPayload[key] === '') {
                    delete cleanPayload[key];
                }
            });

            const response = await fetch(
                `http://192.168.3.20:8080/lms/management/leave/${updatePayload.publicId}/${empId}`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify(cleanPayload)
                }
            );

            if (!response.ok) {
                const errorText = await response.json();
                throw new Error(`${response.status} Message: ${errorText.error || errorText.message}`);
            }

            return cleanPayload;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const processLeaveRequest = createAsyncThunk(
    'leave/processLeaveRequest',
    async ({ publicId, approved }, { rejectWithValue }) => {
        try {
            const storedUserId = sessionStorage.getItem('userId');
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            if (!storedUserId) {
                return rejectWithValue('User ID not found in session storage');
            }

            const endpoint = approved
                ? `http://192.168.3.20:8080/lms/leave/process/${publicId}/${storedUserId}/${empId}`
                : `http://192.168.3.20:8080/lms/leave/reject/${publicId}/${storedUserId}/${empId}`;

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


            const endpoint = approved
                ? `http://192.168.3.20:8080/lms/bulk/approved/leave/${empId}`
                : `http://192.168.3.20:8080/lms/bulk/reject/leave/${empId}`;

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


            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
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
        selectLeaveRequest: (state, action) => {
            const id = action.payload;
            if (state.selected.includes(id)) {
                state.selected = state.selected.filter(item => item !== id);
            } else {
                state.selected.push(id);
            }
        },

        selectAllLeaveRequests: (state) => {
            if (state.selected.length === state.requests.filter(req => req && req.publicId).length) {
                state.selected = [];
            } else {
                state.selected = state.requests.filter(req => req && req.publicId).map(req => req.publicId);
            }
        },

        clearSelectedLeaveRequests: (state) => {
            state.selected = [];
        },

        setNotification: (state, action) => {
            state.notification = action.payload;
        },

        clearNotification: (state) => {
            state.notification = {
                open: false,
                message: '',
                severity: 'info'
            };
        },
        setPageSize: (state, action) => {
            state.pagination.pageSize = action.payload;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(updateLeaveRequest.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateLeaveRequest.fulfilled, (state, action) => {
                state.loading = false;
                state.notification = {
                    open: true,
                    message: 'Leave request updated successfully',
                    severity: 'success'
                };

                const index = state.requests.findIndex(req => req.publicId === action.payload.publicId);
                if (index !== -1) {
                    state.requests[index] = { ...state.requests[index], ...action.payload };
                }
            })
            .addCase(updateLeaveRequest.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.notification = {
                    open: true,
                    message: `Failed to update leave request: ${action.payload}`,
                    severity: 'error'
                };
            })
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
                if (action.payload === 'User ID not found in session storage') {
                    state.notification = {
                        open: true,
                        message: 'User ID not found. Please log in again.',
                        severity: 'error'
                    };
                }
            })

            .addCase(processLeaveRequest.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(processLeaveRequest.fulfilled, (state, action) => {
                state.loading = false;
                const index = state.requests.findIndex(req => req && req.publicId === action.payload.publicId);
                if (index !== -1) {
                    if (action.payload.approved) {
                        state.requests[index].accepted = true;
                        state.requests[index].pending = false;
                        state.requests[index].reject = false;
                    } else {
                        state.requests[index].reject = true;
                        state.requests[index].pending = false;
                        state.requests[index].accepted = false;
                    }
                }
                state.notification = {
                    open: true,
                    message: `Request ${action.payload.approved ? 'approved' : 'rejected'} successfully`,
                    severity: 'success'
                };
            })
            .addCase(processLeaveRequest.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.notification = {
                    open: true,
                    message: `Failed to process request: ${action.payload}`,
                    severity: 'error'
                };
            })

            .addCase(processBulkLeaveRequests.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(processBulkLeaveRequests.fulfilled, (state, action) => {
                state.loading = false;
                action.payload.leaveIds.forEach(id => {
                    const index = state.requests.findIndex(req => req && req.publicId === id);
                    if (index !== -1) {
                        if (action.payload.approved) {
                            state.requests[index].accepted = true;
                            state.requests[index].pending = false;
                            state.requests[index].reject = false;
                        } else {
                            state.requests[index].reject = true;
                            state.requests[index].pending = false;
                            state.requests[index].accepted = false;
                        }
                    }
                });
                state.selected = [];
                state.notification = {
                    open: true,
                    message: `${action.payload.leaveIds.length} requests ${action.payload.approved ? 'approved' : 'rejected'} successfully`,
                    severity: 'success'
                };
            })
            .addCase(processBulkLeaveRequests.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.notification = {
                    open: true,
                    message: `Failed to process requests: ${action.payload}`,
                    severity: 'error'
                };
            });
    }
});

export const {
    selectLeaveRequest,
    selectAllLeaveRequests,
    clearSelectedLeaveRequests,
    setNotification,
    clearNotification,
    setPageSize
} = leaveSlice.actions;

export default leaveSlice.reducer;