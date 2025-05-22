import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

const formatDateForApi = (date) => {
    const d = date instanceof Date ? date : new Date(date);
    if (isNaN(d.getTime())) return '';
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

export const fetchAttendanceData = createAsyncThunk(
    'attendance/fetchAttendanceData',
    async (date, { rejectWithValue }) => {
        try {
            const formattedDate = formatDateForApi(date);
            if (!formattedDate) return rejectWithValue('Invalid date');

            const response = await axios.get(`http://localhost:8080/api/attendance/2025-05-06`);

            // Handle various response formats
            const responseData = response.data || {};
            let attendanceRecords = [];

            if (Array.isArray(responseData)) {
                attendanceRecords = responseData;
            } else if (responseData.content && Array.isArray(responseData.content)) {
                attendanceRecords = responseData.content;
            } else if (responseData) {
                attendanceRecords = [responseData];
            }

            return attendanceRecords;
        } catch (error) {
            console.error('Error fetching attendance data:', error);
            return rejectWithValue(error.response?.data?.message || 'Failed to load attendance data');
        }
    }
);

const initialState = {
    attendanceData: [],
    filteredData: [],
    loading: false,
    error: null,
    selectedDate: new Date().toISOString(), // Store as ISO string
    searchTerm: '',
    filterStatus: 'all',
    page: 0,
    rowsPerPage: 5
};

const attendanceSlice = createSlice({
    name: 'attendance',
    initialState,
    reducers: {
        setSelectedDate: {
            reducer: (state, action) => {
                state.selectedDate = action.payload;
            },
            prepare: (date) => {
                // Convert Date object to ISO string before storing
                const isoDate = date instanceof Date ? date.toISOString() : date;
                return { payload: isoDate };
            }
        },
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload || '';
            state.page = 0;
        },
        setFilterStatus: (state, action) => {
            state.filterStatus = action.payload || 'all';
            state.page = 0;
        },
        setPage: (state, action) => {
            state.page = Math.max(0, action.payload || 0);
        },
        setRowsPerPage: (state, action) => {
            state.rowsPerPage = Math.max(1, action.payload || 5);
            state.page = 0;
        },
        applyFilters: (state) => {
            const { attendanceData = [], searchTerm = '', filterStatus = 'all' } = state;

            state.filteredData = attendanceData.filter(item => {
                if (!item) return false;

                const matchesSearch =
                    String(item.employeeID || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                    String(item.shiftTime || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                    String(item.shiftCode || '').toLowerCase().includes(searchTerm.toLowerCase());

                let matchesStatus = true;
                if (filterStatus !== 'all') {
                    if (filterStatus === 'absent') matchesStatus = item.isAbsent === true;
                    else if (filterStatus === 'late') matchesStatus = item.isLate === true;
                    else if (filterStatus === 'leave') {
                        matchesStatus = item.isFullLeave === true || item.isHalfDay === true || item.isShortLeave === true;
                    }
                }

                return matchesSearch && matchesStatus;
            });
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchAttendanceData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchAttendanceData.fulfilled, (state, action) => {
                state.loading = false;
                state.attendanceData = Array.isArray(action.payload) ? action.payload : [];
                state.filteredData = Array.isArray(action.payload) ? action.payload : [];
                state.page = 0;
            })
            .addCase(fetchAttendanceData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Failed to load data';
                state.attendanceData = [];
                state.filteredData = [];
            });
    }
});

export const {
    setSelectedDate,
    setSearchTerm,
    setFilterStatus,
    setPage,
    setRowsPerPage,
    applyFilters
} = attendanceSlice.actions;

export const selectAttendanceState = (state) => ({
    ...initialState,
    ...(state.attendance || {})
});

export const selectPaginatedData = (state) => {
    const { filteredData = [], page = 0, rowsPerPage = 5 } = selectAttendanceState(state);
    const start = page * rowsPerPage;
    const end = start + rowsPerPage;
    return filteredData.slice(start, end);
};

export default attendanceSlice.reducer;