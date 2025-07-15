import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchRosterData = createAsyncThunk(
    'roster/fetchRosterData',
    async (date, { rejectWithValue }) => {
        try {
            const apiUrl = `http://localhost:8080/api/attendance/roster/${date}`;
            console.log('Fetching from URL:', apiUrl);

            const response = await fetch(apiUrl);

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Error fetching roster data:', error);
            return rejectWithValue(`Failed to fetch roster data: ${error.message}`);
        }
    }
);

const initialState = {
    rosterData: null,
    loading: false,
    error: null,
    selectedDate: (() => {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    })()
};

const rosterSlice = createSlice({
    name: 'roster',
    initialState,
    reducers: {
        setSelectedDate: (state, action) => {
            state.selectedDate = action.payload;
        },
        clearRosterError: (state) => {
            state.error = null;
        }
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
            })
            .addCase(fetchRosterData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

export const { setSelectedDate, clearRosterError } = rosterSlice.actions;
export default rosterSlice.reducer;