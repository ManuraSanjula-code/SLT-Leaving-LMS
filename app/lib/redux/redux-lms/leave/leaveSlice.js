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
            const url = `http://localhost:8080/lms/${isAdmin ? 'leave/all' : `leave/${userId}`}/${empId}?page=${page}&size=${size}&isAdmin=${userAdmin}`;
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

// Updated updateLeaveRequest thunk with proper field mapping
export const updateLeaveRequest = createAsyncThunk(
    'leave/updateLeaveRequest',
    async ({updatePayload, isAdmin}, {rejectWithValue}) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            // Create a clean payload by removing undefined/null values and mapping fields correctly
            const cleanPayload = {};

            // Map the fields correctly to match backend LeaveReq class
            const fieldMapping = {
                // Basic fields
                fromDate: updatePayload.fromDate,
                toDate: updatePayload.toDate,
                leaveType: updatePayload.leaveType,
                description: updatePayload.description,
                numOfDays: updatePayload.numOfDays,
                happenDate: updatePayload.happenDate,
                userId: updatePayload.userId,
                employeeID: updatePayload.employeeID,
                isNoPay: updatePayload.isNoPay,

                // Boolean fields - map frontend names to backend names
                halfDay: updatePayload.halfDay,                    // isHalfDay -> halfDay
                fullDay: updatePayload.fullDay,                    // isFullDay -> fullDay
                unauthorized: updatePayload.unauthorized,           // isUnauthorized -> unauthorized
                manualRequest: updatePayload.manualRequest,        // isManualRequest -> manualRequest
                absent: updatePayload.absent,                      // isAbsent -> absent
                lateCover: updatePayload.lateCover,               // isLateCover -> lateCover
                late: updatePayload.late,                         // isLate -> late
                short_Leave: updatePayload.short_Leave,           // isShort_Leave -> short_Leave
                notUsed: updatePayload.notUsed,                   // notUsed -> notUsed
                unSuccessful: updatePayload.unSuccessful,         // unSuccessful -> unSuccessful

                // Admin-only fields
                edited: updatePayload.edited,                     // isEdited -> edited
                reject: updatePayload.reject,                     // isReject -> reject
                canceled: updatePayload.canceled,                 // isCanceled -> canceled
                accepted: updatePayload.accepted,                 // isAccepted -> accepted
                pending: updatePayload.pending                    // isPending -> pending
            };

            // Only include fields that have actual values
            Object.entries(fieldMapping).forEach(([key, value]) => {
                if (value !== undefined && value !== null && value !== '') {
                    // Handle boolean fields properly
                    if (typeof value === 'boolean') {
                        cleanPayload[key] = value;
                    } else if (typeof value === 'string' && value.trim() !== '') {
                        cleanPayload[key] = value.trim();
                    } else if (typeof value === 'number') {
                        cleanPayload[key] = value;
                    } else if (value instanceof Date) {
                        cleanPayload[key] = value.toISOString();
                    }
                }
            });

            // Handle admin-specific logic
            if (isAdmin) {
                let userInput = prompt("Enter your comment:");
                if (userInput == null || userInput.trim() === '') {
                    return rejectWithValue('Comment is Required');
                }
                cleanPayload.adminId = empId;
                cleanPayload.adminComment = userInput.trim();
            }


            const response = await fetch(
                `http://localhost:8080/lms/management/leave/${updatePayload.publicId}/${empId}`,
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

// Enhanced helper function to include more properties from leave data
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

    // Date fields
    fromDate: item.fromDate || "",
    toDate: item.toDate || "",
    submitDate: item.submitDate || "",

    // Status flags - ensure proper boolean conversion
    late: Boolean(item.late),
    absent: Boolean(item.absent),
    fullDay: Boolean(item.fullDay),
    halfDay: Boolean(item.halfDay),
    pending: Boolean(item.pending),
    accepted: Boolean(item.accepted),
    expired: Boolean(item.expired),
    reject: Boolean(item.reject),
    canceled: Boolean(item.canceled),
    manualRequest: Boolean(item.manualRequest),

    // Additional fields
    numOfDays: item.numOfDays || 0,
    isNoPay: item.isNoPay || 0,

    // Admin and edit tracking
    adminsTra: item.adminsTra || [],
    editedByDTOs: item.editedByDTOs || [],

    // Keep original item for reference with all fields properly mapped
    originalItem: {
        ...item,
        // Ensure all boolean fields are properly set
        unauthorized: Boolean(item.unauthorized),
        lateCover: Boolean(item.lateCover),
        short_Leave: Boolean(item.short_Leave),
        notUsed: Boolean(item.notUsed),
        edited: Boolean(item.edited),
        unSuccessful: Boolean(item.unSuccessful),
        isNoPay: item.isNoPay || 0
    }
});

const getLeaveType = (item) => {
    if (item.fullDay) return "Full Day Leave";
    if (item.halfDay) return "Half Day Leave";
    if (item.absent) return "Absence";
    if (item.late) return "Late Arrival";
    return item.leaveType?.name || "Regular Leave";
};

const getLeaveStatus = (item) => {
    if (item.reject) return "Reject";
    else if (item.pending) return "Pending";
    else if (item.accepted) return "Approved";
    else if (item.canceled) return "Canceled";
    else if (item.late && !item.pending && !item.accepted) return "Recorded Late";
    else if (!item.fullDay && !item.halfDay && !item.late && !item.pending && !item.accepted) return "Recorded Absent";
    return "Processed";
};

export default leaveSlice.reducer;