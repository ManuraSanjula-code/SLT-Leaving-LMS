import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";

const initialState = {
    data: [],
    pagination: {
        totalPages: 0,
        totalElements: 0,
        currentPage: 0,
        pageSize: 10
    },
    loading: false,
    error: null,
    balances: {
        data: [],
        loading: false,
        error: null
    },
    inOutData: null,
    loadingInOutData: false
};

export const fetchLeaveData = createAsyncThunk(
    'leave/fetchData',
    async ({isAdmin, userId, userAdmin, page, size}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const url = `http://192.168.3.20:8080/lms/${isAdmin ? 'leave/all' : `leave/${userId}`}/${empId}?page=${page}&size=${size}`;
            const response = await fetch(url, {credentials: 'include'});
            if (!response.ok) throw new Error('Failed to fetch');
            const data = await response.json();
            return {
                data: data.content.map(transformLeaveItem),
                pagination: {
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                    currentPage: data.number,
                    pageSize: data.size
                }
            };
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const fetchLeaveBalances = createAsyncThunk(
    'leave/fetchBalances',
    async (userId, {rejectWithValue}) => {
        try {
            const loggedInUserId = sessionStorage.getItem('userId');
            if (!loggedInUserId) {
                return rejectWithValue('User ID not found in session storage');
            }

            const url = `http://192.168.3.20:8080/lms/leave-balance/${userId}/${loggedInUserId}`;
            const response = await fetch(url, {
                credentials: 'include'
            });

            if (!response.ok) throw new Error('Failed to fetch balances');

            const data = await response.json();

            return data.leaveDetails || [];
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const fetchInOutData = createAsyncThunk(
    'leave/fetchInOutData',
    async ({ userId, happenDate }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const dateObj = new Date(happenDate);
            dateObj.setDate(dateObj.getDate() + 1);
            const adjustedDate = dateObj.toISOString().split('T')[0];

            const response = await fetch(`http://192.168.3.20:8080/lms/in-out/${adjustedDate}/earliest/${userId}/${empId}`, {
                credentials: 'include'
            });

            if (!response.ok) {
                const errorText = await response.json();
                throw new Error(`ERROR : ${errorText.message}`);
            }

            return await response.json();
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const deleteLeaveRequest = createAsyncThunk(
    'leave/delete',
    async (publicId, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/leave/${publicId}/${empId}`, {
                method: 'DELETE',
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Delete failed');
            return publicId;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const updateLeaveRequest = createAsyncThunk(
    'leave/updateLeaveRequest',
    async ({updatePayload, userAdmin, isAdmin}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

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
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include',
                    body: JSON.stringify(cleanPayload)
                }
            );

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP error! Status: ${response.status}, Message: ${errorText}`);
            }

            // Return the original payload for state update
            return updatePayload;
        } catch (err) {
            console.error('Update request failed:', err);
            return rejectWithValue(err.message);
        }
    }
);

const leaveSlice = createSlice({
    name: 'leaveNo',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchLeaveData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchLeaveData.fulfilled, (state, action) => {
                state.loading = false;
                state.data = action.payload.data;
                state.pagination = action.payload.pagination;
            })
            .addCase(fetchLeaveData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(fetchLeaveBalances.pending, (state) => {
                state.balances.loading = true;
                state.balances.error = null;
            })
            .addCase(fetchLeaveBalances.fulfilled, (state, action) => {
                state.balances.loading = false;
                state.balances.data = action.payload;
            })
            .addCase(fetchLeaveBalances.rejected, (state, action) => {
                state.balances.loading = false;
                state.balances.error = action.payload;
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
            })
            .addCase(deleteLeaveRequest.fulfilled, (state, action) => {
                state.data = state.data.filter(leave => leave.publicId !== action.payload);
            })
            .addCase(updateLeaveRequest.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateLeaveRequest.fulfilled, (state, action) => {
                state.loading = false;
                // Update the specific request in the state
                const updatedRequest = action.payload;
                const index = state.data.findIndex(leave => leave.publicId === updatedRequest.publicId);
                if (index !== -1) {
                    state.data[index] = {
                        ...state.data[index],
                        ...updatedRequest
                    };
                }
            })
            .addCase(updateLeaveRequest.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

const transformLeaveItem = (item) => ({
    id: item.id,
    publicId: item.publicId,
    employeeId: item.employeeID,
    employeeName: `Employee ${item.employeeID?.substring(0, 5) || 'Unknown'}`,
    type: getLeaveType(item),
    startDate: item.fromDate ? new Date(item.fromDate).toISOString().split('T')[0] : "",
    endDate: item.toDate ? new Date(item.toDate).toISOString().split('T')[0] : "",
    status: getLeaveStatus(item),
    comment: item.description || "",
    description: item.description || "",
    category: item.leaveType?.name || "",
    leaveTypeName: item.leaveType?.name || "",
    fromDate: item.fromDate || "",
    toDate: item.toDate || "",
    submitDate: item.submitDate || "",

    componentBehavior: item.componentBehavior,
    requestStatus: item.requestStatus,

    late: item.componentBehavior === 'LATE',
    absent: item.componentBehavior === 'ABSENT',
    fullDay: item.componentBehavior === 'FULL_DAY',
    halfDay: item.componentBehavior === 'HALF_DAY',
    short_Leave: item.componentBehavior === 'SHORT_LEAVE',
    unauthorized: item.componentBehavior === 'UNAUTHORIZED',
    unSuccessful: item.componentBehavior === 'UNSUCCESSFUL',
    lateCover: item.componentBehavior === 'LATE_COVER',

    pending: item.requestStatus === 'PENDING_APPROVAL' || item.requestStatus === 'SUBMITTED',
    accepted: item.requestStatus === 'APPROVED',
    reject: item.requestStatus === 'REJECTED',
    canceled: item.requestStatus === 'CANCELLED',
    expired: item.requestStatus === 'EXPIRED',

    manualRequest: Boolean(item.isManualRequest),
    numOfDays: item.numOfDays || 0,
    actualDays: item.actualDays || 0,
    adminsTra: item.adminsTra || [],
    editedByDTOs: item.editedByDTOs || [],
    notUsed: Boolean(item.notUsed),
    isEdited: Boolean(item.isEdited),

    originalItem: {
        ...item,
        componentBehavior: item.componentBehavior,
        requestStatus: item.requestStatus,
        componentBehaviorString: item.componentBehaviorString,
        leaveStatusString: item.leaveStatusString
    }
});

const getLeaveType = (item) => {
    switch (item.componentBehavior) {
        case 'FULL_DAY':
            return "Full Day Leave";
        case 'HALF_DAY':
            return "Half Day Leave";
        case 'ABSENT':
            return "Absence";
        case 'LATE':
            return "Late Arrival";
        case 'LATE_COVER':
            return "Late Cover";
        case 'SHORT_LEAVE':
            return "Short Leave";
        case 'UNSUCCESSFUL':
            return "Unsuccessful";
        case 'UNAUTHORIZED':
            return "Unauthorized";
        default:
            return item.leaveType?.name || "Regular Leave";
    }
};

const getLeaveStatus = (item) => {
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
            return "Canceled";
        case 'EXPIRED':
            return "Expired";
        default:
            return "Unknown";
    }
};

export default leaveSlice.reducer;