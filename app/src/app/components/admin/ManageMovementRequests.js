"use client";

import React, { useEffect } from "react";
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
  Tooltip
} from "@mui/material";
import { format } from "date-fns";

const ManageMovementRequests = () => {
  const dispatch = useDispatch();

  // Get state from Redux store
  const movementRequests = useSelector(state => state.movement.requests);
  const selected = useSelector(state => state.movement.selected);
  const pagination = useSelector(state => state.movement.pagination);
  const loading = useSelector(state => state.movement.loading);
  const error = useSelector(state => state.movement.error);

  // Fetch movement requests when component mounts
  useEffect(() => {
    dispatch(fetchMovementRequests({
      page: pagination.currentPage,
      size: pagination.pageSize
    }));
  }, [dispatch, pagination.currentPage, pagination.pageSize]);

  // Handle page change
  const handlePageChange = (event, value) => {
    // API pages are 0-indexed, but Pagination component is 1-indexed
    dispatch(fetchMovementRequests({
      page: value - 1,
      size: pagination.pageSize
    }));
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    dispatch(setPageSize(newSize));
    dispatch(fetchMovementRequests({
      page: 0,
      size: newSize
    }));
  };

  // Handle individual row selection
  const handleSelect = (id) => {
    dispatch(selectMovementRequest(id));
  };

  // Handle "Select All" functionality
  const handleSelectAll = () => {
    dispatch(selectAllMovementRequests());
  };

  // Handle bulk approve
  const handleBulkApprove = () => {
    dispatch(processBulkMovementRequests({
      movementIds: selected,
      approved: true
    })).then(() => {
      // Refresh data after bulk approval
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  // Handle bulk reject
  const handleBulkReject = () => {
    dispatch(processBulkMovementRequests({
      movementIds: selected,
      approved: false
    })).then(() => {
      // Refresh data after bulk rejection
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  // Handle individual approve
  const handleApprove = (movementId) => {
    dispatch(processMovementRequest({
      movementId,
      approved: true
    })).then(() => {
      // Refresh data after approval
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

  // Handle individual reject
  const handleReject = (movementId) => {
    dispatch(processMovementRequest({
      movementId,
      approved: false
    })).then(() => {
      // Refresh data after rejection
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
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

  // Format time to readable format
  const formatTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "h:mm a");
    } catch (e) {
      return dateString;
    }
  };

  // Format date and time
  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "MMM dd, yyyy h:mm a");
    } catch (e) {
      return dateString;
    }
  };

  // Get movement type display
  const getMovementTypeDisplay = (movement) => {
    if (!movement) return "Unknown";

    if (movement.movementType) {
      return movement.movementType;
    } else if (movement.late) {
      return "Late Arrival";
    } else if (movement.halfDay) {
      return "Half Day";
    } else if (movement.fullDay) {
      return "Full Day";
    } else if (movement.absent) {
      return "Absent";
    } else {
      return "General Movement";
    }
  };

  // Get status chip for movement request
  const getStatusChip = (movement) => {
    if (!movement) return <Chip label="Unknown" color="default" size="small" />;

    if (movement.expired) {
      return <Chip label="Expired" color="error" size="small" />;
    } else if (movement.accepted) {
      return <Chip label="Approved" color="success" size="small" />;
    } else if (movement.pending) {
      return <Chip label="Pending" color="warning" size="small" />;
    } else if (movement.unAuthorized) {
      return <Chip label="Unauthorized" color="error" size="small" />;
    } else if (movement.lateCover) {
      return <Chip label="Late Cover" color="info" size="small" />;
    } else {
      return <Chip label="Submitted" color="default" size="small" />;
    }
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Movement Requests
          </Typography>

          {/* Error message */}
          {error && (
              <Box sx={{ mb: 2, p: 2, bgcolor: "error.light", borderRadius: 1 }}>
                <Typography color="error">Error: {error}</Typography>
              </Box>
          )}

          {/* Bulk Actions */}
          <Box sx={{ mb: 2, display: "flex", justifyContent: "space-between" }}>
            <Box>
              <Button
                  variant="contained"
                  color="primary"
                  onClick={handleBulkApprove}
                  disabled={selected.length === 0}
                  sx={{ mr: 1 }}
              >
                Approve Selected
              </Button>
              <Button
                  variant="contained"
                  color="secondary"
                  onClick={handleBulkReject}
                  disabled={selected.length === 0}
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
                                  selected.length > 0 && selected.length < movementRequests.length
                              }
                              checked={selected.length === movementRequests.length && movementRequests.length > 0}
                              onChange={handleSelectAll}
                          />
                        </TableCell>
                        <TableCell>Employee ID</TableCell>
                        <TableCell>Request Date</TableCell>
                        <TableCell>Movement Date</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>In/Out Times</TableCell>
                        <TableCell>Destination</TableCell>
                        <TableCell>Comment</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {movementRequests.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={10} align="center">
                              No movement requests found
                            </TableCell>
                          </TableRow>
                      ) : (
                          movementRequests.map((request) => (
                              <TableRow key={request.publicId}>
                                <TableCell padding="checkbox">
                                  <Checkbox
                                      checked={selected.includes(request.publicId)}
                                      onChange={() => handleSelect(request.publicId)}
                                  />
                                </TableCell>
                                <TableCell>
                                  {request.employeeID?.substring(0, 8)}...
                                </TableCell>
                                <TableCell>{formatDateTime(request.reqDate)}</TableCell>
                                <TableCell>{formatDate(request.happenDate)}</TableCell>
                                <TableCell>
                                  <Tooltip title={request.category || ""}>
                                    <span>{getMovementTypeDisplay(request)}</span>
                                  </Tooltip>
                                </TableCell>
                                <TableCell>
                                  {request.inTime ? formatTime(request.inTime) : "N/A"} - {request.outTime ? formatTime(request.outTime) : "N/A"}
                                </TableCell>
                                <TableCell>{request.destination || "N/A"}</TableCell>
                                <TableCell sx={{ maxWidth: "200px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                                  {request.comment || "N/A"}
                                </TableCell>
                                <TableCell>{getStatusChip(request)}</TableCell>
                                <TableCell>
                                  <Button
                                      variant="contained"
                                      color="primary"
                                      size="small"
                                      sx={{ mr: 1, mb: 1 }}
                                      disabled={request.accepted || request.expired || request.isCanceled}
                                      onClick={() => handleApprove(request.publicId)}
                                  >
                                    Approve
                                  </Button>
                                  <Button
                                      variant="outlined"
                                      color="error"
                                      size="small"
                                      disabled={request.accepted || request.expired || request.isCanceled}
                                      onClick={() => handleReject(request.publicId)}
                                  >
                                    Reject
                                  </Button>
                                </TableCell>
                              </TableRow>
                          ))
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
                      />
                      <Typography variant="body2" color="textSecondary">
                        Showing {movementRequests.length} of {pagination.totalElements} results
                      </Typography>
                    </Stack>
                )}
              </>
          )}
        </Box>
      </Container>
  );
};

export default ManageMovementRequests;