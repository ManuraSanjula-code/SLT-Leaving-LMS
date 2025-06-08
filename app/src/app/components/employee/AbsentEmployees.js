"use client";

import React, { useEffect, useCallback, useMemo } from "react";
import { useDispatch, useSelector } from 'react-redux';
import { useRouter } from 'next/navigation';
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Badge,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
  Pagination,
  Chip,
  Stack,
  InputAdornment,
} from "@mui/material";
import { Search as SearchIcon } from "@mui/icons-material";

// Redux imports
import {
  fetchAbsentEmployees,
  resolveAbsence,
  resolveAbsenceOptimistic,
  setFilters,
  clearFilters,
  setPageSize,
  setCurrentPage,
  setIsAdmin,
  clearError
} from '../../../../lib/redux/redux-lms/absent/absentEmployeesSlice';

import {
  selectEmployees,
  selectLoading,
  selectError,
  selectCurrentPage,
  selectTotalPages,
  selectTotalElements,
  selectPageSize,
  selectFilters,
  selectIsAdmin,
  selectPaginationInfo,
  selectHasEmployees,
  selectIsFirstLoad,
  selectIsSubsequentLoad
} from '../../../../lib/redux/redux-lms/absent/absentEmployeesSelectors';

const AbsentEmployees = ({ isAdmin = false }) => {
  const dispatch = useDispatch();
  const router = useRouter();

  // Redux state selectors
  const allEmployees = useSelector(selectEmployees); // All loaded employees
  const loading = useSelector(selectLoading);
  const error = useSelector(selectError);
  const currentPage = useSelector(selectCurrentPage);
  const pageSize = useSelector(selectPageSize);
  const filters = useSelector(selectFilters);
  const isAdminFromState = useSelector(selectIsAdmin);
  const hasEmployees = useSelector(selectHasEmployees);
  const isFirstLoad = useSelector(selectIsFirstLoad);
  const isSubsequentLoad = useSelector(selectIsSubsequentLoad);

  // Destructure filters
  const { startDate, endDate, resolutionFilter, searchQuery } = filters;

  // CLIENT-SIDE FILTERING AND SEARCHING
  const filteredEmployees = useMemo(() => {
    let filtered = [...allEmployees];

    // Search filter (only for admin)
    if (isAdminFromState && searchQuery.trim()) {
      const query = searchQuery.toLowerCase().trim();
      filtered = filtered.filter(employee =>
          employee.employeeName?.toLowerCase().includes(query) ||
          employee.reason?.toLowerCase().includes(query) ||
          employee.publicId?.toLowerCase().includes(query)
      );
    }

    // Date range filter
    if (startDate) {
      filtered = filtered.filter(employee => {
        const employeeDate = new Date(employee.date);
        const filterStartDate = new Date(startDate);
        return employeeDate >= filterStartDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(employee => {
        const employeeDate = new Date(employee.date);
        const filterEndDate = new Date(endDate);
        return employeeDate <= filterEndDate;
      });
    }

    // Resolution filter
    if (resolutionFilter !== 'All') {
      const isResolved = resolutionFilter === 'Resolved';
      filtered = filtered.filter(employee => employee.isResolved === isResolved);
    }

    return filtered;
  }, [allEmployees, searchQuery, startDate, endDate, resolutionFilter, isAdminFromState]);

  // CLIENT-SIDE PAGINATION
  const paginatedEmployees = useMemo(() => {
    const startIndex = currentPage * pageSize;
    const endIndex = startIndex + pageSize;
    return filteredEmployees.slice(startIndex, endIndex);
  }, [filteredEmployees, currentPage, pageSize]);

  // Calculate pagination info based on filtered data
  const paginationInfo = useMemo(() => {
    const totalElements = filteredEmployees.length;
    const totalPages = Math.ceil(totalElements / pageSize);
    const startIndex = (currentPage * pageSize) + 1;
    const endIndex = Math.min((currentPage + 1) * pageSize, totalElements);

    return {
      totalElements,
      totalPages,
      startIndex: totalElements > 0 ? startIndex : 0,
      endIndex
    };
  }, [filteredEmployees.length, currentPage, pageSize]);

  // Set admin status and clear search if not admin
  useEffect(() => {
    dispatch(setIsAdmin(isAdmin));
    if (!isAdmin) {
      dispatch(setFilters({ searchQuery: '' }));
    }
  }, [dispatch, isAdmin]);

  // Initial data fetch ONLY - no subsequent API calls for filtering
  useEffect(() => {
    if (allEmployees.length === 0 && !loading) {
      dispatch(fetchAbsentEmployees({
        page: 0,
        size: 1000, // Load more data initially for better client-side filtering
        search: '',
        startDate: '',
        endDate: '',
        resolutionFilter: 'All',
        isAdmin: isAdminFromState
      }));
    }
  }, [dispatch, isAdminFromState, allEmployees.length, loading]);

  // Handle page change - CLIENT SIDE ONLY
  const handlePageChange = (event, newPage) => {
    dispatch(setCurrentPage(newPage - 1));
  };

  // Handle page size change - CLIENT SIDE ONLY
  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    dispatch(setPageSize(newSize));
    dispatch(setCurrentPage(0)); // Reset to first page
  };

  // Handle search query change - CLIENT SIDE ONLY
  const handleSearchQueryChange = (event) => {
    dispatch(setFilters({ searchQuery: event.target.value }));
    dispatch(setCurrentPage(0)); // Reset to first page on search
  };

  // Handle search - CLIENT SIDE ONLY (no API call)
  const handleSearch = () => {
    dispatch(setCurrentPage(0)); // Reset to first page
    // No API call - filtering happens via useMemo
  };

  // Handle search on Enter key
  const handleSearchKeyPress = (event) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  // Handle filter changes - CLIENT SIDE ONLY
  const handleStartDateChange = (event) => {
    dispatch(setFilters({ startDate: event.target.value }));
    dispatch(setCurrentPage(0));
  };

  const handleEndDateChange = (event) => {
    dispatch(setFilters({ endDate: event.target.value }));
    dispatch(setCurrentPage(0));
  };

  const handleResolutionFilterChange = (event) => {
    dispatch(setFilters({ resolutionFilter: event.target.value }));
    dispatch(setCurrentPage(0));
  };

  // Apply filters - CLIENT SIDE ONLY
  const handleFiltersApply = () => {
    dispatch(setCurrentPage(0));
    // No API call - filtering happens automatically via useMemo
  };

  // Clear all filters - CLIENT SIDE ONLY
  const handleClearFilters = () => {
    dispatch(clearFilters());
    dispatch(setCurrentPage(0));
    // No API call - filtering resets automatically via useMemo
  };

  // Handle applying for leave
  const handleApplyLeave = () => {
    router.push('/apply-leave');
  };

  // Handle resolving an absence (only for admin)
  const handleResolveAbsence = async (id) => {
    try {
      dispatch(resolveAbsenceOptimistic(id));
      const result = await dispatch(resolveAbsence(id));
      if (resolveAbsence.fulfilled.match(result)) {
        console.log(`Resolved absence for employee ID: ${id}`);
      }
    } catch (err) {
      console.error('Error resolving absence:', err);
    }
  };

  // Handle retry - ONLY for initial data load
  const handleRetry = () => {
    dispatch(clearError());
    dispatch(fetchAbsentEmployees({
      page: 0,
      size: 1000,
      search: '',
      startDate: '',
      endDate: '',
      resolutionFilter: 'All',
      isAdmin: isAdminFromState
    }));
  };

  // Handle refresh - ONLY refetch from API
  const handleRefresh = () => {
    dispatch(fetchAbsentEmployees({
      page: 0,
      size: 1000,
      search: '',
      startDate: '',
      endDate: '',
      resolutionFilter: 'All',
      isAdmin: isAdminFromState
    }));
  };

  // Show loading spinner for first load
  if (isFirstLoad) {
    return (
        <Container maxWidth="lg">
          <CssBaseline />
          <Box sx={{ mt: 4, mb: 4, display: 'flex', justifyContent: 'center' }}>
            <CircularProgress />
          </Box>
        </Container>
    );
  }

  // Show error message for first load
  if (error && !hasEmployees) {
    return (
        <Container maxWidth="lg">
          <CssBaseline />
          <Box sx={{ mt: 4, mb: 4 }}>
            <Alert severity="error" sx={{ mb: 2 }}>
              Error: {error}
            </Alert>
            <Button variant="contained" onClick={handleRetry}>
              Retry
            </Button>
          </Box>
        </Container>
    );
  }

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h4" gutterBottom>
              {isAdminFromState ? 'All Employees - Absent History' : 'My Absent History'}
            </Typography>
            {isAdminFromState && (
                <Chip label="Admin View" color="primary" variant="outlined" />
            )}
          </Box>

          {/* Search Bar - Only show for admin */}
          {isAdminFromState && (
              <Box sx={{ mb: 2 }}>
                <TextField
                    fullWidth
                    placeholder="Search by Employee ID, Issue Description..."
                    variant="outlined"
                    value={searchQuery}
                    onChange={handleSearchQueryChange}
                    onKeyPress={handleSearchKeyPress}
                    InputProps={{
                      startAdornment: (
                          <InputAdornment position="start">
                            <SearchIcon />
                          </InputAdornment>
                      ),
                      endAdornment: (
                          <InputAdornment position="end">
                            <Button onClick={handleSearch} variant="contained" size="small">
                              Search
                            </Button>
                          </InputAdornment>
                      ),
                    }}
                />
              </Box>
          )}

          {/* Error Alert */}
          {error && hasEmployees && (
              <Alert severity="error" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
                Error: {error}
              </Alert>
          )}

          {/* Filters */}
          <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: 'wrap' }}>
            <TextField
                label="Start Date"
                type="date"
                variant="outlined"
                value={startDate}
                onChange={handleStartDateChange}
                InputLabelProps={{ shrink: true }}
                sx={{ minWidth: 150 }}
            />
            <TextField
                label="End Date"
                type="date"
                variant="outlined"
                value={endDate}
                onChange={handleEndDateChange}
                InputLabelProps={{ shrink: true }}
                sx={{ minWidth: 150 }}
            />
            <FormControl variant="outlined" sx={{ minWidth: 150 }}>
              <InputLabel>Resolution</InputLabel>
              <Select
                  value={resolutionFilter}
                  onChange={handleResolutionFilterChange}
                  label="Resolution"
              >
                <MenuItem value="All">All</MenuItem>
                <MenuItem value="Resolved">Resolved</MenuItem>
                <MenuItem value="Unresolved">Unresolved</MenuItem>
              </Select>
            </FormControl>
            <FormControl variant="outlined" sx={{ minWidth: 100 }}>
              <InputLabel>Per Page</InputLabel>
              <Select
                  value={pageSize}
                  onChange={handlePageSizeChange}
                  label="Per Page"
              >
                <MenuItem value={5}>5</MenuItem>
                <MenuItem value={10}>10</MenuItem>
                <MenuItem value={25}>25</MenuItem>
                <MenuItem value={50}>50</MenuItem>
                <MenuItem value={100}>100</MenuItem>
              </Select>
            </FormControl>
            <Button variant="contained" onClick={handleFiltersApply}>
              Apply Filters
            </Button>
            <Button variant="outlined" onClick={handleClearFilters}>
              Clear All
            </Button>
            <Button variant="outlined" onClick={handleRefresh}>
              Refresh Data
            </Button>
          </Box>

          {/* Results Info - Now shows filtered results */}
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Showing {paginatedEmployees.length} of {paginationInfo.totalElements} filtered results
              {paginationInfo.totalPages > 1 && ` (Page ${currentPage + 1} of ${paginationInfo.totalPages})`}
              {filteredEmployees.length !== allEmployees.length &&
                  ` (${allEmployees.length} total records)`
              }
            </Typography>
          </Box>

          {/* Table - Now shows paginated + filtered data */}
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Employee ID</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Issue Description</TableCell>
                  <TableCell>Due Date</TableCell>
                  <TableCell>Status</TableCell>
                  {!isAdminFromState && <TableCell>Action</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {paginatedEmployees.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        {filteredEmployees.length === 0 && allEmployees.length > 0
                            ? "No records match your search/filter criteria"
                            : "No records found"
                        }
                      </TableCell>
                    </TableRow>
                ) : (
                    paginatedEmployees.map((employee) => (
                        <TableRow key={employee.id}>
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              {!employee.isResolved && (
                                  <Badge color="error" variant="dot" />
                              )}
                              {employee.employeeName}
                            </Box>
                          </TableCell>
                          <TableCell>{employee.date}</TableCell>
                          <TableCell sx={{ maxWidth: 300, wordWrap: 'break-word' }}>
                            {employee.reason}
                          </TableCell>
                          <TableCell>
                            {employee.dueDateForUA
                                ? new Date(employee.dueDateForUA).toLocaleDateString()
                                : 'N/A'
                            }
                          </TableCell>
                          <TableCell>
                            <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
                              {employee.absent && (
                                  <Chip label="Absent" color="error" size="small" />
                              )}
                              {employee.noPay && (
                                  <Chip label="No Pay" color="warning" size="small" />
                              )}
                              {employee.issues && (
                                  <Chip label="Issues" color="error" size="small" />
                              )}
                              {employee.isResolved && (
                                  <Chip label="Resolved" color="success" size="small" />
                              )}
                            </Stack>
                          </TableCell>
                          {!isAdminFromState && (
                              <TableCell>
                                {!employee.isResolved && (
                                    <Button
                                        variant="contained"
                                        color="secondary"
                                        size="small"
                                        onClick={handleApplyLeave}
                                    >
                                      Apply Leave
                                    </Button>
                                )}
                              </TableCell>
                          )}
                        </TableRow>
                    ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Pagination - Now based on filtered data */}
          {paginationInfo.totalElements > 0 && (
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 3, p: 2, bgcolor: 'background.paper', borderRadius: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  Showing {paginationInfo.startIndex} to {paginationInfo.endIndex} of {paginationInfo.totalElements} entries
                </Typography>
                <Pagination
                    count={Math.max(1, paginationInfo.totalPages)}
                    page={currentPage + 1}
                    onChange={handlePageChange}
                    color="primary"
                    size="large"
                    showFirstButton
                    showLastButton
                    siblingCount={2}
                    boundaryCount={1}
                />
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    Rows per page:
                  </Typography>
                  <Select
                      value={pageSize}
                      onChange={handlePageSizeChange}
                      variant="outlined"
                      size="small"
                      sx={{ minWidth: 70 }}
                  >
                    <MenuItem value={5}>5</MenuItem>
                    <MenuItem value={10}>10</MenuItem>
                    <MenuItem value={25}>25</MenuItem>
                    <MenuItem value={50}>50</MenuItem>
                    <MenuItem value={100}>100</MenuItem>
                  </Select>
                </Box>
              </Box>
          )}

          {/* Loading overlay for API refresh only */}
          {isSubsequentLoad && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                <CircularProgress size={24} />
              </Box>
          )}
        </Box>
      </Container>
  );
};

export default AbsentEmployees;