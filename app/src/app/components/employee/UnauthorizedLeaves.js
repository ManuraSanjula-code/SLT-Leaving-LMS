"use client";

import React, { useState } from "react";
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
} from "@mui/material";
import { Check as CheckIcon, Close as CloseIcon } from "@mui/icons-material";

// Mock data for unsuccessful leave requests
const unauthorizedLeaves = [
  {
    id: 1,
    employeeName: "John Doe",
    type: "Sick Leave",
    startDate: "2023-10-01",
    endDate: "2023-10-03",
    status: "Rejected",
    isResolved: false,
  },
  {
    id: 2,
    employeeName: "Jane Smith",
    type: "Casual Leave",
    startDate: "2023-10-05",
    endDate: "2023-10-06",
    status: "Canceled",
    isResolved: true,
  },
  {
    id: 3,
    employeeName: "Alice Johnson",
    type: "Annual Leave",
    startDate: "2023-10-10",
    endDate: "2023-10-15",
    status: "Rejected",
    isResolved: false,
  },
  {
    id: 4,
    employeeName: "Bob Brown",
    type: "Sick Leave",
    startDate: "2023-10-20",
    endDate: "2023-10-25",
    status: "Canceled",
    isResolved: false,
  },
];

const UnauthorizedLeaves = ({ isAdmin }) => {
  isAdmin = true
  const [searchQuery, setSearchQuery] = useState(""); // State for search query
  const [statusFilter, setStatusFilter] = useState("All"); // State for status filter
  const [typeFilter, setTypeFilter] = useState("All"); // State for type filter
  const [selected, setSelected] = useState([]); // State for selected leave requests
  const [startDateFilter, setStartDateFilter] = useState(""); // State for start date filter
  const [endDateFilter, setEndDateFilter] = useState(""); // State for end date filter
  const [resolutionFilter, setResolutionFilter] = useState("All"); // State for resolution filter

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

  // Handle resolving a leave
  const handleResolveLeave = (id) => {
    const updatedLeaves = unauthorizedLeaves.map((leave) =>
        leave.id === id ? { ...leave, isResolved: true } : leave
    );
    // Update state or send to backend
    console.log(`Resolved leave for employee ID: ${id}`);
    // For now, log the updated list
    console.log(updatedLeaves);
  };

  // Handle approving a leave
  const handleApproveLeave = (id) => {
    console.log(`Approved leave for employee ID: ${id}`);
    // Add your approve logic here
  };

  // Handle rejecting a leave
  const handleRejectLeave = (id) => {
    console.log(`Rejected leave for employee ID: ${id}`);
    // Add your reject logic here
  };

  // Filter unsuccessful leave requests based on search query and filters
  const filteredLeaves = unauthorizedLeaves.filter((leave) => {
    const matchesSearchQuery =
        leave.employeeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        leave.type.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatusFilter =
        statusFilter === "All" || leave.status === statusFilter;

    const matchesTypeFilter =
        typeFilter === "All" || leave.type === typeFilter;

    const matchesStartDateFilter =
        !startDateFilter || leave.startDate >= startDateFilter;

    const matchesEndDateFilter =
        !endDateFilter || leave.endDate <= endDateFilter;

    const matchesResolutionFilter =
        resolutionFilter === "All" ||
        (resolutionFilter === "Resolved" && leave.isResolved) ||
        (resolutionFilter === "Unresolved" && !leave.isResolved);

    return (
        matchesSearchQuery &&
        matchesStatusFilter &&
        matchesTypeFilter &&
        matchesStartDateFilter &&
        matchesEndDateFilter &&
        matchesResolutionFilter
    );
  });

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
  const handleDeleteAllSelected = () => {
    console.log(`Delete selected leave requests: ${selected}`);
    // Add your delete logic here
    setSelected([]); // Clear selection after deletion
  };

  return (
      <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            minHeight: "100vh", // Ensures full viewport height
          }}
      >
        <CssBaseline />
        <Container maxWidth="lg">
          <Box sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom>
              Unauthorized Leave Requests
            </Typography>

            {/* Search Bar */}
            <TextField
                label="Search by Employee Name or Leave Type"
                variant="outlined"
                fullWidth
                value={searchQuery}
                onChange={handleSearchChange}
                sx={{ mb: 2 }}
            />

            {/* Filters */}
            <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Filter by Status</InputLabel>
                <Select
                    value={statusFilter}
                    onChange={handleStatusFilterChange}
                    label="Filter by Status"
                >
                  <MenuItem value="All">All</MenuItem>
                  <MenuItem value="Rejected">Rejected</MenuItem>
                  <MenuItem value="Canceled">Canceled</MenuItem>
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
                  <MenuItem value="Sick Leave">Sick Leave</MenuItem>
                  <MenuItem value="Casual Leave">Casual Leave</MenuItem>
                  <MenuItem value="Annual Leave">Annual Leave</MenuItem>
                </Select>
              </FormControl>

              <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel>Filter by Resolution</InputLabel>
                <Select
                    value={resolutionFilter}
                    onChange={(e) => setResolutionFilter(e.target.value)}
                    label="Filter by Resolution"
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

            {/* Delete All Selected Button */}
            <Button
                variant="contained"
                color="error"
                onClick={handleDeleteAllSelected}
                disabled={selected.length === 0}
                sx={{ mb: 2 }}
            >
              Delete All Selected
            </Button>

            {/* Table */}
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox
                          indeterminate={
                              selected.length > 0 && selected.length < filteredLeaves.length
                          }
                          checked={selected.length === filteredLeaves.length}
                          onChange={handleSelectAll}
                      />
                    </TableCell>
                    <TableCell>Employee Name</TableCell>
                    <TableCell>Leave Type</TableCell>
                    <TableCell>Start Date</TableCell>
                    <TableCell>End Date</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredLeaves.map((leave) => (
                      <TableRow key={leave.id}>
                        <TableCell padding="checkbox">
                          <Checkbox
                              checked={selected.includes(leave.id)}
                              onChange={() => handleSelect(leave.id)}
                          />
                        </TableCell>
                        <TableCell>{leave.employeeName}</TableCell>
                        <TableCell>{leave.type}</TableCell>
                        <TableCell>{leave.startDate}</TableCell>
                        <TableCell>{leave.endDate}</TableCell>
                        <TableCell
                            sx={{
                              color: leave.isResolved ? "green" : "red",
                              fontWeight: "bold",
                            }}
                        >
                          {leave.status}
                        </TableCell>
                        <TableCell>
                          {isAdmin && !leave.isResolved && (
                              <>
                                <IconButton
                                    onClick={() => handleApproveLeave(leave.id)}
                                    color="success"
                                >
                                  <CheckIcon />
                                </IconButton>
                                <IconButton
                                    onClick={() => handleRejectLeave(leave.id)}
                                    color="error"
                                >
                                  <CloseIcon />
                                </IconButton>
                              </>
                          )}
                          {!leave.isResolved && (
                              <Button
                                  variant="contained"
                                  color="primary"
                                  size="small"
                                  onClick={() => handleResolveLeave(leave.id)}
                              >
                                Resolve
                              </Button>
                          )}
                        </TableCell>
                      </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </Container>
        {/*<Footer/>*/}
      </Box>
  );
};

export default UnauthorizedLeaves;