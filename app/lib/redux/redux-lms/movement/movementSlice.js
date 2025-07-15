import {createSlice, createAsyncThunk} from '@reduxjs/toolkit';

export const fetchMovementRequests = createAsyncThunk(
    'movement/fetchMovementRequests',
    async ({isAdmin, userId, useAdmin, page, size}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(
                `http://localhost:8080/lms/${isAdmin ? 'movement/all' : `movement/${userId}`}/${empId}?page=${page}&size=${size}`,
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

export const updateMovementRequest = createAsyncThunk(
    'movement/updateMovementRequest',
    async ({updatePayload, isAdmin, useAdmin}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            const cleanPayload = {
                employeeId: updatePayload.employeeId,
                userId: updatePayload.userId,
                destination: updatePayload.destination,
                movementType: updatePayload.movementType,
                comment: updatePayload.comment,
                category: updatePayload.category,
                inTime: updatePayload.inTime,
                outTime: updatePayload.outTime,
                componentBehavior: updatePayload.componentBehavior,
                requestStatus: updatePayload.requestStatus,
                attSync: updatePayload.attSync || 0,
                attendance: updatePayload.attendance || '',
                isEdited: true
            };

            if (updatePayload.happenDate) {
                cleanPayload.happenDate = updatePayload.happenDate;
            }
            if (updatePayload.reqDate) {
                cleanPayload.reqDate = updatePayload.reqDate;
            }
            if (updatePayload.logTime) {
                cleanPayload.logTime = updatePayload.logTime;
            }

            Object.keys(cleanPayload).forEach(key => {
                if (cleanPayload[key] === undefined || cleanPayload[key] === null || cleanPayload[key] === '') {
                    delete cleanPayload[key];
                }
            });


            const response = await fetch(`http://localhost:8080/lms/management/movement/${updatePayload.publicId}/${empId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(cleanPayload)
            });

            if (!response.ok) {
                const errorText = await response.json();
                throw new Error(`ERROR: ${errorText.message}`);
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
                        inTime: item.inTime || "00:00",
                        outTime: item.outTime || "00:00",
                        logTime: item.logTime ? new Date(item.logTime).toISOString() : "",
                        comment: item.comment || "",
                        destination: item.destination || "",
                        category: item.category || "",

                        componentBehavior: item.componentBehavior,
                        requestStatus: item.requestStatus,
                        movementType: item.movementType || "",

                        unAuthorized: item.componentBehavior === 'UNAUTHORIZED',
                        absent: item.componentBehavior === 'ABSENT',
                        halfDay: item.componentBehavior === 'HALF_DAY',
                        isLate: item.componentBehavior === 'LATE',
                        isLateCover: item.componentBehavior === 'LATE_COVER',
                        isUnSuccessfulAttdate: item.componentBehavior === 'UNSUCCESSFUL',

                        accepted: item.requestStatus === 'APPROVED',
                        pending: item.requestStatus === 'PENDING_APPROVAL' || item.requestStatus === 'SUBMITTED',
                        reject: item.requestStatus === 'REJECTED',

                        happenDate: item.happenDate || "",
                        reqDate: item.reqDate || "",
                        adminsTra: item.adminsTra || [],
                        editedByDTOs: item.editedByDTOs || [],
                        attSync: item.attSync || 0,
                        attendance: item.attendance || "",
                        isEdited: safeBooleanConvert(item.isEdited)
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

const getStatus = (item) => {
    switch (item.requestStatus) {
        case 'DRAFT':
            return "Draft";
        case 'SUBMITTED':
            return "Submitted";
        case 'PENDING_APPROVAL':
            return "Pending";
        case 'APPROVED':
            return "Approved";
        case 'REJECTED':
            return "Rejected";
        case 'CANCELLED':
            return "Cancelled";
        case 'EXPIRED':
            return "Expired";
        default:
            return "Unknown";
    }
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