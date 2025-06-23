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
            const data =  await response.json()
            console.log(data)
            return data;
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
            records,
            searchTerm,
            filterType,
            filterStatus,
            filterIssue
        }
    } = state;

    return records ? records.filter((activity) => {
        // Search by employee ID
        const matchesSearch = activity.employeeId?.toLowerCase().includes(searchTerm.toLowerCase());

        // Filter by attendance type based on new enum structure
        let matchesType = true;
        if (filterType !== "all") {
            if (filterType === "fullDay") {
                matchesType = activity.attendanceType === 'FULL_DAY' && !activity.isLate;
            } else if (filterType === "halfDay") {
                matchesType = activity.attendanceType === 'HALF_DAY';
            } else if (filterType === "absent") {
                matchesType = activity.attendanceType === 'ABSENT';
            } else if (filterType === "fullLeave") {
                matchesType = activity.leaveStatus === 'FULL_LEAVE';
            } else if (filterType === "shortLeave") {
                matchesType = activity.leaveStatus === 'SHORT_LEAVE';
            } else if (filterType === "late") {
                matchesType = activity.isLate;
            }
        }

        // Filter by status based on new structure
        let matchesStatus = true;
        if (filterStatus !== "all") {
            if (filterStatus === "Approved") {
                matchesStatus = activity.leaveStatus === 'LEAVE_APPROVED';
            } else if (filterStatus === "Pending") {
                matchesStatus = activity.leaveStatus === 'LEAVE_REQUESTED' ||
                    (!activity.isUnSuccessful && !activity.isUnauthorized && !activity.hasIssues);
            } else if (filterStatus === "Not Approved") {
                matchesStatus = activity.isUnSuccessful || activity.isUnauthorized;
            }
        }

        let matchesIssue = true;
        if (filterIssue !== "all") {
            matchesIssue = filterIssue === "hasIssue" ? activity.hasIssues : !activity.hasIssues;
        }

        return matchesSearch && matchesType && matchesStatus && matchesIssue;
    }) : [];
};

export const selectActivitiesData = (state) => state.activityRecords;
export const selectIsFiltering = (state) => {
    const { searchTerm, filterType, filterStatus, filterIssue } = state.activityRecords;
    return searchTerm !== "" || filterType !== "all" || filterStatus !== "all" || filterIssue !== "all";
};

export default activityRecordsSlice.reducer;