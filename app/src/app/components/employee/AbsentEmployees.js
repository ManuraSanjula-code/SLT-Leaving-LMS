"use client";

import React, { useEffect, useMemo } from "react";
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

import {
  fetchAbsentEmployees,
  setFilters,
  clearFilters,
  setPageSize,
  setCurrentPage,
  clearError
} from '../../../../lib/redux/redux-lms/absent/absentEmployeesSlice';

const AbsentEmployees = ({ isAdmin: propIsAdmin = true }) => {
  const dispatch = useDispatch();
  const router = useRouter();

  const isAdmin = propIsAdmin !== undefined ? propIsAdmin :
      (sessionStorage.getItem('userRole') === 'ADMIN' || sessionStorage.getItem('isAdmin') === 'true');

  const {
    employees: allEmployees,
    loading,
    error,
    totalPages,
    totalElements,
    currentPage,
    pageSize,
    filters,
    hasDataBeenFetched
  } = useSelector(state => state.absentEmployees);

  const { startDate, endDate, resolutionFilter, searchQuery } = filters;

  const filteredEmployees = useMemo(() => {
    let filtered = [...allEmployees];

    if (isAdmin && searchQuery.trim()) {
      const query = searchQuery.toLowerCase().trim();
      filtered = filtered.filter(employee =>
          employee.employeeName?.toLowerCase().includes(query) ||
          employee.reason?.toLowerCase().includes(query) ||
          employee.publicId?.toLowerCase().includes(query) ||
          employee.employeeId?.toLowerCase().includes(query)
      );
    }

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

    if (resolutionFilter !== 'All') {
      const isResolved = resolutionFilter === 'Resolved';
      filtered = filtered.filter(employee => employee.isResolved === isResolved);
    }

    return filtered;
  }, [allEmployees, searchQuery, startDate, endDate, resolutionFilter, isAdmin]);

  const paginatedEmployees = useMemo(() => {
    const startIndex = currentPage * pageSize;
    const endIndex = startIndex + pageSize;
    return filteredEmployees.slice(startIndex, endIndex);
  }, [filteredEmployees, currentPage, pageSize]);

  const paginationInfo = useMemo(() => {
    const totalFilteredElements = filteredEmployees.length;
    const totalFilteredPages = Math.ceil(totalFilteredElements / pageSize);
    const startIndex = totalFilteredElements > 0 ? (currentPage * pageSize) + 1 : 0;
    const endIndex = Math.min((currentPage + 1) * pageSize, totalFilteredElements);

    return {
      totalElements: totalFilteredElements,
      totalPages: totalFilteredPages,
      startIndex,
      endIndex
    };
  }, [filteredEmployees.length, currentPage, pageSize]);

  useEffect(() => {
    if (!isAdmin) {
      dispatch(setFilters({ searchQuery: '' }));
    }
  }, [dispatch, isAdmin]);

  useEffect(() => {
    dispatch(fetchAbsentEmployees({
      page: 0,
      size: 1000,
      search: '',
      startDate: '',
      endDate: '',
      resolutionFilter: 'All',
      isAdmin: isAdmin
    }));
  }, [dispatch, isAdmin]);

  const handlePageChange = (event, newPage) => {
    dispatch(setCurrentPage(newPage - 1));
  };

  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    dispatch(setPageSize(newSize));
  };

  const handleSearchQueryChange = (event) => {
    const newQuery = event.target.value;
    dispatch(setFilters({ searchQuery: newQuery }));
    dispatch(setCurrentPage(0));
  };

  const handleSearch = () => {
    dispatch(setCurrentPage(0));
  };

  const handleSearchKeyPress = (event) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  const handleStartDateChange = (event) => {
    const newDate = event.target.value;
    dispatch(setFilters({ startDate: newDate }));
    dispatch(setCurrentPage(0));
  };

  const handleEndDateChange = (event) => {
    const newDate = event.target.value;
    dispatch(setFilters({ endDate: newDate }));
    dispatch(setCurrentPage(0));
  };

  const handleResolutionFilterChange = (event) => {
    const newFilter = event.target.value;
    dispatch(setFilters({ resolutionFilter: newFilter }));
    dispatch(setCurrentPage(0));
  };

  const handleClearFilters = () => {
    dispatch(clearFilters());
    dispatch(setCurrentPage(0));
  };

  const handleRefresh = () => {
    dispatch(fetchAbsentEmployees({
      page: 0,
      size: 1000,
      search: '',
      startDate: '',
      endDate: '',
      resolutionFilter: 'All',
      isAdmin: isAdmin
    }));
  };

  const handleApplyLeave = () => {
    router.push('/apply-leave');
  };

  const handleRetry = () => {
    dispatch(clearError());
    handleRefresh();
  };

  const formatAttendanceType = (attendanceType) => {
    if (!attendanceType) return '';
    return attendanceType.replace('_', ' ').toLowerCase()
        .replace(/\b\w/g, l => l.toUpperCase());
  };

  const formatResolveType = (resolveType) => {
    if (!resolveType) return '';
    return resolveType.replace('_', ' ').toLowerCase()
        .replace(/\b\w/g, l => l.toUpperCase());
  };

  if (loading && !hasDataBeenFetched) {
    return (
        <Container maxWidth="lg">
          <CssBaseline />
          <Box sx={{ mt: 4, mb: 4, display: 'flex', justifyContent: 'center' }}>
            <CircularProgress />
            <Typography sx={{ ml: 2 }}>Loading absent employees...</Typography>
          </Box>
        </Container>
    );
  }

  if (error && !hasDataBeenFetched) {
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
              {isAdmin ? 'All Employees - Absent History' : 'My Absent History'}
            </Typography>
          </Box>

          {isAdmin && (
              <Box sx={{ mb: 2 }}>
                <TextField
                    fullWidth
                    placeholder="Search by User ID, Employee ID, Issue Description, Public ID..."
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

          {error && hasDataBeenFetched && (
              <Alert severity="warning" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
                Warning: {error}
              </Alert>
          )}

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
            <Button variant="outlined" onClick={handleClearFilters}>
              Clear Filters
            </Button>
            <Button variant="outlined" onClick={handleRefresh}>
              Refresh Data
            </Button>
          </Box>

          <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Showing {paginatedEmployees.length} of {paginationInfo.totalElements} filtered results
              {paginationInfo.totalPages > 1 && ` (Page ${currentPage + 1} of ${paginationInfo.totalPages})`}
              {filteredEmployees.length !== allEmployees.length &&
                  ` (${allEmployees.length} total records)`
              }
            </Typography>
            {loading && hasDataBeenFetched && (
                <CircularProgress size={20} />
            )}
          </Box>

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>User ID</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Issue Description</TableCell>
                  <TableCell>Due Date</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Resolution</TableCell>
                  {!isAdmin && <TableCell>Action</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {paginatedEmployees.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                        <Typography variant="body1" color="text.secondary">
                          {filteredEmployees.length === 0 && allEmployees.length > 0
                              ? "No records match your search/filter criteria"
                              : allEmployees.length === 0
                                  ? "No absent employee records found"
                                  : "No records to display"
                          }
                        </Typography>
                        {filteredEmployees.length === 0 && allEmployees.length > 0 && (
                            <Button variant="outlined" onClick={handleClearFilters} sx={{ mt: 2 }}>
                              Clear Filters
                            </Button>
                        )}
                      </TableCell>
                    </TableRow>
                ) : (
                    paginatedEmployees.map((employee) => (
                        <TableRow key={employee.id}>
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              {!employee.isResolved && employee.hasIssues && (
                                  <Badge color="error" variant="dot" />
                              )}
                              <Box>
                                <Typography variant="body2" fontWeight="medium">
                                  {employee.employeeName}
                                </Typography>
                                <Typography variant="caption" color="text.secondary">
                                  {employee.publicId}
                                </Typography>
                              </Box>
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {employee.date}
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ maxWidth: 300 }}>
                            <Typography variant="body2" sx={{ wordWrap: 'break-word' }}>
                              {employee.reason}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {employee.dueDateForUA
                                  ? new Date(employee.dueDateForUA).toLocaleDateString()
                                  : 'N/A'
                              }
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
                              {employee.isAbsent && (
                                  <Chip label="Absent" color="error" size="small" />
                              )}
                              {employee.isNoPay && (
                                  <Chip label="No Pay" color="warning" size="small" />
                              )}
                              {employee.hasIssues && (
                                  <Chip label="Has Issues" color="error" size="small" />
                              )}
                              {employee.isLate && (
                                  <Chip label="Late" color="warning" size="small" />
                              )}
                              {employee.isResolved && (
                                  <Chip label="Resolved" color="success" size="small" />
                              )}
                              {employee.leaveStatus === 'LEAVE_REQUESTED' && (
                                  <Chip label="LEAVE_REQUESTED" color="error" size="small" />
                              )}
                              {employee.leaveStatus === 'FULL_LEAVE' && (
                                  <Chip label="FULL_LEAVE" color="error" size="small" />
                              )}
                            </Stack>
                          </TableCell>
                          <TableCell>
                            {employee.resolve || employee.leaveStatus === 'FULL_LEAVE'? (
                                <Chip
                                    label={employee.leaveStatus === 'FULL_LEAVE' ? 'FULL_LEAVE' : formatResolveType(employee.resolve)}
                                    color="info"
                                    size="small"
                                />
                            ) : (
                                <Chip label="Pending" color="default" size="small" />
                            )}
                          </TableCell>
                          {!isAdmin && (
                              <TableCell>
                                {!employee.isResolved && employee.hasIssues && (
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

          {paginationInfo.totalElements > 0 && (
              <Box sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                mt: 3,
                p: 2,
                bgcolor: 'background.paper',
                borderRadius: 1,
                border: 1,
                borderColor: 'divider'
              }}>
                <Typography variant="body2" color="text.secondary">
                  Showing {paginationInfo.startIndex} to {paginationInfo.endIndex} of {paginationInfo.totalElements} entries
                </Typography>

                {paginationInfo.totalPages > 1 && (
                    <Pagination
                        count={paginationInfo.totalPages}
                        page={currentPage + 1}
                        onChange={handlePageChange}
                        color="primary"
                        size="large"
                        showFirstButton
                        showLastButton
                        siblingCount={1}
                        boundaryCount={1}
                    />
                )}

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
        </Box>
      </Container>
  );
};

export default AbsentEmployees;