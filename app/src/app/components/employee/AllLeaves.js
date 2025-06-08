"use client";

import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
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
    Card,
    CardContent,
    Avatar,
    Tooltip,
} from "@mui/material";
import {
    Delete as DeleteIcon,
    Edit as EditIcon,
    Visibility as VisibilityIcon,
    AccessTime as AccessTimeIcon,
    Save as SaveIcon,
    Close as CloseIcon
} from "@mui/icons-material";
import {
    fetchLeaveData,
    fetchLeaveBalances,
    deleteLeaveRequest,
    fetchInOutData,
    updateLeaveRequest,
} from "../../../../lib/redux/redux-lms/leave/leaveSlice";

const PendingLeaves = ({ isAdmin = false, userAdmin = false ,userId = null }) => {
    const dispatch = useDispatch();

    // Check both possible state locations based on your Redux configuration
    const leaveState = useSelector((state) => {
        // First try the correct path based on isAdmin
        const correctReducer = isAdmin ? state?.leave : state?.leaveNo;

        // If the data exists in the expected place, use it
        if (correctReducer?.data) {
            return correctReducer;
        }

        // Otherwise, try the alternate reducer
        const alternateReducer = isAdmin ? state?.leaveNo : state?.leave;
        if (alternateReducer?.data) {
            return alternateReducer;
        }

        // If no data in either place, return empty structure
        return {
            data: [],
            pagination: {
                totalPages: 0,
                totalElements: 0,
                currentPage: 0,
                pageSize: 10
            },
            loading: false,
            error: null,
            inOutData: null,
            loadingInOutData: false
        };
    });

    const {
        data: leaveRequests,
        pagination,
        loading,
        error,
        inOutData,
        loadingInOutData
    } = leaveState;

    // Try both potential paths for balances
    const balanceState = useSelector((state) => {
        // Try both possible locations for balances
        if (state?.leave?.balances?.data) {
            return state.leave.balances;
        } else if (state?.leaveNo?.balances?.data) {
            return state.leaveNo.balances;
        }

        return {
            data: [],
            loading: false,
            error: null
        };
    });

    const {
        data: leaveBalances,
        loading: fetchingBalance,
        error: balanceError,
    } = balanceState;

    // Local state
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState("All");
    const [typeFilter, setTypeFilter] = useState("All");
    const [startDateFilter, setStartDateFilter] = useState("");
    const [endDateFilter, setEndDateFilter] = useState("");
    const [selected, setSelected] = useState([]);
    const [pageSize, setPageSize] = useState(10);
    const [currentPage, setCurrentPage] = useState(1);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [viewDialogOpen, setViewDialogOpen] = useState(false);
    const [currentLeave, setCurrentLeave] = useState(null);
    const [viewLeaveData, setViewLeaveData] = useState(null);
    const [editFormData, setEditFormData] = useState({
        startDate: "",
        endDate: "",
        type: "",
        comment: "",
        isHalfDay: false,
        isFullDay: false,
        // Fields everyone can update
        isUnauthorized: false,
        isManualRequest: false,
        isAbsent: false,
        isLateCover: false,
        isLate: false,
        isShort_Leave: false,
        notUsed: false,
        // Admin-only fields
        isEdited: false,
        isReject: false,
        isCanceled: false,
        isAccepted: false,
        isPending: false,
    });

    useEffect(() => {
        // Get the userID for fetching data
        const userIdToUse = userId || sessionStorage.getItem("userId");
        if (!userIdToUse) return;

        dispatch(
            fetchLeaveData({
                isAdmin,
                userAdmin,
                userId: userIdToUse,
                page: currentPage - 1,
                size: pageSize,
            })
        );

        dispatch(fetchLeaveBalances(userIdToUse));
    }, [dispatch, isAdmin, userId, currentPage, pageSize]);

    // Guard against undefined or null leaveRequests
    const safeLeaveRequests = Array.isArray(leaveRequests) ? leaveRequests : [];

    // Filtering logic with safety check
    const filteredLeaves = safeLeaveRequests.filter((leave) => {
        const matchesSearchQuery =
            leave.employeeName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            leave.type?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            (leave.comment &&
                leave.comment.toLowerCase().includes(searchQuery.toLowerCase())) ||
            (leave.employeeId &&
                leave.employeeId.toLowerCase().includes(searchQuery.toLowerCase()));

        const matchesStatusFilter =
            statusFilter === "All" || leave.status === statusFilter;

        const matchesTypeFilter =
            typeFilter === "All" || leave.type === typeFilter;

        const leaveStartDate = leave.startDate
            ? new Date(leave.startDate).getTime()
            : 0;
        const leaveEndDate = leave.endDate
            ? new Date(leave.endDate).getTime()
            : 0;
        const filterStartDate = startDateFilter
            ? new Date(startDateFilter).getTime()
            : null;
        const filterEndDate = endDateFilter
            ? new Date(endDateFilter).getTime()
            : null;

        const matchesStartDate = filterStartDate
            ? leaveStartDate >= filterStartDate
            : true;
        const matchesEndDate = filterEndDate
            ? leaveEndDate <= filterEndDate
            : true;

        return (
            matchesSearchQuery &&
            matchesStatusFilter &&
            matchesTypeFilter &&
            matchesStartDate &&
            matchesEndDate
        );
    });

    // Handlers
    const handleSearchChange = (event) => setSearchQuery(event.target.value);
    const handleStatusFilterChange = (event) => setStatusFilter(event.target.value);
    const handleTypeFilterChange = (event) => setTypeFilter(event.target.value);
    const handleStartDateFilterChange = (event) => setStartDateFilter(event.target.value);
    const handleEndDateFilterChange = (event) => setEndDateFilter(event.target.value);
    const handlePageChange = (event, value) => setCurrentPage(value);
    const handlePageSizeChange = (event) => {
        setPageSize(Number(event.target.value));
        setCurrentPage(1);
    };

    const handleSelect = (id) => {
        setSelected((prev) =>
            prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
        );
    };

    const handleSelectAll = () => {
        setSelected((prev) =>
            prev.length === filteredLeaves.length
                ? []
                : filteredLeaves.map((leave) => leave.id)
        );
    };

    const openDeleteDialog = (leave) => {
        setCurrentLeave(leave);
        setDeleteDialogOpen(true);
    };

    // Updated openEditDialog function to properly load all data
    const openEditDialog = (leave) => {
        console.log('Opening edit dialog for leave:', leave);

        setCurrentLeave(leave);
        setEditFormData({
            startDate: leave.startDate || "",
            endDate: leave.endDate || "",
            type: leave.leaveTypeName || leave.type || "",
            comment: leave.comment || "",

            // Day type flags
            isHalfDay: Boolean(leave.halfDay),
            isFullDay: Boolean(leave.fullDay),

            // Regular user fields - load from originalItem to get all backend fields
            isUnauthorized: Boolean(leave.originalItem?.unauthorized),
            isManualRequest: Boolean(leave.manualRequest),
            isAbsent: Boolean(leave.absent),
            isLateCover: Boolean(leave.originalItem?.lateCover),
            isLate: Boolean(leave.late),
            isShort_Leave: Boolean(leave.originalItem?.short_Leave),
            notUsed: Boolean(leave.originalItem?.notUsed),

            // Admin-only fields
            isEdited: Boolean(leave.originalItem?.edited),
            isReject: Boolean(leave.reject),
            isCanceled: Boolean(leave.canceled),
            isAccepted: Boolean(leave.accepted),
            isPending: Boolean(leave.pending),
        });

        setEditDialogOpen(true);
    };

    const handleOpenViewDialog = (leave) => {
        setViewLeaveData(leave);
        setViewDialogOpen(true);

        // Fetch in-out data if start date is available
        if (leave.startDate) {
            dispatch(fetchInOutData({
                userId: userId || sessionStorage.getItem('userId'),
                happenDate: leave.startDate
            }));
        }
    };

    const handleCloseDialogs = () => {
        setEditDialogOpen(false);
        setDeleteDialogOpen(false);
        setViewDialogOpen(false);
        setCurrentLeave(null);
        setViewLeaveData(null);
    };

    const handleEditFormChange = (e) => {
        const { name, value, checked, type } = e.target;

        if (type === 'checkbox') {
            // Handle checkbox logic for half-day and full-day (mutually exclusive)
            if (name === 'isHalfDay' && checked) {
                setEditFormData((prev) => ({
                    ...prev,
                    [name]: checked,
                    isFullDay: false // Uncheck full day if half day is selected
                }));
            } else if (name === 'isFullDay' && checked) {
                setEditFormData((prev) => ({
                    ...prev,
                    [name]: checked,
                    isHalfDay: false // Uncheck half day if full day is selected
                }));
            }
            // Handle admin-only status checkboxes (mutually exclusive)
            else if (['isAccepted', 'isReject', 'isPending', 'isCanceled'].includes(name) && checked) {
                setEditFormData((prev) => ({
                    ...prev,
                    // Reset all status fields
                    isAccepted: false,
                    isReject: false,
                    isPending: false,
                    isCanceled: false,
                    // Set the selected one
                    [name]: checked
                }));
            }
            // Handle other checkboxes (can be multiple or independent)
            else {
                setEditFormData((prev) => ({ ...prev, [name]: checked }));
            }
        } else {
            setEditFormData((prev) => ({ ...prev, [name]: value }));
        }
    };

    const handleDeleteLeaveRequest = async () => {
        if (!currentLeave) return;

        try {
            await dispatch(deleteLeaveRequest(currentLeave.publicId)).unwrap();
            dispatch(
                fetchLeaveData({
                    isAdmin,
                    userId: userId || sessionStorage.getItem("userId"),
                    page: currentPage - 1,
                    size: pageSize,
                })
            );
            handleCloseDialogs();
        } catch (err) {
            console.error("Delete failed:", err);
        }
    };

    const handleDeleteAllSelected = async () => {
        try {
            await Promise.all(
                selected.map((id) => {
                    const leave = safeLeaveRequests.find((l) => l.id === id);
                    return dispatch(deleteLeaveRequest(leave.publicId)).unwrap();
                })
            );
            dispatch(
                fetchLeaveData({
                    isAdmin,
                    userId: userId || sessionStorage.getItem("userId"),
                    page: currentPage - 1,
                    size: pageSize,
                })
            );
            setSelected([]);
        } catch (err) {
            console.error("Delete failed:", err);
        }
    };

    // Updated handleSaveEditedLeave function with proper field mapping
    const handleSaveEditedLeave = async () => {
        if (!currentLeave) return;

        // Calculate number of days based on date range
        const startDate = new Date(editFormData.startDate);
        const endDate = new Date(editFormData.endDate);
        const timeDiff = endDate.getTime() - startDate.getTime();
        const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;

        // Create the update payload with proper field mapping to match backend LeaveReq
        const updatePayload = {
            publicId: currentLeave.publicId,

            // Basic fields
            fromDate: editFormData.startDate,
            toDate: editFormData.endDate,
            leaveType: editFormData.type,
            description: editFormData.comment,
            numOfDays: editFormData.isHalfDay ? 0.5 : daysDiff,
            happenDate: editFormData.startDate,
            userId: currentLeave.originalItem?.userId || currentLeave.userId,
            employeeID: currentLeave.employeeId,
            isNoPay: currentLeave.originalItem?.isNoPay || 0,

            // Boolean fields - using exact backend field names (without 'is' prefix)
            halfDay: editFormData.isHalfDay,
            fullDay: editFormData.isFullDay,
            unauthorized: editFormData.isUnauthorized,
            manualRequest: editFormData.isManualRequest,
            absent: editFormData.isAbsent,
            lateCover: editFormData.isLateCover,
            late: editFormData.isLate,
            short_Leave: editFormData.isShort_Leave,
            notUsed: editFormData.notUsed,
            unSuccessful: currentLeave.originalItem?.unSuccessful || false,
        };

        // Admin-only fields (only include if user is admin)
        if (isAdmin) {
            updatePayload.edited = editFormData.isEdited;
            updatePayload.reject = editFormData.isReject;
            updatePayload.canceled = editFormData.isCanceled;
            updatePayload.accepted = editFormData.isAccepted;
            updatePayload.pending = editFormData.isPending;
        }

        try {
            await dispatch(
                updateLeaveRequest({
                    updatePayload: updatePayload,
                    isAdmin: isAdmin
                })
            ).unwrap();

            // Refresh the data
            dispatch(
                fetchLeaveData({
                    isAdmin,
                    userId: userId || sessionStorage.getItem("userId"),
                    page: currentPage - 1,
                    size: pageSize,
                })
            );
            handleCloseDialogs();
        } catch (err) {
            console.error("Update failed:", err);
            // Show error message to user
            alert(`Update failed: ${err.message || err}`);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return "N/A";
        try {
            const date = new Date(dateString);
            return date.toLocaleString();
        } catch (e) {
            return dateString;
        }
    };

    // Safely extract unique values with fallbacks
    const leaveTypes = [...new Set(safeLeaveRequests.map((leave) => leave.type).filter(Boolean))];
    const statuses = [...new Set(safeLeaveRequests.map((leave) => leave.status).filter(Boolean))];

    // Get safe pagination values
    const safePagination = pagination || {
        totalPages: 0,
        totalElements: 0,
        currentPage: 0,
        pageSize: 10
    };

    // Safely handle leave balances
    const safeLeaveBalances = Array.isArray(leaveBalances) ? leaveBalances : [];

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    {isAdmin ? "All Leave Requests" : "My Leave Requests"}
                </Typography>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                {balanceError && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {balanceError}
                    </Alert>
                )}

                {fetchingBalance ? (
                    <Box sx={{ display: "flex", justifyContent: "center", my: 2 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <Grid container spacing={2} sx={{ mb: 3 }}>
                        {safeLeaveBalances.map((type, index) => (
                            <Grid item xs={6} sm={4} key={type.leaveTypeName || `type-${index}`}>
                                <Card variant="outlined">
                                    <CardContent>
                                        <Typography variant="subtitle1">
                                            {type.leaveTypeName || 'Unknown Type'}
                                        </Typography>
                                        <Typography variant="h6" color="primary">
                                            {type.remainingLeaves || 0} days
                                        </Typography>
                                        <Typography variant="caption" color="textSecondary">
                                            Total: {type.totalLeaves || 0} days
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                )}

                {loading ? (
                    <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <>
                        <TextField
                            label="Search by Employee ID, Name, Leave Type or Comments"
                            variant="outlined"
                            fullWidth
                            value={searchQuery}
                            onChange={handleSearchChange}
                            sx={{ mb: 2 }}
                        />

                        <Box sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}>
                            <FormControl variant="outlined" sx={{ minWidth: 200 }}>
                                <InputLabel>Filter by Status</InputLabel>
                                <Select
                                    value={statusFilter}
                                    onChange={handleStatusFilterChange}
                                    label="Filter by Status"
                                >
                                    <MenuItem value="All">All</MenuItem>
                                    {statuses.map((status) => (
                                        <MenuItem key={status} value={status}>
                                            {status}
                                        </MenuItem>
                                    ))}
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
                                    {leaveTypes.map((type) => (
                                        <MenuItem key={type} value={type}>
                                            {type}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>

                            <TextField
                                label="Start Date"
                                type="date"
                                variant="outlined"
                                value={startDateFilter}
                                onChange={handleStartDateFilterChange}
                                InputLabelProps={{ shrink: true }}
                                sx={{ minWidth: 200 }}
                            />

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

                        <Box
                            sx={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                mb: 2,
                            }}
                        >
                            <Typography variant="body2">
                                Showing {filteredLeaves.length} of{" "}
                                {safePagination.totalElements} total requests
                            </Typography>

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
                                </Select>
                            </FormControl>
                        </Box>

                        <Button
                            variant="contained"
                            color="error"
                            onClick={handleDeleteAllSelected}
                            disabled={
                                selected.length === 0 ||
                                selected.some((id) => {
                                    const leave = safeLeaveRequests.find((l) => l.id === id);
                                    return leave?.accepted;
                                })
                            }
                            sx={{ mb: 2 }}
                        >
                            Delete Selected ({selected.length})
                        </Button>

                        {filteredLeaves.length === 0 ? (
                            <Alert severity="info">No leave requests found.</Alert>
                        ) : (
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell padding="checkbox">
                                                <Checkbox
                                                    indeterminate={
                                                        selected.length > 0 &&
                                                        selected.length < filteredLeaves.length
                                                    }
                                                    checked={
                                                        selected.length === filteredLeaves.length &&
                                                        filteredLeaves.length > 0
                                                    }
                                                    onChange={handleSelectAll}
                                                />
                                            </TableCell>
                                            <TableCell>Employee ID</TableCell>
                                            <TableCell>Leave Type</TableCell>
                                            <TableCell>Start Date</TableCell>
                                            <TableCell>End Date</TableCell>
                                            <TableCell>Status</TableCell>
                                            <TableCell>Actions</TableCell>
                                            <TableCell>View</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {filteredLeaves.map((leave, index) => (
                                            <TableRow key={leave.id || `leave-${index}`}>
                                                <TableCell padding="checkbox">
                                                    <Checkbox
                                                        checked={selected.includes(leave.id)}
                                                        onChange={() => handleSelect(leave.id)}
                                                        disabled={leave.accepted || leave.reject || leave.canceled}
                                                    />
                                                </TableCell>
                                                <TableCell>{leave.employeeId || 'N/A'}</TableCell>
                                                <TableCell>{leave.type || 'N/A'}</TableCell>
                                                <TableCell>{leave.startDate || "N/A"}</TableCell>
                                                <TableCell>{leave.endDate || "N/A"}</TableCell>
                                                <TableCell>{leave.status || 'N/A'}</TableCell>
                                                <TableCell>
                                                    <IconButton
                                                        onClick={() => openEditDialog(leave)}
                                                        disabled={leave.accepted || leave.reject || leave.canceled}
                                                        color="primary"
                                                    >
                                                        <EditIcon />
                                                    </IconButton>
                                                    <IconButton
                                                        onClick={() => openDeleteDialog(leave)}
                                                        disabled={leave.accepted || leave.reject || leave.canceled}
                                                        color="error"
                                                    >
                                                        <DeleteIcon />
                                                    </IconButton>
                                                </TableCell>
                                                <TableCell>
                                                    <Tooltip title="View Details">
                                                        <IconButton
                                                            onClick={() => handleOpenViewDialog(leave)}
                                                            color="info"
                                                        >
                                                            <VisibilityIcon />
                                                        </IconButton>
                                                    </Tooltip>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        )}

                        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
                            <Pagination
                                count={safePagination.totalPages}
                                page={currentPage}
                                onChange={handlePageChange}
                                color="primary"
                                showFirstButton
                                showLastButton
                            />
                        </Box>

                        {/* Enhanced Edit Dialog */}
                        <Dialog
                            open={editDialogOpen}
                            onClose={handleCloseDialogs}
                            maxWidth="sm"
                            fullWidth
                        >
                            <DialogTitle>
                                {isAdmin ? "Edit Leave Request (Admin)" : "Edit Leave Request"}
                            </DialogTitle>
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

                                    {/* Day Type Selection */}
                                    <Box sx={{ mt: 2, mb: 2 }}>
                                        <Typography variant="subtitle2" gutterBottom>
                                            Leave Duration
                                        </Typography>
                                        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isHalfDay"
                                                    checked={editFormData.isHalfDay}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Half Day</Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isFullDay"
                                                    checked={editFormData.isFullDay}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Full Day</Typography>
                                            </Box>
                                        </Box>
                                    </Box>

                                    {/* Leave Type Controls - Available to all users */}
                                    <Box sx={{ mt: 2, mb: 2 }}>
                                        <Typography variant="subtitle2" gutterBottom>
                                            Leave Type Controls
                                        </Typography>
                                        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2 }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isAbsent"
                                                    checked={editFormData.isAbsent}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Absent</Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isLate"
                                                    checked={editFormData.isLate}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Late</Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isLateCover"
                                                    checked={editFormData.isLateCover}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Late Cover</Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isShort_Leave"
                                                    checked={editFormData.isShort_Leave}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Short Leave</Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isUnauthorized"
                                                    checked={editFormData.isUnauthorized}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Unauthorized</Typography>
                                            </Box>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isManualRequest"
                                                    checked={editFormData.isManualRequest}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Manual Request</Typography>
                                            </Box>
                                        </Box>
                                        <Box sx={{ mt: 1 }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="notUsed"
                                                    checked={editFormData.notUsed}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Not Used</Typography>
                                            </Box>
                                        </Box>
                                    </Box>

                                    {/* Admin-only fields */}
                                    {isAdmin && (
                                        <>
                                            <Typography variant="subtitle2" gutterBottom sx={{ mt: 3, color: 'primary.main' }}>
                                                Admin-Only Controls
                                            </Typography>

                                            {/* Status Controls - Admin Only */}
                                            <Box sx={{ mt: 2, mb: 2 }}>
                                                <Typography variant="body2" gutterBottom sx={{ fontWeight: 'bold' }}>
                                                    Status Controls (Admin Only)
                                                </Typography>
                                                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2 }}>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <Checkbox
                                                            name="isAccepted"
                                                            checked={editFormData.isAccepted}
                                                            onChange={handleEditFormChange}
                                                        />
                                                        <Typography variant="body2">Accepted</Typography>
                                                    </Box>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <Checkbox
                                                            name="isReject"
                                                            checked={editFormData.isReject}
                                                            onChange={handleEditFormChange}
                                                        />
                                                        <Typography variant="body2">Rejected</Typography>
                                                    </Box>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <Checkbox
                                                            name="isPending"
                                                            checked={editFormData.isPending}
                                                            onChange={handleEditFormChange}
                                                        />
                                                        <Typography variant="body2">Pending</Typography>
                                                    </Box>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <Checkbox
                                                            name="isCanceled"
                                                            checked={editFormData.isCanceled}
                                                            onChange={handleEditFormChange}
                                                        />
                                                        <Typography variant="body2">Canceled</Typography>
                                                    </Box>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <Checkbox
                                                            name="isEdited"
                                                            checked={editFormData.isEdited}
                                                            onChange={handleEditFormChange}
                                                        />
                                                        <Typography variant="body2">Edited</Typography>
                                                    </Box>
                                                </Box>
                                            </Box>
                                        </>
                                    )}

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

                                    {isAdmin && (
                                        <Alert severity="info" sx={{ mt: 2 }}>
                                            As an admin, you will be prompted to enter a comment when saving changes.
                                        </Alert>
                                    )}
                                </Box>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleCloseDialogs} startIcon={<CloseIcon />}>Cancel</Button>
                                <Button
                                    onClick={handleSaveEditedLeave}
                                    variant="contained"
                                    color="primary"
                                    startIcon={<SaveIcon />}
                                >
                                    Save Changes
                                </Button>
                            </DialogActions>
                        </Dialog>

                        {/* Delete Dialog */}
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
                                <Button
                                    onClick={handleDeleteLeaveRequest}
                                    color="error"
                                    autoFocus
                                >
                                    Delete
                                </Button>
                            </DialogActions>
                        </Dialog>

                        {/* Enhanced View Details Dialog */}
                        <Dialog
                            open={viewDialogOpen}
                            onClose={handleCloseDialogs}
                            maxWidth="md"
                            fullWidth
                        >
                            <DialogTitle>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <VisibilityIcon color="primary" />
                                    <Typography variant="h6">Leave Request Details</Typography>
                                </Box>
                            </DialogTitle>
                            <DialogContent>
                                {viewLeaveData && (
                                    <Box sx={{ pt: 2 }}>
                                        <Typography variant="h6" gutterBottom>
                                            Basic Information
                                        </Typography>
                                        <TableContainer component={Paper} sx={{ mb: 3 }}>
                                            <Table size="small">
                                                <TableBody>
                                                    <TableRow>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Request ID</TableCell>
                                                        <TableCell>{viewLeaveData.publicId || 'N/A'}</TableCell>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Employee ID</TableCell>
                                                        <TableCell>{viewLeaveData.employeeId || 'N/A'}</TableCell>
                                                    </TableRow>
                                                    <TableRow>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Leave Type</TableCell>
                                                        <TableCell>{viewLeaveData.type || 'N/A'}</TableCell>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                                        <TableCell>{viewLeaveData.status || 'N/A'}</TableCell>
                                                    </TableRow>
                                                    <TableRow>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Start Date</TableCell>
                                                        <TableCell>{viewLeaveData.startDate || "Not specified"}</TableCell>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>End Date</TableCell>
                                                        <TableCell>{viewLeaveData.endDate || "Not specified"}</TableCell>
                                                    </TableRow>
                                                    <TableRow>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Comment</TableCell>
                                                        <TableCell colSpan={3}>{viewLeaveData.comment || "No comment"}</TableCell>
                                                    </TableRow>
                                                </TableBody>
                                            </Table>
                                        </TableContainer>

                                        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <AccessTimeIcon color="primary" />
                                            Attendance Records
                                        </Typography>

                                        {loadingInOutData ? (
                                            <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                                                <CircularProgress size={30} />
                                            </Box>
                                        ) : inOutData ? (
                                            <Grid container spacing={3} sx={{ mb: 3 }}>
                                                <Grid item xs={12} md={6}>
                                                    <Card variant="outlined">
                                                        <CardContent>
                                                            <Typography variant="h6" color="primary" gutterBottom>
                                                                Morning Record
                                                            </Typography>
                                                            <TableContainer>
                                                                <Table size="small">
                                                                    <TableBody>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Employee ID</TableCell>
                                                                            <TableCell>{inOutData.morning?.employeeID || "N/A"}</TableCell>
                                                                        </TableRow>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Punch In Time</TableCell>
                                                                            <TableCell>{inOutData.morning?.timeMoa || "Not recorded"}</TableCell>
                                                                        </TableRow>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Punch In (Server Time)</TableCell>
                                                                            <TableCell>{inOutData.morning?.punchInMoa ? formatDate(inOutData.morning.punchInMoa) : "Not recorded"}</TableCell>
                                                                        </TableRow>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                                                            <TableCell>
                                                                                {inOutData.morning?.inOut === 1 ? (
                                                                                    <Typography color="primary">Checked In</Typography>
                                                                                ) : (
                                                                                    <Typography color="error">Not Checked In</Typography>
                                                                                )}
                                                                            </TableCell>
                                                                        </TableRow>
                                                                    </TableBody>
                                                                </Table>
                                                            </TableContainer>
                                                        </CardContent>
                                                    </Card>
                                                </Grid>

                                                <Grid item xs={12} md={6}>
                                                    <Card variant="outlined">
                                                        <CardContent>
                                                            <Typography variant="h6" color="primary" gutterBottom>
                                                                Evening Record
                                                            </Typography>
                                                            <TableContainer>
                                                                <Table size="small">
                                                                    <TableBody>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Employee ID</TableCell>
                                                                            <TableCell>{inOutData.evening?.employeeID || "N/A"}</TableCell>
                                                                        </TableRow>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Punch Out Time</TableCell>
                                                                            <TableCell>{inOutData.evening?.timeEve || "Not recorded"}</TableCell>
                                                                        </TableRow>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Punch Out (Server Time)</TableCell>
                                                                            <TableCell>{inOutData.evening?.punchInEv ? formatDate(inOutData.evening.punchInEv) : "Not recorded"}</TableCell>
                                                                        </TableRow>
                                                                        <TableRow>
                                                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                                                            <TableCell>
                                                                                {inOutData.evening?.inOut === 0 ? (
                                                                                    <Typography color="primary">Checked Out</Typography>
                                                                                ) : (
                                                                                    <Typography color="warning.main">Not Checked Out</Typography>
                                                                                )}
                                                                            </TableCell>
                                                                        </TableRow>
                                                                    </TableBody>
                                                                </Table>
                                                            </TableContainer>
                                                        </CardContent>
                                                    </Card>
                                                </Grid>
                                            </Grid>
                                        ) : (
                                            <Alert severity="info" sx={{ mb: 3 }}>
                                                No attendance records found for this date.
                                            </Alert>
                                        )}

                                        <Typography variant="h6" gutterBottom sx={{ mt: 4 }}>
                                            Approval History
                                        </Typography>
                                        {viewLeaveData.adminsTra && viewLeaveData.adminsTra.length > 0 ? (
                                            <TableContainer component={Paper} sx={{ mb: 4 }}>
                                                <Table>
                                                    <TableHead>
                                                        <TableRow>
                                                            <TableCell>Profile</TableCell>
                                                            <TableCell>Admin</TableCell>
                                                            <TableCell>Employee ID</TableCell>
                                                            <TableCell>Email</TableCell>
                                                            <TableCell>Priority</TableCell>
                                                            <TableCell>Status</TableCell>
                                                        </TableRow>
                                                    </TableHead>
                                                    <TableBody>
                                                        {viewLeaveData.adminsTra.map((admin, index) => (
                                                            <TableRow key={admin.id || index}>
                                                                <TableCell>
                                                                    <Avatar
                                                                        src={admin.profilePicture}
                                                                        alt={`${admin.firstName} ${admin.lastName}`}
                                                                        sx={{ width: 40, height: 40 }}
                                                                    >
                                                                        {!admin.profilePicture && `${admin.firstName?.charAt(0) || ''}${admin.lastName?.charAt(0) || ''}`}
                                                                    </Avatar>
                                                                </TableCell>
                                                                <TableCell>{`${admin.firstName || ''} ${admin.lastName || ''}`.trim() || 'N/A'}</TableCell>
                                                                <TableCell>{admin.employeeId || 'N/A'}</TableCell>
                                                                <TableCell>{admin.email || 'N/A'}</TableCell>
                                                                <TableCell>{admin.highestRolePriority || 'N/A'}</TableCell>
                                                                <TableCell>
                                                                    {admin.accepted ? (
                                                                        <Typography color="primary">Approved</Typography>
                                                                    ) : (
                                                                        <Typography color="error">Pending</Typography>
                                                                    )}
                                                                </TableCell>
                                                            </TableRow>
                                                        ))}
                                                    </TableBody>
                                                </Table>
                                            </TableContainer>
                                        ) : (
                                            <Alert severity="info" sx={{ mb: 4 }}>No approval history available.</Alert>
                                        )}

                                        <Typography variant="h6" gutterBottom sx={{ mt: 4 }}>
                                            Edit History
                                        </Typography>
                                        {viewLeaveData.editedByDTOs && viewLeaveData.editedByDTOs.length > 0 ? (
                                            <TableContainer component={Paper}>
                                                <Table>
                                                    <TableHead>
                                                        <TableRow>
                                                            <TableCell>Profile</TableCell>
                                                            <TableCell>Name</TableCell>
                                                            <TableCell>Employee ID</TableCell>
                                                            <TableCell>Comment</TableCell>
                                                            <TableCell>Edit Date</TableCell>
                                                        </TableRow>
                                                    </TableHead>
                                                    <TableBody>
                                                        {viewLeaveData.editedByDTOs.map((editor, index) => (
                                                            <TableRow key={editor.id || index}>
                                                                <TableCell>
                                                                    <Avatar
                                                                        src={editor.profilePicture}
                                                                        alt={editor.name}
                                                                        sx={{ width: 40, height: 40 }}
                                                                    >
                                                                        {!editor.profilePicture && editor.name?.charAt(0)}
                                                                    </Avatar>
                                                                </TableCell>
                                                                <TableCell>{editor.name || 'N/A'}</TableCell>
                                                                <TableCell>{editor.employeeId || 'N/A'}</TableCell>
                                                                <TableCell>{editor.comment || 'No comment'}</TableCell>
                                                                <TableCell>{formatDate(editor.editDate) || 'N/A'}</TableCell>
                                                            </TableRow>
                                                        ))}
                                                    </TableBody>
                                                </Table>
                                            </TableContainer>
                                        ) : (
                                            <Alert severity="info">No edit history available.</Alert>
                                        )}
                                    </Box>
                                )}
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleCloseDialogs} color="primary">
                                    Close
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