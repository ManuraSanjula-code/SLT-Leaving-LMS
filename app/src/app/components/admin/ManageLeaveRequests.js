"use client";

import React, { useState, useEffect } from "react";
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
  MenuItem
} from "@mui/material";
import { format } from "date-fns";

const ManageLeaveRequests = () => {
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10
  });
  const [error, setError] = useState(null);

  // Fetch leave requests from the server
  const fetchLeaveRequests = async (page = 0, size = 10) => {
    setLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/lms/leave/all?page=${page}&size=${size}`, {
        method: 'GET',
        credentials: 'include', // This will send cookies with the request
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      setLeaveRequests(data.content);
      setPagination({
        currentPage: data.number,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
        pageSize: data.pageable.pageSize
      });
    } catch (err) {
      setError(err.message);
      console.error("Error fetching leave requests:", err);
    } finally {
      setLoading(false);
    }
  };

  // Load data on component mount
  useEffect(() => {
    fetchLeaveRequests();
  }, []);

  // Handle page change
  const handlePageChange = (event, value) => {
    // API pages are 0-indexed, but Pagination component is 1-indexed
    fetchLeaveRequests(value - 1, pagination.pageSize);
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newSize = event.target.value;
    fetchLeaveRequests(0, newSize);
  };

  // Handle individual row selection
  const handleSelect = (id) => {
    if (selected.includes(id)) {
      setSelected((prev) => prev.filter((item) => item !== id));
    } else {
      setSelected((prev) => [...prev, id]);
    }
  };

  // Handle "Select All" functionality
  const handleSelectAll = () => {
    if (selected.length === leaveRequests.length) {
      setSelected([]);
    } else {
      setSelected(leaveRequests.map((request) => request.publicId));
    }
  };

  // Handle bulk approve
  const handleBulkApprove = async () => {
    // Implement your approval API call here
    console.log("Approving:", selected);
    // After successful API call:
    // fetchLeaveRequests(pagination.currentPage);
    // setSelected([]);
  };

  // Handle bulk reject
  const handleBulkReject = async () => {
    // Implement your rejection API call here
    console.log("Rejecting:", selected);
    // After successful API call:
    // fetchLeaveRequests(pagination.currentPage);
    // setSelected([]);
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

  // Get status chip for leave request
  const getStatusChip = (leaveRequest) => {
    if (leaveRequest.canceled) {
      return <Chip label="Canceled" color="error" size="small" />;
    } else if (leaveRequest.hodapproved) {
      return <Chip label="Approved" color="success" size="small" />;
    } else if (leaveRequest.supervisedApproved) {
      return <Chip label="Supervisor Approved" color="info" size="small" />;
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
                                  selected.length > 0 && selected.length < leaveRequests.length
                              }
                              checked={selected.length === leaveRequests.length && leaveRequests.length > 0}
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
                          leaveRequests.map((request) => (
                              <TableRow key={request.publicId}>
                                <TableCell padding="checkbox">
                                  <Checkbox
                                      checked={selected.includes(request.publicId)}
                                      onChange={() => handleSelect(request.publicId)}
                                  />
                                </TableCell>
                                <TableCell>{request.employeeID.substring(0, 8)}...</TableCell>
                                <TableCell>{formatDate(request.submitDate)}</TableCell>
                                <TableCell>{formatDate(request.fromDate)}</TableCell>
                                <TableCell>{formatDate(request.toDate)}</TableCell>
                                <TableCell>
                                  {request.leaveType || request.leaveCategory || (request.short_Leave ? "Short Leave" : "Regular")}
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
                                      disabled={request.hodapproved || request.canceled}
                                  >
                                    Approve
                                  </Button>
                                  <Button
                                      variant="outlined"
                                      color="error"
                                      size="small"
                                      disabled={request.canceled}
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
                        Showing {leaveRequests.length} of {pagination.totalElements} results
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