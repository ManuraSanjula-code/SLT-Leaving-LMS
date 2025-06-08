import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Async thunk for fetching absent employees
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
                isAdmin = params.isAdmin
            } = params;

            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                throw new Error('User ID not found in session storage');
            }
            // Construct URL based on admin status
            const baseUrl = isAdmin
                ? `http://localhost:8080/lms/absent/all/${empId}`
                : `http://localhost:8080/lms/absent/${empId}/${empId}`;

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

            // Transform API data to match component structure
            const transformedEmployees = data.content.map((item) => ({
                id: item.id,
                publicId: item.publicId,
                employeeName: item.employeeID,
                date: new Date(item.date).toISOString().split('T')[0],
                reason: item.issueDescription || 'No leave applied',
                isResolved: item.resolve,
                issues: item.issues,
                dueDateForUA: item.dueDateForUA,
                leaveReq: item.leaveReq,
                leaveSuccess: item.leaveSuccess,
                absent: item.absent,
                noPay: item.noPay,
                active: item.active,
                originalData: item
            }));

            return {
                employees: transformedEmployees,
                totalPages: data.totalPages,
                totalElements: data.totalElements,
                currentPage: data.number,
                pageSize: size
            };
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

// Async thunk for resolving absence
export const resolveAbsence = createAsyncThunk(
    'absentEmployees/resolveAbsence',
    async (employeeId, { rejectWithValue }) => {
        try {
            // Uncomment and modify this when you have the resolve API endpoint
            /*
            const response = await fetch(`http://localhost:8080/lms/absent/resolve/${employeeId}`, {
              method: 'PUT',
              credentials: 'include',
              headers: {
                'Content-Type': 'application/json',
              },
            });

            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            */

            // For now, just return the employee ID
            return employeeId;
        } catch (error) {
            return rejectWithValue(error.message);
        }
    }
);

const initialState = {
    employees: [],
    loading: false,
    error: null,
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10,
    filters: {
        startDate: '',
        endDate: '',
        resolutionFilter: 'All',
        searchQuery: ''
    },
    isAdmin: false
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
        // Optimistic update for resolve action
        resolveAbsenceOptimistic: (state, action) => {
            const employeeId = action.payload;
            const employee = state.employees.find(emp => emp.id === employeeId);
            if (employee) {
                employee.isResolved = true;
            }
        }
    },
    extraReducers: (builder) => {
        builder
            // Fetch absent employees
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
            })
            .addCase(fetchAbsentEmployees.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            // Resolve absence
            .addCase(resolveAbsence.pending, (state) => {
                // Loading state handled optimistically
            })
            .addCase(resolveAbsence.fulfilled, (state, action) => {
                // Already updated optimistically
            })
            .addCase(resolveAbsence.rejected, (state, action) => {
                // Revert optimistic update on error
                state.error = action.payload;
                // You might want to revert the optimistic update here
                // by refetching or keeping track of the original state
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
    resolveAbsenceOptimistic
} = absentEmployeesSlice.actions;

export default absentEmployeesSlice.reducer;