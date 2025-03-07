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
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Checkbox,
    Button,
} from "@mui/material";
import { Delete as DeleteIcon, Check as CheckIcon, Close as CloseIcon } from "@mui/icons-material";

// Mock data for leave requests
const allLeaves = [
    {
        id: 1,
        employeeName: "John Doe",
        type: "Sick Leave",
        startDate: "2023-10-01",
        endDate: "2023-10-03",
        status: "Pending",
    },
    {
        id: 2,
        employeeName: "John Doe",
        type: "Casual Leave",
        startDate: "2023-10-05",
        endDate: "2023-10-06",
        status: "Approved",
    },
    {
        id: 3,
        employeeName: "John Doe",
        type: "Annual Leave",
        startDate: "2023-10-10",
        endDate: "2023-10-15",
        status: "Pending",
    },
    {
        id: 4,
        employeeName: "John Doe",
        type: "Sick Leave",
        startDate: "2023-10-20",
        endDate: "2023-10-25",
        status: "Approved",
    },
];

const PendingLeaves = ({ isAdmin }) => {
    const [searchQuery, setSearchQuery] = useState(""); // State for search query
    const [statusFilter, setStatusFilter] = useState("All"); // State for status filter
    const [typeFilter, setTypeFilter] = useState("All"); // State for type filter
    const [startDateFilter, setStartDateFilter] = useState(""); // State for start date filter
    const [endDateFilter, setEndDateFilter] = useState(""); // State for end date filter
    const [selected, setSelected] = useState([]); // State for selected leave requests

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

    // Handle start date filter change
    const handleStartDateFilterChange = (event) => {
        setStartDateFilter(event.target.value);
    };

    // Handle end date filter change
    const handleEndDateFilterChange = (event) => {
        setEndDateFilter(event.target.value);
    };

    // Filter leave requests based on search query, filters, and date range
    const filteredLeaves = allLeaves.filter((leave) => {
        const matchesSearchQuery =
            leave.employeeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.type.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesStatusFilter =
            statusFilter === "All" || leave.status === statusFilter;

        const matchesTypeFilter =
            typeFilter === "All" || leave.type === typeFilter;

        // Convert dates to timestamps for comparison
        const leaveStartDate = new Date(leave.startDate).getTime();
        const leaveEndDate = new Date(leave.endDate).getTime();
        const filterStartDate = startDateFilter ? new Date(startDateFilter).getTime() : null;
        const filterEndDate = endDateFilter ? new Date(endDateFilter).getTime() : null;

        // Check if the leave request falls within the date range
        const matchesStartDate = filterStartDate ? leaveStartDate >= filterStartDate : true;
        const matchesEndDate = filterEndDate ? leaveEndDate <= filterEndDate : true;

        return (
            matchesSearchQuery &&
            matchesStatusFilter &&
            matchesTypeFilter &&
            matchesStartDate &&
            matchesEndDate
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

    // Handle delete individual leave request
    const handleDeleteLeaveRequest = (id) => {
        console.log(`Delete leave request with ID: ${id}`);
        // Add your delete logic here
    };

    // Handle delete all selected leave requests
    const handleDeleteAllSelected = () => {
        console.log(`Delete selected leave requests: ${selected}`);
        // Add your delete logic here
        setSelected([]); // Clear selection after deletion
    };

    // Handle approve leave request
    const handleApproveLeaveRequest = (id) => {
        console.log(`Approve leave request with ID: ${id}`);
        // Add your approve logic here
    };

    // Handle reject leave request
    const handleRejectLeaveRequest = (id) => {
        console.log(`Reject leave request with ID: ${id}`);
        // Add your reject logic here
    };

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    All Leave Requests
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
                <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                    {/* Status Filter */}
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
                        </Select>
                    </FormControl>

                    {/* Type Filter */}
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

                    {/* Start Date Filter */}
                    <TextField
                        label="Start Date"
                        type="date"
                        variant="outlined"
                        value={startDateFilter}
                        onChange={handleStartDateFilterChange}
                        InputLabelProps={{ shrink: true }}
                        sx={{ minWidth: 200 }}
                    />

                    {/* End Date Filter */}
                    <TextField
                        label="End Date"
                        type="date"
                        variant="outlined"
                        value={endDateFilter}
                        onChange={handleEndDateFilterChange}
                        InputLabelProps={{ shrink: true }}
                        sx={{ minWidth: 200 }}
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
                                <TableCell>Actions</TableCell>
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
                                    <TableCell>{leave.status}</TableCell>
                                    <TableCell>
                                        {isAdmin && leave.status === "Pending" && (
                                            <>
                                                <IconButton
                                                    onClick={() => handleApproveLeaveRequest(leave.id)}
                                                    color="success"
                                                >
                                                    <CheckIcon />
                                                </IconButton>
                                                <IconButton
                                                    onClick={() => handleRejectLeaveRequest(leave.id)}
                                                    color="error"
                                                >
                                                    <CloseIcon />
                                                </IconButton>
                                            </>
                                        )}
                                        <IconButton
                                            onClick={() => handleDeleteLeaveRequest(leave.id)}
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

export default PendingLeaves;