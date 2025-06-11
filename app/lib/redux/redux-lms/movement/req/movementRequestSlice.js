import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const initialState = {
    userId: '',
    formData: {
        employeeId: '',
        movementType: '',
        comment: '',
        destination: '',
        category: '',
        happenDate: '',
        logTime: '',
        inTime: '00:00',
        outTime: '00:00',
        componentBehavior: 'FULL_DAY',
        requestStatus: 'DRAFT'
    },
    status: 'idle',
    error: null,
    successMessage: ''
};

export const submitMovementRequest = createAsyncThunk(
    'movementRequest/submit',
    async (submissionData, { getState, rejectWithValue }) => {
        const { movementRequest } = getState();
        const { userId } = movementRequest;
        const empId = sessionStorage.getItem('userId');

        if (!empId) {
            return rejectWithValue('Employee ID not found in session storage');
        }
        if (!userId) {
            return rejectWithValue('User ID not found. Please login again.');
        }

        if (!submissionData.movementType) {
            return rejectWithValue('Movement Type is required');
        }
        if (!submissionData.happenDate) {
            return rejectWithValue('Date is required');
        }
        if (!submissionData.comment) {
            return rejectWithValue('Comment/Reason is required');
        }
        if (!submissionData.destination) {
            return rejectWithValue('Destination is required');
        }
        if (!submissionData.componentBehavior) {
            return rejectWithValue('Component Behavior is required');
        }

        const requestData = {
            employeeId: submissionData.employeeId?.trim(),
            userId: userId,
            movementType: submissionData.movementType,
            comment: submissionData.comment?.trim(),
            destination: submissionData.destination?.trim(),
            category: submissionData.category?.trim() || '',
            happenDate: submissionData.happenDate ? new Date(submissionData.happenDate).toISOString() : null,
            logTime: submissionData.logTime ? new Date(submissionData.logTime).toISOString() : new Date('1990-01-01T00:00:00').toISOString(),
            inTime: submissionData.inTime || '00:00',
            outTime: submissionData.outTime || '00:00',
            componentBehavior: submissionData.componentBehavior,
            requestStatus: 'DRAFT'
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
                let errorMessage = `HTTP ${response.status}`;
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    const errorText = await response.text();
                    errorMessage = errorText || errorMessage;
                }
                return rejectWithValue(errorMessage);
            }

            return 'Movement request submitted successfully!';
        } catch (error) {
            return rejectWithValue('Error submitting request: ' + error.message);
        }
    }
);

const movementRequestSlice = createSlice({
    name: 'movementRequest',
    initialState,
    reducers: {
        setUserId: (state, action) => {
            state.userId = action.payload;
        },

        updateFormField: (state, action) => {
            const { name, value } = action.payload;
            state.formData[name] = value;

            if (name === 'movementType') {
                switch (value) {
                    case 'ABSENT':
                        state.formData.componentBehavior = 'ABSENT';
                        break;
                    case 'LATEWORK':
                        state.formData.componentBehavior = 'LATE';
                        break;
                    case 'UNSUCCESSFUL':
                        state.formData.componentBehavior = 'UNSUCCESSFUL';
                        break;
                    case 'UNAUTHORIZED':
                        state.formData.componentBehavior = 'UNAUTHORIZED';
                        break;
                    case 'REMOTEWORK':
                        state.formData.componentBehavior = 'FULL_DAY';
                        break;
                    default:
                        state.formData.componentBehavior = 'FULL_DAY';
                        break;
                }
            }
        },

        setComponentBehavior: (state, action) => {
            state.formData.componentBehavior = action.payload;
        },

        resetForm: (state) => {
            state.formData = initialState.formData;
            state.status = 'idle';
            state.error = null;
            state.successMessage = '';
        },

        clearError: (state) => {
            state.error = null;
        },

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

export const {
    setUserId,
    updateFormField,
    setComponentBehavior,
    resetForm,
    clearError,
    clearSuccessMessage
} = movementRequestSlice.actions;

export const selectMovementRequestForm = state => state.movementRequest.formData;
export const selectMovementRequestStatus = state => state.movementRequest.status;
export const selectMovementRequestError = state => state.movementRequest.error;
export const selectMovementRequestSuccess = state => state.movementRequest.successMessage;

export default movementRequestSlice.reducer;