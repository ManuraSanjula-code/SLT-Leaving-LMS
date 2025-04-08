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
    Chip,
    CircularProgress,
    Pagination
} from "@mui/material";
import { Check as CheckIcon, Close as CloseIcon } from "@mui/icons-material";
import axios from "axios";

const UnsuccessfulLeaves = ({ isAdmin = false }) => {
    // State for data and pagination
    const [leaveData, setLeaveData] = useState({
        content: [],
        totalPages: 0,
        totalElements: 0,
        number: 0,
        size: 10
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);

    // State for filters
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState("All");
    const [resolveFilter, setResolveFilter] = useState("All");
    const [startDateFilter, setStartDateFilter] = useState("");
    const [endDateFilter, setEndDateFilter] = useState("");
    const [selected, setSelected] = useState([]);

    // Fetch data from the server
    const fetchData = async () => {
        try {
            setLoading(true);
            const response = await axios.get(`http://localhost:8080/lms/un-successful`, {
                params: {
                    page: currentPage,
                    size: 10
                },
                withCredentials: true
            });
            setLeaveData(response.data);
            setError(null);
        } catch (err) {
            console.error("Error fetching unsuccessful leaves:", err);
            setError("Failed to load data. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    // Fetch data on component mount and page change
    useEffect(() => {
        fetchData();
    }, [currentPage]);

    // Handle page change
    const handlePageChange = (event, value) => {
        setCurrentPage(value - 1); // API uses 0-based indexing
    };

    // Handle search input change
    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value);
    };

    // Handle status filter change
    const handleStatusFilterChange = (event) => {
        setStatusFilter(event.target.value);
    };

    // Handle resolve filter change
    const handleResolveFilterChange = (event) => {
        setResolveFilter(event.target.value);
    };

    // Handle resolving a leave
    const handleResolveLeave = async (id) => {
        try {
            await axios.post(`http://localhost:8080/lms/resolve-unsuccessful/${id}`, {}, {
                withCredentials: true
            });
            // Refresh data after resolving
            fetchData();
        } catch (err) {
            console.error(`Error resolving leave ID ${id}:`, err);
            setError("Failed to resolve leave request. Please try again.");
        }
    };

    // Filter leaves based on search query and filters
    const filteredLeaves = leaveData.content?.filter((leave) => {
        const matchesSearchQuery =
            leave.employeeID?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.publicId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.issueDescription?.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesStatusFilter =
            statusFilter === "All" ||
            (statusFilter === "Late" && leave.late) ||
            (statusFilter === "Absent" && leave.absent);

        const matchesResolveFilter =
            resolveFilter === "All" ||
            (resolveFilter === "Resolved" && leave.resolve) ||
            (resolveFilter === "Unresolved" && !leave.resolve);

        const matchesStartDateFilter =
            !startDateFilter || new Date(leave.date) >= new Date(startDateFilter);

        const matchesEndDateFilter =
            !endDateFilter || new Date(leave.date) <= new Date(endDateFilter);

        return (
            matchesSearchQuery &&
            matchesStatusFilter &&
            matchesResolveFilter &&
            matchesStartDateFilter &&
            matchesEndDateFilter
        );
    }) || [];

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
        if (selected.length === filteredLeaves.length) {
            setSelected([]);
        } else {
            setSelected(filteredLeaves.map((leave) => leave.id));
        }
    };

    // Handle bulk resolution
    const handleBulkResolve = async () => {
        try {
            await Promise.all(
                selected.map((id) =>
                    axios.post(`http://localhost:8080/lms/resolve-unsuccessful/${id}`, {}, {
                        withCredentials: true
                    })
                )
            );
            setSelected([]);
            fetchData();
        } catch (err) {
            console.error("Error resolving selected leaves:", err);
            setError("Failed to resolve selected leaves. Please try again.");
        }
    };

    // Format date for display
    const formatDate = (dateString) => {
        if (!dateString) return "-";
        const date = new Date(dateString);
        return date.toLocaleDateString();
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
                        Unsuccessful Leave Requests
                    </Typography>

                    {/* Search Bar */}
                    <TextField
                        label="Search by Employee ID or Issue Description"
                        variant="outlined"
                        fullWidth
                        value={searchQuery}
                        onChange={handleSearchChange}
                        sx={{ mb: 2 }}
                    />

                    {/* Filters */}
                    <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                        <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                            <InputLabel>Status</InputLabel>
                            <Select
                                value={statusFilter}
                                onChange={handleStatusFilterChange}
                                label="Status"
                            >
                                <MenuItem value="All">All</MenuItem>
                                <MenuItem value="Late">Late</MenuItem>
                                <MenuItem value="Absent">Absent</MenuItem>
                            </Select>
                        </FormControl>

                        <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                            <InputLabel>Resolved Status</InputLabel>
                            <Select
                                value={resolveFilter}
                                onChange={handleResolveFilterChange}
                                label="Resolved Status"
                            >
                                <MenuItem value="All">All</MenuItem>
                                <MenuItem value="Resolved">Resolved</MenuItem>
                                <MenuItem value="Unresolved">Unresolved</MenuItem>
                            </Select>
                        </FormControl>

                        <TextField
                            label="From Date"
                            type="date"
                            variant="outlined"
                            value={startDateFilter}
                            onChange={(e) => setStartDateFilter(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                        />

                        <TextField
                            label="To Date"
                            type="date"
                            variant="outlined"
                            value={endDateFilter}
                            onChange={(e) => setEndDateFilter(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                        />
                    </Box>

                    {/* Bulk Actions */}
                    {selected.length > 0 && (
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleBulkResolve}
                            sx={{ mb: 2 }}
                        >
                            Resolve All Selected ({selected.length})
                        </Button>
                    )}

                    {/* Error Message */}
                    {error && (
                        <Typography color="error" sx={{ mb: 2 }}>
                            {error}
                        </Typography>
                    )}

                    {/* Loading Indicator */}
                    {loading ? (
                        <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                            <CircularProgress />
                        </Box>
                    ) : (
                        <>
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
                                                    checked={filteredLeaves.length > 0 && selected.length === filteredLeaves.length}
                                                    onChange={handleSelectAll}
                                                />
                                            </TableCell>
                                            <TableCell>ID</TableCell>
                                            <TableCell>Employee ID</TableCell>
                                            <TableCell>Date</TableCell>
                                            <TableCell>Arrival Time</TableCell>
                                            <TableCell>Status</TableCell>
                                            <TableCell>Due Date</TableCell>
                                            <TableCell>Issue</TableCell>
                                            <TableCell>Actions</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {filteredLeaves.length === 0 ? (
                                            <TableRow>
                                                <TableCell colSpan={9} align="center">
                                                    No unsuccessful leaves found
                                                </TableCell>
                                            </TableRow>
                                        ) : (
                                            filteredLeaves.map((leave) => (
                                                <TableRow key={leave.id}>
                                                    <TableCell padding="checkbox">
                                                        <Checkbox
                                                            checked={selected.includes(leave.id)}
                                                            onChange={() => handleSelect(leave.id)}
                                                            disabled={leave.resolve}
                                                        />
                                                    </TableCell>
                                                    <TableCell>{leave.publicId}</TableCell>
                                                    <TableCell>{leave.employeeID}</TableCell>
                                                    <TableCell>{formatDate(leave.date)}</TableCell>
                                                    <TableCell>{leave.arrivalTime || "-"}</TableCell>
                                                    <TableCell>
                                                        {leave.late && <Chip label="Late" color="warning" size="small" sx={{ mr: 0.5 }} />}
                                                        {leave.absent && <Chip label="Absent" color="error" size="small" sx={{ mr: 0.5 }} />}
                                                        {leave.resolve ? (
                                                            <Chip label="Resolved" color="success" size="small" />
                                                        ) : (
                                                            <Chip label="Unresolved" color="default" size="small" />
                                                        )}
                                                    </TableCell>
                                                    <TableCell>{formatDate(leave.dueDateForUA)}</TableCell>
                                                    <TableCell sx={{ maxWidth: 250, overflow: "hidden", textOverflow: "ellipsis" }}>
                                                        {leave.issueDescription}
                                                    </TableCell>
                                                    <TableCell>
                                                        {!leave.resolve && (
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
                                            ))
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>

                            {/* Pagination */}
                            <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
                                <Pagination
                                    count={leaveData.totalPages || 1}
                                    page={leaveData.number + 1}
                                    onChange={handlePageChange}
                                    color="primary"
                                />
                            </Box>
                        </>
                    )}
                </Box>
            </Container>
        </Box>
    );
};

export default UnsuccessfulLeaves;