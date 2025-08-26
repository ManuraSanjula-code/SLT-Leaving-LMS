import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchLeaveBalances = createAsyncThunk(
    'leaveApplication/fetchBalances',
    async (userId, { rejectWithValue }) => {
        try {
            if (!userId) {
                return rejectWithValue('User ID not found');
            }
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/leave-balance/${userId}/${empId}`, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`Error fetching leave balances: ${response.status}`);
            }

            const data = await response.json();
            if (data && data.leaveDetails) {
                return data.leaveDetails;
            } else {
                throw new Error('Invalid response format');
            }
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const submitLeaveRequest = createAsyncThunk(
    'leaveApplication/submitRequest',
    async ({ formData, userId }, { rejectWithValue }) => {
        try {
            if (!userId) {
                return rejectWithValue('User ID not found');
            }
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            const payload = {
                fromDate: new Date(formData.fromDate).toISOString(),
                toDate: new Date(formData.toDate).toISOString(),
                leaveType: formData.leaveType,
                description: formData.description,
                userId: userId,
                numOfDays: formData.componentBehavior === 'HALF_DAY' ? 0.5 : formData.numOfDays,
                happenDate: formData.happenDate ? new Date(formData.happenDate).toISOString() : new Date().toISOString(),
                componentBehavior: formData.componentBehavior,
                requestStatus: 'DRAFT',
                notUsed: false,
                isManualRequest: false,
                isEdited: false,
                adminId: null,
                adminComment: null
            };

            const response = await fetch(`http://192.168.3.20:8080/lms/management/leave/create/${userId}/${empId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
                credentials: 'include'
            });

            if (!response.ok) {
                let errorMessage = `HTTP ${response.status}`;
                try {
                    const errorText = await response.text();
                    if (errorText) {
                        errorMessage += ` - ${errorText}`;
                    }
                } catch (e) {
                    console.error('Error parsing error response', e);
                }
                throw new Error(errorMessage);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                try {
                    return await response.json();
                } catch (e) {
                    return { success: true, message: "Leave request created successfully" };
                }
            } else {
                return { success: true, message: "Leave request created successfully" };
            }
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);


const calculateBusinessDays = (startDate, endDate) => {
        const start = new Date(startDate);
        const end = new Date(endDate);
        start.setHours(0, 0, 0, 0);
        end.setHours(0, 0, 0, 0);

        let count = 0;
        const current = new Date(start);

        while (current <= end) {
            const day = current.getDay();
            if (day !== 0 && day !== 6) { // Skip weekends (0=Sunday, 6=Saturday)
                count++;
            }
            current.setDate(current.getDate() + 1);
        }

        return count;
    };

