import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchNoPayRecords = createAsyncThunk(
    'noPay/fetchNoPayRecords',
    async ({ isAdmin, userId, page = 0, size = 10, userIdFilter = '' }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            let baseUrl = isAdmin
                ? "http://localhost:8080/lms/no-pay/" + empId
                : `http://localhost:8080/lms/no-pay/user/${userId}/${empId}`;

            const queryParams = new URLSearchParams({
                page: page.toString(),
                size: size.toString()
            });

            if (isAdmin && userIdFilter) {
                queryParams.append('userId', userIdFilter);
            }

            const response = await fetch(`${baseUrl}?${queryParams.toString()}`, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            return data;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

const noPaySlice = createSlice({
    name: 'noPay',
    initialState: {
        records: [],
        pagination: {
            totalPages: 0,
            totalElements: 0,
            currentPage: 0,
            pageSize: 10
        },
        loading: false,
        error: null,
        filters: {
            searchQuery: '',
            startDateFilter: '',
            endDateFilter: '',
            userIdFilter: ''
        }
    },
    reducers: {
        // Update filters
        setSearchQuery: (state, action) => {
            state.filters.searchQuery = action.payload;
        },
        setStartDateFilter: (state, action) => {
            state.filters.startDateFilter = action.payload;
        },
        setEndDateFilter: (state, action) => {
            state.filters.endDateFilter = action.payload;
        },
        setUserIdFilter: (state, action) => {
            state.filters.userIdFilter = action.payload;
        },
        // Update pagination
        setCurrentPage: (state, action) => {
            state.pagination.currentPage = action.payload;
        },
        setPageSize: (state, action) => {
            state.pagination.pageSize = action.payload;
            state.pagination.currentPage = 0;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchNoPayRecords.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchNoPayRecords.fulfilled, (state, action) => {
                state.loading = false;

                if (!action.payload || !action.payload.content) {
                    state.records = [];
                    state.pagination.totalPages = 0;
                    state.pagination.totalElements = 0;
                } else {
                    const transformedData = action.payload.content.map(item => ({
                        id: item.id || 0,
                        publicId: item.publicId || "",
                        employeeID: item.employeeID || "",
                        submissionDate: item.submissionDate ? new Date(item.submissionDate).toISOString().split('T')[0] : "",
                        acctualDate: item.acctualDate ? new Date(item.acctualDate).toISOString().split('T')[0] : "",
                        happenDate: item.happenDate ? new Date(item.happenDate).toISOString().split('T')[0] : "",
                        unSuccessful: item.unSuccessful || false,
                        attendance: item.attendance || "",
                        comment: item.comment || "",
                        halfDay: item.halfDay || false,
                        absent: item.absent || false,
                        late: item.late || false,
                        lateCover: item.lateCover || false
                    }));

                    state.records = transformedData;
                    state.pagination.totalPages = action.payload.totalPages || 0;
                    state.pagination.totalElements = action.payload.totalElements || 0;
                    state.pagination.currentPage = action.payload.number || 0;
                }
            })
            .addCase(fetchNoPayRecords.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.records = [];
            });
    }
});

export const {
    setSearchQuery,
    setStartDateFilter,
    setEndDateFilter,
    setUserIdFilter,
    setCurrentPage,
    setPageSize
} = noPaySlice.actions;

export default noPaySlice.reducer;