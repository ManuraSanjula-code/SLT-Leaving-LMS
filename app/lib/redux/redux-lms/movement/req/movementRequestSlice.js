import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const MovementType = {
    FULLDAY: 'FULLDAY',
    OFFICE_TO_HOME: 'OFFICE_TO_HOME',
    HOME_TO_OFFICE: 'HOME_TO_OFFICE',
    REMOTEWORK: 'REMOTEWORK',
};

const initialState = {
    userId: '',
    formData: {
        employeeId: '',
        movementType: '',
        comment: '',
        destination: '',
        category: 'UN-AUTHORIZED',
        happenDate: '',
        logTime: '',
        inTime: '',
        outTime: '',
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

        const requiredFields = [
            'employeeId', 'movementType', 'comment', 'destination',
            'category', 'happenDate', 'logTime', 'inTime', 'outTime'
        ];

        for (const field of requiredFields) {
            if (!submissionData[field]) {
                return rejectWithValue(`${field.replace(/([A-Z])/g, ' $1').trim()} is required`);
            }
        }

        if (submissionData.inTime >= submissionData.outTime) {
            return rejectWithValue('Out time must be after in time');
        }

        const requestData = {
            employeeId: submissionData.employeeId?.trim(),
            userId: userId,
            movementType: submissionData.movementType,
            comment: submissionData.comment?.trim(),
            destination: submissionData.destination?.trim(),
            category: submissionData.category?.trim(),
            happenDate: new Date(submissionData.happenDate).toISOString(),
            logTime: new Date(submissionData.logTime).toISOString(),
            inTime: submissionData.inTime,
            outTime: submissionData.outTime,
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
    resetForm,
    clearError,
    clearSuccessMessage
} = movementRequestSlice.actions;

export const selectMovementRequestForm = state => state.movementRequest.formData;
export const selectMovementRequestStatus = state => state.movementRequest.status;
export const selectMovementRequestError = state => state.movementRequest.error;
export const selectMovementRequestSuccess = state => state.movementRequest.successMessage;

export default movementRequestSlice.reducer;