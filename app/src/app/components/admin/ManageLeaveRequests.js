"use client";

import React, { useEffect } from "react";
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchLeaveRequests,
  processLeaveRequest,
  processBulkLeaveRequests,
  selectLeaveRequest,
  selectAllLeaveRequests,
  clearNotification,
  setPageSize
} from '../../../../lib/redux/redux-lms/leave/admin/leaveSlice';
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
  Button,
  Checkbox,
  Pagination,
  Stack,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Snackbar,
  Alert
} from "@mui/material";
import { format } from "date-fns";

const ManageLeaveRequests = () => {
  const dispatch = useDispatch();

  // Get state from Redux store
  const leaveRequests = useSelector(state => state.leave.requests);
  const selected = useSelector(state => state.leave.selected);
  const pagination = useSelector(state => state.leave.pagination);
  const loading = useSelector(state => state.leave.loading);
  const error = useSelector(state => state.leave.error);
  const notification = useSelector(state => state.leave.notification);

  // Fetch leave requests when component mounts
  useEffect(() => {
    dispatch(fetchLeaveRequests({
      page: pagination.currentPage,
      size: pagination.pageSize
    }));
  }, [dispatch, pagination.currentPage, pagination.pageSize]);

  // Handle page change
  const handlePageChange = (event, value) => {
    // API pages are 0-indexed, but Pagination component is 1-indexed
    dispatch(fetchLeaveRequests({
      page: value - 1,
      size: pagination.pageSize
    }));
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    dispatch(setPageSize(newSize));
    dispatch(fetchLeaveRequests({
      page: 0,
      size: newSize
    }));
  };

  // Handle individual row selection
  const handleSelect = (id) => {
    dispatch(selectLeaveRequest(id));
  };

  // Handle "Select All" functionality
  const handleSelectAll = () => {
    dispatch(selectAllLeaveRequests());
  };

  // Handle approve for a single request
  const handleApprove = (publicId) => {
    dispatch(processLeaveRequest({
      publicId,
      approved: true
    }));
  };

  // Handle reject for a single request
  const handleReject = (publicId) => {
    dispatch(processLeaveRequest({
      publicId,
      approved: false
    }));
  };

  // Handle bulk approve
  const handleBulkApprove = () => {
    dispatch(processBulkLeaveRequests({
      leaveIds: selected,
      approved: true
    }));
  };

  // Handle bulk reject
  const handleBulkReject = () => {
    dispatch(processBulkLeaveRequests({
      leaveIds: selected,
      approved: false
    }));
  };

  // Handle notification close
  const handleNotificationClose = () => {
    dispatch(clearNotification());
  };

  // Format date to readable format
  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "MMM dd, yyyy");
    } catch (e) {
      return dateString;
    }
  };

  // Get leave type display text
  const getLeaveTypeText = (request) => {
    if (!request) return "N/A";

    if (request.leaveType && request.leaveType.name) {
      return request.leaveType.name;
    } else if (request.leaveCategory) {
      return request.leaveCategory;
    } else if (request.short_Leave) {
      return "Short Leave";
    } else {
      return "Regular";
    }
  };

  // Get status chip for leave request
  const getStatusChip = (leaveRequest) => {
    if (!leaveRequest) return <Chip label="Unknown" color="default" size="small" />;

    if (leaveRequest.cancelled) {
      return <Chip label="Canceled" color="error" size="small" />;
    } else if (leaveRequest.reject) {
      return <Chip label="Reject" color="error" size="small" />;
    } else if (leaveRequest.accepted) {
      return <Chip label="Approved" color="success" size="small" />;
    } else if (leaveRequest.pending) {
      return <Chip label="Pending" color="warning" size="small" />;
    } else {
      return <Chip label="Submitted" color="default" size="small" />;
    }
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Leave Requests
          </Typography>

          {/* Error message */}
          {error && (
              <Box sx={{ mb: 2, p: 2, bgcolor: "error.light", borderRadius: 1 }}>
                <Typography color="error">Error: {error}</Typography>
              </Box>
          )}

          {/* Notification Snackbar */}
          <Snackbar
              open={notification.open}
              autoHideDuration={6000}
              onClose={handleNotificationClose}
              anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
          >
            <Alert
                onClose={handleNotificationClose}
                severity={notification.severity}
                sx={{ width: '100%' }}
            >
              {notification.message}
            </Alert>
          </Snackbar>

          {/* Bulk Actions */}
          <Box sx={{ mb: 2, display: "flex", justifyContent: "space-between" }}>
            <Box>
              <Button
                  variant="contained"
                  color="primary"
                  onClick={handleBulkApprove}
                  disabled={selected.length === 0 || loading}
                  sx={{ mr: 1 }}
              >
                Approve Selected
              </Button>
              <Button
                  variant="contained"
                  color="secondary"
                  onClick={handleBulkReject}
                  disabled={selected.length === 0 || loading}
                  sx={{ mr: 1 }}
              >
                Reject Selected
              </Button>
            </Box>
            <FormControl sx={{ minWidth: 120 }}>
              <InputLabel id="rows-per-page-label">Rows</InputLabel>
              <Select
                  labelId="rows-per-page-label"
                  id="rows-per-page"
                  value={pagination.pageSize}
                  label="Rows"
                  onChange={handlePageSizeChange}
                  disabled={loading}
              >
                <MenuItem value={5}>5</MenuItem>
                <MenuItem value={10}>10</MenuItem>
                <MenuItem value={25}>25</MenuItem>
                <MenuItem value={50}>50</MenuItem>
              </Select>
            </FormControl>
          </Box>

          {/* Loading indicator */}
          {loading ? (
              <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                <CircularProgress />
              </Box>
          ) : (
              <>
                {/* Table */}
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead sx={{ bgcolor: "primary.light" }}>
                      <TableRow>
                        <TableCell padding="checkbox">
                          <Checkbox
                              indeterminate={
                                  selected.length > 0 && selected.length < leaveRequests.filter(req => req && req.publicId).length
                              }
                              checked={selected.length === leaveRequests.filter(req => req && req.publicId).length && leaveRequests.length > 0}
                              onChange={handleSelectAll}
                          />
                        </TableCell>
                        <TableCell>Employee ID</TableCell>
                        <TableCell>Requested On</TableCell>
                        <TableCell>From Date</TableCell>
                        <TableCell>To Date</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>Days</TableCell>
                        <TableCell>Description</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {leaveRequests.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={10} align="center">
                              No leave requests found
                            </TableCell>
                          </TableRow>
                      ) : (
                          leaveRequests.map((request, index) => {
                            // Skip rendering if request is null or undefined
                            if (!request || !request.publicId) {
                              return null;
                            }

                            return (
                                <TableRow key={request.publicId || index}>
                                  <TableCell padding="checkbox">
                                    <Checkbox
                                        checked={selected.includes(request.publicId)}
                                        onChange={() => handleSelect(request.publicId)}
                                    />
                                  </TableCell>
                                  <TableCell>{request.employeeID ? request.employeeID.substring(0, 8) + '...' : 'N/A'}</TableCell>
                                  <TableCell>{formatDate(request.submitDate)}</TableCell>
                                  <TableCell>{formatDate(request.fromDate)}</TableCell>
                                  <TableCell>{formatDate(request.toDate)}</TableCell>
                                  <TableCell>
                                    {getLeaveTypeText(request)}
                                    {request.halfDay && " (Half Day)"}
                                    {request.isNoPay === 1 && " (No Pay)"}
                                  </TableCell>
                                  <TableCell>{request.numOfDays}</TableCell>
                                  <TableCell sx={{ maxWidth: "200px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                                    {request.description || "N/A"}
                                  </TableCell>
                                  <TableCell>{getStatusChip(request)}</TableCell>
                                  <TableCell>
                                    <Button
                                        variant="contained"
                                        color="primary"
                                        size="small"
                                        sx={{ mr: 1, mb: 1 }}
                                        disabled={request.accepted || request.reject || loading}
                                        onClick={() => handleApprove(request.publicId)}
                                    >
                                      Approve
                                    </Button>
                                    <Button
                                        variant="outlined"
                                        color="error"
                                        size="small"
                                        disabled={request.accepted || request.reject || loading}
                                        onClick={() => handleReject(request.publicId)}
                                    >
                                      Reject
                                    </Button>
                                  </TableCell>
                                </TableRow>
                            );
                          })
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>

                {/* Pagination */}
                {pagination.totalPages > 0 && (
                    <Stack spacing={2} sx={{ mt: 2, display: "flex", alignItems: "center" }}>
                      <Pagination
                          count={pagination.totalPages}
                          page={pagination.currentPage + 1}
                          onChange={handlePageChange}
                          color="primary"
                          showFirstButton
                          showLastButton
                          disabled={loading}
                      />
                      <Typography variant="body2" color="textSecondary">
                        Showing {leaveRequests.filter(req => req && req.publicId).length} of {pagination.totalElements} results
                      </Typography>
                    </Stack>
                )}
              </>
          )}
        </Box>
      </Container>
  );
};

export default ManageLeaveRequests;