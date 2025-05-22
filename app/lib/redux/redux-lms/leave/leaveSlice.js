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
    }
};

export const fetchLeaveData = createAsyncThunk(
    'leave/fetchData',
    async ({isAdmin, userId, page, size}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const url = `http://localhost:8080/lms/${isAdmin ? 'leave/all' : `leave/${userId}`}/${empId}?page=${page}&size=${size}`;
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

            const url = `http://localhost:8080/lms/leave-balance/${userId}/${loggedInUserId}`;
            const response = await fetch(url, {
                credentials: 'include'
            });

            if (!response.ok) throw new Error('Failed to fetch balances');

            const data = await response.json();

            // Extract leaveDetails from the response based on your API format
            return data.leaveDetails || [];
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
            const response = await fetch(`http://localhost:8080/lms/leave/${publicId}/${empId}`, {
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

const leaveSlice = createSlice({
    name: 'leaveNo',  // Use the correct slice name based on your Redux store structure
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
            });
    }
});

// Helper function from original component
const transformLeaveItem = (item) => ({
    id: item.id,
    publicId: item.publicId,
    employeeId: item.employeeID,
    employeeName: `Employee ${item.employeeID.substring(0, 5)}`,
    type: getLeaveType(item),
    startDate: item.fromDate ? new Date(item.fromDate).toISOString().split('T')[0] : "",
    endDate: item.toDate ? new Date(item.toDate).toISOString().split('T')[0] : "",
    status: getLeaveStatus(item),
    comment: item.description,
    category: item.leaveType?.name || "",
    leaveTypeName: item.leaveType?.name || "",
    late: item.late,
    absent: !item.late && !item.fullDay && !item.halfDay,
    fullDay: item.fullDay,
    halfDay: item.halfDay,
    pending: item.pending,
    accepted: item.accepted,
    expired: false,
    adminsTra: [...item.adminsTra]
});

const getLeaveType = (item) => {
    if (item.fullDay) return "Full Day Leave";
    if (item.halfDay) return "Half Day Leave";
    if (item.absent) return "Absence";
    if (item.late) return "Late Arrival";
    return item.leaveType?.name || "Regular Leave";
};

const getLeaveStatus = (item) => {
    if (item.pending) return "Pending";
    if (item.accepted) return "Approved";
    if (item.canceled) return "Canceled";
    if (item.late && !item.pending && !item.accepted) return "Recorded Late";
    if (!item.fullDay && !item.halfDay && !item.late && !item.pending && !item.accepted) return "Recorded Absent";
    return "Processed";
};

export default leaveSlice.reducer;