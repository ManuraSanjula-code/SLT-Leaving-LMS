import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const formatDate = (date) => {
  const d = new Date(date);
  let month = '' + (d.getMonth() + 1);
  let day = '' + d.getDate();
  const year = d.getFullYear();

  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  return [year, month, day].join('-');
};

export const fetchAttendanceData = createAsyncThunk(
  'attendance/fetchAttendanceData',
  async (dateString, { rejectWithValue }) => {
    try {
      const response = await fetch(`http://192.168.3.20:8080/api/attendance/${dateString}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      return data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

const initialState = {
  attendanceData: null,
  filteredData: [],
  
  loading: false,
  error: null,
  
  dateInput: formatDate(new Date()),
  page: 1,
  rowsPerPage: 10,
  employeeFilter: '',
  statusFilter: 'all',
};

const attendanceSlice = createSlice({
  name: 'attendance',
  initialState,
  reducers: {
    setDateInput: (state, action) => {
      state.dateInput = action.payload;
    },
    setPage: (state, action) => {
      state.page = action.payload;
    },
    setRowsPerPage: (state, action) => {
      state.rowsPerPage = action.payload;
      state.page = 1; 
    },
    
    setEmployeeFilter: (state, action) => {
      state.employeeFilter = action.payload;
      state.page = 1;
    },
    setStatusFilter: (state, action) => {
      state.statusFilter = action.payload;
      state.page = 1; 
    },
    
    applyFilters: (state) => {
      if (!state.attendanceData) return;

      let result = [...state.attendanceData.content];

      if (state.employeeFilter) {
        result = result.filter(record =>
          record.employeeId.toLowerCase().includes(state.employeeFilter.toLowerCase())
        );
      }

      if (state.statusFilter !== 'all') {
        result = result.filter(record => {
          if (state.statusFilter === 'present') return record.attendanceType === 'FULL_DAY';
          if (state.statusFilter === 'absent') return record.attendanceType === 'ABSENT';
          if (state.statusFilter === 'halfday') return record.attendanceType === 'HALF_DAY';
          if (state.statusFilter === 'leave') return record.leaveStatus;
          return true;
        });
      }

      state.filteredData = result;
    },
    
    resetFilters: (state) => {
      state.employeeFilter = '';
      state.statusFilter = 'all';
      state.page = 1;
      if (state.attendanceData) {
        state.filteredData = state.attendanceData.content;
      }
    },
    
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAttendanceData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAttendanceData.fulfilled, (state, action) => {
        state.loading = false;
        state.attendanceData = action.payload;
        state.filteredData = action.payload.content;
        state.error = null;
      })
      .addCase(fetchAttendanceData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.attendanceData = null;
        state.filteredData = [];
      });
  },
});

export const {
  setDateInput,
  setPage,
  setRowsPerPage,
  setEmployeeFilter,
  setStatusFilter,
  applyFilters,
  resetFilters,
  clearError,
} = attendanceSlice.actions;

export default attendanceSlice.reducer;