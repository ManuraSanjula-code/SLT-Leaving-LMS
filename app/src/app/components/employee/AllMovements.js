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
  IconButton,
  Button,
  Checkbox,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  CircularProgress,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Pagination,
  Tooltip,
} from "@mui/material";
import {
  Delete as DeleteIcon,
  Edit as EditIcon,
  Save as SaveIcon,
  Close as CloseIcon
} from "@mui/icons-material";

const ManageMovementRequests = () => {
  const [movementRequests, setMovementRequests] = useState([]);
  const [selected, setSelected] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("All");
  const [typeFilter, setTypeFilter] = useState("All");
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    pageSize: 10
  });

  // Edit related states
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [currentEdit, setCurrentEdit] = useState(null);
  const [editValues, setEditValues] = useState({
    happenDate: "",
    destination: "",
    movementType: ""
  });

  // Delete confirmation dialog
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteMovementId, setDeleteMovementId] = useState(null);

  // Fetch movement data from the server
  useEffect(() => {
    fetchMovementData(pagination.currentPage, pagination.pageSize);
  }, [pagination.currentPage, pagination.pageSize]);

  const fetchMovementData = async (page = 0, size = pagination.pageSize) => {
    try {
      setLoading(true);

      // Get userId from sessionStorage
      const userId = sessionStorage.getItem('userId');

      if (!userId) {
        throw new Error("User ID not found in sessionStorage");
      }

      // Make API call to fetch movement data with pagination
      const response = await fetch(`http://localhost:8080/lms/movement/${userId}?page=${page}&size=${size}`, {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      if (!data || !data.content) {
        setMovementRequests([]);
        setPagination({
          ...pagination,
          totalPages: 0,
          totalElements: 0
        });
      } else {
        // Transform the data to match our component structure
        const transformedData = data.content.map(item => ({
          id: item.id || 0,
          publicId: item.publicId || "",
          employeeId: item.employeeId || "",
          type: item.movementType || "Unknown",
          startDate: item.happenDate ? new Date(item.happenDate).toISOString().split('T')[0] : "",
          endDate: item.reqDate ? new Date(item.reqDate).toISOString().split('T')[0] : "",
          status: getStatus(item),
          inTime: item.inTime || "",
          outTime: item.outTime || "",
          comment: item.comment || "",
          destination: item.destination || "",
          category: item.category || "",
          late: item.late || false,
          absent: item.absent || false,
          fullDay: item.fullDay || false,
          halfDay: item.halfDay || false,
          pending: item.pending || false,
          accepted: item.accepted || false,
          expired: item.expired || false,
          // Keep original field names for editing
          happenDate: item.happenDate || "",
          reqDate: item.reqDate || "",
          movementType: item.movementType || ""
        }));

        setMovementRequests(transformedData);
        setPagination({
          totalPages: data.totalPages || 0,
          totalElements: data.totalElements || 0,
          currentPage: data.number || 0,
          pageSize: data.pageable?.pageSize || size
        });
      }

      setError(null);
    } catch (err) {
      console.error("Error fetching movement data:", err);
      setError(err.message);
      setMovementRequests([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newPageSize = parseInt(event.target.value, 10);

    // Reset to first page when changing page size
    setPagination({
      ...pagination,
      currentPage: 0,
      pageSize: newPageSize
    });
  };

  // Handle page change
  const handlePageChange = (event, value) => {
    // Reset selected items when changing pages
    setSelected([]);

    // Update current page (subtract 1 because API is 0-indexed but MUI Pagination is 1-indexed)
    setPagination({
      ...pagination,
      currentPage: value - 1
    });
  };

  // Helper function to determine status based on item properties
  const getStatus = (item) => {
    if (item.pending) return "Pending";
    if (item.accepted) return "Approved";
    if (item.expired) return "Expired";
    if (item.late) return "Late";
    if (item.absent) return "Absent";
    return "Unknown";
  };

  // Check if a movement request is approved
  const isApproved = (request) => {
    return request.status === "Approved" || request.accepted === true;
  };

  // Handle individual row selection (prevent selecting approved movements)
  const handleSelect = (id) => {
    const request = movementRequests.find(req => req.id === id);
    if (request && isApproved(request)) {
      return; // Don't select approved movements
    }

    if (selected.includes(id)) {
      setSelected((prev) => prev.filter((item) => item !== id));
    } else {
      setSelected((prev) => [...prev, id]);
    }
  };

  // Handle "Select All" functionality (only non-approved items)
  const handleSelectAll = () => {
    const selectableRequests = filteredMovementRequests.filter(request => !isApproved(request));

    if (selected.length === selectableRequests.length && selectableRequests.length > 0) {
      setSelected([]);
    } else {
      setSelected(selectableRequests.map((request) => request.id));
    }
  };

  // Handle bulk delete
  const handleBulkDelete = async () => {
    try {
      setLoading(true);

      // Process all selected items
      for (const id of selected) {
        const request = movementRequests.find(req => req.id === id);
        if (request && request.publicId && !isApproved(request)) {
          await deleteMovementRequest(request.publicId);
        }
      }

      // Refresh data after bulk delete
      await fetchMovementData(pagination.currentPage, pagination.pageSize);
      setSelected([]);
    } catch (err) {
      console.error("Error during bulk delete:", err);
      setError("Failed to delete some selected items. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Open delete confirmation dialog
  const handleOpenDeleteDialog = (movementId) => {
    setDeleteMovementId(movementId);
    setDeleteDialogOpen(true);
  };

  // Handle delete confirmation
  const handleConfirmDelete = async () => {
    if (deleteMovementId) {
      try {
        await deleteMovementRequest(deleteMovementId);
        await fetchMovementData(pagination.currentPage, pagination.pageSize);
        setDeleteDialogOpen(false);
        setDeleteMovementId(null);
      } catch (err) {
        console.error("Error deleting movement request:", err);
        setError("Failed to delete the movement request. Please try again.");
      }
    }
  };

  // API call to delete a movement request
  const deleteMovementRequest = async (publicId) => {
    const response = await fetch(`http://localhost:8080/lms/movement/${publicId}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    // Check if there's content before trying to parse JSON
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json") && response.headers.get("content-length") !== "0") {
      return await response.json();
    }

    // Return a simple success object if no JSON content
    return { success: true };
  };

  // Open edit dialog
  const handleOpenEditDialog = (request) => {
    setCurrentEdit(request);
    setEditValues({
      happenDate: request.happenDate ? new Date(request.happenDate).toISOString().split('T')[0] : "",
      destination: request.destination || "",
      movementType: request.type || ""
    });
    setEditDialogOpen(true);
  };

  // Handle edit input changes
  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditValues(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Save edited movement request
  const handleSaveEdit = async () => {
    try {
      setLoading(true);

      if (!currentEdit || !currentEdit.publicId) {
        throw new Error("Invalid movement request data");
      }

      // Don't allow editing approved movements
      if (isApproved(currentEdit)) {
        throw new Error("Cannot edit approved movement requests");
      }

      const response = await fetch(`http://localhost:8080/lms/management/movement/${currentEdit.publicId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          happenDate: editValues.happenDate,
          destination: editValues.destination,
          movementType: editValues.movementType
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      // Close dialog and refresh data
      setEditDialogOpen(false);
      await fetchMovementData(pagination.currentPage, pagination.pageSize);

    } catch (err) {
      console.error("Error updating movement request:", err);
      setError("Failed to update the movement request. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Handle search input change
  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  // Handle status filter change
  const handleStatusFilterChange = (event) => {
    setStatusFilter(event.target.value);
  };

  // Handle type filter change
  const handleTypeFilterChange = (event) => {
    setTypeFilter(event.target.value);
  };

  // Apply filter and search
  const handleApplyFilters = () => {
    // Reset to first page when applying new filters
    setPagination({
      ...pagination,
      currentPage: 0
    });

    // In a real implementation, you would pass these filters to your API call
    fetchMovementData(0, pagination.pageSize);
  };

  // Filter movement requests based on search query and filters
  const filteredMovementRequests = movementRequests.filter((request) => {
    // Safely handle potentially undefined string properties
    const employeeIdLower = (request.employeeId || "").toLowerCase();
    const typeLower = (request.type || "").toLowerCase();
    const destinationLower = (request.destination || "").toLowerCase();
    const searchQueryLower = searchQuery.toLowerCase();

    const matchesSearchQuery =
        employeeIdLower.includes(searchQueryLower) ||
        typeLower.includes(searchQueryLower) ||
        destinationLower.includes(searchQueryLower);

    const matchesStatusFilter =
        statusFilter === "All" || request.status === statusFilter;

    const matchesTypeFilter =
        typeFilter === "All" || request.type === typeFilter;

    const matchesStartDateFilter =
        !startDateFilter || request.startDate >= startDateFilter;

    const matchesEndDateFilter =
        !endDateFilter || request.endDate <= endDateFilter;

    return (
        matchesSearchQuery &&
        matchesStatusFilter &&
        matchesTypeFilter &&
        matchesStartDateFilter &&
        matchesEndDateFilter
    );
  });

  // Get unique movement types for filter dropdown
  const movementTypes = [...new Set(movementRequests.map(req => req.type).filter(Boolean))];

  // Get unique statuses for filter dropdown
  const statuses = [...new Set(movementRequests.map(req => req.status).filter(Boolean))];

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Movement Requests
          </Typography>

          {/* Error display */}
          {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                Error: {error}
              </Alert>
          )}

          {/* Loading indicator */}
          {loading ? (
              <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                <CircularProgress />
              </Box>
          ) : (
              <>
                {/* Header with Page Size Selector in top right */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  {/* Movement Requests Count */}
                  <Typography variant="body2">
                    Showing {filteredMovementRequests.length} of {pagination.totalElements} total movement requests
                  </Typography>

                  {/* Page Size Selector - Now in top right */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Typography variant="body2">Rows per page:</Typography>
                    <FormControl variant="outlined" size="small">
                      <Select
                          value={pagination.pageSize}
                          onChange={handlePageSizeChange}
                          sx={{ minWidth: 80 }}
                      >
                        <MenuItem value={5}>5</MenuItem>
                        <MenuItem value={10}>10</MenuItem>
                        <MenuItem value={25}>25</MenuItem>
                        <MenuItem value={50}>50</MenuItem>
                      </Select>
                    </FormControl>
                  </Box>
                </Box>

                {/* Search Bar */}
                <TextField
                    label="Search by Employee ID, Type or Destination"
                    variant="outlined"
                    fullWidth
                    value={searchQuery}
                    onChange={handleSearchChange}
                    sx={{ mb: 2 }}
                />

                {/* Filters */}
                <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                  <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                    <InputLabel>Filter by Status</InputLabel>
                    <Select
                        value={statusFilter}
                        onChange={handleStatusFilterChange}
                        label="Filter by Status"
                    >
                      <MenuItem value="All">All</MenuItem>
                      {statuses.map(status => (
                          <MenuItem key={status} value={status}>{status}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                    <InputLabel>Filter by Type</InputLabel>
                    <Select
                        value={typeFilter}
                        onChange={handleTypeFilterChange}
                        label="Filter by Type"
                    >
                      <MenuItem value="All">All</MenuItem>
                      {movementTypes.map(type => (
                          <MenuItem key={type} value={type}>{type}</MenuItem>
                      ))}
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

                  <Button
                      variant="contained"
                      color="primary"
                      onClick={handleApplyFilters}
                  >
                    Apply Filters
                  </Button>
                </Box>

                {/* Bulk Actions */}
                <Box sx={{ mb: 2 }}>
                  <Button
                      variant="contained"
                      color="error"
                      onClick={handleBulkDelete}
                      disabled={selected.length === 0}
                  >
                    Delete Selected ({selected.length})
                  </Button>
                </Box>

                {/* Table */}
                {movementRequests.length === 0 ? (
                    <Alert severity="info">No movement requests found.</Alert>
                ) : (
                    <TableContainer component={Paper}>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell padding="checkbox">
                              <Checkbox
                                  indeterminate={
                                      selected.length > 0 &&
                                      selected.length < filteredMovementRequests.filter(req => !isApproved(req)).length
                                  }
                                  checked={
                                      selected.length === filteredMovementRequests.filter(req => !isApproved(req)).length &&
                                      filteredMovementRequests.filter(req => !isApproved(req)).length > 0
                                  }
                                  onChange={handleSelectAll}
                              />
                            </TableCell>
                            <TableCell>Employee ID</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Destination</TableCell>
                            <TableCell>Date</TableCell>
                            <TableCell>Request Date</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Actions</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {filteredMovementRequests.map((request) => (
                              <TableRow key={request.id}>
                                <TableCell padding="checkbox">
                                  <Tooltip title={isApproved(request) ? "Approved movements cannot be selected" : ""}>
                                    <span>
                                      <Checkbox
                                          checked={selected.includes(request.id)}
                                          onChange={() => handleSelect(request.id)}
                                          disabled={isApproved(request)}
                                      />
                                    </span>
                                  </Tooltip>
                                </TableCell>
                                <TableCell>{request.employeeId || ""}</TableCell>
                                <TableCell>{request.type || "Unknown"}</TableCell>
                                <TableCell>{request.destination || "Not specified"}</TableCell>
                                <TableCell>{request.startDate || "Not specified"}</TableCell>
                                <TableCell>{request.endDate || "Not specified"}</TableCell>
                                <TableCell>{request.status || "Unknown"}</TableCell>
                                <TableCell>
                                  <Tooltip title={isApproved(request) ? "Approved movements cannot be edited" : "Edit"}>
                                    <span>
                                      <IconButton
                                          onClick={() => !isApproved(request) && handleOpenEditDialog(request)}
                                          color="primary"
                                          disabled={isApproved(request)}
                                      >
                                        <EditIcon />
                                      </IconButton>
                                    </span>
                                  </Tooltip>
                                  <Tooltip title={isApproved(request) ? "Approved movements cannot be deleted" : "Delete"}>
                                    <span>
                                      <IconButton
                                          onClick={() => !isApproved(request) && handleOpenDeleteDialog(request.publicId)}
                                          color="error"
                                          disabled={isApproved(request)}
                                      >
                                        <DeleteIcon />
                                      </IconButton>
                                    </span>
                                  </Tooltip>
                                </TableCell>
                              </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                )}

                {/* Pagination Controls - without Page Size Selector */}
                {pagination.totalPages > 0 && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                      <Pagination
                          count={pagination.totalPages}
                          page={pagination.currentPage + 1}
                          onChange={handlePageChange}
                          color="primary"
                          showFirstButton
                          showLastButton
                      />
                    </Box>
                )}
              </>
          )}
        </Box>

        {/* Edit Dialog */}
        <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)}>
          <DialogTitle>Edit Movement Request</DialogTitle>
          <DialogContent>
            <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField
                  name="happenDate"
                  label="Date"
                  type="date"
                  value={editValues.happenDate}
                  onChange={handleEditChange}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
              />

              <TextField
                  name="destination"
                  label="Destination"
                  value={editValues.destination}
                  onChange={handleEditChange}
                  fullWidth
              />

              <FormControl fullWidth margin="normal">
                <InputLabel>Movement Type</InputLabel>
                <Select
                    name="movementType"
                    value={editValues.movementType}
                    onChange={handleEditChange}
                    label="Movement Type"
                >
                  <MenuItem value="ABSENT">Absent</MenuItem>
                  <MenuItem value="LATEWORK">Late Work</MenuItem>
                  <MenuItem value="UNSUCCESSFUL">Unsuccessful</MenuItem>
                  <MenuItem value="UNAUTHORIZED">Unauthorized</MenuItem>
                  <MenuItem value="REMOTEWORK">Remote Work</MenuItem>

                </Select>
              </FormControl>

            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditDialogOpen(false)} startIcon={<CloseIcon />}>
              Cancel
            </Button>
            <Button onClick={handleSaveEdit} variant="contained" color="primary" startIcon={<SaveIcon />}>
              Save
            </Button>
          </DialogActions>
        </Dialog>

        {/* Delete Confirmation Dialog */}
        <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
          <DialogTitle>Confirm Delete</DialogTitle>
          <DialogContent>
            <Typography>
              Are you sure you want to delete this movement request? This action cannot be undone.
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleConfirmDelete} variant="contained" color="error">
              Delete
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
  );
};

export default ManageMovementRequests;