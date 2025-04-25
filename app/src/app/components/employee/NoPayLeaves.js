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
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Pagination,
  Tooltip,
  Switch,
  FormControlLabel,
} from "@mui/material";
import {
  Info as InfoIcon,
  FilterAlt as FilterIcon,
} from "@mui/icons-material";

const ManageNoPay = ({ isAdmin = false }) => {
  const [noPayRecords, setNoPayRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [startDateFilter, setStartDateFilter] = useState("");
  const [endDateFilter, setEndDateFilter] = useState("");
  const [userIdFilter, setUserIdFilter] = useState(""); // For admin to filter by userId
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    pageSize: 10
  });

  // Detail dialog state
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);

  // Fetch no pay data from the server
  useEffect(() => {
    fetchNoPayData(pagination.currentPage);
  }, [pagination.currentPage, pagination.pageSize, isAdmin]);

  const fetchNoPayData = async (page = 0) => {
    try {
      setLoading(true);

      // Get userId from sessionStorage
      const userId = sessionStorage.getItem('userId');

      if (!userId) {
        throw new Error("User ID not found in sessionStorage");
      }

      // Set the base URL based on admin status
      let baseUrl = isAdmin
          ? "http://localhost:8080/lms/no-pay"
          : `http://localhost:8080/lms/no-pay/user/${userId}`;

      // Add query parameters
      const queryParams = new URLSearchParams({
        page: page.toString(),
        size: pagination.pageSize.toString()
      });

      // Add additional query params if in admin mode and filtering by user ID
      if (isAdmin && userIdFilter) {
        queryParams.append('userId', userIdFilter);
      }

      // Make API call to fetch no pay data with pagination
      const response = await fetch(`${baseUrl}?${queryParams.toString()}`, {
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();

      if (!data || !data.content) {
        setNoPayRecords([]);
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
          employeeID: item.employeeID || "",
          submissionDate: item.submissionDate ? new Date(item.submissionDate).toISOString().split('T')[0] : "",
          acctualDate: item.acctualDate ? new Date(item.acctualDate).toISOString().split('T')[0] : "",
          happenDate: item.happenDate ? new Date(item.happenDate).toISOString().split('T')[0] : "",
          unSuccessful: item.unSuccessful || false,
          attendance: item.attendance || "",
          comment: item.comment || "",
          halfDay: item.halfDay || false,
          absent: item.absent || false,
          late: item.late || false,
          lateCover: item.lateCover || false
        }));

        setNoPayRecords(transformedData);
        setPagination({
          totalPages: data.totalPages || 0,
          totalElements: data.totalElements || 0,
          currentPage: data.number || 0,
          pageSize: pagination.pageSize
        });
      }

      setError(null);
    } catch (err) {
      console.error("Error fetching no pay data:", err);
      setError(err.message);
      setNoPayRecords([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle page change
  const handlePageChange = (event, value) => {
    // Update current page (subtract 1 because API is 0-indexed but MUI Pagination is 1-indexed)
    setPagination({
      ...pagination,
      currentPage: value - 1
    });
  };

  // Handle page size change
  const handlePageSizeChange = (event) => {
    const newSize = parseInt(event.target.value);
    setPagination({
      ...pagination,
      pageSize: newSize,
      currentPage: 0 // Reset to first page when changing page size
    });
  };

  // Handle admin mode toggle
  const handleAdminModeToggle = (event) => {
    setIsAdmin(event.target.checked);
    // Reset pagination and filters when switching modes
    setPagination({
      ...pagination,
      currentPage: 0
    });
    setUserIdFilter("");
  };

  // Handle search input change
  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  // Handle user ID filter change (admin only)
  const handleUserIdFilterChange = (event) => {
    setUserIdFilter(event.target.value);
  };

  // Apply filter and search
  const handleApplyFilters = () => {
    // Reset to first page when applying new filters
    setPagination({
      ...pagination,
      currentPage: 0
    });

    // In a real implementation, you would pass these filters to your API call
    fetchNoPayData(0);
  };

  // Filter no pay records based on search query and date filters (client-side filtering)
  const filteredNoPayRecords = noPayRecords.filter((record) => {
    // Safely handle potentially undefined string properties
    const employeeIdLower = (record.employeeID || "").toLowerCase();
    const commentLower = (record.comment || "").toLowerCase();
    const searchQueryLower = searchQuery.toLowerCase();

    const matchesSearchQuery =
        employeeIdLower.includes(searchQueryLower) ||
        commentLower.includes(searchQueryLower);

    const matchesStartDateFilter =
        !startDateFilter || record.happenDate >= startDateFilter;

    const matchesEndDateFilter =
        !endDateFilter || record.happenDate <= endDateFilter;

    return (
        matchesSearchQuery &&
        matchesStartDateFilter &&
        matchesEndDateFilter
    );
  });

  // Open detail dialog
  const handleOpenDetailDialog = (record) => {
    setSelectedRecord(record);
    setDetailDialogOpen(true);
  };

  // Get type of no pay record
  const getNoPayType = (record) => {
    if (record.absent) return "Absent";
    if (record.halfDay) return "Half Day";
    if (record.late) return "Late";
    if (record.unSuccessful) return "Unsuccessful";
    return "Unknown";
  };

  // Check if user has admin role
  const checkIsAdminRole = () => {
    const userRole = sessionStorage.getItem('userRole');
    return userRole && userRole.includes("ADMIN");
  };

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            No Pay Records
          </Typography>

          {/* Admin toggle (only shown if user has admin role) */}
          {checkIsAdminRole() && (
              <FormControlLabel
                  control={
                    <Switch
                        checked={isAdmin}
                        onChange={handleAdminModeToggle}
                        color="primary"
                    />
                  }
                  label="Admin Mode"
                  sx={{ mb: 2 }}
              />
          )}

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
                {/* Search and Filter Section */}
                <Box sx={{ mb: 3 }}>
                  <TextField
                      label="Search by Employee ID or Comments"
                      variant="outlined"
                      fullWidth
                      value={searchQuery}
                      onChange={handleSearchChange}
                      sx={{ mb: 2 }}
                  />

                  <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap", mb: 2 }}>
                    {/* Admin-only user ID filter */}
                    {isAdmin && checkIsAdminRole() && (
                        <TextField
                            label="Filter by User ID"
                            variant="outlined"
                            value={userIdFilter}
                            onChange={handleUserIdFilterChange}
                            placeholder="Enter employee ID"
                        />
                    )}

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
                        startIcon={<FilterIcon />}
                    >
                      Apply Filters
                    </Button>
                  </Box>
                </Box>

                {/* Records per page selector and record count */}
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
                  <Typography variant="body2">
                    Showing {filteredNoPayRecords.length} of {pagination.totalElements} total no pay records
                  </Typography>

                  <FormControl variant="outlined" size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Records per page</InputLabel>
                    <Select
                        value={pagination.pageSize}
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
                {noPayRecords.length === 0 ? (
                    <Alert severity="info">No pay records found.</Alert>
                ) : (
                    <TableContainer component={Paper}>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell>Employee ID</TableCell>
                            <TableCell>Date</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Submission Date</TableCell>
                            <TableCell>Comment</TableCell>
                            <TableCell>Actions</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {filteredNoPayRecords.map((record) => (
                              <TableRow key={record.id}>
                                <TableCell>{record.employeeID || ""}</TableCell>
                                <TableCell>{record.happenDate || "Not specified"}</TableCell>
                                <TableCell>{getNoPayType(record)}</TableCell>
                                <TableCell>{record.submissionDate || "Not specified"}</TableCell>
                                <TableCell>
                                  {record.comment ? (
                                      record.comment.length > 30
                                          ? `${record.comment.substring(0, 30)}...`
                                          : record.comment
                                  ) : "No comment"}
                                </TableCell>
                                <TableCell>
                                  <Tooltip title="View Details">
                                    <IconButton
                                        onClick={() => handleOpenDetailDialog(record)}
                                        color="primary"
                                    >
                                      <InfoIcon />
                                    </IconButton>
                                  </Tooltip>
                                </TableCell>
                              </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                )}

                {/* Pagination and Items Per Page */}
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 3, gap: 2 }}>
                  {pagination.totalPages > 0 && (
                      <Pagination
                          count={pagination.totalPages}
                          page={pagination.currentPage + 1}
                          onChange={handlePageChange}
                          color="primary"
                          showFirstButton
                          showLastButton
                      />
                  )}
                </Box>
              </>
          )}
        </Box>

        {/* Detail Dialog */}
        <Dialog
            open={detailDialogOpen}
            onClose={() => setDetailDialogOpen(false)}
            maxWidth="md"
            fullWidth
        >
          <DialogTitle>No Pay Record Details</DialogTitle>
          {selectedRecord && (
              <DialogContent dividers>
                <TableContainer>
                  <Table>
                    <TableBody>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Employee ID
                        </TableCell>
                        <TableCell>{selectedRecord.employeeID}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Date
                        </TableCell>
                        <TableCell>{selectedRecord.happenDate}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Type
                        </TableCell>
                        <TableCell>{getNoPayType(selectedRecord)}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Submission Date
                        </TableCell>
                        <TableCell>{selectedRecord.submissionDate}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Comment
                        </TableCell>
                        <TableCell>{selectedRecord.comment || "No comment"}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Attendance ID
                        </TableCell>
                        <TableCell>{selectedRecord.attendance || "N/A"}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>
                          Record ID
                        </TableCell>
                        <TableCell>{selectedRecord.publicId}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </TableContainer>
              </DialogContent>
          )}
          <DialogActions>
            <Button onClick={() => setDetailDialogOpen(false)} color="primary">
              Close
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
  );
};

export default ManageNoPay;