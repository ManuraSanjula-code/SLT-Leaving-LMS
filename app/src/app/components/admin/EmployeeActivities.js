"use client"; // Ensure this is a Client Component in Next.js

import React, { useState, useEffect } from "react";
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
    TablePagination,
    CircularProgress,
    Chip,
} from "@mui/material";
import { ThemeProvider, CssBaseline, createTheme } from "@mui/material";

// Define the theme
const theme = createTheme();

const EmployeeActivities = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Search and filter states
    const [searchTerm, setSearchTerm] = useState("");
    const [filterType, setFilterType] = useState("all");
    const [filterStatus, setFilterStatus] = useState("all");
    const [filterIssue, setFilterIssue] = useState("all");

    // Pagination states
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // Fetch data from API with pagination parameters
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const response = await fetch(`http://localhost:8080/lms?page=${page}&size=${rowsPerPage}`, {
                    credentials: 'include' // This sends cookies with the request
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const result = await response.json();
                setData(result);
                setTotalElements(result.totalElements);
                setTotalPages(result.totalPages);
                setLoading(false);
            } catch (err) {
                setError(err.message);
                setLoading(false);
            }
        };

        fetchData();
    }, [page, rowsPerPage]); // Re-fetch when page or rowsPerPage changes

    // Handle page change
    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    // Handle rows per page change
    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0); // Reset to first page when changing rows per page
    };

    // Filter activities based on search term and filters
    const filteredActivities = data?.content ? data.content.filter((activity) => {
        const matchesSearch = activity.employeeID.toLowerCase().includes(searchTerm.toLowerCase());

        // Type filter
        let matchesType = true;
        if (filterType !== "all") {
            if (filterType === "fullDay") matchesType = activity.fullDay;
            else if (filterType === "halfDay") matchesType = activity.halfDay;
            else if (filterType === "fullLeave") matchesType = activity.fullLeave;
            else if (filterType === "shortLeave") matchesType = activity.shortLeave;
            else if (filterType === "absent") matchesType = activity.absent;
            else if (filterType === "late") matchesType = activity.late;
        }

        // Status filter
        let matchesStatus = true;
        if (filterStatus !== "all") {
            if (filterStatus === "Approved") matchesStatus = activity.leaveSuccess;
            else if (filterStatus === "Pending") matchesStatus = !activity.leaveSuccess && !activity.unSuccessful && !activity.unAuthorized;
            else if (filterStatus === "Not Approved") matchesStatus = activity.unSuccessful || activity.unAuthorized;
        }

        // Issue filter
        let matchesIssue = true;
        if (filterIssue !== "all") {
            matchesIssue = filterIssue === "hasIssue" ? activity.issues : !activity.issues;
        }

        return matchesSearch && matchesType && matchesStatus && matchesIssue;
    }) : [];

    // Track if we're doing client-side filtering
    const isFiltering = searchTerm !== "" || filterType !== "all" || filterStatus !== "all" || filterIssue !== "all";

    // Get status chip color
    const getStatusColor = (activity) => {
        if (activity.leaveSuccess) return "success";
        if (activity.unSuccessful || activity.unAuthorized) return "error";
        return "success";
    };

    // Get status text
    const getStatusText = (activity) => {
        if (activity.leaveSuccess) return "Approved";
        if (activity.unSuccessful) return "Not Approved (Unsuccessful)";
        if (activity.unAuthorized) return "Not Approved (Unauthorized)";
        return "Okay";
    };

    // Get activity type
    const getActivityType = (activity) => {
        if (activity.fullDay) return "Full Day";
        if (activity.halfDay) return "Half Day";
        if (activity.fullLeave) return "Full Leave";
        if (activity.shortLeave) return "Short Leave";
        if (activity.absent) return "Absent";
        if (activity.late) return "Late";
        return "Unknown";
    };

    if (loading) {
        return (
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                    <CircularProgress />
                </Box>
            </ThemeProvider>
        );
    }

    if (error) {
        return (
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Box sx={{ p: 3 }}>
                    <Typography color="error">Error: {error}</Typography>
                </Box>
            </ThemeProvider>
        );
    }

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Box sx={{ p: 3, backgroundColor: "#fff", minHeight: "100vh" }}>
                <Typography variant="h4" gutterBottom>
                    Employee Activities
                </Typography>

                {/* Search and Filters */}
                <Box sx={{ mb: 3, display: 'flex', flexWrap: 'wrap', gap: 2 }}>
                    <TextField
                        label="Search by Employee ID"
                        variant="outlined"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        sx={{ width: 300 }}
                    />

                    <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                        <InputLabel>Activity Type</InputLabel>
                        <Select value={filterType} onChange={(e) => setFilterType(e.target.value)} label="Activity Type">
                            <MenuItem value="all">All Types</MenuItem>
                            <MenuItem value="fullDay">Full Day</MenuItem>
                            <MenuItem value="halfDay">Half Day</MenuItem>
                            <MenuItem value="fullLeave">Full Leave</MenuItem>
                            <MenuItem value="shortLeave">Short Leave</MenuItem>
                            <MenuItem value="absent">Absent</MenuItem>
                            <MenuItem value="late">Late</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                        <InputLabel>Status</InputLabel>
                        <Select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)} label="Status">
                            <MenuItem value="all">All Statuses</MenuItem>
                            <MenuItem value="Approved">Approved</MenuItem>
                            <MenuItem value="Pending">Pending</MenuItem>
                            <MenuItem value="Not Approved">Not Approved</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                        <InputLabel>Issue</InputLabel>
                        <Select value={filterIssue} onChange={(e) => setFilterIssue(e.target.value)} label="Issue">
                            <MenuItem value="all">All</MenuItem>
                            <MenuItem value="hasIssue">Has Issue</MenuItem>
                            <MenuItem value="noIssue">No Issue</MenuItem>
                        </Select>
                    </FormControl>
                </Box>

                {/* Table of Employee Activities */}
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>SLT ID</TableCell>
                                <TableCell>Type</TableCell>
                                <TableCell>Date</TableCell>
                                <TableCell>Arrival Time</TableCell>
                                <TableCell>Left Time</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell>Issue</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredActivities.map((activity) => (
                                <TableRow key={activity.id}>
                                    <TableCell>{activity.employeeID}</TableCell>
                                    <TableCell>{getActivityType(activity)}</TableCell>
                                    <TableCell>{new Date(activity.arrivalDate).toLocaleDateString()}</TableCell>
                                    <TableCell>{activity.arrivalTime || '-'}</TableCell>
                                    <TableCell>{activity.leftTime || '-'}</TableCell>
                                    <TableCell>
                                        <Chip
                                            label={getStatusText(activity)}
                                            color={getStatusColor(activity)}
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        {activity.issues ? (
                                            <Chip
                                                label="Yes"
                                                color="error"
                                                size="small"
                                                title={activity.issueDescription}
                                            />
                                        ) : (
                                            <Chip
                                                label="No"
                                                color="success"
                                                size="small"
                                            />
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

                {/* Pagination */}
                <TablePagination
                    rowsPerPageOptions={[5, 10, 25, 50]}
                    component="div"
                    count={isFiltering ? filteredActivities.length : totalElements}
                    rowsPerPage={rowsPerPage}
                    page={isFiltering ? 0 : page}
                    onPageChange={isFiltering ? null : handleChangePage}
                    onRowsPerPageChange={isFiltering ? null : handleChangeRowsPerPage}
                    disabled={isFiltering}
                    labelDisplayedRows={
                        isFiltering
                            ? ({ from, to, count }) => `${from}-${to} of ${count} (filtered)`
                            : ({ from, to, count }) => `${from}-${to} of ${count}`
                    }
                />
            </Box>
        </ThemeProvider>
    );
};

export default EmployeeActivities;