const leaveApplicationSlice = createSlice({
    name: 'leaveApplication',
    initialState: {
        userId: null,
        formData: {
            fromDate: '',
            toDate: '',
            leaveType: '',
            description: '',
            numOfDays: 0,
            happenDate: '',
            componentBehavior: 'FULL_DAY',
            isManualRequest: true
        },
        errors: {},
        isValid: false,
        leaveBalances: [],
        loading: false,
        fetchingBalance: false,
        notification: {
            open: false,
            message: '',
            severity: 'info'
        }
    },
    reducers: {
        setUserId: (state, action) => {
            state.userId = action.payload;
        },
        updateFormField: (state, action) => {
            const { name, value, checked, type } = action.payload;

            if (type === 'checkbox') {
                state.formData[name] = checked;
            } else {
                state.formData[name] = value;
            }

            if (state.errors[name]) {
                state.errors[name] = null;
            }
        },

        calculateDays: (state) => {
            if (state.formData.fromDate && state.formData.toDate) {
                const diffDays = calculateBusinessDays(
                    state.formData.fromDate,
                    state.formData.toDate
                );

                switch (state.formData.componentBehavior) {
                    case 'HALF_DAY':
                        state.formData.numOfDays = 0.5;
                        break;
                    case 'FULL_DAY':
                    default:
                        state.formData.numOfDays = diffDays;
                        break;
                }
            }
        },

        setComponentBehavior: (state, action) => {
            const newBehavior = action.payload;
            state.formData.componentBehavior = newBehavior;

            const restrictedCategories = ['UNAUTHORIZED', 'ABSENT', 'UNSUCCESSFUL'];
            const allowedManualCategories = ['FULL_DAY', 'HALF_DAY'];

            if (restrictedCategories.includes(newBehavior)) {
                state.formData.isManualRequest = false;
            } else if (allowedManualCategories.includes(newBehavior)) {
                state.formData.isManualRequest = true;
            }

            if (state.formData.fromDate && state.formData.toDate) {
                const diffDays = calculateBusinessDays(
                    state.formData.fromDate,
                    state.formData.toDate
                );

                switch (newBehavior) {
                    case 'HALF_DAY':
                        state.formData.numOfDays = 0.5;
                        state.formData.toDate = state.formData.fromDate;
                        break;
                    case 'FULL_DAY':
                    default:
                        state.formData.numOfDays = diffDays;
                        break;
                }
            }

            state.errors = {};
            state.isValid = false;
        },

        validateForm: (state) => {
            const newErrors = {};

            if (!state.formData.fromDate) newErrors.fromDate = 'Start date is required';
            if (!state.formData.toDate) newErrors.toDate = 'End date is required';
            if (!state.formData.leaveType) newErrors.leaveType = 'Leave type is required';
            if (!state.formData.description) newErrors.description = 'Reason is required';
            if (!state.formData.componentBehavior) newErrors.componentBehavior = 'Leave category is required';

            if (state.formData.fromDate && state.formData.toDate) {
                const start = new Date(state.formData.fromDate);
                const end = new Date(state.formData.toDate);
                if (end < start) {
                    newErrors.toDate = 'End date cannot be before start date';
                }
            }

            if (state.formData.componentBehavior === 'HALF_DAY' &&
                state.formData.fromDate &&
                state.formData.toDate &&
                state.formData.fromDate !== state.formData.toDate) {
                newErrors.componentBehavior = 'Half day leave must be for the same day';
            }

            if (state.formData.leaveType &&
                state.formData.leaveType !== "Duty Leave" &&
                state.formData.leaveType !== "Special Leave" &&
                state.formData.numOfDays > 0) {
                const selectedType = state.formData.leaveType;
                const typeBalance = state.leaveBalances.find(
                    balance => balance.leaveTypeName === selectedType
                );

                if (typeBalance && typeBalance.remainingLeaves < state.formData.numOfDays) {
                    newErrors.leaveType = `Insufficient ${selectedType.toLowerCase()} leave balance`;
                }
            }

            const restrictedCategories = ['UNAUTHORIZED', 'ABSENT', 'UNSUCCESSFUL'];
            if (restrictedCategories.includes(state.formData.componentBehavior)) {
                if (state.formData.isManualRequest) {
                    newErrors.isManualRequest = 'Manual Request is not allowed for this category';
                }
            }

            state.errors = newErrors;
            state.isValid = Object.keys(newErrors).length === 0;
        },

        resetForm: (state) => {
            state.formData = {
                fromDate: '',
                toDate: '',
                leaveType: '',
                description: '',
                numOfDays: 0,
                happenDate: '',
                componentBehavior: 'FULL_DAY',
                isManualRequest: true
            };
            state.errors = {};
            state.isValid = false;
        },

        setNotification: (state, action) => {
            state.notification = action.payload;
        },

        closeNotification: (state) => {
            state.notification.open = false;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchLeaveBalances.pending, (state) => {
                state.fetchingBalance = true;
            })
            .addCase(fetchLeaveBalances.fulfilled, (state, action) => {
                state.fetchingBalance = false;
                state.leaveBalances = action.payload;
            })
            .addCase(fetchLeaveBalances.rejected, (state, action) => {
                state.fetchingBalance = false;
                state.notification = {
                    open: true,
                    message: `Failed to fetch leave balances: ${action.payload}`,
                    severity: 'warning'
                };
            })
            .addCase(submitLeaveRequest.pending, (state) => {
                state.loading = true;
            })
            .addCase(submitLeaveRequest.fulfilled, (state) => {
                state.loading = false;
                state.notification = {
                    open: true,
                    message: 'Leave request submitted successfully!',
                    severity: 'success'
                };
                state.formData = {
                    fromDate: '',
                    toDate: '',
                    leaveType: '',
                    description: '',
                    numOfDays: 0,
                    happenDate: '',
                    componentBehavior: 'FULL_DAY',
                    isManualRequest: true
                };
                state.errors = {};
                state.isValid = false;
            })
            .addCase(submitLeaveRequest.rejected, (state, action) => {
                state.loading = false;
                let errorMessage = "Failed to submit leave request";

                if (action.payload && typeof action.payload === 'string') {
                    try {
                        const message = action.payload.split('"message":"')[1]?.split('"')[0];
                        if (message) errorMessage = `Failed to submit leave request: ${message}`;
                    } catch (e) {
                        console.error('Error parsing error message', e);
                    }
                }

                state.notification = {
                    open: true,
                    message: errorMessage,
                    severity: 'error'
                };
            });
    }
});

export const {
    setUserId,
    updateFormField,
    calculateDays,
    setComponentBehavior,
    validateForm,
    resetForm,
    setNotification,
    closeNotification
} = leaveApplicationSlice.actions;

export const selectUserId = state => state.leaveApplication.userId;
export const selectFormData = state => state.leaveApplication.formData;
export const selectErrors = state => state.leaveApplication.errors;
export const selectIsValid = state => state.leaveApplication.isValid;
export const selectLeaveBalances = state => state.leaveApplication.leaveBalances;
export const selectLoading = state => state.leaveApplication.loading;
export const selectFetchingBalance = state => state.leaveApplication.fetchingBalance;
export const selectNotification = state => state.leaveApplication.notification;

export const leaveHelpers = {
    getRemainingLeaveBalance: (leaveBalances, typeName) => {
        const leaveType = leaveBalances.find(b => b.leaveTypeName === typeName);
        return leaveType ? leaveType.remainingLeaves : 0;
    },

    leaveTypes: [
        { value: "Annual Leave", label: "Annual Leave" },
        { value: "Medical Leave", label: "Medical Leave" },
        { value: "Casual Leave", label: "Casual Leave" },
        { value: "Maternity Leave", label: "Maternity Leave" },
        { value: "Duty Leave", label: "Duty Leave" },
        { value: "Special Leave", label: "Special Leave" }
    ],

    componentBehaviors: [
        { value: "FULL_DAY", label: "Full Day Leave", type: "leave", allowsManualRequest: true },
        { value: "HALF_DAY", label: "Half Day Leave", type: "leave", allowsManualRequest: true },
    ],

    getCategoryType: (componentBehavior) => {
        const behavior = leaveHelpers.componentBehaviors.find(cb => cb.value === componentBehavior);
        return behavior ? behavior.type : 'leave';
    },

    isManualRequestAllowed: (componentBehavior) => {
        const behavior = leaveHelpers.componentBehaviors.find(cb => cb.value === componentBehavior);
        return behavior ? behavior.allowsManualRequest : false;
    },

    allowedManualCategories: ['FULL_DAY', 'HALF_DAY'],
    restrictedCategories: ['UNAUTHORIZED', 'ABSENT', 'UNSUCCESSFUL'],
    calculateBusinessDays
};

export default leaveApplicationSlice.reducer;