"use client";

import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchMovementRequests,
  processMovementRequest,
  processBulkMovementRequests,
  selectMovementRequest,
  selectAllMovementRequests,
  setPageSize
} from '../../../../lib/redux/redux-lms/movement/admin/movementSlice';
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
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Divider,
  TextField,
  Grid,
  Alert
} from "@mui/material";
import { format } from "date-fns";

const ManageMovementRequests = () => {
  const dispatch = useDispatch();
  const [openDetails, setOpenDetails] = useState(false);
  const [openEdit, setOpenEdit] = useState(false);
  const [selectedMovement, setSelectedMovement] = useState(null);
  const [editFormData, setEditFormData] = useState({});
  const [editLoading, setEditLoading] = useState(false);

  const movementRequests = useSelector(state => state.movement.requests);
  const selected = useSelector(state => state.movement.selected);
  const pagination = useSelector(state => state.movement.pagination);
  const loading = useSelector(state => state.movement.loading);
  const error = useSelector(state => state.movement.error);

  useEffect(() => {
    dispatch(fetchMovementRequests({
      page: pagination.currentPage,
      size: pagination.pageSize
    }));
  }, [dispatch, pagination.currentPage, pagination.pageSize]);

  // Check if movement can be approved/rejected
  const canProcessRequest = (request) => {
    const nonProcessableStatuses = ['APPROVED', 'CANCELLED', 'REJECTED', 'EXPIRED'];
    return !nonProcessableStatuses.includes(request.requestStatus);
  };

  // Check if movement is selectable for bulk operations
  const isNonSelectable = (request) => {
    return !canProcessRequest(request);
  };

  const selectableRequests = movementRequests.filter(request =>
      request && request.publicId && !isNonSelectable(request)
  );

  const handleRefresh = () => {
    dispatch(fetchMovementRequests({
      page: pagination.currentPage,
      size: pagination.pageSize
    }));
  };

  const handlePageChange = (event, value) => {
    dispatch(fetchMovementRequests({
      page: value - 1,
      size: pagination.pageSize
    }));
  };

  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    dispatch(setPageSize(newSize));
    dispatch(fetchMovementRequests({
      page: 0,
      size: newSize
    }));
  };

  const handleSelect = (id) => {
    dispatch(selectMovementRequest(id));
  };

  const handleSelectAll = () => {
    const selectableIds = selectableRequests.map(req => req.publicId);
    const allSelectableSelected = selectableIds.every(id => selected.includes(id));

    if (allSelectableSelected) {
      selectableIds.forEach(id => {
        if (selected.includes(id)) {
          dispatch(selectMovementRequest(id));
        }
      });
    } else {
      selectableIds.forEach(id => {
        if (!selected.includes(id)) {
          dispatch(selectMovementRequest(id));
        }
      });
    }
  };

  const handleBulkApprove = () => {
    const validSelectedIds = selected.filter(id => {
      const request = movementRequests.find(req => req.publicId === id);
      return request && canProcessRequest(request);
    });

    if (validSelectedIds.length === 0) return;

    dispatch(processBulkMovementRequests({
      movementIds: validSelectedIds,
      approved: true
    })).then(() => {
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  const handleBulkReject = () => {
    const validSelectedIds = selected.filter(id => {
      const request = movementRequests.find(req => req.publicId === id);
      return request && canProcessRequest(request);
    });

    if (validSelectedIds.length === 0) return;

    dispatch(processBulkMovementRequests({
      movementIds: validSelectedIds,
      approved: false
    })).then(() => {
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  const handleApprove = (movementId) => {
    dispatch(processMovementRequest({
      movementId,
      approved: true
    })).then(() => {
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  const handleReject = (movementId) => {
    dispatch(processMovementRequest({
      movementId,
      approved: false
    })).then(() => {
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  const handleOpenDetails = (movement) => {
    setSelectedMovement(movement);
    setOpenDetails(true);
  };

  const handleCloseDetails = () => {
    setOpenDetails(false);
    setSelectedMovement(null);
  };

  const handleOpenEdit = (movement) => {
    setSelectedMovement(movement);
    setEditFormData({
      inTime: movement.inTime || '',
      outTime: movement.outTime || '',
      comment: movement.comment || '',
      destination: movement.destination || '',
      movementType: movement.movementType || '',
      category: movement.category || '',
      requestStatus: movement.requestStatus || 'SUBMITTED'
    });
    setOpenEdit(true);
  };

  const handleCloseEdit = () => {
    setOpenEdit(false);
    setSelectedMovement(null);
    setEditFormData({});
  };

  const handleEditFormChange = (field, value) => {
    setEditFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSaveEdit = async () => {
    if (!selectedMovement) return;

    setEditLoading(true);
    try {
      const empId = sessionStorage.getItem('userId');
      if (!empId) {
        throw new Error('Employee ID not found in session storage');
      }

      const updatePayload = {
        publicId: selectedMovement.publicId,
        employeeId: selectedMovement.employeeId,
        userId: selectedMovement.userId,
        destination: editFormData.destination,
        movementType: editFormData.movementType,
        comment: editFormData.comment,
        category: editFormData.category,
        requestStatus: editFormData.requestStatus,
        inTime: editFormData.inTime,
        outTime: editFormData.outTime,
        componentBehavior: selectedMovement.componentBehavior,
        attSync: selectedMovement.attSync || 0,
        attendance: selectedMovement.attendance || '',
        happenDate: selectedMovement.happenDate,
        reqDate: selectedMovement.reqDate,
        logTime: selectedMovement.logTime,
        isEdited: true
      };

      Object.keys(updatePayload).forEach(key => {
        if (updatePayload[key] === undefined || updatePayload[key] === null) {
          delete updatePayload[key];
        }
      });

      updatePayload.inTimeRaw = editFormData.inTime;
      updatePayload.outTimeRaw = editFormData.outTime;
      updatePayload.happenDateRaw = selectedMovement.happenDate;

      const response = await fetch(`http://192.168.3.20:8080/lms/management/movement/${selectedMovement.publicId}/${empId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(updatePayload)
      });

      if (!response.ok) {
        const errorText = await response.json();
        throw new Error(`ERROR: ${errorText.message}`);
      }

      await dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));

      handleCloseEdit();
    } catch (error) {
      console.error('Failed to update movement:', error);
      alert(`Failed to update movement: ${error.message}`);
    } finally {
      setEditLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "MMM dd, yyyy");
    } catch (e) {
      return dateString;
    }
  };

  const formatTime = (timeString) => {
    if (!timeString) return "N/A";
    try {
      if (timeString.includes('T')) {
        return format(new Date(timeString), "h:mm a");
      } else {
        const [hours, minutes] = timeString.split(':');
        const date = new Date();
        date.setHours(parseInt(hours), parseInt(minutes), 0);
        return format(date, "h:mm a");
      }
    } catch (e) {
      return timeString;
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "MMM dd, yyyy h:mm a");
    } catch (e) {
      return dateString;
    }
  };

  const getMovementTypeDisplay = (movement) => {
    if (!movement) return "Unknown";
    return movement.movementType || movement.category || "General Movement";
  };

  const getStatusChip = (movement) => {
    if (!movement) return <Chip label="Unknown" color="default" size="small" />;

    switch (movement.requestStatus) {
      case 'APPROVED':
        return <Chip label="Approved" color="success" size="small" />;
      case 'REJECTED':
        return <Chip label="Rejected" color="error" size="small" />;
      case 'CANCELLED':
        return <Chip label="Cancelled" color="error" size="small" />;
      case 'EXPIRED':
        return <Chip label="Expired" color="error" size="small" />;
      case 'PENDING_APPROVAL':
        return <Chip label="Pending Approval" color="warning" size="small" />;
      case 'SUBMITTED':
        return <Chip label="Submitted" color="info" size="small" />;
      case 'DRAFT':
        return <Chip label="Draft" color="default" size="small" />;
      default:
        if (movement.accepted) {
          return <Chip label="Approved" color="success" size="small" />;
        }
        if (movement.reject || movement.rejected) {
          return <Chip label="Rejected" color="error" size="small" />;
        }
        if (movement.isCanceled || movement.canceled) {
          return <Chip label="Cancelled" color="error" size="small" />;
        }
        if (movement.pending) {
          return <Chip label="Pending" color="warning" size="small" />;
        }
        return <Chip label="Submitted" color="default" size="small" />;
    }
  };

  const getAdminApprovalStatus = (movement) => {
    if (!movement.adminsTra || movement.adminsTra.length === 0) {
      return <Chip label="No Approvers" color="default" size="small" />;
    }

    const approved = movement.adminsTra.filter(admin => admin.accepted).length;
    const total = movement.adminsTra.length;

    if (approved === total) {
      return <Chip label={`Approved (${approved}/${total})`} color="success" size="small" />;
    } else if (approved > 0) {
      return <Chip label={`Partially Approved (${approved}/${total})`} color="warning" size="small" />;
    } else {
      return <Chip label={`Pending (0/${total})`} color="default" size="small" />;
    }
  };

  const selectedSelectableCount = selected.filter(id => {
    const request = movementRequests.find(req => req.publicId === id);
    return request && canProcessRequest(request);
  }).length;

  const allSelectableSelected = selectableRequests.length > 0 &&
      selectableRequests.every(req => selected.includes(req.publicId));

  return (
      <Container maxWidth="xl">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Movement Requests
          </Typography>

          {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                Error: {error}
              </Alert>
          )}

          <Box sx={{ mb: 2, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
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
              <Button
                  variant="outlined"
                  color="info"
                  onClick={handleRefresh}
                  disabled={loading}
                  sx={{ mr: 1 }}
              >
                {loading ? <CircularProgress size={20} /> : 'Refresh'}
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
                        <TableCell>Request Date</TableCell>
                        <TableCell>Movement Date</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>In/Out Times</TableCell>
                        <TableCell>Approval Status</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {movementRequests.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={9} align="center">
                              No movement requests found
                            </TableCell>
                          </TableRow>
                      ) : (
                          movementRequests.map((request) => {
                            const canProcess = canProcessRequest(request);

                            return (
                                <TableRow key={request.publicId}>
                                  <TableCell padding="checkbox">
                                    <Checkbox
                                        checked={selected.includes(request.publicId)}
                                        onChange={() => handleSelect(request.publicId)}
                                        disabled={!canProcess}
                                    />
                                  </TableCell>
                                  <TableCell>
                                    {request.employeeId}
                                  </TableCell>
                                  <TableCell>{formatDateTime(request.reqDate)}</TableCell>
                                  <TableCell>{formatDate(request.happenDate)}</TableCell>
                                  <TableCell>
                                    <Tooltip title={request.category || ""}>
                                      <span>{getMovementTypeDisplay(request)}</span>
                                    </Tooltip>
                                  </TableCell>
                                  <TableCell>
                                    {formatTime(request.inTime)} - {formatTime(request.outTime)}
                                  </TableCell>
                                  <TableCell>
                                    {getAdminApprovalStatus(request)}
                                  </TableCell>
                                  <TableCell>{getStatusChip(request)}</TableCell>
                                  <TableCell>
                                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                                      {canProcess && (
                                          <>
                                            <Button
                                                variant="contained"
                                                color="primary"
                                                size="small"
                                                disabled={loading}
                                                onClick={() => handleApprove(request.publicId)}
                                            >
                                              Approve
                                            </Button>
                                            <Button
                                                variant="outlined"
                                                color="error"
                                                size="small"
                                                disabled={loading}
                                                onClick={() => handleReject(request.publicId)}
                                            >
                                              Reject
                                            </Button>
                                          </>
                                      )}
                                      <Button
                                          variant="outlined"
                                          color="warning"
                                          size="small"
                                          onClick={() => handleOpenEdit(request)}
                                      >
                                        Edit
                                      </Button>
                                      <Button
                                          variant="outlined"
                                          size="small"
                                          onClick={() => handleOpenDetails(request)}
                                      >
                                        View Details
                                      </Button>
                                    </Box>
                                  </TableCell>
                                </TableRow>
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
                        Showing {movementRequests.length} of {pagination.totalElements} results
                      </Typography>
                    </Stack>
                )}
              </>
          )}
        </Box>

        <Dialog open={openDetails} onClose={handleCloseDetails} maxWidth="md" fullWidth>
          {selectedMovement && (
              <>
                <DialogTitle>Movement Request Details</DialogTitle>
                <DialogContent dividers>
                  <Box sx={{ mb: 3 }}>
                    <Typography variant="h6" gutterBottom>Basic Information</Typography>
                    <Divider sx={{ mb: 2 }} />
                    <Grid container spacing={2}>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Employee ID:</Typography>
                        <Typography>{selectedMovement.employeeId}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Movement Type:</Typography>
                        <Typography>{getMovementTypeDisplay(selectedMovement)}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Movement Date:</Typography>
                        <Typography>{formatDate(selectedMovement.happenDate)}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Request Date:</Typography>
                        <Typography>{formatDateTime(selectedMovement.reqDate)}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">In Time:</Typography>
                        <Typography>{formatTime(selectedMovement.inTime)}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Out Time:</Typography>
                        <Typography>{formatTime(selectedMovement.outTime)}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Duration:</Typography>
                        <Typography>
                          {selectedMovement.movementDurationMinutes !== undefined
                              ? `${selectedMovement.movementDurationMinutes} minutes`
                              : "N/A"}
                        </Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Same Day Movement:</Typography>
                        <Typography>{selectedMovement.sameDayMovement ? "Yes" : "No"}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Category:</Typography>
                        <Typography>{selectedMovement.category || "N/A"}</Typography>
                      </Grid>
                      <Grid item xs={6}>
                        <Typography variant="subtitle2">Destination:</Typography>
                        <Typography>{selectedMovement.destination || "N/A"}</Typography>
                      </Grid>
                      <Grid item xs={12}>
                        <Typography variant="subtitle2">Status:</Typography>
                        {getStatusChip(selectedMovement)}
                      </Grid>
                    </Grid>
                  </Box>

                  <Box sx={{ mb: 3 }}>
                    <Typography variant="h6" gutterBottom>Comments</Typography>
                    <Divider sx={{ mb: 2 }} />
                    <Typography>
                      {selectedMovement.comment || "No comments provided"}
                    </Typography>
                  </Box>

                  <Box>
                    <Typography variant="h6" gutterBottom>Approval Status</Typography>
                    <Divider sx={{ mb: 2 }} />
                    <List>
                      {selectedMovement.adminsTra && selectedMovement.adminsTra.length > 0 ? (
                          selectedMovement.adminsTra.map((admin, index) => (
                              <ListItem key={index}>
                                <ListItemAvatar>
                                  <Avatar>
                                    {admin.firstName ? admin.firstName.charAt(0) : 'A'}
                                  </Avatar>
                                </ListItemAvatar>
                                <ListItemText
                                    primary={`${admin.firstName || 'Unknown'} ${admin.lastName || ''}`}
                                    secondary={
                                      <>
                                        <Typography component="span" variant="body2" color="text.primary">
                                          {admin.email} - {admin.employeeId}
                                        </Typography>
                                        <br />
                                        Status: {admin.accepted ? (
                                          <Chip label={`Approved on ${admin.approvedDate ? formatDateTime(admin.approvedDate) : 'N/A'}`} color="success" size="small" />
                                      ) : (
                                          <Chip label="Pending" color="warning" size="small" />
                                      )}
                                      </>
                                    }
                                />
                              </ListItem>
                          ))
                      ) : (
                          <Typography>No approvers assigned</Typography>
                      )}
                    </List>
                  </Box>
                </DialogContent>
                <DialogActions>
                  <Button onClick={handleCloseDetails}>Close</Button>
                </DialogActions>
              </>
          )}
        </Dialog>

        <Dialog open={openEdit} onClose={handleCloseEdit} maxWidth="sm" fullWidth>
          {selectedMovement && (
              <>
                <DialogTitle>Edit Movement Request</DialogTitle>
                <DialogContent dividers>
                  <Box sx={{ pt: 1 }}>
                    <Alert severity="info" sx={{ mb: 2 }}>
                      Editing movement for Employee ID: {selectedMovement.employeeId}
                    </Alert>

                    <Grid container spacing={2}>
                      <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="In Time"
                            type="time"
                            value={editFormData.inTime}
                            onChange={(e) => handleEditFormChange('inTime', e.target.value)}
                            InputLabelProps={{ shrink: true }}
                        />
                      </Grid>
                      <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Out Time"
                            type="time"
                            value={editFormData.outTime}
                            onChange={(e) => handleEditFormChange('outTime', e.target.value)}
                            InputLabelProps={{ shrink: true }}
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="Destination"
                            value={editFormData.destination}
                            onChange={(e) => handleEditFormChange('destination', e.target.value)}
                        />
                      </Grid>
                      <Grid item xs={6}>
                        <FormControl fullWidth>
                          <InputLabel>Movement Type</InputLabel>
                          <Select
                              value={editFormData.movementType}
                              label="Movement Type"
                              onChange={(e) => handleEditFormChange('movementType', e.target.value)}
                          >
                            <MenuItem value="FULLDAY">Full Day</MenuItem>
                            <MenuItem value="OFFICE_TO_HOME">Office to Home</MenuItem>
                            <MenuItem value="HOME_TO_OFFICE">Home to Office</MenuItem>
                            <MenuItem value="REMOTEWORK">Remote Work</MenuItem>
                          </Select>
                        </FormControl>
                      </Grid>
                      <Grid item xs={6}>
                        <FormControl fullWidth>
                          <InputLabel>Request Status</InputLabel>
                          <Select
                              value={editFormData.requestStatus}
                              label="Request Status"
                              onChange={(e) => handleEditFormChange('requestStatus', e.target.value)}
                          >
                            <MenuItem value="DRAFT">Draft</MenuItem>
                            <MenuItem value="SUBMITTED">Submitted</MenuItem>
                            <MenuItem value="PENDING_APPROVAL">Pending Approval</MenuItem>
                            <MenuItem value="APPROVED">Approved</MenuItem>
                            <MenuItem value="REJECTED">Rejected</MenuItem>
                            <MenuItem value="CANCELLED">Cancelled</MenuItem>
                            <MenuItem value="EXPIRED">Expired</MenuItem>
                          </Select>
                        </FormControl>
                      </Grid>
                      <Grid item xs={12}>
                        <FormControl fullWidth>
                          <InputLabel>Category</InputLabel>
                          <Select
                              value={editFormData.category}
                              label="Category"
                              onChange={(e) => handleEditFormChange('category', e.target.value)}
                          >
                            <MenuItem value="AUTHORIZED">Authorized</MenuItem>
                            <MenuItem value="UN-AUTHORIZED">Unauthorized</MenuItem>
                            <MenuItem value="LATE">Late</MenuItem>
                            <MenuItem value="EARLY">Early</MenuItem>
                          </Select>
                        </FormControl>
                      </Grid>
                      <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="Comments"
                            multiline
                            rows={4}
                            value={editFormData.comment}
                            onChange={(e) => handleEditFormChange('comment', e.target.value)}
                        />
                      </Grid>
                    </Grid>
                  </Box>
                </DialogContent>
                <DialogActions>
                  <Button onClick={handleCloseEdit} disabled={editLoading}>
                    Cancel
                  </Button>
                  <Button
                      onClick={handleSaveEdit}
                      variant="contained"
                      disabled={editLoading}
                  >
                    {editLoading ? <CircularProgress size={20} /> : 'Save Changes'}
                  </Button>
                </DialogActions>
              </>
          )}
        </Dialog>
      </Container>
  );
};

export default ManageMovementRequests;