"use client";

import React, { useEffect, useState } from "react";
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
  Alert,
  Tooltip,
  Collapse,
  IconButton
} from "@mui/material";
import { format } from "date-fns";
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

const ManageLeaveRequests = () => {
  const dispatch = useDispatch();
  const [expandedRequest, setExpandedRequest] = useState(null);

  const leaveRequests = useSelector(state => state.leave.requests);
  const selected = useSelector(state => state.leave.selected);
  const pagination = useSelector(state => state.leave.pagination);
  const loading = useSelector(state => state.leave.loading);
  const error = useSelector(state => state.leave.error);
  const notification = useSelector(state => state.leave.notification);

  useEffect(() => {
    dispatch(fetchLeaveRequests({
      page: pagination.currentPage,
      size: pagination.pageSize
    }));
  }, [dispatch, pagination.currentPage, pagination.pageSize]);

  const isNonSelectable = (request) => {
    return request.accepted ||
        request.expired ||
        request.isCanceled ||
        request.canceled ||
        request.reject ||
        request.rejected;
  };

  const hasNonSelectableRequests = leaveRequests.some(request =>
      request && request.publicId && isNonSelectable(request)
  );

  const selectableRequests = leaveRequests.filter(request =>
      request && request.publicId && !isNonSelectable(request)
  );

  const handlePageChange = (event, value) => {
    dispatch(fetchLeaveRequests({
      page: value - 1,
      size: pagination.pageSize
    }));
  };

  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    dispatch(setPageSize(newSize));
    dispatch(fetchLeaveRequests({
      page: 0,
      size: newSize
    }));
  };

  const handleSelect = (id) => {
    const request = leaveRequests.find(req => req.publicId === id);
    if (request && isNonSelectable(request)) return;

    dispatch(selectLeaveRequest(id));
  };

  const handleSelectAll = () => {
    dispatch(selectAllLeaveRequests());
  };

  const handleApprove = (publicId) => {
    dispatch(processLeaveRequest({
      publicId,
      approved: true
    }));
  };

  const handleReject = (publicId) => {
    dispatch(processLeaveRequest({
      publicId,
      approved: false
    }));
  };

  const handleBulkApprove = () => {
    dispatch(processBulkLeaveRequests({
      leaveIds: selected,
      approved: true
    }));
  };

  const handleBulkReject = () => {
    dispatch(processBulkLeaveRequests({
      leaveIds: selected,
      approved: false
    }));
  };

  const handleNotificationClose = () => {
    dispatch(clearNotification());
  };

  const toggleExpandRequest = (publicId) => {
    setExpandedRequest(expandedRequest === publicId ? null : publicId);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "MMM dd, yyyy HH:mm");
    } catch (e) {
      return dateString;
    }
  };

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

  const getStatusChip = (leaveRequest) => {
    if (!leaveRequest) return <Chip label="Unknown" color="default" size="small" />;

    if (leaveRequest.cancelled || leaveRequest.canceled) {
      return <Chip label="Canceled" color="error" size="small" />;
    }

    if (leaveRequest.reject || leaveRequest.rejected) {
      const rejectingAdmin = leaveRequest.adminsTra?.find(a => a.accepted === false);
      const tooltipTitle = rejectingAdmin
          ? `Rejected by ${rejectingAdmin.firstName} ${rejectingAdmin.lastName}`
          : 'Rejected';

      return (
          <Tooltip title={tooltipTitle}>
            <Chip label="Rejected" color="error" size="small" />
          </Tooltip>
      );
    }

    if (leaveRequest.accepted) {
      const allApproved = leaveRequest.adminsTra?.every(a => a.accepted);
      const someApproved = leaveRequest.adminsTra?.some(a => a.accepted);

      if (allApproved) {
        return <Chip label="Approved" color="success" size="small" />;
      } else if (someApproved) {
        return <Chip label="Partially Approved" color="info" size="small" />;
      }
    }

    if (leaveRequest.pending) {
      const pendingAdmins = leaveRequest.adminsTra?.filter(a => !a.accepted && !a.rejected);
      const tooltipTitle = pendingAdmins?.length
          ? `Pending approval from ${pendingAdmins.map(a => `${a.firstName} ${a.lastName}`).join(', ')}`
          : 'Pending approval';

      return (
          <Tooltip title={tooltipTitle}>
            <Chip label="Pending" color="warning" size="small" />
          </Tooltip>
      );
    }

    return <Chip label="Submitted" color="default" size="small" />;
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Leave Requests
          </Typography>

          {error && (
              <Box sx={{ mb: 2, p: 2, bgcolor: "error.light", borderRadius: 1 }}>
                <Typography color="error">Error: {error}</Typography>
              </Box>
          )}

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

          <Box sx={{ mb: 2, display: "flex", justifyContent: "space-between" }}>
            <Box>
              <Button
                  variant="contained"
                  color="primary"
                  onClick={handleBulkApprove}
                  disabled={
                      selected.length === 0 ||
                      loading ||
                      hasNonSelectableRequests ||
                      selected.some(id => {
                        const request = leaveRequests.find(req => req.publicId === id);
                        return request && isNonSelectable(request);
                      })
                  }
                  sx={{ mr: 1 }}
              >
                Approve Selected ({selected.length})
                {hasNonSelectableRequests && " - Some items cannot be processed"}
              </Button>
              <Button
                  variant="contained"
                  color="secondary"
                  onClick={handleBulkReject}
                  disabled={
                      selected.length === 0 ||
                      loading ||
                      hasNonSelectableRequests ||
                      selected.some(id => {
                        const request = leaveRequests.find(req => req.publicId === id);
                        return request && isNonSelectable(request);
                      })
                  }
                  sx={{ mr: 1 }}
              >
                Reject Selected ({selected.length})
                {hasNonSelectableRequests && " - Some items cannot be processed"}
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

          {loading ? (
              <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                <CircularProgress />
              </Box>
          ) : (
              <>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead sx={{ bgcolor: "primary.light" }}>
                      <TableRow>
                        <TableCell padding="checkbox">
                          <Checkbox
                              indeterminate={
                                  selected.length > 0 && selected.length < selectableRequests.length
                              }
                              checked={
                                  selected.length === selectableRequests.length &&
                                  selectableRequests.length > 0
                              }
                              onChange={handleSelectAll}
                              disabled={hasNonSelectableRequests}
                          />
                        </TableCell>
                        <TableCell>Employee ID</TableCell>
                        <TableCell>Requested On</TableCell>
                        <TableCell>From Date</TableCell>
                        <TableCell>To Date</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>Days</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Actions</TableCell>
                        <TableCell>Details</TableCell>
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
                            if (!request || !request.publicId) {
                              return null;
                            }

                            const isRequestNonSelectable = isNonSelectable(request);
                            const isExpanded = expandedRequest === request.publicId;

                            return (
                                <React.Fragment key={request.publicId || index}>
                                  <TableRow>
                                    <TableCell padding="checkbox">
                                      <Checkbox
                                          checked={selected.includes(request.publicId)}
                                          onChange={() => handleSelect(request.publicId)}
                                          disabled={isRequestNonSelectable}
                                      />
                                    </TableCell>
                                    <TableCell>{request.employeeID || 'N/A'}</TableCell>
                                    <TableCell>{formatDate(request.submitDate)}</TableCell>
                                    <TableCell>{formatDate(request.fromDate)}</TableCell>
                                    <TableCell>{formatDate(request.toDate)}</TableCell>
                                    <TableCell>
                                      {getLeaveTypeText(request)}
                                      {request.halfDay && " (Half Day)"}
                                      {request.isNoPay === 1 && " (No Pay)"}
                                    </TableCell>
                                    <TableCell>{request.numOfDays}</TableCell>
                                    <TableCell>{getStatusChip(request)}</TableCell>
                                    <TableCell>
                                      {request.adminsTra?.some(a => a.accepted) ? (
                                          <Typography variant="caption" color="textSecondary">
                                            Partially approved
                                          </Typography>
                                      ) : (
                                          <>
                                            <Button
                                                variant="contained"
                                                color="primary"
                                                size="small"
                                                sx={{ mr: 1, mb: 1 }}
                                                disabled={isRequestNonSelectable || loading}
                                                onClick={() => handleApprove(request.publicId)}
                                            >
                                              Approve
                                            </Button>
                                            <Button
                                                variant="outlined"
                                                color="error"
                                                size="small"
                                                disabled={isRequestNonSelectable || loading}
                                                onClick={() => handleReject(request.publicId)}
                                            >
                                              Reject
                                            </Button>
                                          </>
                                      )}
                                    </TableCell>
                                    <TableCell>
                                      <IconButton
                                          aria-label="expand row"
                                          size="small"
                                          onClick={() => toggleExpandRequest(request.publicId)}
                                      >
                                        {isExpanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                                      </IconButton>
                                    </TableCell>
                                  </TableRow>
                                  <TableRow>
                                    <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={10}>
                                      <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                                        <Box sx={{ margin: 1 }}>
                                          <Typography variant="subtitle1" gutterBottom>
                                            Leave Request Details
                                          </Typography>
                                          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 2 }}>
                                            <div>
                                              <Typography variant="body2">
                                                <strong>Submitted:</strong> {formatDate(request.submitDate)}
                                              </Typography>
                                              <Typography variant="body2">
                                                <strong>From:</strong> {formatDate(request.fromDate)}
                                              </Typography>
                                              <Typography variant="body2">
                                                <strong>To:</strong> {formatDate(request.toDate)}
                                              </Typography>
                                              <Typography variant="body2">
                                                <strong>Duration:</strong> {request.numOfDays} day(s)
                                              </Typography>
                                            </div>
                                            <div>
                                              <Typography variant="body2">
                                                <strong>Type:</strong> {getLeaveTypeText(request)}
                                              </Typography>
                                              <Typography variant="body2">
                                                <strong>Behavior:</strong> {request.componentBehaviorString || 'N/A'}
                                              </Typography>
                                              <Typography variant="body2">
                                                <strong>Description:</strong> {request.description || 'N/A'}
                                              </Typography>
                                            </div>
                                          </Box>

                                          {request.adminsTra && request.adminsTra.length > 0 && (
                                              <>
                                                <Typography variant="subtitle1" sx={{ mt: 2 }}>
                                                  Approval Process
                                                </Typography>
                                                <Table size="small" sx={{ mt: 1 }}>
                                                  <TableHead>
                                                    <TableRow>
                                                      <TableCell>Administrator</TableCell>
                                                      <TableCell>Email</TableCell>
                                                      <TableCell>Status</TableCell>
                                                      <TableCell>Action Date</TableCell>
                                                    </TableRow>
                                                  </TableHead>
                                                  <TableBody>
                                                    {request.adminsTra.map((admin) => (
                                                        <TableRow key={admin.id}>
                                                          <TableCell>{admin.firstName} {admin.lastName}</TableCell>
                                                          <TableCell>{admin.email}</TableCell>
                                                          <TableCell>
                                                            {admin.accepted ? (
                                                                <Chip label="Approved" color="success" size="small" />
                                                            ) : admin.rejected ? (
                                                                <Chip label="Rejected" color="error" size="small" />
                                                            ) : (
                                                                <Chip label="Pending" color="warning" size="small" />
                                                            )}
                                                          </TableCell>
                                                          <TableCell>
                                                            {admin.approvedDate ? formatDate(admin.approvedDate) : 'N/A'}
                                                          </TableCell>
                                                        </TableRow>
                                                    ))}
                                                  </TableBody>
                                                </Table>
                                              </>
                                          )}
                                        </Box>
                                      </Collapse>
                                    </TableCell>
                                  </TableRow>
                                </React.Fragment>
                            );
                          })
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>

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