import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchAbsentEmployees = createAsyncThunk(
    'absentEmployees/fetchAbsentEmployees',
    async (params, { rejectWithValue }) => {
        try {
            const {
                page = 0,
                size = 10,
                search = '',
                startDate = '',
                endDate = '',
                resolutionFilter = 'All',
                isAdmin = false
            } = params;

            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                throw new Error('User ID not found in session storage');
            }
            const baseUrl = isAdmin
                ? `http://localhost:8080/lms/absent/all/${empId}`
                : `http://localhost:8080/lms/absent/${empId}/${empId}`

            // Add query parameters
            const urlParams = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (search.trim()) {
                urlParams.append('search', search.trim());
            }

            if (startDate) {
                urlParams.append('startDate', startDate);
            }

            if (endDate) {
                urlParams.append('endDate', endDate);
            }

            if (resolutionFilter !== 'All') {
                urlParams.append('resolved', resolutionFilter === 'Resolved' ? 'true' : 'false');
            }

            const response = await fetch(
                `${baseUrl}?${urlParams.toString()}`,
                {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log(data)
            const transformedEmployees = data.content.map((item) => ({
                id: item.id,
                publicId: item.publicId,
                employeeId: item.employeeId,
                employeeName: item.userId,
                date: new Date(item.date).toISOString().split('T')[0],
                reason: item.issueDescription || 'No issue description',

                isResolved: item.isResolved || false,
                isLate: item.isLate || false,
                isLateCovered: item.isLateCovered || false,
                isUnauthorized: item.isUnauthorized || false,
                isUnSuccessful: item.isUnSuccessful || false,
                isHoliday: item.isHoliday || false,
                hasIssues: item.hasIssues || false,
                isManual: item.isManual || false,
                isActive: item.isActive !== false,

                attendanceType: item.attendanceType,
                leaveStatus: item.leaveStatus,
                payStatus: item.payStatus,
                resolve: item.resolve,

                isFullDay: item.isFullDay || false,
                isHalfDay: item.isHalfDay || false,
                isAbsent: item.isAbsent || false,
                isNoPay: item.isNoPay || false,

                arrivalDate: item.arrivalDate,
                arrivalTime: item.arrivalTime,
                leftTime: item.leftTime,
                dueDateForUA: item.dueDateForUA,
                etlRunTime: item.etlRunTime,
                createdDate: item.createdDate,
                updatedDate: item.updatedDate,

                terminalId: item.terminalId,
                viaMovement: item.viaMovement,
                viaLeave: item.viaLeave,

                originalData: item
            }));

            return {
                employees: transformedEmployees,
                totalPages: data.totalPages,
                totalElements: data.totalElements,
                currentPage: data.number,
                pageSize: data.size
            };
        } catch (error) {
            console.error('Fetch error:', error);
            return rejectWithValue(error.message);
        }
    }
);

const initialState = {
    employees: [],
    loading: false,
    error: null,
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    pageSize: 10,
    filters: {
        startDate: '',
        endDate: '',
        resolutionFilter: 'All',
        searchQuery: ''
    },
    isAdmin: false,
    hasDataBeenFetched: false
};

const absentEmployeesSlice = createSlice({
    name: 'absentEmployees',
    initialState,
    reducers: {
        setFilters: (state, action) => {
            state.filters = { ...state.filters, ...action.payload };
        },
        clearFilters: (state) => {
            state.filters = {
                startDate: '',
                endDate: '',
                resolutionFilter: 'All',
                searchQuery: ''
            };
        },
        setPageSize: (state, action) => {
            state.pageSize = action.payload;
            state.currentPage = 0;
        },
        setCurrentPage: (state, action) => {
            state.currentPage = action.payload;
        },
        setIsAdmin: (state, action) => {
            state.isAdmin = action.payload;
        },
        clearError: (state) => {
            state.error = null;
        },
        resolveAbsenceOptimistic: (state, action) => {
            const employeeId = action.payload;
            const employee = state.employees.find(emp => emp.id === employeeId);
            if (employee) {
                employee.isResolved = true;
            }
        },

        resetState: () => initialState
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchAbsentEmployees.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchAbsentEmployees.fulfilled, (state, action) => {
                state.loading = false;
                state.employees = action.payload.employees;
                state.totalPages = action.payload.totalPages;
                state.totalElements = action.payload.totalElements;
                state.currentPage = action.payload.currentPage;
                state.pageSize = action.payload.pageSize;
                state.error = null;
                state.hasDataBeenFetched = true;
            })
            .addCase(fetchAbsentEmployees.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.hasDataBeenFetched = true;
            });
    },
});

export const {
    setFilters,
    clearFilters,
    setPageSize,
    setCurrentPage,
    setIsAdmin,
    clearError,
    resolveAbsenceOptimistic,
    resetState
} = absentEmployeesSlice.actions;

export default absentEmployeesSlice.reducer;