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
  IconButton,
  Button,
  Checkbox,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from "@mui/material";
import { Delete as DeleteIcon } from "@mui/icons-material";

// Mock data for movement requests
const initialMovementRequests = [
  {
    id: 1,
    employeeId: 1,
    type: "Remote Work",
    startDate: "2023-10-01",
    endDate: "2023-10-03",
    status: "Pending",
  },
  {
    id: 2,
    employeeId: 2,
    type: "Office Relocation",
    startDate: "2023-10-05",
    endDate: "2023-10-06",
    status: "Approved",
  },
  {
    id: 3,
    employeeId: 3,
    type: "Remote Work",
    startDate: "2023-10-10",
    endDate: "2023-10-15",
    status: "Pending",
  },
  {
    id: 4,
    employeeId: 4,
    type: "Office Relocation",
    startDate: "2023-10-20",
    endDate: "2023-10-25",
    status: "Rejected",
  },
];

const ManageMovementRequests = () => {
  const [movementRequests, setMovementRequests] = useState(initialMovementRequests);
  const [selected, setSelected] = useState([]); // State for selected movement requests
  const [searchQuery, setSearchQuery] = useState(""); // State for search query
  const [statusFilter, setStatusFilter] = useState("All"); // State for status filter
  const [typeFilter, setTypeFilter] = useState("All"); // State for type filter
  const [startDateFilter, setStartDateFilter] = useState(""); // State for start date filter
  const [endDateFilter, setEndDateFilter] = useState(""); // State for end date filter

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
    if (selected.length === movementRequests.length) {
      setSelected([]); // Un-select all
    } else {
      setSelected(movementRequests.map((request) => request.id)); // Select all
    }
  };

  // Handle bulk approve
  const handleBulkApprove = () => {
    setMovementRequests((prev) =>
        prev.map((request) =>
            selected.includes(request.id) ? { ...request, status: "Approved" } : request
        )
    );
    setSelected([]); // Clear selection after action
  };

  // Handle bulk reject
  const handleBulkReject = () => {
    setMovementRequests((prev) =>
        prev.map((request) =>
            selected.includes(request.id) ? { ...request, status: "Rejected" } : request
        )
    );
    setSelected([]); // Clear selection after action
  };

  // Handle bulk delete
  const handleBulkDelete = () => {
    setMovementRequests((prev) => prev.filter((request) => !selected.includes(request.id)));
    setSelected([]); // Clear selection after action
  };

  // Handle individual delete
  const handleDeleteMovementRequest = (id) => {
    setMovementRequests((prev) => prev.filter((request) => request.id !== id));
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

  // Filter movement requests based on search query and filters
  const filteredMovementRequests = movementRequests.filter((request) => {
    const matchesSearchQuery =
        request.employeeId.toString().includes(searchQuery.toLowerCase()) ||
        request.type.toLowerCase().includes(searchQuery.toLowerCase());

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

  return (
      <Container maxWidth="lg">
        <CssBaseline />
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Manage Movement Requests
          </Typography>

          {/* Search Bar */}
          <TextField
              label="Search by Employee ID or Type"
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
                <MenuItem value="Pending">Pending</MenuItem>
                <MenuItem value="Approved">Approved</MenuItem>
                <MenuItem value="Rejected">Rejected</MenuItem>
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
                <MenuItem value="Remote Work">Remote Work</MenuItem>
                <MenuItem value="Office Relocation">Office Relocation</MenuItem>
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

          {/* Bulk Actions */}
          <Box sx={{ mb: 2 }}>
            <Button
                variant="contained"
                color="error"
                onClick={handleBulkDelete}
                disabled={selected.length === 0}
            >
              Delete All Movements
            </Button>
          </Box>

          {/* Table */}
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                        indeterminate={
                            selected.length > 0 && selected.length < filteredMovementRequests.length
                        }
                        checked={selected.length === filteredMovementRequests.length}
                        onChange={handleSelectAll}
                    />
                  </TableCell>
                  <TableCell>Employee</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Start Date</TableCell>
                  <TableCell>End Date</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredMovementRequests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell padding="checkbox">
                        <Checkbox
                            checked={selected.includes(request.id)}
                            onChange={() => handleSelect(request.id)}
                        />
                      </TableCell>
                      <TableCell>Employee {request.employeeId}</TableCell>
                      <TableCell>{request.type}</TableCell>
                      <TableCell>{request.startDate}</TableCell>
                      <TableCell>{request.endDate}</TableCell>
                      <TableCell>{request.status}</TableCell>
                      <TableCell>
                        <IconButton
                            onClick={() => handleDeleteMovementRequest(request.id)}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </Container>
  );
};

export default ManageMovementRequests;