import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Define the initial state for the form
const initialState = {
    userId: '',
    formData: {
        employeeId: '',
        movementType: '',
        comment: '',
        destination: '',
        category: '',
        happenDate: '',
        isAbsent: false,
        isUnSuccessfulAttdate: false,
        isHalfDay: false,
        unAuthorized: false,
        isLate: false,
        isLateCover: false
    },
    status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
    successMessage: ''
};

// Async thunk to submit movement request
export const submitMovementRequest = createAsyncThunk(
    'movementRequest/submit',
    async (_, { getState, rejectWithValue }) => {
        const { movementRequest } = getState();
        const { userId, formData } = movementRequest;
        const empId = sessionStorage.getItem('userId');
        if (!empId) {
            return rejectWithValue('Employee ID not found in session storage');
        }
        if (!userId) {
            return rejectWithValue('User ID not found. Please login again.');
        }

        // Validate required fields
        if (!formData.movementType) {
            return rejectWithValue('Movement Type is required');
        }
        if (!formData.happenDate) {
            return rejectWithValue('Date is required');
        }
        if (!formData.comment) {
            return rejectWithValue('Comment/Reason is required');
        }

        // Prepare data for API
        const requestData = {
            ...formData,
            userId: userId,
            happenDate: formData.happenDate ? new Date(formData.happenDate) : null
        };

        try {
            const response = await fetch('http://localhost:8080/lms/management/movement/create/' + empId, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                return rejectWithValue(errorData.message || 'Failed to submit movement request');
            }

            return 'Movement request submitted successfully!';
        } catch (error) {
            return rejectWithValue('Error submitting request: ' + error.message);
        }
    }
);

// Create the movement request slice
const movementRequestSlice = createSlice({
    name: 'movementRequest',
    initialState,
    reducers: {
        // Set the user ID
        setUserId: (state, action) => {
            state.userId = action.payload;
        },

        // Update form field
        updateFormField: (state, action) => {
            const { name, value } = action.payload;
            state.formData[name] = value;
        },

        // Reset form
        resetForm: (state) => {
            state.formData = initialState.formData;
            state.status = 'idle';
            state.error = null;
            state.successMessage = '';
        },

        // Clear error
        clearError: (state) => {
            state.error = null;
        },

        // Clear success message
        clearSuccessMessage: (state) => {
            state.successMessage = '';
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(submitMovementRequest.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(submitMovementRequest.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.successMessage = action.payload;
                // Reset the form data on success
                state.formData = initialState.formData;
            })
            .addCase(submitMovementRequest.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload;
            });
    }
});

// Export actions
export const {
    setUserId,
    updateFormField,
    resetForm,
    clearError,
    clearSuccessMessage
} = movementRequestSlice.actions;

// Export selectors
export const selectMovementRequestForm = state => state.movementRequest.formData;
export const selectMovementRequestStatus = state => state.movementRequest.status;
export const selectMovementRequestError = state => state.movementRequest.error;
export const selectMovementRequestSuccess = state => state.movementRequest.successMessage;

// Export reducer
export default movementRequestSlice.reducer;