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
} from "@mui/material";
import { Delete as DeleteIcon, Edit as EditIcon, Visibility as VisibilityIcon } from "@mui/icons-material";
import {
    fetchLeaveData,
    fetchLeaveBalances,
    deleteLeaveRequest,
    // updateLeaveRequest,
} from "../../../../lib/redux/redux-lms/leave/leaveSlice";

const PendingLeaves = ({ isAdmin = false, userId = null }) => {
    const dispatch = useDispatch();
    const [viewDetailsId, setViewDetailsId] = useState(null);

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
            error: null
        };
    });

    const { data: leaveRequests, pagination, loading, error } = leaveState;

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
    const [currentLeave, setCurrentLeave] = useState(null);
    const [editFormData, setEditFormData] = useState({
        startDate: "",
        endDate: "",
        type: "",
        comment: "",
    });

    useEffect(() => {
        // Get the userID for fetching data
        const userIdToUse = userId || sessionStorage.getItem("userId");
        if (!userIdToUse) return;

        // Get the loggedInUserId - the one making the request
        const loggedInUserId = sessionStorage.getItem("userId");
        if (!loggedInUserId) return;

        dispatch(
            fetchLeaveData({
                isAdmin,
                userId: userIdToUse,
                page: currentPage - 1,
                size: pageSize,
            })
        );

        dispatch(fetchLeaveBalances(userIdToUse));
    }, [dispatch, isAdmin, userId, currentPage, pageSize]);

    const toggleViewDetails = (id) => {
        setViewDetailsId(viewDetailsId === id ? null : id);
    };

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
    const handleStartDateFilterChange = (event) =>
        setStartDateFilter(event.target.value);
    const handleEndDateFilterChange = (event) =>
        setEndDateFilter(event.target.value);
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
        setEditFormData({
            startDate: leave.startDate || "",
            endDate: leave.endDate || "",
            type: leave.leaveTypeName || "",
            comment: leave.comment || "",
        });
        setEditDialogOpen(true);
    };

    const handleCloseDialogs = () => {
        setEditDialogOpen(false);
        setDeleteDialogOpen(false);
        setCurrentLeave(null);
    };

    const handleEditFormChange = (e) => {
        const { name, value } = e.target;
        setEditFormData((prev) => ({ ...prev, [name]: value }));
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

    const handleSaveEditedLeave = async () => {
        if (!currentLeave) return;

        const updatedLeave = {
            ...currentLeave,
            fromDate: editFormData.startDate,
            toDate: editFormData.endDate,
            description: editFormData.comment,
            leaveType: editFormData.type,
        };

        /*try {
            await dispatch(
                updateLeaveRequest({
                    id: currentLeave.publicId,
                    data: updatedLeave,
                })
            ).unwrap();
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
        }*/

        // For now just close since updateLeaveRequest is commented out
        handleCloseDialogs();
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

                        <Box
                            sx={{ display: "flex", gap: 2, mb: 2, flexWrap: "wrap" }}
                        >
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
                                                        selected.length ===
                                                        filteredLeaves.length &&
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
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {filteredLeaves.map((leave, index) => (
                                            <React.Fragment key={leave.id || `leave-${index}`}>
                                                <TableRow>
                                                    <TableCell padding="checkbox">
                                                        <Checkbox
                                                            checked={selected.includes(leave.id)}
                                                            onChange={() => handleSelect(leave.id)}
                                                            disabled={leave.accepted || leave.reject ||  leave.canceled }
                                                        />
                                                    </TableCell>
                                                    <TableCell>{leave.employeeId || 'N/A'}</TableCell>
                                                    <TableCell>{leave.type || 'N/A'}</TableCell>
                                                    <TableCell>
                                                        {leave.startDate || "N/A"}
                                                    </TableCell>
                                                    <TableCell>
                                                        {leave.endDate || "N/A"}
                                                    </TableCell>
                                                    <TableCell>{leave.status || 'N/A'}</TableCell>
                                                    <TableCell>
                                                        <IconButton
                                                            onClick={() => openEditDialog(leave)}
                                                            disabled={leave.accepted || leave.reject || leave.canceled ||false}
                                                            color="primary"
                                                        >
                                                            <EditIcon />
                                                        </IconButton>
                                                        <IconButton
                                                            onClick={() => openDeleteDialog(leave)}
                                                            disabled={leave.accepted || leave.reject || leave.canceled ||false}
                                                            color="error"
                                                        >
                                                            <DeleteIcon />
                                                        </IconButton>
                                                        <IconButton
                                                            onClick={() => toggleViewDetails(leave.id)}
                                                            color="info"
                                                            disabled={leave.reject ||  leave.canceled || false}
                                                        >
                                                            <VisibilityIcon />
                                                        </IconButton>
                                                    </TableCell>
                                                </TableRow>
                                                {viewDetailsId === leave.id && (
                                                    <TableRow>
                                                        <TableCell colSpan={7}>
                                                            <Box sx={{ p: 2, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
                                                                <Typography variant="subtitle1" gutterBottom>
                                                                    Full Leave Details
                                                                </Typography>
                                                                <Grid container spacing={2}>
                                                                    <Grid item xs={12} sm={6}>
                                                                        <Typography><strong>Public ID:</strong> {leave.publicId || 'N/A'}</Typography>
                                                                        <Typography><strong>Submit Date:</strong> {leave.submitDate || 'N/A'}</Typography>
                                                                        <Typography><strong>Description:</strong> {leave.description || 'N/A'}</Typography>
                                                                        <Typography><strong>Number of Days:</strong> {leave.numOfDays || 'N/A'}</Typography>
                                                                        <Typography><strong>Is No Pay:</strong> {leave.isNoPay ? 'Yes' : 'No'}</Typography>
                                                                    </Grid>
                                                                    <Grid item xs={12} sm={6}>
                                                                        <Typography><strong>Status Flags:</strong></Typography>
                                                                        <Typography><strong>Pending:</strong> {leave.pending ? 'Yes' : 'No'}</Typography>
                                                                        <Typography><strong>Accepted:</strong> {leave.accepted ? 'Yes' : 'No'}</Typography>
                                                                        <Typography><strong>Canceled:</strong> {leave.canceled ? 'Yes' : 'No'}</Typography>
                                                                        <Typography><strong>Manual Request:</strong> {leave.manualRequest ? 'Yes' : 'No'}</Typography>
                                                                    </Grid>
                                                                    {leave.adminsTra && leave.adminsTra.length > 0 && (
                                                                        <Grid item xs={12}>
                                                                            <Typography variant="subtitle2" gutterBottom>
                                                                                Approval Chain:
                                                                            </Typography>
                                                                            <Table size="small">
                                                                                <TableHead>
                                                                                    <TableRow>
                                                                                        <TableCell>Admin ID</TableCell>
                                                                                        <TableCell>Name</TableCell>
                                                                                        <TableCell>Email</TableCell>
                                                                                        <TableCell>Priority</TableCell>
                                                                                        <TableCell>Status</TableCell>
                                                                                    </TableRow>
                                                                                </TableHead>
                                                                                <TableBody>
                                                                                    {leave.adminsTra.map((admin, idx) => (
                                                                                        <TableRow key={`admin-${idx}`}>
                                                                                            <TableCell>{admin.employeeId || 'N/A'}</TableCell>
                                                                                            <TableCell>{`${admin.firstName || ''} ${admin.lastName || ''}`.trim() || 'N/A'}</TableCell>
                                                                                            <TableCell>{admin.email || 'N/A'}</TableCell>
                                                                                            <TableCell>{admin.highestRolePriority || 'N/A'}</TableCell>
                                                                                            <TableCell>{admin.accepted ? 'Approved' : 'Pending'}</TableCell>
                                                                                        </TableRow>
                                                                                    ))}
                                                                                </TableBody>
                                                                            </Table>
                                                                        </Grid>
                                                                    )}
                                                                </Grid>
                                                            </Box>
                                                        </TableCell>
                                                    </TableRow>
                                                )}
                                            </React.Fragment>
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

                        <Dialog
                            open={editDialogOpen}
                            onClose={handleCloseDialogs}
                            maxWidth="sm"
                            fullWidth
                        >
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
                                            <MenuItem value="Medical Leave">
                                                Medical Leave
                                            </MenuItem>
                                            <MenuItem value="Sick Leave">Sick Leave</MenuItem>
                                            <MenuItem value="Casual Leave">
                                                Casual Leave
                                            </MenuItem>
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
                                <Button
                                    onClick={handleSaveEditedLeave}
                                    variant="contained"
                                    color="primary"
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
                    </>
                )}
            </Box>
        </Container>
    );
};

export default PendingLeaves;