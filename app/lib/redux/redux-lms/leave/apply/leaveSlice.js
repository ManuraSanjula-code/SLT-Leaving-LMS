import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Async thunk to fetch leave balances
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
            const response = await fetch(`http://localhost:8080/lms/leave-balance/${userId}/${empId}`, {
                credentials: 'include' // Include cookies for authentication
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

// Async thunk to submit leave request
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
            // Format dates for backend
            const payload = {
                ...formData,
                userId: userId,
                fromDate: new Date(formData.fromDate).toISOString(),
                toDate: new Date(formData.toDate).toISOString(),
                happenDate: formData.happenDate ? new Date(formData.happenDate).toISOString() : null,
                numOfDays: Math.round(formData.numOfDays) // Ensure we send an integer
            };

            const response = await fetch(`http://localhost:8080/lms/management/leave/create/${userId}/${empId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
                credentials: 'include' // Include cookies for authentication
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Create the slice
const leaveApplicationSlice = createSlice({
    name: 'leaveApplication',
    initialState: {
        // User info
        userId: null,

        // Form data
        formData: {
            fromDate: '',
            toDate: '',
            leaveCategory: '', // HALF_DAY or FULL_DAY
            leaveType: '', // Annual Leave, Medical Leave, etc.
            description: '',
            isHalfDay: false,
            isFullDay: true, // Default to true
            numOfDays: 0,
            happenDate: '',
            isUnauthorized: false,
            isManualRequest: true, // Default to true as per requirements
            isAbsent: false,
            isLateCover: false,
            isLate: false,
            unSuccessful: false
        },

        // Form validation
        errors: {},

        // Leave balances
        leaveBalances: [],

        // UI state
        loading: false,
        fetchingBalance: false,
        notification: {
            open: false,
            message: '',
            severity: 'info'
        }
    },
    reducers: {
        // Set user ID
        setUserId: (state, action) => {
            state.userId = action.payload;
        },

        // Update form field
        updateFormField: (state, action) => {
            const { name, value, checked, type } = action.payload;

            if (type === 'checkbox') {
                state.formData[name] = checked;

                // Handle half day and full day relationship
                if (name === 'isHalfDay') {
                    state.formData.isFullDay = !checked;
                    state.formData.leaveCategory = checked ? "HALF_DAY" : "FULL_DAY";
                } else if (name === 'isFullDay') {
                    state.formData.isHalfDay = !checked;
                    state.formData.leaveCategory = !checked ? "HALF_DAY" : "FULL_DAY";
                }

                // Set isManualRequest to false if any of these are checked
                if ((name === 'isHalfDay' || name === 'isUnauthorized' || name === 'isAbsent' ||
                    name === 'isLateCover' || name === 'isLate' || name === 'unSuccessful') && checked) {
                    state.formData.isManualRequest = false;
                }
            } else {
                state.formData[name] = value;
            }

            // Clear error when field is edited
            if (state.errors[name]) {
                state.errors[name] = null;
            }
        },

        // Calculate number of days
        calculateDays: (state) => {
            if (state.formData.fromDate && state.formData.toDate) {
                const start = new Date(state.formData.fromDate);
                const end = new Date(state.formData.toDate);
                const diffTime = Math.abs(end - start);
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // +1 to include both start and end dates

                state.formData.numOfDays = state.formData.isHalfDay ? diffDays / 2 : diffDays;
                state.formData.leaveCategory = state.formData.isHalfDay ? "HALF_DAY" : "FULL_DAY";
            }
        },

        // Update manual request based on other flags
        updateManualRequest: (state) => {
            if (state.formData.isHalfDay || state.formData.isUnauthorized || state.formData.isAbsent ||
                state.formData.isLateCover || state.formData.isLate || state.formData.unSuccessful) {
                state.formData.isManualRequest = false;
            }
        },

        // Validate form
        validateForm: (state) => {
            const newErrors = {};

            if (!state.formData.fromDate) newErrors.fromDate = 'Start date is required';
            if (!state.formData.toDate) newErrors.toDate = 'End date is required';
            if (!state.formData.leaveType) newErrors.leaveType = 'Leave type is required';
            if (!state.formData.description) newErrors.description = 'Reason is required';

            if (state.formData.fromDate && state.formData.toDate) {
                const start = new Date(state.formData.fromDate);
                const end = new Date(state.formData.toDate);
                if (end < start) {
                    newErrors.toDate = 'End date cannot be before start date';
                }
            }

            // Validate available leave balance
            if (state.formData.leaveType && state.formData.numOfDays > 0) {
                const selectedType = state.formData.leaveType;
                const typeBalance = state.leaveBalances.find(
                    balance => balance.leaveTypeName === selectedType
                );

                if (typeBalance && typeBalance.remainingLeaves < state.formData.numOfDays) {
                    newErrors.leaveType = `Insufficient ${selectedType.toLowerCase()} leave balance`;
                }
            }

            state.errors = newErrors;
            return Object.keys(newErrors).length === 0; // Return true if no errors
        },

        // Reset form
        resetForm: (state) => {
            state.formData = {
                fromDate: '',
                toDate: '',
                leaveCategory: '',
                leaveType: '',
                description: '',
                isHalfDay: false,
                isFullDay: true,
                numOfDays: 0,
                happenDate: '',
                isUnauthorized: false,
                isManualRequest: true, // Reset to default true
                isAbsent: false,
                isLateCover: false,
                isLate: false,
                unSuccessful: false
            };
            state.errors = {};
        },

        // Set notification
        setNotification: (state, action) => {
            state.notification = action.payload;
        },

        // Close notification
        closeNotification: (state) => {
            state.notification.open = false;
        }
    },
    extraReducers: (builder) => {
        builder
            // Handle fetchLeaveBalances
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

            // Handle submitLeaveRequest
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
                // Reset form
                state.formData = {
                    fromDate: '',
                    toDate: '',
                    leaveCategory: '',
                    leaveType: '',
                    description: '',
                    isHalfDay: false,
                    isFullDay: true,
                    numOfDays: 0,
                    happenDate: '',
                    isUnauthorized: false,
                    isManualRequest: true,
                    isAbsent: false,
                    isLateCover: false,
                    isLate: false,
                    unSuccessful: false
                };
            })
            .addCase(submitLeaveRequest.rejected, (state, action) => {
                state.loading = false;
                state.notification = {
                    open: true,
                    message: `Failed to submit leave request: ${action.payload}`,
                    severity: 'error'
                };
            });
    }
});

// Export actions
export const {
    setUserId,
    updateFormField,
    calculateDays,
    updateManualRequest,
    validateForm,
    resetForm,
    setNotification,
    closeNotification
} = leaveApplicationSlice.actions;

// Export selectors
export const selectUserId = state => state.leaveApplication.userId;
export const selectFormData = state => state.leaveApplication.formData;
export const selectErrors = state => state.leaveApplication.errors;
export const selectLeaveBalances = state => state.leaveApplication.leaveBalances;
export const selectLoading = state => state.leaveApplication.loading;
export const selectFetchingBalance = state => state.leaveApplication.fetchingBalance;
export const selectNotification = state => state.leaveApplication.notification;

// Export helpers
export const leaveHelpers = {
    // Helper function to get remaining leave balance
    getRemainingLeaveBalance: (leaveBalances, typeName) => {
        const leaveType = leaveBalances.find(b => b.leaveTypeName === typeName);
        return leaveType ? leaveType.remainingLeaves : 0;
    },

    // Leave types for the form
    leaveTypes: [
        { value: "Annual Leave", label: "Annual Leave" },
        { value: "Medical Leave", label: "Medical Leave" },
        { value: "Casual Leave", label: "Casual Leave" },
        { value: "Maternity Leave", label: "Maternity Leave" }
    ]
};

// Export reducer
export default leaveApplicationSlice.reducer;