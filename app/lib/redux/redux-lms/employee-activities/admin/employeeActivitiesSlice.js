import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchActivityRecords = createAsyncThunk(
    'activityRecords/fetchActivityRecords',
    async ({ page, rowsPerPage }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');

            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(
                `http://192.168.3.20:8080/lms/${empId}?page=${page}&size=${rowsPerPage}`,
                { credentials: 'include' }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

const initialState = {
    records: [],
    totalElements: 0,
    totalPages: 0,
    loading: false,
    error: null,
    searchTerm: '',
    filterType: 'all',
    filterStatus: 'all',
    filterIssue: 'all',
    page: 0,
    rowsPerPage: 10,
};

const activityRecordsSlice = createSlice({
    name: 'activityRecords',
    initialState,
    reducers: {
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload;
        },
        setFilterType: (state, action) => {
            state.filterType = action.payload;
        },
        setFilterStatus: (state, action) => {
            state.filterStatus = action.payload;
        },
        setFilterIssue: (state, action) => {
            state.filterIssue = action.payload;
        },
        setPage: (state, action) => {
            state.page = action.payload;
        },
        setRowsPerPage: (state, action) => {
            state.rowsPerPage = action.payload;
            state.page = 0;
        },
        clearFilters: (state) => {
            state.searchTerm = '';
            state.filterType = 'all';
            state.filterStatus = 'all';
            state.filterIssue = 'all';
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchActivityRecords.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchActivityRecords.fulfilled, (state, action) => {
                state.loading = false;
                state.records = action.payload.content;
                state.totalElements = action.payload.totalElements;
                state.totalPages = action.payload.totalPages;
            })
            .addCase(fetchActivityRecords.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    },
});

export const {
    setSearchTerm,
    setFilterType,
    setFilterStatus,
    setFilterIssue,
    setPage,
    setRowsPerPage,
    clearFilters,
} = activityRecordsSlice.actions;

export const selectFilteredActivities = (state) => {
    const {
        activityRecords: {
            records = [],
            searchTerm = '',
            filterType = 'all',
            filterStatus = 'all',
            filterIssue = 'all'
        }
    } = state;

    if (!records || records.length === 0) return [];

    const searchTermLower = searchTerm.toLowerCase();

    return records.filter((activity) => {
        if (searchTerm) {
            const idToSearch = (activity.userId || activity.employeeId || '').toLowerCase();
            if (!idToSearch.includes(searchTermLower)) {
                return false;
            }
        }

        if (filterType !== 'all') {
            switch (filterType) {
                case 'fullDay':
                    if (activity.attendanceType !== 'FULL_DAY' || activity.isLate) return false;
                    break;
                case 'halfDay':
                    if (activity.attendanceType !== 'HALF_DAY') return false;
                    break;
                case 'absent':
                    if (activity.attendanceType !== 'ABSENT') return false;
                    break;
                case 'fullLeave':
                    if (activity.leaveStatus !== 'FULL_LEAVE') return false;
                    break;
                case 'shortLeave':
                    if (activity.leaveStatus !== 'SHORT_LEAVE') return false;
                    break;
                case 'late':
                    if (!activity.isLate) return false;
                    break;
                default:
                    break;
            }
        }

        if (filterStatus !== 'all') {
            switch (filterStatus) {
                case 'Approved':
                    if (activity.leaveStatus !== 'LEAVE_APPROVED') return false;
                    break;
                case 'Pending':
                    if (!(
                        activity.leaveStatus === 'LEAVE_REQUESTED' ||
                        (!activity.isUnSuccessful && !activity.isUnauthorized && !activity.hasIssues)
                    )) return false;
                    break;
                case 'Not Approved':
                    if (!(activity.isUnSuccessful || activity.isUnauthorized)) return false;
                    break;
                default:
                    break;
            }
        }

        if (filterIssue !== 'all') {
            const hasIssue = activity.hasIssues;
            if (filterIssue === 'hasIssue' && !hasIssue) return false;
            if (filterIssue === 'noIssue' && hasIssue) return false;
        }

        return true;
    });
};

export const selectActivitiesData = (state) => state.activityRecords;
export const selectIsFiltering = (state) => {
    const { searchTerm, filterType, filterStatus, filterIssue } = state.activityRecords;
    return searchTerm !== "" || filterType !== "all" || filterStatus !== "all" || filterIssue !== "all";
};

export default activityRecordsSlice.reducer;