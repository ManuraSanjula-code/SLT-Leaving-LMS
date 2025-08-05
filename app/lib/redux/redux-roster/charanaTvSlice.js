import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchRosterData = createAsyncThunk(
    'roster/fetchRosterData',
    async (weekStartDate, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/api/duty-roster/charana-tv/week/${weekStartDate}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            if (response.status === 204 || response.headers.get('Content-Length') === '0') {
                return null;
            }

            const data = await response.json();

            if (!data || Object.keys(data).length === 0) {
                throw new Error('No data found for the selected week');
            }

            if (!data.dailyDuties || !Array.isArray(data.dailyDuties)) {
                throw new Error('Invalid data structure received from server');
            }

            return data;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

const getCurrentMonday = () => {
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - today.getDay() + 1);
    return monday.toISOString().split('T')[0];
};

const initialState = {
    rosterData: null,
    loading: false,
    error: null,
    selectedWeekStart: getCurrentMonday(),
};

const charanaRosterSlice = createSlice({
    name: 'charanaRoster',
    initialState,
    reducers: {
        setSelectedWeekStart: (state, action) => {
            state.selectedWeekStart = action.payload;
        },
        clearError: (state) => {
            state.error = null;
        },
        clearRosterData: (state) => {
            state.rosterData = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchRosterData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchRosterData.fulfilled, (state, action) => {
                state.loading = false;
                state.rosterData = action.payload;
                state.error = null;
            })
            .addCase(fetchRosterData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.rosterData = null;
            });
    },
});

export const { setSelectedWeekStart, clearError, clearRosterData } = charanaRosterSlice.actions;
export default charanaRosterSlice.reducer;