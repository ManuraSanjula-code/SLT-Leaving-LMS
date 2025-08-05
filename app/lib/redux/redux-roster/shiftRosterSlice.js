import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchRosterData = createAsyncThunk(
  'shiftRoster/fetchRosterData',
  async ({ year, month }, { rejectWithValue }) => {
    try {
      const response = await fetch(`http://192.168.3.20:8080/api/roster/shift-roster/${year}/${month}`);
      
      if (!response.ok) {
        if (response.status === 404) {
          return null;
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (!data || Object.keys(data).length === 0) {
        throw new Error('No data found for the selected month and year');
      }
      
      if (!data.dutyTurn || !data.dates) {
        throw new Error('Invalid data structure received from server');
      }
      
      return data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

const initialState = {
  rosterData: null,
  
  loading: false,
  error: null,
  
  selectedYear: new Date().getFullYear(),
  selectedMonth: new Date().toLocaleString('default', { month: 'long' }),
  
  months: [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ],
  shifts: ['00:00 - 08:00', '08:00 - 16:00', '16:00 - 24:00'],
  teamColors: {
    'T 1': '#e3f2fd',
    'T 1 ROT': '#bbdefb',
    'T 2': '#e8f5e8',
    'T 2 ROT': '#c8e6c8',
    'T 3': '#fff3e0',
    'T 3 ROT': '#ffcc02',
    'Na': '#f5f5f5'
  },
};

const shiftRosterSlice = createSlice({
  name: 'shiftRoster',
  initialState,
  reducers: {
    setSelectedYear: (state, action) => {
      state.selectedYear = action.payload;
    },
    setSelectedMonth: (state, action) => {
      state.selectedMonth = action.payload;
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

export const {
  setSelectedYear,
  setSelectedMonth,
  clearError,
  clearRosterData,
} = shiftRosterSlice.actions;

export default shiftRosterSlice.reducer;