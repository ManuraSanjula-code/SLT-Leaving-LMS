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
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Checkbox,
    Button,
    CircularProgress,
    Alert,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Pagination,
    Grid,
} from "@mui/material";
import {
    Delete as DeleteIcon,
    Edit as EditIcon
} from "@mui/icons-material";

const PendingLeaves = () => {
    const [leaveRequests, setLeaveRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState("All");
    const [typeFilter, setTypeFilter] = useState("All");
    const [startDateFilter, setStartDateFilter] = useState("");
    const [endDateFilter, setEndDateFilter] = useState("");
    const [selected, setSelected] = useState([]);
    const [pagination, setPagination] = useState({
        totalPages: 0,
        totalElements: 0,
        currentPage: 0,
        pageSize: 10
    });

    // Add state for page size selection
    const [pageSize, setPageSize] = useState(10);
    const [currentPage, setCurrentPage] = useState(1);

    // Edit and delete dialog states
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [currentLeave, setCurrentLeave] = useState(null);
    const [editFormData, setEditFormData] = useState({
        startDate: "",
        endDate: "",
        type: "",
        comment: ""
    });

    // Fetch leave data from the server
    useEffect(() => {
        fetchLeaveData();
    }, [currentPage, pageSize]); // Add currentPage and pageSize as dependencies

    const fetchLeaveData = async () => {
        try {
            setLoading(true);

            // Get userId from sessionStorage
            const userId = sessionStorage.getItem('userId');

            if (!userId) {
                throw new Error("User ID not found in sessionStorage");
            }

            // Update the URL to include page and size parameters
            const response = await fetch(
                `http://localhost:8080/lms/leave/${userId}?page=${currentPage - 1}&size=${pageSize}`,
                {
                    credentials: 'include'
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();

            // Transform the data to match our leave component structure
            const transformedData = data.content.map(item => ({
                id: item.id,
                publicId: item.publicId,
                employeeId: item.employeeID,
                employeeName: `Employee ${item.employeeID.substring(0, 5)}`,
                type: getLeaveType(item),
                startDate: item.fromDate ? new Date(item.fromDate).toISOString().split('T')[0] : "",
                endDate: item.toDate ? new Date(item.toDate).toISOString().split('T')[0] : "",
                status: getLeaveStatus(item),
                comment: item.description,
                category: item.leaveType?.name || "",
                leaveTypeName: item.leaveType?.name || "", // Added this to store the original leave type name
                late: item.late,
                absent: !item.late && !item.fullDay && !item.halfDay,
                fullDay: item.fullDay,
                halfDay: item.halfDay,
                pending: item.pending,
                accepted: item.accepted,
                expired: false
            }));

            setLeaveRequests(transformedData);
            setPagination({
                totalPages: data.totalPages,
                totalElements: data.totalElements,
                currentPage: data.number,
                pageSize: data.pageable.pageSize
            });

            setError(null);
        } catch (err) {
            console.error("Error fetching leave data:", err);
            setError(err.message);
            setLeaveRequests([]);
        } finally {
            setLoading(false);
        }
    };

    // Helper function to determine leave type based on item properties
    const getLeaveType = (item) => {
        if (item.fullDay) return "Full Day Leave";
        if (item.halfDay) return "Half Day Leave";
        if (item.absent) return "Absence";
        if (item.late) return "Late Arrival";
        return item.leaveType?.name || "Regular Leave";
    };

    // Helper function to determine status based on item properties
    const getLeaveStatus = (item) => {
        if (item.pending) return "Pending";
        if (item.accepted) return "Approved";
        if (item.canceled) return "Canceled";
        if (item.late && !item.pending && !item.accepted) return "Recorded Late";
        if (!item.fullDay && !item.halfDay && !item.late && !item.pending && !item.accepted) return "Recorded Absent";
        return "Processed";
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

    // Handle start date filter change
    const handleStartDateFilterChange = (event) => {
        setStartDateFilter(event.target.value);
    };

    // Handle end date filter change
    const handleEndDateFilterChange = (event) => {
        setEndDateFilter(event.target.value);
    };

    // Handle page size change
    const handlePageSizeChange = (event) => {
        const newPageSize = parseInt(event.target.value);
        setPageSize(newPageSize);
        setCurrentPage(1); // Reset to first page when changing page size
    };

    // Handle page change
    const handlePageChange = (event, value) => {
        setCurrentPage(value);
    };

    // Filter leave requests based on search query, filters, and date range
    const filteredLeaves = leaveRequests.filter((leave) => {
        const matchesSearchQuery =
            leave.employeeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
            (leave.comment && leave.comment.toLowerCase().includes(searchQuery.toLowerCase())) ||
            (leave.employeeId && leave.employeeId.toLowerCase().includes(searchQuery.toLowerCase()));

        const matchesStatusFilter =
            statusFilter === "All" || leave.status === statusFilter;

        const matchesTypeFilter =
            typeFilter === "All" || leave.type === typeFilter;

        // Convert dates to timestamps for comparison
        const leaveStartDate = leave.startDate ? new Date(leave.startDate).getTime() : 0;
        const leaveEndDate = leave.endDate ? new Date(leave.endDate).getTime() : 0;
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

    // Open delete confirmation dialog
    const openDeleteDialog = (leave) => {
        setCurrentLeave(leave);
        setDeleteDialogOpen(true);
    };

    // Open edit dialog
    const openEditDialog = (leave) => {
        setCurrentLeave(leave);
        // Fix: Use the correct property for the leave type
        setEditFormData({
            startDate: leave.startDate || "",
            endDate: leave.endDate || "",
            type: leave.leaveTypeName || "", // Use leaveTypeName instead of category
            comment: leave.comment || ""
        });
        setEditDialogOpen(true);
    };

    // Handle close dialogs
    const handleCloseDialogs = () => {
        setEditDialogOpen(false);
        setDeleteDialogOpen(false);
        setCurrentLeave(null);
    };

    // Handle form input changes for edit
    const handleEditFormChange = (e) => {
        const { name, value } = e.target;
        setEditFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // Handle delete individual leave request
    const handleDeleteLeaveRequest = async () => {
        if (!currentLeave) return;

        try {
            const response = await fetch(`http://localhost:8080/lms/leave/${currentLeave.publicId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            // Remove from UI
            setLeaveRequests((prev) => prev.filter((leave) => leave.id !== currentLeave.id));
            // Close dialog
            handleCloseDialogs();
        } catch (err) {
            console.error("Error deleting leave request:", err);
            setError(`Failed to delete leave request: ${err.message}`);
        }
    };

    // Handle delete all selected leave requests
    const handleDeleteAllSelected = async () => {
        try {
            // In a real application, you might want to batch delete or send multiple requests
            // For now, let's handle them one by one
            const deletePromises = selected.map(id => {
                const leave = leaveRequests.find(l => l.id === id);
                if (!leave) return Promise.resolve();

                return fetch(`http://localhost:8080/lms/leave/${leave.publicId}`, {
                    method: 'DELETE',
                    credentials: 'include',
                });
            });

            await Promise.all(deletePromises);

            // Update UI
            setLeaveRequests((prev) => prev.filter((leave) => !selected.includes(leave.id)));
            setSelected([]); // Clear selection after deletion
        } catch (err) {
            console.error("Error deleting selected leave requests:", err);
            setError(`Failed to delete selected leave requests: ${err.message}`);
        }
    };

    // Handle save edited leave request
    const handleSaveEditedLeave = async () => {
        if (!currentLeave) return;

        try {
            // Construct the updated leave object to match backend expectations
            const updatedLeave = {
                id: currentLeave.id,
                publicId: currentLeave.publicId,
                employeeID: currentLeave.employeeId,
                fromDate: editFormData.startDate,
                toDate: editFormData.endDate,
                description: editFormData.comment,
                leaveType: editFormData.type,
                fullDay: currentLeave.fullDay,
                halfDay: currentLeave.halfDay,
                late: currentLeave.late,
                pending: currentLeave.pending,
                accepted: currentLeave.accepted,
                canceled: currentLeave.canceled
            };

            const response = await fetch(`http://localhost:8080/lms/management/leave/${currentLeave.publicId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updatedLeave)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            // Update the item in UI
            setLeaveRequests(prev =>
                prev.map(leave => {
                    if (leave.id === currentLeave.id) {
                        return {
                            ...leave,
                            startDate: editFormData.startDate,
                            endDate: editFormData.endDate,
                            comment: editFormData.comment,
                            leaveTypeName: editFormData.type, // Update leaveTypeName
                            category: editFormData.type,
                            type: getLeaveTypeFromName(editFormData.type, leave) // Update type based on the new name
                        };
                    }
                    return leave;
                })
            );

            handleCloseDialogs();
        } catch (err) {
            console.error("Error updating leave request:", err);
            setError(`Failed to update leave request: ${err.message}`);
        }
    };

    // Helper function to determine leave type based on name and leave properties
    const getLeaveTypeFromName = (typeName, leave) => {
        if (leave.fullDay) return "Full Day Leave";
        if (leave.halfDay) return "Half Day Leave";
        if (leave.absent) return "Absence";
        if (leave.late) return "Late Arrival";
        return typeName || "Regular Leave";
    };

    // Get unique leave types for filter dropdown
    const leaveTypes = [...new Set(leaveRequests.map(leave => leave.type).filter(Boolean))];

    // Get unique statuses for filter dropdown
    const statuses = [...new Set(leaveRequests.map(leave => leave.status).filter(Boolean))];

    // Get available leave type names from the server data
    const availableLeaveTypes = [...new Set(leaveRequests
        .map(leave => leave.leaveTypeName)
        .filter(Boolean))];

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    All Leave Requests
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
                        {/* Search Bar */}
                        <TextField
                            label="Search by Employee ID, Name, Leave Type or Comments"
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
                                    {statuses.map(status => (
                                        <MenuItem key={status} value={status}>{status}</MenuItem>
                                    ))}
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
                                    {leaveTypes.map(type => (
                                        <MenuItem key={type} value={type}>{type}</MenuItem>
                                    ))}
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

                        {/* Page Size and Record Count Display */}
                        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
                            <Typography variant="body2">
                                Showing {filteredLeaves.length} of {pagination.totalElements} total leave requests
                            </Typography>

                            {/* Page Size Selector */}
                            <FormControl variant="outlined" sx={{ minWidth: 120 }}>
                                <InputLabel>Records per page</InputLabel>
                                <Select
                                    value={pageSize}
                                    onChange={handlePageSizeChange}
                                    label="Records per page"
                                >
                                    <MenuItem value={5}>5</MenuItem>
                                    <MenuItem value={10}>10</MenuItem>
                                    <MenuItem value={20}>20</MenuItem>
                                    <MenuItem value={50}>50</MenuItem>
                                    <MenuItem value={100}>100</MenuItem>
                                </Select>
                            </FormControl>
                        </Box>

                        {/* Delete All Selected Button */}
                        <Button
                            variant="contained"
                            color="error"
                            onClick={handleDeleteAllSelected}
                            disabled={selected.length === 0 || selected.some(id => {
                                const leave = leaveRequests.find(l => l.id === id);
                                return leave && leave.accepted;
                            })}
                            sx={{ mb: 2 }}
                        >
                            Delete Selected ({selected.length})
                        </Button>

                        {/* Table */}
                        {leaveRequests.length === 0 ? (
                            <Alert severity="info">No leave requests found.</Alert>
                        ) : (
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell padding="checkbox">
                                                <Checkbox
                                                    indeterminate={
                                                        selected.length > 0 && selected.length < filteredLeaves.length
                                                    }
                                                    checked={selected.length === filteredLeaves.length && filteredLeaves.length > 0}
                                                    onChange={handleSelectAll}
                                                />
                                            </TableCell>
                                            <TableCell>Employee ID</TableCell>
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
                                                        disabled={leave.accepted}
                                                    />
                                                </TableCell>
                                                <TableCell>{leave.employeeId}</TableCell>
                                                <TableCell>{leave.type}</TableCell>
                                                <TableCell>{leave.startDate || "N/A"}</TableCell>
                                                <TableCell>{leave.endDate || "N/A"}</TableCell>
                                                <TableCell>{leave.status}</TableCell>
                                                <TableCell>
                                                    <IconButton
                                                        onClick={() => openEditDialog(leave)}
                                                        disabled={leave.accepted}
                                                        color="primary"
                                                    >
                                                        <EditIcon />
                                                    </IconButton>
                                                    <IconButton
                                                        onClick={() => openDeleteDialog(leave)}
                                                        disabled={leave.accepted}
                                                        color="error"
                                                    >
                                                        <DeleteIcon />
                                                    </IconButton>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        )}

                        {/* Pagination Controls */}
                        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
                            <Pagination
                                count={pagination.totalPages}
                                page={currentPage}
                                onChange={handlePageChange}
                                color="primary"
                                showFirstButton
                                showLastButton
                            />
                        </Box>

                        {/* Edit Dialog */}
                        <Dialog open={editDialogOpen} onClose={handleCloseDialogs} maxWidth="sm" fullWidth>
                            <DialogTitle>Edit Leave Request</DialogTitle>
                            <DialogContent>
                                <Box sx={{ mt: 2 }}>
                                    <TextField
                                        label="Start Date"
                                        type="date"
                                        name="startDate"
                                        value={editFormData.startDate}
                                        onChange={handleEditFormChange}
                                        fullWidth
                                        margin="normal"
                                        InputLabelProps={{ shrink: true }}
                                    />
                                    <TextField
                                        label="End Date"
                                        type="date"
                                        name="endDate"
                                        value={editFormData.endDate}
                                        onChange={handleEditFormChange}
                                        fullWidth
                                        margin="normal"
                                        InputLabelProps={{ shrink: true }}
                                    />
                                    <FormControl fullWidth margin="normal">
                                        <InputLabel>Leave Type</InputLabel>
                                        <Select
                                            name="type"
                                            value={editFormData.type}
                                            onChange={handleEditFormChange}
                                            label="Leave Type"
                                        >
                                            <MenuItem value="Annual Leave">Annual Leave</MenuItem>
                                            <MenuItem value="Medical Leave">Medical Leave</MenuItem>
                                            <MenuItem value="Sick Leave">Sick Leave</MenuItem>
                                            <MenuItem value="Casual Leave">Casual Leave</MenuItem>
                                        </Select>
                                    </FormControl>
                                    <TextField
                                        label="Comment"
                                        name="comment"
                                        value={editFormData.comment}
                                        onChange={handleEditFormChange}
                                        fullWidth
                                        margin="normal"
                                        multiline
                                        rows={3}
                                    />
                                </Box>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleCloseDialogs}>Cancel</Button>
                                <Button onClick={handleSaveEditedLeave} variant="contained" color="primary">
                                    Save Changes
                                </Button>
                            </DialogActions>
                        </Dialog>

                        {/* Delete Confirmation Dialog */}
                        <Dialog
                            open={deleteDialogOpen}
                            onClose={handleCloseDialogs}
                        >
                            <DialogTitle>Confirm Delete</DialogTitle>
                            <DialogContent>
                                <DialogContentText>
                                    Are you sure you want to delete this leave request?
                                    This action cannot be undone.
                                </DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleCloseDialogs}>Cancel</Button>
                                <Button onClick={handleDeleteLeaveRequest} color="error" autoFocus>
                                    Delete
                                </Button>
                            </DialogActions>
                        </Dialog>
                    </>
                )}
            </Box>
        </Container>
    );
};

export default PendingLeaves;