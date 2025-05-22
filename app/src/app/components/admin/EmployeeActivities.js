"use client";

import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
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
    Button,
} from "@mui/material";
import { ThemeProvider, CssBaseline, createTheme } from "@mui/material";
import {
    fetchActivityRecords,
    setSearchTerm,
    setFilterType,
    setFilterStatus,
    setFilterIssue,
    setPage,
    setRowsPerPage,
    clearFilters,
    selectFilteredActivities,
    selectActivitiesData,
    selectIsFiltering,
} from "../../../../lib/redux/redux-lms/employee-activities/admin/employeeActivitiesSlice";

// Define the theme
const theme = createTheme();

const EmployeeActivities = () => {
    const dispatch = useDispatch();

    // Get state from Redux store
    const filteredActivities = useSelector(selectFilteredActivities);
    const {
        loading,
        error,
        searchTerm,
        filterType,
        filterStatus,
        filterIssue,
        page,
        rowsPerPage,
        totalElements,
        totalPages,
    } = useSelector(selectActivitiesData);

    // Check if filtering is applied
    const isFiltering = useSelector(selectIsFiltering);

    // Fetch data from API with pagination parameters
    useEffect(() => {
        if (!isFiltering) {
            dispatch(fetchActivityRecords({ page, rowsPerPage }));
        }
    }, [dispatch, page, rowsPerPage, isFiltering]);

    // Handle page change
    const handleChangePage = (event, newPage) => {
        dispatch(setPage(newPage));
    };

    // Handle rows per page change
    const handleChangeRowsPerPage = (event) => {
        dispatch(setRowsPerPage(parseInt(event.target.value, 10)));
    };

    // Handle search term change
    const handleSearchTermChange = (event) => {
        dispatch(setSearchTerm(event.target.value));
    };

    // Handle filter type change
    const handleFilterTypeChange = (event) => {
        dispatch(setFilterType(event.target.value));
    };

    // Handle filter status change
    const handleFilterStatusChange = (event) => {
        dispatch(setFilterStatus(event.target.value));
    };

    // Handle filter issue change
    const handleFilterIssueChange = (event) => {
        dispatch(setFilterIssue(event.target.value));
    };

    // Handle clear filters
    const handleClearFilters = () => {
        dispatch(clearFilters());
    };

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
                    <Button
                        variant="contained"
                        onClick={() => dispatch(fetchActivityRecords({ page, rowsPerPage }))}
                        sx={{ mt: 2 }}
                    >
                        Try Again
                    </Button>
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
                        onChange={handleSearchTermChange}
                        sx={{ width: 300 }}
                    />

                    <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                        <InputLabel>Activity Type</InputLabel>
                        <Select
                            value={filterType}
                            onChange={handleFilterTypeChange}
                            label="Activity Type"
                        >
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
                        <Select
                            value={filterStatus}
                            onChange={handleFilterStatusChange}
                            label="Status"
                        >
                            <MenuItem value="all">All Statuses</MenuItem>
                            <MenuItem value="Approved">Approved</MenuItem>
                            <MenuItem value="Pending">Pending</MenuItem>
                            <MenuItem value="Not Approved">Not Approved</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                        <InputLabel>Issue</InputLabel>
                        <Select
                            value={filterIssue}
                            onChange={handleFilterIssueChange}
                            label="Issue"
                        >
                            <MenuItem value="all">All</MenuItem>
                            <MenuItem value="hasIssue">Has Issue</MenuItem>
                            <MenuItem value="noIssue">No Issue</MenuItem>
                        </Select>
                    </FormControl>

                    {isFiltering && (
                        <Button
                            variant="outlined"
                            color="secondary"
                            onClick={handleClearFilters}
                        >
                            Clear Filters
                        </Button>
                    )}
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