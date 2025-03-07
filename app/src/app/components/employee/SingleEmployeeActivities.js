"use client";

import React, { useState } from "react";
import {
    Typography,
    Box,
    TextField,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    MenuItem,
    Select,
    FormControl,
    InputLabel,
} from "@mui/material";
import { ThemeProvider, CssBaseline, createTheme } from "@mui/material";

const theme = createTheme();

// Mock data for employee activities
const mockActivities = [
    { id: 1, employeeName: "John Doe", type: "Sick Leave", date: "2023-10-01", status: "Approved" },
    { id: 2, employeeName: "John Doe", type: "Casual Leave", date: "2023-10-05", status: "Pending" },
    { id: 3, employeeName: "John Doe", type: "Annual Leave", date: "2023-10-10", status: "Approved" },
    { id: 4, employeeName: "John Doe", type: "Movement", date: "2023-10-15", status: "Pending" },
    { id: 5, employeeName: "John Doe", type: "Absence", date: "2023-10-20", status: "Not Approved" },
];

const SingleEmployeeActivities = () => {
    console.log("EmployeeActivities component is rendering...");

    const [searchTerm, setSearchTerm] = useState("");
    const [filterType, setFilterType] = useState("all");
    const [filterStatus, setFilterStatus] = useState("all");
    const [startDateFilter, setStartDateFilter] = useState("");
    const [endDateFilter, setEndDateFilter] = useState("");

    // Handle search input change
    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
    };

    // Handle type filter change
    const handleTypeFilterChange = (event) => {
        setFilterType(event.target.value);
    };

    // Handle status filter change
    const handleStatusFilterChange = (event) => {
        setFilterStatus(event.target.value);
    };

    // Filter activities based on search term, type, status, and date range
    const filteredActivities = mockActivities.filter((activity) => {
        const matchesSearch = activity.employeeName.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesType = filterType === "all" || activity.type === filterType;
        const matchesStatus = filterStatus === "all" || activity.status === filterStatus;
        const matchesStartDate = !startDateFilter || activity.date >= startDateFilter;
        const matchesEndDate = !endDateFilter || activity.date <= endDateFilter;

        return matchesSearch && matchesType && matchesStatus && matchesStartDate && matchesEndDate;
    });

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Box sx={{ p: 3, backgroundColor: "#fff", minHeight: "100vh" }}>
                <Typography variant="h4" gutterBottom>
                    Your Activities
                </Typography>

                {/* Search and Filters */}
                <Box sx={{ mb: 3 }}>
                    <TextField
                        label="Search by Employee Name"
                        variant="outlined"
                        value={searchTerm}
                        onChange={handleSearchChange}
                        sx={{ mr: 2, width: 300 }}
                    />

                    <FormControl variant="outlined" sx={{ mr: 2, minWidth: 150 }}>
                        <InputLabel>Type</InputLabel>
                        <Select value={filterType} onChange={handleTypeFilterChange} label="Type">
                            <MenuItem value="all">All Types</MenuItem>
                            <MenuItem value="Sick Leave">Sick Leave</MenuItem>
                            <MenuItem value="Casual Leave">Casual Leave</MenuItem>
                            <MenuItem value="Annual Leave">Annual Leave</MenuItem>
                            <MenuItem value="Movement">Movement</MenuItem>
                            <MenuItem value="Absence">Absence</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl variant="outlined" sx={{ mr: 2, minWidth: 150 }}>
                        <InputLabel>Status</InputLabel>
                        <Select value={filterStatus} onChange={handleStatusFilterChange} label="Status">
                            <MenuItem value="all">All Statuses</MenuItem>
                            <MenuItem value="Approved">Approved</MenuItem>
                            <MenuItem value="Pending">Pending</MenuItem>
                            <MenuItem value="Not Approved">Not Approved</MenuItem>
                        </Select>
                    </FormControl>

                    <TextField
                        label="Start Date"
                        type="date"
                        variant="outlined"
                        value={startDateFilter}
                        onChange={(e) => setStartDateFilter(e.target.value)}
                        InputLabelProps={{ shrink: true }}
                        sx={{ mr: 2 }}
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

                {/* Table of Employee Activities */}
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Employee Name</TableCell>
                                <TableCell>Type</TableCell>
                                <TableCell>Date</TableCell>
                                <TableCell>Status</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredActivities.map((activity) => (
                                <TableRow key={activity.id}>
                                    <TableCell>{activity.employeeName}</TableCell>
                                    <TableCell>{activity.type}</TableCell>
                                    <TableCell>{activity.date}</TableCell>
                                    <TableCell>{activity.status}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Box>
        </ThemeProvider>
    );
};

export default SingleEmployeeActivities;