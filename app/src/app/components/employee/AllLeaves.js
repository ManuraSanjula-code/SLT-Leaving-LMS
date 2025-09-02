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
    Snackbar,
    Chip,
    Divider,
} from "@mui/material";
import {
    Delete as DeleteIcon,
    Edit as EditIcon,
    Visibility as VisibilityIcon,
    AccessTime as AccessTimeIcon,
    Save as SaveIcon,
    Close as CloseIcon,
    Error as ErrorIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon,
    Pending as PendingIcon,
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
    ABSENT: "Absent",
};

const REQUEST_STATUSES = {
    DRAFT: "Draft",
    SUBMITTED: "Submitted",
    PENDING_APPROVAL: "Pending Approval",
    APPROVED: "Approved",
    REJECTED: "Rejected",
    CANCELLED: "Cancelled",
    EXPIRED: "Expired",
};

const PendingLeaves = ({ isAdmin = false, userAdmin = false, userId = null }) => {
    const dispatch = useDispatch();
    const leaveState = useSelector((state) => state.leaveNo);
    const {
        data: leaveRequests = [],
        pagination = {
            totalPages: 0,
            totalElements: 0,
            currentPage: 0,
            pageSize: 10,
        },
        loading,
        error,
        inOutData,
        loadingInOutData,
    } = leaveState;

    const balanceState = useSelector((state) => state.leaveNo.balances);
    const { data: leaveBalances = [], loading: fetchingBalance } = balanceState;

    const [serverError, setServerError] = useState(null);
    const [operationError, setOperationError] = useState(null);
    const [showErrorSnackbar, setShowErrorSnackbar] = useState(false);
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
        numOfDays: 0,
        adminComment: "",
    });

    const calculateBusinessDays = (startDate, endDate) => {
        const start = new Date(startDate);
        const end = new Date(endDate);
        start.setHours(0, 0, 0, 0);
        end.setHours(0, 0, 0, 0);

        let count = 0;
        const current = new Date(start);

        while (current <= end) {
            const day = current.getDay();
            if (day !== 0 && day !== 6) { // Skip weekends (0=Sunday, 6=Saturday)
                count++;
            }
            current.setDate(current.getDate() + 1);
        }

        return count;
    };

    useEffect(() => {
        const userIdToUse = userId || sessionStorage.getItem("userId");
        if (!userIdToUse) return;

        const fetchData = async () => {
            try {
                await dispatch(
                    fetchLeaveData({
                        isAdmin,
                        userAdmin,
                        userId: userIdToUse,
                        page: currentPage - 1,
                        size: pageSize,
                    })
                ).unwrap();
                await dispatch(fetchLeaveBalances(userIdToUse)).unwrap();
            } catch (err) {
                setOperationError(`Failed to fetch data: ${err.message}`);
                setShowErrorSnackbar(true);
            }
        };

        fetchData();
    }, [dispatch, isAdmin, userId, currentPage, pageSize, userAdmin]);

    const handleEditFormChange = (e) => {
        const { name, value, checked, type } = e.target;
        setEditFormData((prev) => {
            const newData = {
                ...prev,
                [name]: type === "checkbox" ? checked : value,
            };

            // Recalculate days when dates or component behavior changes
            if (
                name === "startDate" ||
                name === "endDate" ||
                name === "componentBehavior"
            ) {
                if (newData.startDate && newData.endDate) {
                    const diffDays = calculateBusinessDays(
                        newData.startDate,
                        newData.endDate
                    );
                    newData.numOfDays =
                        newData.componentBehavior === "HALF_DAY" ? 0.5 : diffDays;
                    if (name === "startDate" && newData.componentBehavior === "HALF_DAY") {
                        newData.endDate = newData.startDate;
                    }
                }
            }

            return newData;
        });
    };

    const handleSaveEditedLeave = async () => {
        if (!currentLeave) return;

        try {
            const diffDays = calculateBusinessDays(
                editFormData.startDate,
                editFormData.endDate
            );

            const updatePayload = {
                publicId: currentLeave.publicId,
                fromDate: editFormData.startDate,
                toDate: editFormData.endDate,
                leaveType: editFormData.type,
                description: editFormData.comment,
                numOfDays: editFormData.componentBehavior === "HALF_DAY" ? 0.5 : diffDays,
                happenDate: editFormData.startDate,
                userId: currentLeave.originalItem?.userId || currentLeave.userId,
                employeeID: currentLeave.employeeId,
                componentBehavior: editFormData.componentBehavior,
                requestStatus: editFormData.requestStatus,
                notUsed: editFormData.notUsed,
                isManualRequest: editFormData.isManualRequest,
                isEdited: true,
                adminComment: editFormData.adminComment,
            };

            await dispatch(
                updateLeaveRequest({
                    updatePayload,
                    userAdmin,
                    isAdmin,
                })
            ).unwrap();

            await dispatch(
                fetchLeaveData({
                    isAdmin,
                    userId: userId || sessionStorage.getItem("userId"),
                    userAdmin,
                    page: currentPage - 1,
                    size: pageSize,
                })
            ).unwrap();

            setEditDialogOpen(false);
        } catch (err) {
            setOperationError(`Failed to update leave: ${err || err.message}`);
            setShowErrorSnackbar(true);
        }
    };

    const openEditDialog = (leave) => {
        setCurrentLeave(leave);
        setEditFormData({
            startDate: leave.startDate || "",
            endDate: leave.endDate || "",
            type: leave.leaveTypeName || leave.type || "",
            comment: leave.comment || "",
            componentBehavior: leave.originalItem?.componentBehavior || "FULL_DAY",
            requestStatus: leave.originalItem?.requestStatus || "PENDING_APPROVAL",
            isManualRequest: Boolean(leave.manualRequest),
            notUsed: Boolean(leave.originalItem?.notUsed),
            numOfDays: leave.numOfDays || 0,
            adminComment: leave.originalItem?.adminComment || "",
        });
        setEditDialogOpen(true);
    };

    const openDeleteDialog = (leave) => {
        setCurrentLeave(leave);
        setDeleteDialogOpen(true);
    };

    const handleDeleteLeaveRequest = async () => {
        if (!currentLeave) return;

        try {
            await dispatch(deleteLeaveRequest(currentLeave.publicId)).unwrap();
            await dispatch(
                fetchLeaveData({
                    isAdmin,
                    userId: userId || sessionStorage.getItem("userId"),
                    userAdmin,
                    page: currentPage - 1,
                    size: pageSize,
                })
            ).unwrap();
            setDeleteDialogOpen(false);
        } catch (err) {
            setOperationError(`Failed to delete leave: ${err || err.message}`);
            setShowErrorSnackbar(true);
        }
    };

    const handleOpenViewDialog = async (leave) => {
        setViewLeaveData(leave);
        setViewDialogOpen(true);

        if (leave.startDate) {
            try {
                await dispatch(
                    fetchInOutData({
                        userId: userId || sessionStorage.getItem("userId"),
                        happenDate: leave.startDate,
                    })
                ).unwrap();
            } catch (err) {
                setOperationError(`Failed to fetch attendance data: ${err.message}`);
                setShowErrorSnackbar(true);
            }
        }
    };

    const handleCloseDialogs = () => {
        setEditDialogOpen(false);
        setDeleteDialogOpen(false);
        setViewDialogOpen(false);
        setCurrentLeave(null);
        setViewLeaveData(null);
    };

    const filteredLeaves = leaveRequests.filter((leave) => {
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
        const leaveEndDate = leave.endDate ? new Date(leave.endDate).getTime() : 0;
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

    const isLeaveEditable = (leave) => {
        return !(
            leave.accepted ||
            leave.reject ||
            leave.canceled ||
            leave.expired ||
            leave.requestStatus === "SUBMITTED"
        );
    };

    const getStatusColor = (status) => {
        switch (status) {
            case "APPROVED": return "success";
            case "REJECTED": return "error";
            case "PENDING_APPROVAL": return "warning";
            case "DRAFT": return "default";
            case "SUBMITTED": return "info";
            case "CANCELLED": return "secondary";
            case "EXPIRED": return "error";
            default: return "default";
        }
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case "APPROVED": return <CheckCircleIcon fontSize="small" />;
            case "REJECTED": return <CancelIcon fontSize="small" />;
            case "PENDING_APPROVAL": return <PendingIcon fontSize="small" />;
            default: return null;
        }
    };

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    {isAdmin || userAdmin ? "All Leave Requests" : "My Leave Requests"}
                </Typography>

                {serverError && (
                    <Alert severity="error" onClose={() => setServerError(null)}>
                        {serverError}
                    </Alert>
                )}

                <Snackbar
                    open={showErrorSnackbar}
                    autoHideDuration={6000}
                    onClose={() => setShowErrorSnackbar(false)}
                >
                    <Alert
                        severity="error"
                        onClose={() => setShowErrorSnackbar(false)}
                    >
                        {operationError}
                    </Alert>
                </Snackbar>

                {/* Leave Balance Cards */}
                <Grid container spacing={2} sx={{ mb: 3 }}>
                    {leaveBalances.map((type, index) => (
                        <Grid item xs={6} sm={4} key={index}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="subtitle1">
                                        {type.leaveTypeName}
                                    </Typography>
                                    <Typography variant="h6" color="primary">
                                        {type.remainingLeaves} days
                                    </Typography>
                                    <Typography variant="caption" color="textSecondary">
                                        Total: {type.totalLeaves} days
                                    </Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>

                {/* Search and Filters */}
                <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
                    <TextField
                        label="Search"
                        variant="outlined"
                        fullWidth
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        sx={{ flex: 1, minWidth: 200 }}
                    />

                    <FormControl sx={{ minWidth: 200 }}>
                        <InputLabel>Status</InputLabel>
                        <Select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            label="Status"
                        >
                            <MenuItem value="All">All Statuses</MenuItem>
                            {Object.values(REQUEST_STATUSES).map((status) => (
                                <MenuItem key={status} value={status}>
                                    {status}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl sx={{ minWidth: 200 }}>
                        <InputLabel>Leave Type</InputLabel>
                        <Select
                            value={typeFilter}
                            onChange={(e) => setTypeFilter(e.target.value)}
                            label="Leave Type"
                        >
                            <MenuItem value="All">All Types</MenuItem>
                            {Array.from(
                                new Set(leaveRequests.map((leave) => leave.type).filter(Boolean))
                            ).map((type) => (
                                <MenuItem key={type} value={type}>
                                    {type}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <TextField
                        label="From Date"
                        type="date"
                        value={startDateFilter}
                        onChange={(e) => setStartDateFilter(e.target.value)}
                        InputLabelProps={{ shrink: true }}
                        sx={{ minWidth: 200 }}
                    />

                    <TextField
                        label="To Date"
                        type="date"
                        value={endDateFilter}
                        onChange={(e) => setEndDateFilter(e.target.value)}
                        InputLabelProps={{ shrink: true }}
                        sx={{ minWidth: 200 }}
                    />
                </Box>

                {/* Leave Requests Table */}
                <TableContainer component={Paper} sx={{ mb: 3 }}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Employee</TableCell>
                                <TableCell>Leave Type</TableCell>
                                <TableCell>Dates</TableCell>
                                <TableCell>Days</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredLeaves.map((leave) => (
                                <TableRow key={leave.id}>
                                    <TableCell>
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Avatar sx={{ width: 32, height: 32 }}>
                                                {leave.employeeName?.charAt(0) || 'U'}
                                            </Avatar>
                                            <Box>
                                                <Typography>{leave.employeeName || 'Unknown'}</Typography>
                                                <Typography variant="caption">{leave.employeeId}</Typography>
                                            </Box>
                                        </Box>
                                    </TableCell>
                                    <TableCell>{leave.type}</TableCell>
                                    <TableCell>
                                        {leave.startDate} to {leave.endDate}
                                    </TableCell>
                                    <TableCell>{leave.numOfDays}</TableCell>
                                    <TableCell>
                                        <Chip
                                            label={REQUEST_STATUSES[leave.originalItem?.requestStatus] || leave.status}
                                            color={getStatusColor(leave.originalItem?.requestStatus)}
                                            size="small"
                                            icon={getStatusIcon(leave.originalItem?.requestStatus)}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Tooltip title="Edit">
                                            <IconButton
                                                onClick={() => openEditDialog(leave)}
                                              /*   disabled={!isLeaveEditable(leave)} */
                                            >
                                                <EditIcon />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="Delete">
                                            <IconButton
                                                onClick={() => openDeleteDialog(leave)}
                                                disabled={!isLeaveEditable(leave)}
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="View Details">
                                            <IconButton onClick={() => handleOpenViewDialog(leave)}>
                                                <VisibilityIcon />
                                            </IconButton>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>

                {/* Pagination */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <FormControl sx={{ minWidth: 120 }}>
                        <InputLabel>Rows</InputLabel>
                        <Select
                            value={pageSize}
                            onChange={(e) => setPageSize(e.target.value)}
                            label="Rows"
                        >
                            <MenuItem value={5}>5</MenuItem>
                            <MenuItem value={10}>10</MenuItem>
                            <MenuItem value={25}>25</MenuItem>
                            <MenuItem value={50}>50</MenuItem>
                        </Select>
                    </FormControl>
                    <Pagination
                        count={pagination.totalPages}
                        page={currentPage}
                        onChange={(e, value) => setCurrentPage(value)}
                        color="primary"
                        showFirstButton
                        showLastButton
                    />
                </Box>

                {/* Edit Leave Dialog */}
                <Dialog
                    open={editDialogOpen}
                    onClose={() => setEditDialogOpen(false)}
                    maxWidth="md"
                    fullWidth
                >
                    <DialogTitle>
                        {isAdmin || userAdmin ? "Edit Leave Request (Admin)" : "Edit Leave Request"}
                    </DialogTitle>
                    <DialogContent>
                        <Box sx={{ mt: 2 }}>
                            <Grid container spacing={2}>
                                <Grid item xs={12} md={6}>
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
                                </Grid>
                                <Grid item xs={12} md={6}>
                                    <TextField
                                        label="End Date"
                                        type="date"
                                        name="endDate"
                                        value={editFormData.endDate}
                                        onChange={handleEditFormChange}
                                        fullWidth
                                        margin="normal"
                                        InputLabelProps={{ shrink: true }}
                                        disabled={editFormData.componentBehavior === "HALF_DAY"}
                                    />
                                </Grid>
                            </Grid>

                            <Box sx={{ mt: 2, p: 2, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
                                <Typography variant="body1">
                                    <strong>Calculated Leave Duration:</strong> {editFormData.numOfDays}{" "}
                                    day(s)
                                    {editFormData.componentBehavior === "HALF_DAY" ? " (Half Day)" : ""}
                                </Typography>
                            </Box>

                            <Grid container spacing={2}>
                                <Grid item xs={12} md={6}>
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
                                </Grid>
                                <Grid item xs={12} md={6}>
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
                                </Grid>
                            </Grid>

                            {(isAdmin || userAdmin) && (
                                <>
                                    <Divider sx={{ my: 2 }} />
                                    <Typography variant="subtitle1" gutterBottom>
                                        Admin Controls
                                    </Typography>
                                    <Grid container spacing={2}>
                                        <Grid item xs={12} md={6}>
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
                                        </Grid>
                                        <Grid item xs={12} md={6}>
                                            <TextField
                                                label="Admin Comment"
                                                name="adminComment"
                                                value={editFormData.adminComment}
                                                onChange={handleEditFormChange}
                                                fullWidth
                                                margin="normal"
                                                multiline
                                                rows={2}
                                                placeholder="Enter reason for approval/rejection"
                                            />
                                        </Grid>
                                    </Grid>
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
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
                        <Button onClick={handleSaveEditedLeave} variant="contained" color="primary">
                            Save Changes
                        </Button>
                    </DialogActions>
                </Dialog>

                {/* Delete Confirmation Dialog */}
                <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                    <DialogTitle>Confirm Delete</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Are you sure you want to delete this leave request? This action cannot be
                            undone.
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
                        <Button onClick={handleDeleteLeaveRequest} color="error" autoFocus>
                            Delete
                        </Button>
                    </DialogActions>
                </Dialog>

                <Dialog
                    open={viewDialogOpen}
                    onClose={() => setViewDialogOpen(false)}
                    maxWidth="md"
                    fullWidth
                >
                    <DialogTitle>Leave Request Details</DialogTitle>
                    <DialogContent>
                        {viewLeaveData && (
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="h6" gutterBottom>
                                    Basic Information
                                </Typography>
                                <TableContainer component={Paper} sx={{ mb: 3 }}>
                                    <Table>
                                        <TableBody>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Employee</TableCell>
                                                <TableCell>
                                                    {viewLeaveData.employeeName} ({viewLeaveData.employeeId})
                                                </TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Leave Type</TableCell>
                                                <TableCell>{viewLeaveData.type}</TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Dates</TableCell>
                                                <TableCell>
                                                    {viewLeaveData.startDate} to {viewLeaveData.endDate}
                                                </TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Days</TableCell>
                                                <TableCell>{viewLeaveData.numOfDays}</TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                                <TableCell>
                                                    <Chip
                                                        label={
                                                            REQUEST_STATUSES[viewLeaveData.originalItem?.requestStatus] ||
                                                            viewLeaveData.status
                                                        }
                                                        color={getStatusColor(viewLeaveData.originalItem?.requestStatus)}
                                                        icon={getStatusIcon(viewLeaveData.originalItem?.requestStatus)}
                                                    />
                                                </TableCell>
                                            </TableRow>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Comment</TableCell>
                                                <TableCell>{viewLeaveData.comment || "None"}</TableCell>
                                            </TableRow>
                                            {viewLeaveData.originalItem?.adminComment && (
                                                <TableRow>
                                                    <TableCell sx={{ fontWeight: 'bold' }}>Admin Comment</TableCell>
                                                    <TableCell>{viewLeaveData.originalItem.adminComment}</TableCell>
                                                </TableRow>
                                            )}
                                        </TableBody>
                                    </Table>
                                </TableContainer>

                                {viewLeaveData.adminsTra && viewLeaveData.adminsTra.length > 0 && (
                                    <>
                                        <Typography variant="h6" gutterBottom>
                                            Approvers
                                        </Typography>
                                        <TableContainer component={Paper} sx={{ mb: 3 }}>
                                            <Table>
                                                <TableHead>
                                                    <TableRow>
                                                        <TableCell>Approver</TableCell>
                                                        <TableCell>Employee ID</TableCell>
                                                        <TableCell>Status</TableCell>
                                                    </TableRow>
                                                </TableHead>
                                                <TableBody>
                                                    {viewLeaveData.adminsTra.map((admin, index) => (
                                                        <TableRow key={index}>
                                                            <TableCell>
                                                                {admin.firstName} {admin.lastName}
                                                            </TableCell>
                                                            <TableCell>{admin.employeeId}</TableCell>
                                                            <TableCell>
                                                                {admin.accepted ? (
                                                                    <Chip label="Approved" color="success" size="small" />
                                                                ) : (
                                                                    <Chip label="Pending" color="warning" size="small" />
                                                                )}
                                                            </TableCell>
                                                        </TableRow>
                                                    ))}
                                                </TableBody>
                                            </Table>
                                        </TableContainer>
                                    </>
                                )}

                                <Typography variant="h6" gutterBottom>
                                    Attendance Records
                                </Typography>
                                {loadingInOutData ? (
                                    <Box sx={{ display: 'flex', justifyContent: 'center', my: 3 }}>
                                        <CircularProgress />
                                    </Box>
                                ) : inOutData ? (
                                    <TableContainer component={Paper} sx={{ mb: 3 }}>
                                        <Table>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell>Type</TableCell>
                                                    <TableCell>Time</TableCell>
                                                    <TableCell>Status</TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                <TableRow>
                                                    <TableCell>Check In</TableCell>
                                                    <TableCell>
                                                        {inOutData.morning?.timeMoa || "Not recorded"}
                                                    </TableCell>
                                                    <TableCell>
                                                        {inOutData.morning?.inOut === 1 ? (
                                                            <Chip label="Present" color="success" size="small" />
                                                        ) : (
                                                            <Chip label="Absent" color="error" size="small" />
                                                        )}
                                                    </TableCell>
                                                </TableRow>
                                                <TableRow>
                                                    <TableCell>Check Out</TableCell>
                                                    <TableCell>
                                                        {inOutData.evening?.timeEve || "Not recorded"}
                                                    </TableCell>
                                                    <TableCell>
                                                        {inOutData.evening?.inOut === 0 ? (
                                                            <Chip label="Present" color="success" size="small" />
                                                        ) : (
                                                            <Chip label="Absent" color="error" size="small" />
                                                        )}
                                                    </TableCell>
                                                </TableRow>
                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                ) : (
                                    <Alert severity="info">No attendance records found for this date</Alert>
                                )}
                            </Box>
                        )}
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setViewDialogOpen(false)}>Close</Button>
                    </DialogActions>
                </Dialog>
            </Box>
        </Container>
    );
};

export default PendingLeaves;