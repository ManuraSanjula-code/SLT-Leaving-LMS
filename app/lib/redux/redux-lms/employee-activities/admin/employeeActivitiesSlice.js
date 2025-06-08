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
                `http://localhost:8080/lms/${empId}?page=${page}&size=${rowsPerPage}`,
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

// Initial state for the activity records
const initialState = {
    records: [],
    totalElements: 0,
    totalPages: 0,
    loading: false,
    error: null,
    // Search and filter states
    searchTerm: '',
    filterType: 'all',
    filterStatus: 'all',
    filterIssue: 'all',
    // Pagination states
    page: 0,
    rowsPerPage: 10,
};

const activityRecordsSlice = createSlice({
    name: 'activityRecords',
    initialState,
    reducers: {
        // Set search term
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload;
        },
        // Set filter type
        setFilterType: (state, action) => {
            state.filterType = action.payload;
        },
        // Set filter status
        setFilterStatus: (state, action) => {
            state.filterStatus = action.payload;
        },
        // Set filter issue
        setFilterIssue: (state, action) => {
            state.filterIssue = action.payload;
        },
        // Set page
        setPage: (state, action) => {
            state.page = action.payload;
        },
        // Set rows per page
        setRowsPerPage: (state, action) => {
            state.rowsPerPage = action.payload;
            state.page = 0; // Reset to first page when changing rows per page
        },
        // Clear all filters
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

// Export actions
export const {
    setSearchTerm,
    setFilterType,
    setFilterStatus,
    setFilterIssue,
    setPage,
    setRowsPerPage,
    clearFilters,
} = activityRecordsSlice.actions;

// Create selector functions for filtered activities
export const selectFilteredActivities = (state) => {
    const {
        activityRecords: {
            records,
            searchTerm,
            filterType,
            filterStatus,
            filterIssue
        }
    } = state;

    return records ? records.filter((activity) => {
        const matchesSearch = activity.employeeID.toLowerCase().includes(searchTerm.toLowerCase());

        // Type filter
        let matchesType = true;
        if (filterType !== "all") {
            if (filterType === "fullDay") matchesType = activity.fullDay;
            else if (filterType === "halfDay") matchesType = activity.halfDay;
            else if (filterType === "fullLeave") matchesType = activity.fullLeave;
            else if (filterType === "shortLeave") matchesType = activity.shortLeave;
            else if (filterType === "absent") matchesType = activity.absent;
            else if (filterType === "late") matchesType = activity.late;
        }

        // Status filter
        let matchesStatus = true;
        if (filterStatus !== "all") {
            if (filterStatus === "Approved") matchesStatus = activity.leaveSuccess;
            else if (filterStatus === "Pending") matchesStatus = !activity.leaveSuccess && !activity.unSuccessful && !activity.unAuthorized;
            else if (filterStatus === "Not Approved") matchesStatus = activity.unSuccessful || activity.unAuthorized;
        }

        // Issue filter
        let matchesIssue = true;
        if (filterIssue !== "all") {
            matchesIssue = filterIssue === "hasIssue" ? activity.issues : !activity.issues;
        }

        return matchesSearch && matchesType && matchesStatus && matchesIssue;
    }) : [];
};

// Export selectors
export const selectActivitiesData = (state) => state.activityRecords;
export const selectIsFiltering = (state) => {
    const { searchTerm, filterType, filterStatus, filterIssue } = state.activityRecords;
    return searchTerm !== "" || filterType !== "all" || filterStatus !== "all" || filterIssue !== "all";
};

export default activityRecordsSlice.reducer;