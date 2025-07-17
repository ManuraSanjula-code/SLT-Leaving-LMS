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

const COMPONENT_BEHAVIORS = {
    HALF_DAY: "Half Day",
    FULL_DAY: "Full Day",
    UNSUCCESSFUL: "Unsuccessful",
    UNAUTHORIZED: "Unauthorized",
    ABSENT: "Absent"
};

const REQUEST_STATUSES = {
    DRAFT: "Draft",
    SUBMITTED: "Submitted",
    PENDING_APPROVAL: "Pending Approval",
    APPROVED: "Approved",
    REJECTED: "Rejected",
    CANCELLED: "Cancelled",
    EXPIRED: "Expired"
};

const PendingLeaves = ({ isAdmin = false, userAdmin = false, userId = null }) => {
    const dispatch = useDispatch();

    const leaveState = useSelector((state) => {
        const correctReducer = isAdmin ? state?.leave : state?.leaveNo;

        if (correctReducer?.data) {
            return correctReducer;
        }

        const alternateReducer = isAdmin ? state?.leaveNo : state?.leave;
        if (alternateReducer?.data) {
            return alternateReducer;
        }

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

    const balanceState = useSelector((state) => {
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
        componentBehavior: "FULL_DAY",
        requestStatus: "PENDING_APPROVAL",
        isManualRequest: false,
        notUsed: false,
    });

    useEffect(() => {
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
    }, [dispatch, isAdmin, userId, currentPage, pageSize, userAdmin]);

    const safeLeaveRequests = Array.isArray(leaveRequests) ? leaveRequests : [];

    const filteredLeaves = safeLeaveRequests.filter((leave) => {
        const unwantedBehaviors = ['LATE', 'LATE_COVER', 'SHORT_LEAVE'];
        if (unwantedBehaviors.includes(leave.originalItem?.componentBehavior)) {
            return false;
        }

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

    const hasNonEditableLeaves = filteredLeaves.some(leave =>
        leave.accepted || leave.reject || leave.canceled || leave.expired
    );

    const isLeaveEditable = (leave) => {
        return !(leave.accepted || leave.reject || leave.canceled || leave.expired || leave.requestStatus == "SUBMITTED");
    };

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

    const openEditDialog = (leave) => {
        setCurrentLeave(leave);

        const validComponentBehavior = Object.keys(COMPONENT_BEHAVIORS).includes(leave.originalItem?.componentBehavior)
            ? leave.originalItem.componentBehavior
            : "FULL_DAY";

        const validRequestStatus = Object.keys(REQUEST_STATUSES).includes(leave.originalItem?.requestStatus)
            ? leave.originalItem.requestStatus
            : "PENDING_APPROVAL";

        setEditFormData({
            startDate: leave.startDate || "",
            endDate: leave.endDate || "",
            type: leave.leaveTypeName || leave.type || "",
            comment: leave.comment || "",
            componentBehavior: validComponentBehavior,
            requestStatus: validRequestStatus,
            isManualRequest: Boolean(leave.manualRequest),
            notUsed: Boolean(leave.originalItem?.notUsed),
        });

        setEditDialogOpen(true);
    };

    const handleOpenViewDialog = (leave) => {
        setViewLeaveData(leave);
        setViewDialogOpen(true);

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
            setEditFormData((prev) => ({ ...prev, [name]: checked }));
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
                    userAdmin,
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

    const handleSaveEditedLeave = async () => {
        if (!currentLeave) return;

        // Calculate number of days based on date range and component behavior
        const startDate = new Date(editFormData.startDate);
        const endDate = new Date(editFormData.endDate);
        const timeDiff = endDate.getTime() - startDate.getTime();
        const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;

        let numOfDays;
        switch (editFormData.componentBehavior) {
            case 'HALF_DAY':
                numOfDays = 1;
                break;
            default:
                numOfDays = daysDiff * 2;
                break;
        }

        const updatePayload = {
            publicId: currentLeave.publicId,
            fromDate: editFormData.startDate,
            toDate: editFormData.endDate,
            leaveType: editFormData.type,
            description: editFormData.comment,
            numOfDays: numOfDays,
            happenDate: editFormData.startDate,
            userId: currentLeave.originalItem?.userId || currentLeave.userId,
            employeeID: currentLeave.employeeId,

            componentBehavior: editFormData.componentBehavior,
            requestStatus: editFormData.requestStatus,

            notUsed: editFormData.notUsed,
            isManualRequest: editFormData.isManualRequest,
            isEdited: true,
        };

        try {
            await dispatch(
                updateLeaveRequest({
                    updatePayload: updatePayload,
                    userAdmin,
                    isAdmin: isAdmin
                })
            ).unwrap();

            dispatch(
                fetchLeaveData({
                    isAdmin,
                    userId: userId || sessionStorage.getItem("userId"),
                    userAdmin,
                    page: currentPage - 1,
                    size: pageSize,
                })
            );
            handleCloseDialogs();
        } catch (err) {
            console.error("Update failed:", err);
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

    const leaveTypes = [...new Set(safeLeaveRequests.map((leave) => leave.type).filter(Boolean))];
    const statuses = [...new Set(safeLeaveRequests.map((leave) => leave.status).filter(Boolean))];

    // Get safe pagination values
    const safePagination = pagination || {
        totalPages: 0,
        totalElements: 0,
        currentPage: 0,
        pageSize: 10
    };

    const safeLeaveBalances = Array.isArray(leaveBalances) ? leaveBalances : [];

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    {isAdmin || userAdmin ? "All Leave Requests" : "My Leave Requests"}
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
                                hasNonEditableLeaves ||
                                selected.some((id) => {
                                    const leave = safeLeaveRequests.find((l) => l.id === id);
                                    return !isLeaveEditable(leave);
                                })
                            }
                            sx={{ mb: 2 }}
                        >
                            Delete Selected ({selected.length})
                            {hasNonEditableLeaves && " - Some items cannot be deleted"}
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
                                                    disabled={hasNonEditableLeaves}
                                                />
                                            </TableCell>
                                            <TableCell>Employee ID</TableCell>
                                            <TableCell>Leave Type</TableCell>
                                            <TableCell>Start Date</TableCell>
                                            <TableCell>End Date</TableCell>
                                            <TableCell>Status</TableCell>
                                            <TableCell>Component Behavior</TableCell>
                                            <TableCell>Actions</TableCell>
                                            <TableCell>View</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {filteredLeaves.map((leave, index) => {
                                            const isEditable = isLeaveEditable(leave);
                                            return (
                                                <TableRow
                                                    key={leave.id || `leave-${index}`}
                                                    sx={{
                                                        backgroundColor: leave.expired ? '#ffebee' : 'inherit', // Light red background for expired
                                                        opacity: leave.expired ? 0.7 : 1 // Slightly faded for expired
                                                    }}
                                                >
                                                    <TableCell padding="checkbox">
                                                        <Checkbox
                                                            checked={selected.includes(leave.id)}
                                                            onChange={() => handleSelect(leave.id)}
                                                            disabled={!isEditable}
                                                        />
                                                    </TableCell>
                                                    <TableCell>{leave.employeeId || 'N/A'}</TableCell>
                                                    <TableCell>{leave.leaveTypeName || 'N/A'}</TableCell>
                                                    <TableCell>{leave.startDate || "N/A"}</TableCell>
                                                    <TableCell>{leave.endDate || "N/A"}</TableCell>
                                                    <TableCell>
                                                        <Typography
                                                            color={leave.expired ? 'error' : 'inherit'}
                                                            sx={{
                                                                fontWeight: leave.expired ? 'bold' : 'normal',
                                                                display: 'flex',
                                                                alignItems: 'center',
                                                                gap: 1
                                                            }}
                                                        >
                                                            {leave.status || 'N/A'}
                                                            {leave.expired && <Tooltip title="This leave request has expired and cannot be modified"><span>⏰</span></Tooltip>}
                                                        </Typography>
                                                    </TableCell>
                                                    <TableCell>
                                                        {COMPONENT_BEHAVIORS[leave.originalItem?.componentBehavior] || leave.originalItem?.componentBehavior || 'N/A'}
                                                    </TableCell>
                                                    <TableCell>
                                                        <Tooltip title={!isEditable ? "This leave cannot be edited" : ""}>
                                                            <span>
                                                                <IconButton
                                                                    onClick={() => openEditDialog(leave)}
                                                                    disabled={!isEditable}
                                                                    color="primary"
                                                                    sx={{
                                                                        opacity: !isEditable ? 0.3 : 1
                                                                    }}
                                                                >
                                                                    <EditIcon />
                                                                </IconButton>
                                                                <IconButton
                                                                    onClick={() => openDeleteDialog(leave)}
                                                                    disabled={!isEditable}
                                                                    color="error"
                                                                    sx={{
                                                                        opacity: !isEditable ? 0.3 : 1
                                                                    }}
                                                                >
                                                                    <DeleteIcon />
                                                                </IconButton>
                                                            </span>
                                                        </Tooltip>
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
                                            )
                                        })}
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

                        <Dialog
                            open={editDialogOpen}
                            onClose={handleCloseDialogs}
                            maxWidth="md"
                            fullWidth
                        >
                            <DialogTitle>
                                {isAdmin || userAdmin ? "Edit Leave Request (Admin)" : "Edit Leave Request"}
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

                                    {/* Component Behavior Selection - Only showing allowed options */}
                                    <FormControl fullWidth margin="normal">
                                        <InputLabel>Component Behavior</InputLabel>
                                        <Select
                                            name="componentBehavior"
                                            value={editFormData.componentBehavior}
                                            onChange={handleEditFormChange}
                                            label="Component Behavior"
                                        >
                                            {Object.entries(COMPONENT_BEHAVIORS).map(([key, value]) => (
                                                <MenuItem key={key} value={key}>
                                                    {value}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>

                                    {(isAdmin || userAdmin) && (
                                        <FormControl fullWidth margin="normal">
                                            <InputLabel>Request Status</InputLabel>
                                            <Select
                                                name="requestStatus"
                                                value={editFormData.requestStatus}
                                                onChange={handleEditFormChange}
                                                label="Request Status"
                                            >
                                                {Object.entries(REQUEST_STATUSES).map(([key, value]) => (
                                                    <MenuItem key={key} value={key}>
                                                        {value}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                    )}

                                    <Box sx={{ mt: 2, mb: 2 }}>
                                        <Typography variant="subtitle2" gutterBottom>
                                            Additional Options
                                        </Typography>
                                        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <Checkbox
                                                    name="isManualRequest"
                                                    checked={editFormData.isManualRequest}
                                                    onChange={handleEditFormChange}
                                                />
                                                <Typography variant="body2">Manual Request</Typography>
                                            </Box>
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

                                    {(isAdmin || userAdmin) && (
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
                                                        <TableCell>{viewLeaveData.leaveTypeName || 'N/A'}</TableCell>
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
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Component Behavior</TableCell>
                                                        <TableCell>{COMPONENT_BEHAVIORS[viewLeaveData.originalItem?.componentBehavior] || 'N/A'}</TableCell>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Request Status</TableCell>
                                                        <TableCell>{REQUEST_STATUSES[viewLeaveData.originalItem?.requestStatus] || 'N/A'}</TableCell>
                                                    </TableRow>
                                                    <TableRow>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Days</TableCell>
                                                        <TableCell>{viewLeaveData.actualDays || viewLeaveData.numOfDays || 'N/A'}</TableCell>
                                                        <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Manual Request</TableCell>
                                                        <TableCell>{viewLeaveData.manualRequest ? 'Yes' : 'No'}</TableCell>
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