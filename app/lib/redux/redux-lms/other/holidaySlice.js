import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchHolidays = createAsyncThunk(
    'holidays/fetchHolidays',
    async ({ userId, year }, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${userId}?year=${year}`, {
                method: 'GET',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error('Failed to fetch holidays');
            }
            const data = await response.json();
            return data;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const addHoliday = createAsyncThunk(
    'holidays/addHoliday',
    async ({ userId, holidayData }, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${userId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(holidayData)
            });
            if (!response.ok) {
                throw new Error('Failed to add holiday');
            }
            return { ...holidayData };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const updateHoliday = createAsyncThunk(
    'holidays/updateHoliday',
    async ({ holidayId, userId, holidayData }, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${holidayId}/${userId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(holidayData)
            });
            if (!response.ok) {
                throw new Error('Failed to update holiday');
            }
            return { id: holidayId, ...holidayData };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

export const deleteHoliday = createAsyncThunk(
    'holidays/deleteHoliday',
    async ({ holidayId, userId }, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/lms/holiday/${holidayId}/${userId}`, {
                method: 'DELETE',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error('Failed to delete holiday');
            }
            return holidayId;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

const holidaySlice = createSlice({
    name: 'holidays',
    initialState: {
        holidays: [],
        year: new Date().getFullYear(),
        loading: false,
        error: null,
        openAddDialog: false,
        openEditDialog: false,
        currentHoliday: null,
        formData: {
            holidayDate: '',
            description: '',
            isRecurring: false
        }
    },
    reducers: {
        setYear: (state, action) => {
            state.year = action.payload;
        },
        openAddDialog: (state) => {
            state.openAddDialog = true;
            state.formData = {
                holidayDate: '',
                description: '',
                isRecurring: false
            };
        },
        closeAddDialog: (state) => {
            state.openAddDialog = false;
            state.formData = {
                holidayDate: '',
                description: '',
                isRecurring: false
            };
        },
        openEditDialog: (state, action) => {
            state.openEditDialog = true;
            state.currentHoliday = action.payload;
            state.formData = {
                holidayDate: action.payload.holidayDate,
                description: action.payload.description,
                isRecurring: action.payload.recurring
            };
        },
        closeEditDialog: (state) => {
            state.openEditDialog = false;
            state.currentHoliday = null;
            state.formData = {
                holidayDate: '',
                description: '',
                isRecurring: false
            };
        },
        updateFormData: (state, action) => {
            state.formData = { ...state.formData, ...action.payload };
        },
        clearHolidays: (state) => {
            state.holidays = [];
            state.year = new Date().getFullYear();
        },
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchHolidays.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchHolidays.fulfilled, (state, action) => {
                state.loading = false;
                state.holidays = action.payload;
            })
            .addCase(fetchHolidays.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(addHoliday.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(addHoliday.fulfilled, (state, action) => {
                state.loading = false;
                state.openAddDialog = false;
                state.formData = {
                    holidayDate: '',
                    description: '',
                    isRecurring: false
                };
            })
            .addCase(addHoliday.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(updateHoliday.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateHoliday.fulfilled, (state, action) => {
                state.loading = false;
                state.openEditDialog = false;
                state.currentHoliday = null;
                state.formData = {
                    holidayDate: '',
                    description: '',
                    isRecurring: false
                };
            })
            .addCase(updateHoliday.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(deleteHoliday.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(deleteHoliday.fulfilled, (state, action) => {
                state.loading = false;
                state.holidays = state.holidays.filter(holiday => holiday.id !== action.payload);
            })
            .addCase(deleteHoliday.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

export const {
    setYear,
    openAddDialog,
    closeAddDialog,
    openEditDialog,
    closeEditDialog,
    updateFormData,
    clearHolidays,
    clearError
} = holidaySlice.actions;

export default holidaySlice.reducer;