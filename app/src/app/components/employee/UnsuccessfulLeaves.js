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
    Pagination,
    Alert
} from "@mui/material";
import { useSelector, useDispatch } from "react-redux";
import {
    fetchUnsuccessfulLeaves,
    resolveLeave,
    bulkResolveLeaves,
    setCurrentPage,
    setPageSize,
    clearError
} from "../../../../lib/redux/redux-lms/unsuccessful-leaves/unsuccessfulLeavesSlice";

const UnsuccessfulLeaves = ({ isAdmin = false }) => {
    const dispatch = useDispatch();
    const {
        leaves,
        loading,
        error,
        currentPage,
        pageSize
    } = useSelector((state) => state.unsuccessfulLeaves);

    const [searchQuery, setSearchQuery] = useState("");
    const [attendanceTypeFilter, setAttendanceTypeFilter] = useState("All");
    const [leaveStatusFilter, setLeaveStatusFilter] = useState("All");
    const [payStatusFilter, setPayStatusFilter] = useState("All");
    const [resolveFilter, setResolveFilter] = useState("All");
    const [startDateFilter, setStartDateFilter] = useState("");
    const [endDateFilter, setEndDateFilter] = useState("");
    const [selected, setSelected] = useState([]);
    const reduxUser = useSelector((state) => state.auth);

    useEffect(() => {
        const userId = sessionStorage.getItem('userId');
        if (userId) {
            dispatch(fetchUnsuccessfulLeaves({ isAdmin, currentPage, pageSize, userId }));
        }
    }, [currentPage, pageSize, dispatch, isAdmin]);

    const handlePageChange = (event, value) => {
        dispatch(setCurrentPage(value - 1));
    };

    const handlePageSizeChange = (event) => {
        dispatch(setPageSize(event.target.value));
    };

    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value);
    };

    const handleAttendanceTypeFilterChange = (event) => {
        setAttendanceTypeFilter(event.target.value);
    };

    const handleLeaveStatusFilterChange = (event) => {
        setLeaveStatusFilter(event.target.value);
    };

    const handlePayStatusFilterChange = (event) => {
        setPayStatusFilter(event.target.value);
    };

    const handleResolveFilterChange = (event) => {
        setResolveFilter(event.target.value);
    };

    const handleResolveLeave = (id) => {
        dispatch(resolveLeave(id));
    };

    const filteredLeaves = leaves.content?.filter((leave) => {
        const matchesSearchQuery =
            leave.employeeId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.publicId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.issueDescription?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.userId?.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesAttendanceTypeFilter =
            attendanceTypeFilter === "All" ||
            (attendanceTypeFilter === "FULL_DAY" && leave.attendanceType === "FULL_DAY") ||
            (attendanceTypeFilter === "HALF_DAY" && leave.attendanceType === "HALF_DAY") ||
            (attendanceTypeFilter === "ABSENT" && leave.attendanceType === "ABSENT");

        const matchesLeaveStatusFilter =
            leaveStatusFilter === "All" ||
            leave.leaveStatus === leaveStatusFilter;

        const matchesPayStatusFilter =
            payStatusFilter === "All" ||
            (payStatusFilter === "NO_PAY" && leave.payStatus === "NO_PAY") ||
            (payStatusFilter === "PAID" && leave.payStatus !== "NO_PAY");

        const matchesResolveFilter =
            resolveFilter === "All" ||
            (resolveFilter === "Resolved" && leave.resolve !== null) ||
            (resolveFilter === "Unresolved" && leave.resolve === null);

        const matchesStartDateFilter =
            !startDateFilter || new Date(leave.date) >= new Date(startDateFilter);

        const matchesEndDateFilter =
            !endDateFilter || new Date(leave.date) <= new Date(endDateFilter);

        return (
            matchesSearchQuery &&
            matchesAttendanceTypeFilter &&
            matchesLeaveStatusFilter &&
            matchesPayStatusFilter &&
            matchesResolveFilter &&
            matchesStartDateFilter &&
            matchesEndDateFilter
        );
    }) || [];

    const handleSelect = (id) => {
        if (selected.includes(id)) {
            setSelected((prev) => prev.filter((item) => item !== id));
        } else {
            setSelected((prev) => [...prev, id]);
        }
    };

    const handleSelectAll = () => {
        if (selected.length === filteredLeaves.length) {
            setSelected([]);
        } else {
            setSelected(filteredLeaves.map((leave) => leave.id));
        }
    };

    const handleBulkResolve = () => {
        dispatch(bulkResolveLeaves(selected));
        setSelected([]);
    };

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        const date = new Date(dateString);
        return date.toLocaleDateString();
    };

    const formatTime = (timeString) => {
        if (!timeString) return "-";
        return timeString;
    };

    const getAttendanceTypeChip = (attendanceType) => {
        switch (attendanceType) {
            case "FULL_DAY":
                return <Chip label="Full Day" color="success" size="small" />;
            case "HALF_DAY":
                return <Chip label="Half Day" color="warning" size="small" />;
            case "ABSENT":
                return <Chip label="Absent" color="error" size="small" />;
            default:
                return <Chip label="Not Set" color="default" size="small" />;
        }
    };

    const getLeaveStatusChip = (leaveStatus) => {
        switch (leaveStatus) {
            case "NO_LEAVE":
                return <Chip label="No Leave" color="default" size="small" />;
            case "FULL_LEAVE":
                return <Chip label="Full Leave" color="info" size="small" />;
            case "SHORT_LEAVE":
                return <Chip label="Short Leave" color="warning" size="small" />;
            case "LEAVE_REQUESTED":
                return <Chip label="Leave Requested" color="secondary" size="small" />;
            case "LEAVE_APPROVED":
                return <Chip label="Leave Approved" color="success" size="small" />;
            default:
                return null;
        }
    };

    const getPayStatusChip = (payStatus) => {
        if (payStatus === "NO_PAY") {
            return <Chip label="No Pay" color="error" size="small" />;
        }
        return null;
    };

    const getResolveStatusChip = (resolve) => {
        if (resolve === null) {
            return <Chip label="Unresolved" color="default" size="small" />;
        }
        switch (resolve) {
            case "VIA_MOVEMENT":
                return <Chip label="Via Movement" color="success" size="small" />;
            case "VIA_LEAVE":
                return <Chip label="Via Leave" color="success" size="small" />;
            case "EXPIRED":
                return <Chip label="Expired" color="error" size="small" />;
            default:
                return <Chip label="Resolved" color="success" size="small" />;
        }
    };

    useEffect(() => {
        return () => {
            dispatch(clearError());
        };
    }, [dispatch]);

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
                    <Box sx={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        mb: 3
                    }}>
                        <Typography variant="h4">
                            UnSuccessful Leave
                        </Typography>

                        <FormControl variant="outlined" sx={{ minWidth: 150 }}>
                            <InputLabel id="rows-per-page-label">Rows per page</InputLabel>
                            <Select
                                labelId="rows-per-page-label"
                                value={pageSize}
                                onChange={handlePageSizeChange}
                                label="Rows per page"
                                size="small"
                            >
                                <MenuItem value={5}>5</MenuItem>
                                <MenuItem value={10}>10</MenuItem>
                                <MenuItem value={25}>25</MenuItem>
                                <MenuItem value={50}>50</MenuItem>
                                <MenuItem value={100}>100</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
                            {error}
                        </Alert>
                    )}

                    <TextField
                        label="Search by Employee ID, User ID or Issue Description"
                        variant="outlined"
                        fullWidth
                        value={searchQuery}
                        onChange={handleSearchChange}
                        sx={{ mb: 2 }}
                    />

                    <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                        <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                            <InputLabel>Attendance Type</InputLabel>
                            <Select
                                value={attendanceTypeFilter}
                                onChange={handleAttendanceTypeFilterChange}
                                label="Attendance Type"
                            >
                                <MenuItem value="All">All</MenuItem>
                                <MenuItem value="FULL_DAY">Full Day</MenuItem>
                                <MenuItem value="HALF_DAY">Half Day</MenuItem>
                                <MenuItem value="ABSENT">Absent</MenuItem>
                            </Select>
                        </FormControl>

                        <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                            <InputLabel>Leave Status</InputLabel>
                            <Select
                                value={leaveStatusFilter}
                                onChange={handleLeaveStatusFilterChange}
                                label="Leave Status"
                            >
                                <MenuItem value="All">All</MenuItem>
                                <MenuItem value="NO_LEAVE">No Leave</MenuItem>
                                <MenuItem value="FULL_LEAVE">Full Leave</MenuItem>
                                <MenuItem value="SHORT_LEAVE">Short Leave</MenuItem>
                                <MenuItem value="LEAVE_REQUESTED">Leave Requested</MenuItem>
                                <MenuItem value="LEAVE_APPROVED">Leave Approved</MenuItem>
                            </Select>
                        </FormControl>

                        <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                            <InputLabel>Pay Status</InputLabel>
                            <Select
                                value={payStatusFilter}
                                onChange={handlePayStatusFilterChange}
                                label="Pay Status"
                            >
                                <MenuItem value="All">All</MenuItem>
                                <MenuItem value="NO_PAY">No Pay</MenuItem>
                                <MenuItem value="PAID">Paid</MenuItem>
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

                    {!isAdmin && selected.length > 0 && (
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleBulkResolve}
                            sx={{ mb: 2 }}
                        >
                            Resolve All Selected ({selected.length})
                        </Button>
                    )}

                    {loading ? (
                        <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                            <CircularProgress />
                        </Box>
                    ) : (
                        <>
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            {isAdmin && (
                                                <TableCell padding="checkbox">
                                                    <Checkbox
                                                        indeterminate={
                                                            selected.length > 0 && selected.length < filteredLeaves.length
                                                        }
                                                        checked={filteredLeaves.length > 0 && selected.length === filteredLeaves.length}
                                                        onChange={handleSelectAll}
                                                    />
                                                </TableCell>
                                            )}
                                            <TableCell>ID</TableCell>
                                            <TableCell>Employee ID</TableCell>
                                            <TableCell>User ID</TableCell>
                                            <TableCell>Date</TableCell>
                                            <TableCell>Arrival Time</TableCell>
                                            <TableCell>Left Time</TableCell>
                                            <TableCell>Status</TableCell>
                                            <TableCell>Due Date</TableCell>
                                            <TableCell>Issue Description</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {filteredLeaves.length === 0 ? (
                                            <TableRow>
                                                <TableCell colSpan={isAdmin ? 10 : 9} align="center">
                                                    No un-successful leaves found
                                                </TableCell>
                                            </TableRow>
                                        ) : (
                                            filteredLeaves.map((leave) => (
                                                <TableRow key={leave.id}>
                                                    {isAdmin && (
                                                        <TableCell padding="checkbox">
                                                            <Checkbox
                                                                checked={selected.includes(leave.id)}
                                                                onChange={() => handleSelect(leave.id)}
                                                                disabled={leave.resolve !== null}
                                                            />
                                                        </TableCell>
                                                    )}
                                                    <TableCell>{leave.publicId}</TableCell>
                                                    <TableCell>{leave.employeeId}</TableCell>
                                                    <TableCell>{leave.userId}</TableCell>
                                                    <TableCell>{formatDate(leave.date)}</TableCell>
                                                    <TableCell>{formatTime(leave.arrivalTime)}</TableCell>
                                                    <TableCell>{formatTime(leave.leftTime)}</TableCell>
                                                    <TableCell>
                                                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                                            {getAttendanceTypeChip(leave.attendanceType)}
                                                            {getLeaveStatusChip(leave.leaveStatus)}
                                                            {getPayStatusChip(leave.payStatus)}
                                                            {getResolveStatusChip(leave.resolve)}
                                                        </Box>
                                                    </TableCell>
                                                    <TableCell>{formatDate(leave.dueDateForUA)}</TableCell>
                                                    <TableCell sx={{ maxWidth: 250, overflow: "hidden", textOverflow: "ellipsis" }}>
                                                        {leave.issueDescription}
                                                    </TableCell>

                                                </TableRow>
                                            ))
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>

                            <Box sx={{ display: "flex", justifyContent: "flex-end", alignItems: "center", mt: 2 }}>
                                <Typography variant="body2" sx={{ mr: 2 }}>
                                    {leaves.totalElements > 0 ?
                                        `Showing ${currentPage * pageSize + 1} to 
                                        ${Math.min((currentPage + 1) * pageSize, leaves.totalElements)} 
                                        of ${leaves.totalElements} entries` :
                                        'No entries to display'}
                                </Typography>
                                <Pagination
                                    count={leaves.totalPages || 1}
                                    page={currentPage + 1}
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