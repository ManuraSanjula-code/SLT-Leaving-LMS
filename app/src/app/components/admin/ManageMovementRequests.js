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

  const isNonSelectable = (request) => {
    return request.accepted ||
        request.expired ||
        request.isCanceled ||
        request.canceled ||
        request.reject ||
        request.rejected;
  };

  const hasNonSelectableRequests = movementRequests.some(request =>
      request && request.publicId && isNonSelectable(request)
  );

  const selectableRequests = movementRequests.filter(request =>
      request && request.publicId && !isNonSelectable(request)
  );

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
    const request = movementRequests.find(req => req.publicId === id);
    if (request && isNonSelectable(request)) return;

    dispatch(selectMovementRequest(id));
  };

  const handleSelectAll = () => {
    dispatch(selectAllMovementRequests());
  };

  const handleBulkApprove = () => {
    dispatch(processBulkMovementRequests({
      movementIds: selected,
      approved: true
    })).then(() => {
      dispatch(fetchMovementRequests({
        page: pagination.currentPage,
        size: pagination.pageSize
      }));
    });
  };

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

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "MMM dd, yyyy");
    } catch (e) {
      return dateString;
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return format(new Date(dateString), "h:mm a");
    } catch (e) {
      return dateString;
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

  const getStatusChip = (movement) => {
    if (!movement) return <Chip label="Unknown" color="default" size="small" />;

    if (movement.isCanceled || movement.canceled) {
      return <Chip label="Canceled" color="error" size="small" />;
    }
    if (movement.reject || movement.rejected) {
      return <Chip label="Rejected" color="error" size="small" />;
    }

    if (movement.expired) {
      return <Chip label="Expired" color="error" size="small" />;
    }

    if (movement.accepted) {
      return <Chip label="Approved" color="success" size="small" />;
    }

    if (movement.pending) {
      return <Chip label="Pending" color="warning" size="small" />;
    }

    if (movement.unAuthorized) {
      return <Chip label="Unauthorized" color="error" size="small" />;
    }

    if (movement.lateCover) {
      return <Chip label="Late Cover" color="info" size="small" />;
    }

    return <Chip label="Submitted" color="default" size="small" />;
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Movement Requests
          </Typography>

          {error && (
              <Box sx={{ mb: 2, p: 2, bgcolor: "error.light", borderRadius: 1 }}>
                <Typography color="error">Error: {error}</Typography>
              </Box>
          )}

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
                        const request = movementRequests.find(req => req.publicId === id);
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
                        const request = movementRequests.find(req => req.publicId === id);
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
                {/* Table */}
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
                          movementRequests.map((request) => {
                            const isRequestNonSelectable = isNonSelectable(request);

                            return (
                                <TableRow key={request.publicId}>
                                  <TableCell padding="checkbox">
                                    <Tooltip
                                        title={isRequestNonSelectable ? "This movement request cannot be selected" : ""}
                                    >
                                    <span>
                                      <Checkbox
                                          checked={selected.includes(request.publicId)}
                                          onChange={() => handleSelect(request.publicId)}
                                          disabled={isRequestNonSelectable}
                                      />
                                    </span>
                                    </Tooltip>
                                  </TableCell>
                                  <TableCell>
                                    {request.employeeId?.substring(0, 8)}...
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
                                    <Tooltip
                                        title={isRequestNonSelectable ? "This movement request cannot be processed" : ""}
                                    >
                                    <span>
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
                                    </span>
                                    </Tooltip>
                                    <Tooltip
                                        title={isRequestNonSelectable ? "This movement request cannot be processed" : ""}
                                    >
                                    <span>
                                      <Button
                                          variant="outlined"
                                          color="error"
                                          size="small"
                                          disabled={isRequestNonSelectable || loading}
                                          onClick={() => handleReject(request.publicId)}
                                      >
                                        Reject
                                      </Button>
                                    </span>
                                    </Tooltip>
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
      </Container>
  );
};

export default ManageMovementRequests;