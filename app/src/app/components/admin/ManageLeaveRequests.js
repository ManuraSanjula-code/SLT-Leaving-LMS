"use client";

import React, { useEffect, useState, useCallback } from "react";
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchLeaveRequests,
  processLeaveRequest,
  processBulkLeaveRequests,
  updateLeaveRequest,
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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  AlertTitle,
  TextField,
  Grid
} from "@mui/material";
import { format } from "date-fns";
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import RefreshIcon from '@mui/icons-material/Refresh';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import EditIcon from '@mui/icons-material/Edit';

const ManageLeaveRequests = () => {
  const dispatch = useDispatch();
  const [expandedRequest, setExpandedRequest] = useState(null);
  const [retryCount, setRetryCount] = useState(0);
  const [showErrorDialog, setShowErrorDialog] = useState(false);
  const [lastError, setLastError] = useState(null);
  const [processingRequests, setProcessingRequests] = useState(new Set());
  
  // Update dialog state
  const [showUpdateDialog, setShowUpdateDialog] = useState(false);
  const [selectedRequestForUpdate, setSelectedRequestForUpdate] = useState(null);
  const [updateFormData, setUpdateFormData] = useState({
    fromDate: '',
    toDate: '',
    leaveType: '',
    description: '',
    numOfDays: '',
    componentBehavior: '',
    requestStatus: ''
  });
  const [updating, setUpdating] = useState(false);

  const leaveRequests = useSelector(state => state.leave.requests);
  const selected = useSelector(state => state.leave.selected);
  const pagination = useSelector(state => state.leave.pagination);
  const loading = useSelector(state => state.leave.loading);
  const error = useSelector(state => state.leave.error);
  const notification = useSelector(state => state.leave.notification);

  // Error boundary state
  const [hasError, setHasError] = useState(false);
  const [errorInfo, setErrorInfo] = useState(null);

  // Available leave types and component behaviors (you might want to fetch these from your backend)
  const leaveTypes = [
    { id: 1, name: "Annual Leave" },
    { id: 2, name: "Sick Leave" },
    { id: 3, name: "Casual Leave" },
    { id: 4, name: "Maternity Leave" },
    { id: 5, name: "Emergency Leave" }
  ];

  const componentBehaviors = [
    { value: "HALF_DAY", label: "Half Day" },
    { value: "FULL_DAY", label: "Full Day" },
    { value: "SHORT_LEAVE", label: "Short Leave" },
    { value: "LATE", label: "Late" },
    { value: "LATE_COVER", label: "Late Cover" },
    { value: "UNSUCCESSFUL", label: "Unsuccessful" },
    { value: "UNAUTHORIZED", label: "Unauthorized" },
    { value: "ABSENT", label: "Absent" }
  ];

  const requestStatuses = [
    { value: "DRAFT", label: "Draft" },
    { value: "SUBMITTED", label: "Submitted" },
    { value: "PENDING_APPROVAL", label: "Pending Approval" },
    { value: "APPROVED", label: "Approved" },
    { value: "REJECTED", label: "Rejected" },
    { value: "CANCELLED", label: "Cancelled" },
    { value: "EXPIRED", label: "Expired" }
  ];

  // Helper function to check if error is internal server error
  const isInternalServerError = (error) => {
    return error?.response?.status === 500 ||
        error?.status === 500 ||
        (error?.message && error.message.toLowerCase().includes('internal server error'));
  };

  // Memoized error handler - only show UI errors for server errors
  const handleError = useCallback((error, context = 'Unknown') => {
    console.error(`Error in ${context}:`, error);

    // Only show error dialog for internal server errors
    if (isInternalServerError(error)) {
      setLastError({
        message: error?.message || 'Internal server error occurred',
        context,
        timestamp: new Date().toISOString(),
        stack: error?.stack
      });
      setShowErrorDialog(true);
    }
    // For other errors, just log them silently
  }, []);

  // Safe data access with error handling
  const safeLeaveRequests = Array.isArray(leaveRequests) ? leaveRequests : [];

  // Enhanced fetch with retry logic - only show errors for server errors
  const fetchWithRetry = useCallback(async (page = pagination.currentPage, size = pagination.pageSize) => {
    try {
      await dispatch(fetchLeaveRequests({ page, size })).unwrap();
      setRetryCount(0); // Reset retry count on success
    } catch (error) {
      // Only retry and show errors for server errors
      if (isInternalServerError(error)) {
        if (retryCount < 3) {
          setTimeout(() => {
            setRetryCount(prev => prev + 1);
            fetchWithRetry(page, size);
          }, 1000 * (retryCount + 1)); // Exponential backoff
        } else {
          handleError(error, 'Fetching leave requests');
        }
      } else {
        // For non-server errors, just log and continue
        console.warn('Non-critical error in fetch:', error);
      }
    }
  }, [dispatch, pagination.currentPage, pagination.pageSize, retryCount, handleError]);

  useEffect(() => {
    try {
      fetchWithRetry();
    } catch (error) {
      handleError(error, 'Initial data fetch');
    }
  }, [fetchWithRetry]);

  // Error boundary effect - only for critical errors
  useEffect(() => {
    const handleUnhandledError = (event) => {
      // Only show error boundary for internal server errors or critical errors
      if (isInternalServerError(event.error) ||
          event.error?.message?.toLowerCase().includes('critical')) {
        setHasError(true);
        setErrorInfo({
          message: event.error?.message || 'Critical error occurred',
          stack: event.error?.stack,
          timestamp: new Date().toISOString()
        });
      } else {
        console.warn('Non-critical unhandled error:', event.error);
      }
    };

    const handleUnhandledRejection = (event) => {
      // Only show error boundary for internal server errors or critical errors
      if (isInternalServerError(event.reason) ||
          event.reason?.message?.toLowerCase().includes('critical')) {
        setHasError(true);
        setErrorInfo({
          message: event.reason?.message || 'Critical promise rejection',
          stack: event.reason?.stack,
          timestamp: new Date().toISOString()
        });
      } else {
        console.warn('Non-critical unhandled rejection:', event.reason);
      }
    };

    window.addEventListener('error', handleUnhandledError);
    window.addEventListener('unhandledrejection', handleUnhandledRejection);

    return () => {
      window.removeEventListener('error', handleUnhandledError);
      window.removeEventListener('unhandledrejection', handleUnhandledRejection);
    };
  }, []);

  const isNonSelectable = (request) => {
    try {
      if (!request || typeof request !== 'object') return true;

      return request.accepted ||
          request.expired ||
          request.isCanceled ||
          request.canceled ||
          request.reject ||
          request.rejected;
    } catch (error) {
      handleError(error, 'Checking request selectability');
      return true; // Fail safe - make non-selectable if error
    }
  };

  // Check if a request can be updated by admin - Fix: Always allow admin to edit
  const canUpdateRequest = (request) => {
    try {
      if (!request || typeof request !== 'object') return false;
      
      // Admin can always edit leave requests (no restrictions)
      return true;
    } catch (error) {
      handleError(error, 'Checking request editability');
      return false;
    }
  };

  const selectableRequests = safeLeaveRequests.filter(request =>
      request && request.publicId && !isNonSelectable(request)
  );

  const handlePageChange = async (event, value) => {
    try {
      await fetchWithRetry(value - 1, pagination.pageSize);
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Page change');
      } else {
        console.warn('Non-critical error in page change:', error);
      }
    }
  };

  const handlePageSizeChange = async (event) => {
    try {
      const newSize = event.target.value;
      dispatch(setPageSize(newSize));
      await fetchWithRetry(0, newSize);
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Page size change');
      } else {
        console.warn('Non-critical error in page size change:', error);
      }
    }
  };

  const handleSelect = (id) => {
    try {
      dispatch(selectLeaveRequest(id));
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Selecting request');
      } else {
        console.warn('Non-critical error in select:', error);
      }
    }
  };

  // Modified handleSelectAll to only select selectable requests
  const handleSelectAll = () => {
    try {
      const selectableIds = selectableRequests
          .filter(req => req && req.publicId)
          .map(req => req.publicId);
      const allSelectableSelected = selectableIds.every(id => selected.includes(id));

      if (allSelectableSelected) {
        // If all selectable items are selected, deselect only the selectable ones
        selectableIds.forEach(id => {
          if (selected.includes(id)) {
            dispatch(selectLeaveRequest(id));
          }
        });
      } else {
        // Select all selectable items that aren't already selected
        selectableIds.forEach(id => {
          if (!selected.includes(id)) {
            dispatch(selectLeaveRequest(id));
          }
        });
      }
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Selecting all requests');
      } else {
        console.warn('Non-critical error in select all:', error);
      }
    }
  };

  const handleApprove = async (publicId) => {
    try {
      setProcessingRequests(prev => new Set(prev).add(publicId));
      await dispatch(processLeaveRequest({
        publicId,
        approved: true
      })).unwrap();
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, `Approving request ${publicId}`);
      } else {
        console.warn('Non-critical error in approve:', error);
      }
    } finally {
      setProcessingRequests(prev => {
        const newSet = new Set(prev);
        newSet.delete(publicId);
        return newSet;
      });
    }
  };

  const handleReject = async (publicId) => {
    try {
      setProcessingRequests(prev => new Set(prev).add(publicId));
      await dispatch(processLeaveRequest({
        publicId,
        approved: false
      })).unwrap();
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, `Rejecting request ${publicId}`);
      } else {
        console.warn('Non-critical error in reject:', error);
      }
    } finally {
      setProcessingRequests(prev => {
        const newSet = new Set(prev);
        newSet.delete(publicId);
        return newSet;
      });
    }
  };

  const handleBulkApprove = async () => {
    try {
      // Filter out any non-selectable items from selected array
      const validSelectedIds = selected.filter(id => {
        if (!id) return false;
        const request = safeLeaveRequests.find(req => req && req.publicId && req.publicId === id);
        return request && !isNonSelectable(request);
      });

      if (validSelectedIds.length === 0) return;

      await dispatch(processBulkLeaveRequests({
        leaveIds: validSelectedIds,
        approved: true
      })).unwrap();
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Bulk approve');
      } else {
        console.warn('Non-critical error in bulk approve:', error);
      }
    }
  };

  const handleBulkReject = async () => {
    try {
      // Filter out any non-selectable items from selected array
      const validSelectedIds = selected.filter(id => {
        if (!id) return false;
        const request = safeLeaveRequests.find(req => req && req.publicId && req.publicId === id);
        return request && !isNonSelectable(request);
      });

      if (validSelectedIds.length === 0) return;

      await dispatch(processBulkLeaveRequests({
        leaveIds: validSelectedIds,
        approved: false
      })).unwrap();
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Bulk reject');
      } else {
        console.warn('Non-critical error in bulk reject:', error);
      }
    }
  };

  // Handle opening update dialog
  const handleOpenUpdateDialog = (request) => {
    try {
      setSelectedRequestForUpdate(request);
      setUpdateFormData({
        fromDate: request.fromDate ? format(new Date(request.fromDate), 'yyyy-MM-dd') : '',
        toDate: request.toDate ? format(new Date(request.toDate), 'yyyy-MM-dd') : '',
        leaveType: request.leaveType?.name || '',
        description: request.description || '',
        numOfDays: request.numOfDays || '',
        componentBehavior: request.componentBehavior || '',
        requestStatus: request.requestStatus || ''
      });
      setShowUpdateDialog(true);
    } catch (error) {
      handleError(error, 'Opening update dialog');
    }
  };

  // Handle closing update dialog
  const handleCloseUpdateDialog = () => {
    setShowUpdateDialog(false);
    setSelectedRequestForUpdate(null);
    setUpdateFormData({
      fromDate: '',
      toDate: '',
      leaveType: '',
      description: '',
      numOfDays: '',
      componentBehavior: '',
      requestStatus: ''
    });
  };

  // Handle form field changes
  const handleUpdateFormChange = (field, value) => {
    setUpdateFormData(prev => ({
      ...prev,
      [field]: value
    }));

    // Auto-calculate days when dates change
    if (field === 'fromDate' || field === 'toDate') {
      const fromDate = field === 'fromDate' ? new Date(value) : new Date(updateFormData.fromDate);
      const toDate = field === 'toDate' ? new Date(value) : new Date(updateFormData.toDate);
      
      if (fromDate && toDate && !isNaN(fromDate.getTime()) && !isNaN(toDate.getTime())) {
        const timeDiff = Math.abs(toDate.getTime() - fromDate.getTime());
        const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;
        setUpdateFormData(prev => ({
          ...prev,
          numOfDays: daysDiff * 2 // Assuming 2 units per day
        }));
      }
    }
  };

  // Handle update submission
  const handleSubmitUpdate = async () => {
    try {
      setUpdating(true);
      
      if (!selectedRequestForUpdate) {
        throw new Error('No request selected for update');
      }

      const updatePayload = {
        publicId: selectedRequestForUpdate.publicId,
        fromDate: new Date(updateFormData.fromDate).toISOString(),
        toDate: new Date(updateFormData.toDate).toISOString(),
        leaveType: updateFormData.leaveType,
        description: updateFormData.description,
        numOfDays: parseInt(updateFormData.numOfDays),
        componentBehavior: updateFormData.componentBehavior,
        requestStatus: updateFormData.requestStatus,
        userId: selectedRequestForUpdate.userId,
        employeeID: selectedRequestForUpdate.employeeID,
        happenDate: new Date().toISOString(),
        isEdited: true
      };

      await dispatch(updateLeaveRequest({
        updatePayload,
        userAdmin: true,
        isAdmin: true
      })).unwrap();

      handleCloseUpdateDialog();
      
      // Refresh the data
      await fetchWithRetry();
      
    } catch (error) {
      if (isInternalServerError(error)) {
        handleError(error, 'Updating leave request');
      } else {
        console.warn('Non-critical error in update:', error);
      }
    } finally {
      setUpdating(false);
    }
  };

  const handleNotificationClose = () => {
    try {
      dispatch(clearNotification());
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Closing notification');
      } else {
        console.warn('Non-critical error in notification close:', error);
      }
    }
  };

  const toggleExpandRequest = (publicId) => {
    try {
      setExpandedRequest(expandedRequest === publicId ? null : publicId);
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Toggling request details');
      } else {
        console.warn('Non-critical error in toggle expand:', error);
      }
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        throw new Error('Invalid date');
      }
      return format(date, "MMM dd, yyyy HH:mm");
    } catch (error) {
      console.warn('Date formatting error:', error);
      return dateString || "Invalid Date";
    }
  };

  const getLeaveTypeText = (request) => {
    try {
      if (!request || typeof request !== 'object') return "N/A";

      if (request.leaveType && request.leaveType.name) {
        return request.leaveType.name;
      } else if (request.leaveCategory) {
        return request.leaveCategory;
      } else if (request.short_Leave) {
        return "Short Leave";
      } else {
        return "Regular";
      }
    } catch (error) {
      console.warn('Error getting leave type:', error);
      return "N/A";
    }
  };

  const getStatusChip = (leaveRequest) => {
    try {
      if (!leaveRequest || typeof leaveRequest !== 'object') {
        return <Chip label="Unknown" color="default" size="small" />;
      }

      if (leaveRequest.cancelled || leaveRequest.canceled) {
        return <Chip label="Canceled" color="error" size="small" />;
      }

      if (leaveRequest.reject || leaveRequest.rejected) {
        const rejectingAdmin = Array.isArray(leaveRequest.adminsTra)
            ? leaveRequest.adminsTra.find(a => a?.accepted === false)
            : null;
        const tooltipTitle = rejectingAdmin
            ? `Rejected by ${rejectingAdmin.firstName || ''} ${rejectingAdmin.lastName || ''}`.trim()
            : 'Rejected';

        return (
            <Tooltip title={tooltipTitle}>
              <Chip label="Rejected" color="error" size="small" />
            </Tooltip>
        );
      }

      if (leaveRequest.accepted) {
        const adminsTra = Array.isArray(leaveRequest.adminsTra) ? leaveRequest.adminsTra : [];
        const allApproved = adminsTra.length > 0 && adminsTra.every(a => a?.accepted);
        const someApproved = adminsTra.some(a => a?.accepted);

        if (allApproved) {
          return <Chip label="Approved" color="success" size="small" />;
        } else if (someApproved) {
          return <Chip label="Partially Approved" color="info" size="small" />;
        }
      }

      if (leaveRequest.pending) {
        const adminsTra = Array.isArray(leaveRequest.adminsTra) ? leaveRequest.adminsTra : [];
        const pendingAdmins = adminsTra.filter(a => a && !a.accepted && !a.rejected);
        const tooltipTitle = pendingAdmins.length
            ? `Pending approval from ${pendingAdmins.map(a => `${a.firstName || ''} ${a.lastName || ''}`.trim()).join(', ')}`
            : 'Pending approval';

        return (
            <Tooltip title={tooltipTitle}>
              <Chip label="Pending" color="warning" size="small" />
            </Tooltip>
        );
      }

      return <Chip label="Submitted" color="default" size="small" />;
    } catch (error) {
      console.warn('Error getting status chip:', error);
      return <Chip label="Error" color="error" size="small" />;
    }
  };

  const handleRefresh = () => {
    try {
      setRetryCount(0);
      fetchWithRetry();
    } catch (error) {
      // Only show error for server errors
      if (isInternalServerError(error)) {
        handleError(error, 'Manual refresh');
      } else {
        console.warn('Non-critical error in refresh:', error);
      }
    }
  };

  const closeErrorDialog = () => {
    setShowErrorDialog(false);
    setLastError(null);
  };

  const resetErrorBoundary = () => {
    setHasError(false);
    setErrorInfo(null);
    window.location.reload(); // Full page reload as last resort
  };

  // Calculate counts for display
  const selectedSelectableCount = selected.filter(id => {
    if (!id) return false;
    const request = safeLeaveRequests.find(req => req && req.publicId && req.publicId === id);
    return request && !isNonSelectable(request);
  }).length;

  const allSelectableSelected = selectableRequests.length > 0 &&
      selectableRequests
          .filter(req => req && req.publicId)
          .every(req => selected.includes(req.publicId));

  // Error boundary render
  if (hasError) {
    return (
        <Container maxWidth="lg">
          <CssBaseline />
          <Box sx={{ mt: 4, mb: 4, textAlign: 'center' }}>
            <ErrorOutlineIcon sx={{ fontSize: 64, color: 'error.main', mb: 2 }} />
            <Typography variant="h4" gutterBottom color="error">
              Something went wrong
            </Typography>
            <Typography variant="body1" sx={{ mb: 2 }}>
              An unexpected error occurred. Please try refreshing the page.
            </Typography>
            {errorInfo && (
                <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1, textAlign: 'left' }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Error Details:
                  </Typography>
                  <Typography variant="body2" component="pre" sx={{ fontSize: '0.8rem' }}>
                    {errorInfo.message}
                    {'\n'}
                    {errorInfo.timestamp}
                  </Typography>
                </Box>
            )}
            <Button
                variant="contained"
                onClick={resetErrorBoundary}
                sx={{ mt: 2 }}
            >
              Reload Page
            </Button>
          </Box>
        </Container>
    );
  }

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h4" gutterBottom>
              Manage Leave Requests
            </Typography>
            <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={handleRefresh}
                disabled={loading}
            >
              Refresh
            </Button>
          </Box>

          {error && isInternalServerError({ message: error }) && (
              <Alert severity="error" sx={{ mb: 2 }}>
                <AlertTitle>Internal Server Error</AlertTitle>
                {error}
                {retryCount > 0 && (
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      Retry attempt: {retryCount}/3
                    </Typography>
                )}
              </Alert>
          )}

          {/* Error Dialog */}
          <Dialog
              open={showErrorDialog}
              onClose={closeErrorDialog}
              maxWidth="md"
              fullWidth
          >
            <DialogTitle>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <ErrorOutlineIcon sx={{ color: 'error.main', mr: 1 }} />
                Error Details
              </Box>
            </DialogTitle>
            <DialogContent>
              {lastError && (
                  <Box>
                    <Typography variant="subtitle1" gutterBottom>
                      <strong>Context:</strong> {lastError.context}
                    </Typography>
                    <Typography variant="body1" gutterBottom>
                      <strong>Message:</strong> {lastError.message}
                    </Typography>
                    <Typography variant="body2" color="textSecondary" gutterBottom>
                      <strong>Time:</strong> {new Date(lastError.timestamp).toLocaleString()}
                    </Typography>
                    {lastError.stack && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" gutterBottom>
                            Stack Trace:
                          </Typography>
                          <Box
                              component="pre"
                              sx={{
                                bgcolor: 'grey.100',
                                p: 1,
                                borderRadius: 1,
                                fontSize: '0.8rem',
                                overflow: 'auto',
                                maxHeight: 200
                              }}
                          >
                            {lastError.stack}
                          </Box>
                        </Box>
                    )}
                  </Box>
              )}
            </DialogContent>
            <DialogActions>
              <Button onClick={closeErrorDialog}>Close</Button>
              <Button onClick={handleRefresh} variant="contained">
                Retry
              </Button>
            </DialogActions>
          </Dialog>

          {/* Update Dialog */}
          <Dialog
              open={showUpdateDialog}
              onClose={handleCloseUpdateDialog}
              maxWidth="md"
              fullWidth
          >
            <DialogTitle>
              Update Leave Request - {selectedRequestForUpdate?.employeeID}
            </DialogTitle>
            <DialogContent>
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid item xs={12} sm={6}>
                  <TextField
                      fullWidth
                      label="From Date"
                      type="date"
                      value={updateFormData.fromDate}
                      onChange={(e) => handleUpdateFormChange('fromDate', e.target.value)}
                      InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                      fullWidth
                      label="To Date"
                      type="date"
                      value={updateFormData.toDate}
                      onChange={(e) => handleUpdateFormChange('toDate', e.target.value)}
                      InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Leave Type</InputLabel>
                    <Select
                        value={updateFormData.leaveType}
                        label="Leave Type"
                        onChange={(e) => handleUpdateFormChange('leaveType', e.target.value)}
                    >
                      {leaveTypes.map((type) => (
                          <MenuItem key={type.id} value={type.name}>
                            {type.name}
                          </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Component Behavior</InputLabel>
                    <Select
                        value={updateFormData.componentBehavior}
                        label="Component Behavior"
                        onChange={(e) => handleUpdateFormChange('componentBehavior', e.target.value)}
                    >
                      {componentBehaviors.map((behavior) => (
                          <MenuItem key={behavior.value} value={behavior.value}>
                            {behavior.label}
                          </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Request Status</InputLabel>
                    <Select
                        value={updateFormData.requestStatus}
                        label="Request Status"
                        onChange={(e) => handleUpdateFormChange('requestStatus', e.target.value)}
                    >
                      {requestStatuses.map((status) => (
                          <MenuItem key={status.value} value={status.value}>
                            {status.label}
                          </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                      fullWidth
                      label="Number of Days (Units)"
                      type="number"
                      value={updateFormData.numOfDays}
                      onChange={(e) => handleUpdateFormChange('numOfDays', e.target.value)}
                      helperText="2 units = 1 full day, 1 unit = half day"
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                      fullWidth
                      label="Description"
                      multiline
                      rows={3}
                      value={updateFormData.description}
                      onChange={(e) => handleUpdateFormChange('description', e.target.value)}
                  />
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseUpdateDialog} disabled={updating}>
                Cancel
              </Button>
              <Button 
                onClick={handleSubmitUpdate} 
                variant="contained" 
                disabled={updating || !updateFormData.fromDate || !updateFormData.toDate || !updateFormData.leaveType}
              >
                {updating ? <CircularProgress size={20} /> : 'Update Request'}
              </Button>
            </DialogActions>
          </Dialog>

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
                  disabled={selectedSelectableCount === 0 || loading}
                  sx={{ mr: 1 }}
              >
                Approve Selected ({selectedSelectableCount})
              </Button>
              <Button
                  variant="contained"
                  color="secondary"
                  onClick={handleBulkReject}
                  disabled={selectedSelectableCount === 0 || loading}
                  sx={{ mr: 1 }}
              >
                Reject Selected ({selectedSelectableCount})
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
                <Typography sx={{ ml: 2 }}>Loading leave requests...</Typography>
              </Box>
          ) : (
              <>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead sx={{ bgcolor: "primary.light" }}>
                      <TableRow>
                        <TableCell padding="checkbox">
                          <Tooltip title={selectableRequests.length === 0 ? "No selectable items" : "Select all selectable items"}>
                        <span>
                          <Checkbox
                              indeterminate={
                                  selected.length > 0 && !allSelectableSelected
                              }
                              checked={allSelectableSelected}
                              onChange={handleSelectAll}
                              disabled={selectableRequests.length === 0}
                          />
                        </span>
                          </Tooltip>
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
                      {safeLeaveRequests.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={10} align="center">
                              {error ? 'Failed to load leave requests' : 'No leave requests found'}
                            </TableCell>
                          </TableRow>
                      ) : (
                          safeLeaveRequests.map((request, index) => {
                            if (!request || !request.publicId) {
                              return null;
                            }

                            const isRequestNonSelectable = isNonSelectable(request);
                            const isExpanded = expandedRequest === request.publicId;
                            const isProcessing = processingRequests.has(request.publicId);
                            const canUpdate = canUpdateRequest(request);

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
                                    <TableCell>{request.numOfDays || 'N/A'}</TableCell>
                                    <TableCell>{getStatusChip(request)}</TableCell>
                                    <TableCell>
                                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                                        <Tooltip
                                            title={isRequestNonSelectable ? "This leave request cannot be processed" : ""}
                                        >
                                  <span>
                                    <Button
                                        variant="contained"
                                        color="primary"
                                        size="small"
                                        disabled={isRequestNonSelectable || loading || isProcessing}
                                        onClick={() => handleApprove(request.publicId)}
                                    >
                                      {isProcessing ? <CircularProgress size={16} /> : 'Approve'}
                                    </Button>
                                  </span>
                                        </Tooltip>
                                        <Tooltip
                                            title={isRequestNonSelectable ? "This leave request cannot be processed" : ""}
                                        >
                                  <span>
                                    <Button
                                        variant="outlined"
                                        color="error"
                                        size="small"
                                        disabled={isRequestNonSelectable || loading || isProcessing}
                                        onClick={() => handleReject(request.publicId)}
                                    >
                                      {isProcessing ? <CircularProgress size={16} /> : 'Reject'}
                                    </Button>
                                  </span>
                                        </Tooltip>
                                        <Tooltip title={canUpdate ? "Edit this leave request" : "Cannot edit finalized requests"}>
                                  <span>
                                    <Button
                                        variant="outlined"
                                        color="info"
                                        size="small"
                                        startIcon={<EditIcon />}
                                        disabled={!canUpdate || loading}
                                        onClick={() => handleOpenUpdateDialog(request)}
                                    >
                                      Edit
                                    </Button>
                                  </span>
                                        </Tooltip>
                                      </Box>
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
                                                <strong>Duration:</strong> {request.numOfDays || 'N/A'} day(s)
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
                                              <Typography variant="body2">
                                                <strong>Edited:</strong> {request.isEdited ? 'Yes' : 'No'}
                                              </Typography>
                                            </div>
                                          </Box>

                                          {Array.isArray(request.adminsTra) && request.adminsTra.length > 0 && (
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
                                                    {request.adminsTra.map((admin, adminIndex) => {
                                                      if (!admin || typeof admin !== 'object') return null;
                                                      return (
                                                          <TableRow key={admin.id || adminIndex}>
                                                            <TableCell>
                                                              {`${admin.firstName || ''} ${admin.lastName || ''}`.trim() || 'N/A'}
                                                            </TableCell>
                                                            <TableCell>{admin.email || 'N/A'}</TableCell>
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
                                                      );
                                                    })}
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
                        Showing {safeLeaveRequests.filter(req => req && req.publicId).length} of {pagination.totalElements || 0} results
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