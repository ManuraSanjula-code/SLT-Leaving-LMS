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
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Checkbox,
  Button,
  IconButton,
  Pagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Alert,
  Chip,
  CircularProgress
} from "@mui/material";
import { Check as CheckIcon, Visibility as VisibilityIcon } from "@mui/icons-material";
import { useSelector } from "react-redux";

const UnauthorizedLeaves = ({ isAdmin = false }) => {
  // State for leaves data
  const [leavesData, setLeavesData] = useState({
    content: [],
    totalPages: 0,
    totalElements: 0,
    number: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters and pagination state
  const [searchQuery, setSearchQuery] = useState("");
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");
  const [resolutionFilter, setResolutionFilter] = useState("All");
  const [selected, setSelected] = useState([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Detail dialog state
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedLeave, setSelectedLeave] = useState(null);

  // Fetch data from the server
  const fetchLeaves = async () => {
    try {
      setLoading(true);

      // Get userId from sessionStorage or use admin endpoint
      const userId = sessionStorage.getItem('userId');

      if (!userId && !isAdmin) {
        setError("User ID not found. Please login again.");
        setLoading(false);
        return;
      }

      // Determine the endpoint based on isAdmin flag
      const endpoint = isAdmin
          ? `http://localhost:8080/lms/un-authorized`
          : `http://localhost:8080/lms/un-authorized/${userId}`;

      const response = await fetch(`${endpoint}?page=${page}&size=${pageSize}`, {
        credentials: 'include', // This sends all cookies with the request
      });

      if (!response.ok) {
        throw new Error('Failed to fetch data');
      }

      const data = await response.json();
      setLeavesData(data);
      setLoading(false);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  // Initial data load
  useEffect(() => {
    fetchLeaves();
  }, [page, pageSize, isAdmin]);

  // Format date for display
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  // Handle search input change
  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newSize = parseInt(event.target.value);
    setPageSize(newSize);
    setPage(0); // Reset to first page when changing page size
  };

  // Handle resolving a leave
  const handleResolveLeave = async (id) => {
    try {
      const response = await fetch(`http://localhost:8080/lms/resolve-unauthorized/${id}`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error('Failed to resolve leave');
      }

      // Refresh the data
      fetchLeaves();
    } catch (err) {
      setError(err.message);
    }
  };

  // Handle approving a leave
  const handleApproveLeave = async (id) => {
    try {
      const response = await fetch(`http://localhost:8080/lms/approve-unauthorized/${id}`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error('Failed to approve leave');
      }

      // Refresh the data
      fetchLeaves();
    } catch (err) {
      setError(err.message);
    }
  };

  // Open details dialog
  const handleViewDetails = (leave) => {
    setSelectedLeave(leave);
    setDetailDialogOpen(true);
  };

  // Close details dialog
  const handleCloseDetails = () => {
    setDetailDialogOpen(false);
  };

  // Filter leaves based on search query and filters
  const filteredLeaves = leavesData.content ? leavesData.content.filter((leave) => {
    const matchesSearchQuery =
        leave.employeeID?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (leave.issueDescription && leave.issueDescription?.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (leave.publicId && leave.publicId?.toLowerCase().includes(searchQuery.toLowerCase()));

    const leaveDate = new Date(leave.date);
    const matchesStartDateFilter = !startDateFilter || leaveDate >= new Date(startDateFilter);
    const matchesEndDateFilter = !endDateFilter || leaveDate <= new Date(endDateFilter);

    const matchesResolutionFilter =
        resolutionFilter === "All" ||
        (resolutionFilter === "Resolved" && leave.resolve) ||
        (resolutionFilter === "Unresolved" && !leave.resolve);

    return matchesSearchQuery && matchesStartDateFilter && matchesEndDateFilter && matchesResolutionFilter;
  }) : [];

  // Handle individual row selection
  const handleSelect = (id) => {
    if (selected.includes(id)) {
      setSelected((prev) => prev.filter((item) => item !== id)); // Un-select
    } else {
      setSelected((prev) => [...prev, id]); // Select
    }
  };

  // Handle "Select All" functionality
  const handleSelectAll = () => {
    if (selected.length === filteredLeaves.length) {
      setSelected([]); // Un-select all
    } else {
      setSelected(filteredLeaves.map((leave) => leave.id)); // Select all
    }
  };

  // Handle delete all selected leave requests
  const handleDeleteAllSelected = async () => {
    try {
      const response = await fetch(`http://localhost:8080/lms/un-authorized/delete-multiple`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ids: selected }),
      });

      if (!response.ok) {
        throw new Error('Failed to delete selected leaves');
      }

      // Refresh the data and clear selection
      fetchLeaves();
      setSelected([]);
    } catch (err) {
      setError(err.message);
    }
  };

  // Handle bulk resolution
  const handleBulkResolve = async () => {
    try {
      await Promise.all(
          selected.map((id) =>
              fetch(`http://localhost:8080/lms/resolve-unauthorized/${id}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                  'Content-Type': 'application/json',
                }
              })
          )
      );
      // Refresh data after resolving
      setSelected([]);
      fetchLeaves();
    } catch (err) {
      setError("Failed to resolve selected leaves. Please try again.");
    }
  };

  // Handle page change
  const handlePageChange = (event, value) => {
    setPage(value - 1); // API uses 0-based indexing
  };

  // Determine leave type based on the data properties
  const getLeaveType = (leave) => {
    if (leave.fullLeave) return "Full Leave";
    if (leave.halfDay) return "Half Day";
    if (leave.shortLeave) return "Short Leave";
    if (leave.absent) return "Absent";
    return "Unauthorized";
  };

  return (
      <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            minHeight: "100vh",
          }}
      >
        <CssBaseline />
        <Container maxWidth="lg">
          <Box sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom>
              Unauthorized Leave
            </Typography>

            {/* Error Message */}
            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {error}
                </Alert>
            )}

            {/* Search Bar */}
            <TextField
                label="Search by Employee ID, Public ID or Issue Description"
                variant="outlined"
                fullWidth
                value={searchQuery}
                onChange={handleSearchChange}
                sx={{ mb: 2 }}
            />

            {/* Filters */}
            <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Resolution Status</InputLabel>
                <Select
                    value={resolutionFilter}
                    onChange={(e) => setResolutionFilter(e.target.value)}
                    label="Resolution Status"
                >
                  <MenuItem value="All">All</MenuItem>
                  <MenuItem value="Resolved">Resolved</MenuItem>
                  <MenuItem value="Unresolved">Unresolved</MenuItem>
                </Select>
              </FormControl>

              <TextField
                  label="Start Date"
                  type="date"
                  variant="outlined"
                  value={startDateFilter}
                  onChange={(e) => setStartDateFilter(e.target.value)}
                  InputLabelProps={{ shrink: true }}
              />

              <TextField
                  label="End Date"
                  type="date"
                  variant="outlined"
                  value={endDateFilter}
                  onChange={(e) => setEndDateFilter(e.target.value)}
                  InputLabelProps={{ shrink: true }}
              />
            </Box>

            {/* Action Buttons */}
            <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
              {!isAdmin && selected.length > 0 && (
                  <>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleBulkResolve}
                        disabled={selected.length === 0}
                    >
                      Resolve Selected ({selected.length})
                    </Button>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={handleDeleteAllSelected}
                        disabled={selected.length === 0}
                    >
                      Delete Selected
                    </Button>
                  </>
              )}
            </Box>

            {/* Loading Indicator */}
            {loading ? (
                <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                  <CircularProgress />
                </Box>
            ) : (
                <>
                  {/* Records per page selector and data info */}
                  <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
                    <Typography variant="body2">
                      Showing {filteredLeaves.length} of {leavesData.totalElements || 0} total unauthorized leaves
                    </Typography>

                    <FormControl variant="outlined" size="small" sx={{ minWidth: 120 }}>
                      <InputLabel>Records per page</InputLabel>
                      <Select
                          value={pageSize}
                          onChange={handlePageSizeChange}
                          label="Records per page"
                      >
                        <MenuItem value={5}>5</MenuItem>
                        <MenuItem value={10}>10</MenuItem>
                        <MenuItem value={25}>25</MenuItem>
                        <MenuItem value={50}>50</MenuItem>
                        <MenuItem value={100}>100</MenuItem>
                      </Select>
                    </FormControl>
                  </Box>

                  {/* Table */}
                  <TableContainer component={Paper}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          {!isAdmin && (
                              <TableCell padding="checkbox">
                                <Checkbox
                                    indeterminate={
                                        selected.length > 0 && selected.length < filteredLeaves.length
                                    }
                                    checked={selected.length === filteredLeaves.length && filteredLeaves.length > 0}
                                    onChange={handleSelectAll}
                                />
                              </TableCell>
                          )}
                          <TableCell>Public ID</TableCell>
                          <TableCell>Employee ID</TableCell>
                          <TableCell>Date</TableCell>
                          <TableCell>Leave Type</TableCell>
                          <TableCell>Left Time</TableCell>
                          <TableCell>Issue Description</TableCell>
                          <TableCell>Due Date</TableCell>
                          <TableCell>Status</TableCell>
                          <TableCell>Action</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {filteredLeaves.length === 0 ? (
                            <TableRow>
                              <TableCell colSpan={!isAdmin ? 10 : 9} align="center">
                                No unauthorized leaves found
                              </TableCell>
                            </TableRow>
                        ) : (
                            filteredLeaves.map((leave) => (
                                <TableRow key={leave.id}>
                                  {!isAdmin && (
                                      <TableCell padding="checkbox">
                                        <Checkbox
                                            checked={selected.includes(leave.id)}
                                            onChange={() => handleSelect(leave.id)}
                                            disabled={leave.resolve}
                                        />
                                      </TableCell>
                                  )}
                                  <TableCell>{leave.publicId}</TableCell>
                                  <TableCell>{leave.employeeID}</TableCell>
                                  <TableCell>{formatDate(leave.arrivalDate)}</TableCell>
                                  <TableCell>{getLeaveType(leave)}</TableCell>
                                  <TableCell>{leave.leftTime || 'N/A'}</TableCell>
                                  <TableCell sx={{ maxWidth: 250, overflow: "hidden", textOverflow: "ellipsis" }}>
                                    {leave.issueDescription || 'N/A'}
                                  </TableCell>
                                  <TableCell>{formatDate(leave.dueDateForUA)}</TableCell>
                                  <TableCell>
                                    {leave.resolve ? (
                                        <Chip label="Resolved" color="success" size="small" />
                                    ) : (
                                        <Chip label="Unresolved" color="default" size="small" />
                                    )}
                                  </TableCell>
                                  <TableCell>
                                    {!isAdmin && !leave.resolve && (
                                        <IconButton
                                            onClick={() => handleApproveLeave(leave.id)}
                                            color="success"
                                            size="small"
                                            title="Approve"
                                        >
                                          <CheckIcon />
                                        </IconButton>
                                    )}
                                    {/* Show resolve button to both admin and non-admin users */}
                                    {!leave.resolve && !isAdmin && (
                                        <Button
                                            variant="contained"
                                            color="primary"
                                            size="small"
                                            onClick={() => handleResolveLeave(leave.id)}
                                            sx={{ mx: 1 }}
                                        >
                                          Resolve
                                        </Button>
                                    )}
                                    {!isAdmin && (
                                        <IconButton
                                            onClick={() => handleViewDetails(leave)}
                                            color="info"
                                            size="small"
                                            title="View Details"
                                        >
                                          <VisibilityIcon />
                                        </IconButton>
                                    )}
                                  </TableCell>
                                </TableRow>
                            ))
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  {/* Pagination */}
                  <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", mt: 3, gap: 2 }}>
                    <Pagination
                        count={leavesData.totalPages || 1}
                        page={(leavesData.number || 0) + 1} // Add 1 because API uses 0-based indexing
                        onChange={handlePageChange}
                        color="primary"
                        showFirstButton
                        showLastButton
                    />
                  </Box>
                </>
            )}
          </Box>
        </Container>

        {/* Details Dialog */}
        <Dialog
            open={detailDialogOpen}
            onClose={handleCloseDetails}
            maxWidth="md"
            fullWidth
        >
          <DialogTitle>
            Leave Details - {selectedLeave?.employeeID}
          </DialogTitle>
          <DialogContent dividers>
            {selectedLeave && (
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle1">Basic Information</Typography>
                    <Paper sx={{ p: 2, mb: 2 }}>
                      <Typography><strong>ID:</strong> {selectedLeave.id}</Typography>
                      <Typography><strong>Public ID:</strong> {selectedLeave.publicId}</Typography>
                      <Typography><strong>Employee ID:</strong> {selectedLeave.employeeID}</Typography>
                      <Typography><strong>Date:</strong> {formatDate(selectedLeave.date)}</Typography>
                      <Typography><strong>Due Date:</strong> {formatDate(selectedLeave.dueDateForUA)}</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle1">Time Information</Typography>
                    <Paper sx={{ p: 2, mb: 2 }}>
                      <Typography><strong>Arrival Time:</strong> {selectedLeave.arrivalTime || 'N/A'}</Typography>
                      <Typography><strong>Left Time:</strong> {selectedLeave.leftTime || 'N/A'}</Typography>
                      <Typography><strong>Arrival Date:</strong> {formatDate(selectedLeave.arrivalDate) || 'N/A'}</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="subtitle1">Leave Status</Typography>
                    <Paper sx={{ p: 2, mb: 2 }}>
                      <Typography><strong>Status:</strong> {selectedLeave.resolve ? 'Resolved' : 'Unresolved'}</Typography>
                      <Typography><strong>Leave Type:</strong> {getLeaveType(selectedLeave)}</Typography>
                      <Typography><strong>Half Day:</strong> {selectedLeave.halfDay ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Full Leave:</strong> {selectedLeave.fullLeave ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Short Leave:</strong> {selectedLeave.shortLeave ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Full Day:</strong> {selectedLeave.fullDay ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>No Pay:</strong> {selectedLeave.noPay ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Unauthorized:</strong> {selectedLeave.unAuthorized ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Active:</strong> {selectedLeave.active ? 'Yes' : 'No'}</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="subtitle1">Issue Information</Typography>
                    <Paper sx={{ p: 2 }}>
                      <Typography><strong>Has Issues:</strong> {selectedLeave.issues ? 'Yes' : 'No'}</Typography>
                      <Typography><strong>Issue Description:</strong></Typography>
                      <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                        {selectedLeave.issueDescription || 'No description provided'}
                      </Paper>
                    </Paper>
                  </Grid>
                </Grid>
            )}
          </DialogContent>
          <DialogActions>
            {!isAdmin && selectedLeave && !selectedLeave.resolve && (
                <Button
                    onClick={() => {
                      handleResolveLeave(selectedLeave.id);
                      handleCloseDetails();
                    }}
                    color="primary"
                    variant="contained"
                >
                  Resolve
                </Button>
            )}
            <Button onClick={handleCloseDetails} color="inherit">
              Close
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
  );
};

export default UnauthorizedLeaves;