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
    Button,
    Checkbox,
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    CircularProgress,
    Alert,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Pagination,
    Tooltip,
    Divider,
    List,
    ListItem,
    ListItemText,
    Grid,
    Card,
    CardContent,
    Avatar,
} from "@mui/material";
import {
    Delete as DeleteIcon,
    Edit as EditIcon,
    Save as SaveIcon,
    Close as CloseIcon
} from "@mui/icons-material";
import VisibilityIcon from '@mui/icons-material/Visibility';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { useDispatch, useSelector } from "react-redux";
import {
    fetchMovementRequests,
    deleteMovementRequest,
    updateMovementRequest,
    fetchInOutData,
    setSelected,
    setSearchQuery,
    setStatusFilter,
    setTypeFilter,
    setStartDateFilter,
    setEndDateFilter,
    setPageSize,
    setCurrentPage,
} from "../../../../lib/redux/redux-lms/movement/movementSlice";

const MOVEMENT_TYPES = {
    OFFICE_TO_HOME: 'Office to Home',
    HOME_TO_OFFICE: 'Home to Office',
    REMOTE_WORK: 'Remote Work',
    SITE_VISIT: 'Site Visit',
    TRAINING: 'Training',
    MEETING: 'Meeting',
    OTHER: 'Other'
};

const REQUEST_STATUSES = {
    DRAFT: 'Draft',
    SUBMITTED: 'Submitted',
    PENDING_APPROVAL: 'Pending Approval',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired'
};

const ADMIN_SELECTABLE_STATUSES = {
    DRAFT: 'Draft',
    SUBMITTED: 'Submitted',
    PENDING_APPROVAL: 'Pending Approval',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled'
};

const ManageMovementRequests = ({ isAdmin = false, useAdmin = false, userId = null }) => {
    const dispatch = useDispatch();
    const {
        requests: movementRequests,
        selected,
        searchQuery,
        statusFilter,
        typeFilter,
        startDateFilter,
        endDateFilter,
        loading,
        error,
        pagination,
        inOutData,
        loadingInOutData
    } = useSelector(state => state.movementNo);

    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [currentEdit, setCurrentEdit] = useState(null);
    const [editValues, setEditValues] = useState({
        employeeId: "",
        userId: "",
        happenDate: "",
        reqDate: "",
        destination: "",
        movementType: "",
        comment: "",
        category: "",
        inTime: "",
        outTime: "",
        logTime: "",
        requestStatus: "DRAFT",
        attSync: 0,
        attendance: "",
    });
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteMovementId, setDeleteMovementId] = useState(null);
    const [viewDialogOpen, setViewDialogOpen] = useState(false);
    const [viewMovementData, setViewMovementData] = useState(null);

    useEffect(() => {
        if (userId == null) userId = sessionStorage.getItem('userId');
        if (userId) {
            dispatch(fetchMovementRequests({
                isAdmin,
                userId,
                useAdmin,
                page: pagination.currentPage,
                size: pagination.pageSize
            }));
        }
    }, [dispatch, isAdmin, userId, pagination.currentPage, pagination.pageSize]);

    const isApproved = (request) => {
        return request.status === "Approved" || request.accepted === true || request.requestStatus === "APPROVED";
    };

    const isRejected = (request) => {
        return request.status === "Rejected" || request.reject === true || request.requestStatus === "REJECTED";
    };

    const isCancelled = (request) => {
        return request.status === "Cancelled" || request.canceled === true || request.requestStatus === "CANCELLED";
    };

    const isExpired = (request) => {
        return request.status === "Expired" || request.requestStatus === "EXPIRED";
    };

    const isMovementEditable = (request) => {
        return !(isApproved(request) || isRejected(request) || isCancelled(request) || isExpired(request));
    };

    const getFinalStateInfo = (request) => {
        if (isExpired(request)) return { type: 'expired', color: 'error', icon: '⏰', message: 'expired and cannot be modified' };
        if (isApproved(request)) return { type: 'approved', color: 'success', icon: '✅', message: 'approved and cannot be modified' };
        if (isRejected(request)) return { type: 'rejected', color: 'error', icon: '❌', message: 'rejected and cannot be modified' };
        if (isCancelled(request)) return { type: 'cancelled', color: 'warning', icon: '🚫', message: 'cancelled and cannot be modified' };
        return null;
    };

    const filteredMovementRequests = movementRequests.filter((request) => {
        const employeeIdLower = (request.employeeId || "").toLowerCase();
        const typeLower = (request.type || "").toLowerCase();
        const destinationLower = (request.destination || "").toLowerCase();
        const searchQueryLower = searchQuery.toLowerCase();

        const matchesSearchQuery =
            employeeIdLower.includes(searchQueryLower) ||
            typeLower.includes(searchQueryLower) ||
            destinationLower.includes(searchQueryLower);

        const matchesStatusFilter =
            statusFilter === "All" || request.status === statusFilter;

        const matchesTypeFilter =
            typeFilter === "All" || request.type === typeFilter;

        const matchesStartDateFilter =
            !startDateFilter || request.startDate >= startDateFilter;

        const matchesEndDateFilter =
            !endDateFilter || request.endDate <= endDateFilter;

        return (
            matchesSearchQuery &&
            matchesStatusFilter &&
            matchesTypeFilter &&
            matchesStartDateFilter &&
            matchesEndDateFilter
        );
    });

    const hasNonEditableRequests = filteredMovementRequests.some(request =>
        !isMovementEditable(request)
    );

    const handleSelect = (id) => {
        const request = movementRequests.find(req => req.id === id);
        if (request && !isMovementEditable(request)) return;

        if (selected.includes(id)) {
            dispatch(setSelected(selected.filter(item => item !== id)));
        } else {
            dispatch(setSelected([...selected, id]));
        }
    };

    const handleSelectAll = () => {
        const selectableRequests = filteredMovementRequests.filter(request =>
            isMovementEditable(request)
        );

        if (selected.length === selectableRequests.length && selectableRequests.length > 0) {
            dispatch(setSelected([]));
        } else {
            dispatch(setSelected(selectableRequests.map(request => request.id)));
        }
    };

    const handleBulkDelete = async () => {
        try {
            for (const id of selected) {
                const request = movementRequests.find(req => req.id === id);
                if (request && request.publicId && isMovementEditable(request)) {
                    await dispatch(deleteMovementRequest(request.publicId)).unwrap();
                }
            }
        } catch (err) {
            console.error("Error during bulk delete:", err);
        }
    };

    const handleOpenDeleteDialog = (movementId) => {
        setDeleteMovementId(movementId);
        setDeleteDialogOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (deleteMovementId) {
            try {
                await dispatch(deleteMovementRequest(deleteMovementId)).unwrap();
                setDeleteDialogOpen(false);
                setDeleteMovementId(null);
            } catch (err) {
                console.error("Error deleting movement request:", err);
            }
        }
    };

    const formatDateForInput = (dateString) => {
        if (!dateString) return "";
        try {
            const date = new Date(dateString);
            return date.toISOString().split('T')[0];
        } catch (e) {
            return "";
        }
    };

    const formatTimeForInput = (timeString) => {
        if (!timeString) return "";
        if (timeString.includes('T')) {
            return new Date(timeString).toISOString().slice(11, 19);
        }
        return timeString;
    };

    const formatDateTimeForInput = (dateString) => {
        if (!dateString) return "";
        try {
            const date = new Date(dateString);
            return date.toISOString().slice(0, 16);
        } catch (e) {
            return "";
        }
    };

    const handleOpenEditDialog = (request) => {
        setCurrentEdit(request);

        const validRequestStatus = Object.keys(ADMIN_SELECTABLE_STATUSES).includes(request.requestStatus)
            ? request.requestStatus
            : "DRAFT";

        const validMovementType = Object.keys(MOVEMENT_TYPES).includes(request.movementType)
            ? request.movementType
            : Object.keys(MOVEMENT_TYPES)[0];

        setEditValues({
            employeeId: request.employeeId || "",
            userId: request.userId || "",
            happenDate: formatDateForInput(request.happenDate),
            reqDate: formatDateForInput(request.reqDate),
            destination: request.destination || "",
            movementType: validMovementType,
            comment: request.comment || "",
            category: request.category || "",
            inTime: formatTimeForInput(request.inTime),
            outTime: formatTimeForInput(request.outTime),
            logTime: formatDateTimeForInput(request.logTime),
            requestStatus: validRequestStatus,
            attSync: request.attSync || 0,
            attendance: request.attendance || "",
        });
        setEditDialogOpen(true);
    };

    const handleEditChange = (e) => {
        const { name, value, type, checked } = e.target;
        setEditValues(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSaveEdit = async () => {
        try {
            if (!currentEdit || !currentEdit.publicId) {
                throw new Error("Invalid movement request data");
            }

            if (!isMovementEditable(currentEdit)) {
                throw new Error("Cannot edit this movement request");
            }

            // Build the complete payload with enum structure
            const updatePayload = {
                publicId: currentEdit.publicId,
                employeeId: editValues.employeeId,
                userId: editValues.userId,
                destination: editValues.destination,
                movementType: editValues.movementType,
                comment: editValues.comment,
                category: editValues.category,
                inTime: editValues.inTime,
                outTime: editValues.outTime,
                requestStatus: editValues.requestStatus,
                attSync: editValues.attSync,
                attendance: editValues.attendance,
                isEdited: true
            };

            if (editValues.happenDate) {
                updatePayload.happenDate = new Date(editValues.happenDate).toISOString();
            }
            if (editValues.reqDate) {
                updatePayload.reqDate = new Date(editValues.reqDate).toISOString();
            }
            if (editValues.logTime) {
                updatePayload.logTime = new Date(editValues.logTime).toISOString();
            }

            console.log("Sending update payload:", updatePayload);

            await dispatch(updateMovementRequest({
                updatePayload,
                isAdmin,
                useAdmin
            })).unwrap();

            setEditDialogOpen(false);

            if (userId == null) userId = sessionStorage.getItem('userId');
            if (userId) {
                dispatch(fetchMovementRequests({
                    isAdmin,
                    userId,
                    useAdmin,
                    page: pagination.currentPage,
                    size: pagination.pageSize
                }));
            }
        } catch (err) {
            console.error("Error updating movement request:", err);
            alert(`Update failed: ${err.message || err}`);
        }
    };

    const handlePageSizeChange = (event) => {
        const newPageSize = parseInt(event.target.value, 10);
        dispatch(setPageSize(newPageSize));
    };

    const handlePageChange = (event, value) => {
        dispatch(setSelected([]));
        dispatch(setCurrentPage(value - 1));
    };

    const handleSearchChange = (event) => {
        dispatch(setSearchQuery(event.target.value));
    };

    const handleStatusFilterChange = (event) => {
        dispatch(setStatusFilter(event.target.value));
    };

    const handleTypeFilterChange = (event) => {
        dispatch(setTypeFilter(event.target.value));
    };

    const handleStartDateChange = (e) => {
        dispatch(setStartDateFilter(e.target.value));
    };

    const handleEndDateChange = (e) => {
        dispatch(setEndDateFilter(e.target.value));
    };

    const handleApplyFilters = () => {
        dispatch(setCurrentPage(0));

        if (userId == null) userId = sessionStorage.getItem('userId');

        if (userId) {
            dispatch(fetchMovementRequests({
                isAdmin,
                userId,
                useAdmin,
                page: 0,
                size: pagination.pageSize
            }));
        }
    };

    const handleOpenDialog = (publicId) => {
        const request = movementRequests.find(req => req.publicId === publicId);
        if (request) {
            setViewMovementData(request);
            setViewDialogOpen(true);

            if (request.startDate) {
                dispatch(fetchInOutData({
                    userId: userId || sessionStorage.getItem('userId'),
                    happenDate: request.startDate
                }));
            }
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

    const formatTime = (timeString) => {
        if (!timeString) return "N/A";
        return timeString;
    };

    const movementTypes = [...new Set(movementRequests.map(req => req.type).filter(Boolean))];
    const statuses = [...new Set(movementRequests.map(req => req.status).filter(Boolean))];

    return (
        <Container maxWidth="lg">
            <CssBaseline />
            <Box sx={{ mt: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>
                    Manage Movement Requests
                </Typography>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        Error: {error}
                    </Alert>
                )}

                {loading ? (
                    <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                            <Typography variant="body2">
                                Showing {filteredMovementRequests.length} of {pagination.totalElements} total movement requests
                            </Typography>

                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                <Typography variant="body2">Rows per page:</Typography>
                                <FormControl variant="outlined" size="small">
                                    <Select
                                        value={pagination.pageSize}
                                        onChange={handlePageSizeChange}
                                        sx={{ minWidth: 80 }}
                                    >
                                        <MenuItem value={5}>5</MenuItem>
                                        <MenuItem value={10}>10</MenuItem>
                                        <MenuItem value={25}>25</MenuItem>
                                        <MenuItem value={50}>50</MenuItem>
                                    </Select>
                                </FormControl>
                            </Box>
                        </Box>

                        <TextField
                            label="Search by Employee ID, Type or Destination"
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
                                    {statuses.map(status => (
                                        <MenuItem key={status} value={status}>{status}</MenuItem>
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
                                    {movementTypes.map(type => (
                                        <MenuItem key={type} value={type}>{type}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>

                            <TextField
                                label="Start Date"
                                type="date"
                                variant="outlined"
                                value={startDateFilter}
                                onChange={handleStartDateChange}
                                InputLabelProps={{ shrink: true }}
                            />
                            <TextField
                                label="End Date"
                                type="date"
                                variant="outlined"
                                value={endDateFilter}
                                onChange={handleEndDateChange}
                                InputLabelProps={{ shrink: true }}
                            />

                            <Button
                                variant="contained"
                                color="primary"
                                onClick={handleApplyFilters}
                            >
                                Apply Filters
                            </Button>
                        </Box>

                        <Box sx={{ mb: 2 }}>
                            <Button
                                variant="contained"
                                color="error"
                                onClick={handleBulkDelete}
                                disabled={
                                    selected.length === 0 ||
                                    hasNonEditableRequests ||
                                    selected.some((id) => {
                                        const request = movementRequests.find((req) => req.id === id);
                                        return !isMovementEditable(request);
                                    })
                                }
                            >
                                Delete Selected ({selected.length})
                                {hasNonEditableRequests && " - Some items cannot be deleted"}
                            </Button>
                        </Box>

                        {movementRequests.length === 0 ? (
                            <Alert severity="info">No movement requests found.</Alert>
                        ) : (
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell padding="checkbox">
                                                <Checkbox
                                                    indeterminate={
                                                        selected.length > 0 &&
                                                        selected.length < filteredMovementRequests.filter(req => isMovementEditable(req)).length
                                                    }
                                                    checked={
                                                        selected.length === filteredMovementRequests.filter(req => isMovementEditable(req)).length &&
                                                        filteredMovementRequests.filter(req => isMovementEditable(req)).length > 0
                                                    }
                                                    onChange={handleSelectAll}
                                                    disabled={hasNonEditableRequests}
                                                />
                                            </TableCell>
                                            <TableCell>Employee ID</TableCell>
                                            <TableCell>Type</TableCell>
                                            <TableCell>Destination</TableCell>
                                            <TableCell>Date</TableCell>
                                            <TableCell>Request Date</TableCell>
                                            <TableCell>Status</TableCell>
                                            <TableCell>Actions</TableCell>
                                            <TableCell>View</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {filteredMovementRequests.map((request) => {
                                            const isEditable = isMovementEditable(request);
                                            const finalStateInfo = getFinalStateInfo(request);

                                            return (
                                                <TableRow
                                                    key={request.id}
                                                    sx={{
                                                        backgroundColor: finalStateInfo ? (
                                                            finalStateInfo.type === 'expired' ? '#ffebee' :
                                                                finalStateInfo.type === 'approved' ? '#e8f5e8' :
                                                                    finalStateInfo.type === 'rejected' ? '#ffebee' :
                                                                        finalStateInfo.type === 'cancelled' ? '#fff3e0' :
                                                                            'inherit'
                                                        ) : 'inherit',
                                                        opacity: finalStateInfo ? 0.7 : 1 // Slightly faded for final states
                                                    }}
                                                >
                                                    <TableCell padding="checkbox">
                                                        <Tooltip
                                                            title={!isEditable ? `This movement request is ${finalStateInfo?.message || 'not editable'}` : ""}>
                                                            <span>
                                                                <Checkbox
                                                                    checked={selected.includes(request.id)}
                                                                    onChange={() => handleSelect(request.id)}
                                                                    disabled={!isEditable}
                                                                />
                                                            </span>
                                                        </Tooltip>
                                                    </TableCell>
                                                    <TableCell>{request.employeeId || ""}</TableCell>
                                                    <TableCell>{MOVEMENT_TYPES[request.movementType] || request.type || "Unknown"}</TableCell>
                                                    <TableCell>{request.destination || "Not specified"}</TableCell>
                                                    <TableCell>{request.startDate || "Not specified"}</TableCell>
                                                    <TableCell>{request.endDate || "Not specified"}</TableCell>
                                                    <TableCell>
                                                        <Typography
                                                            color={finalStateInfo ? finalStateInfo.color : 'inherit'}
                                                            sx={{
                                                                fontWeight: finalStateInfo ? 'bold' : 'normal',
                                                                display: 'flex',
                                                                alignItems: 'center',
                                                                gap: 1
                                                            }}
                                                        >
                                                            {request.status || "Unknown"}
                                                            {finalStateInfo && (
                                                                <Tooltip title={`This movement request is ${finalStateInfo.message}`}>
                                                                    <span>{finalStateInfo.icon}</span>
                                                                </Tooltip>
                                                            )}
                                                        </Typography>
                                                    </TableCell>
                                                    <TableCell>
                                                        <Tooltip
                                                            title={!isEditable ? `This movement request is ${finalStateInfo?.message || 'not editable'}` : "Edit"}>
                                                            <span>
                                                                <IconButton
                                                                    onClick={() => isEditable && handleOpenEditDialog(request)}
                                                                    color="primary"
                                                                    disabled={!isEditable}
                                                                    sx={{
                                                                        opacity: !isEditable ? 0.3 : 1
                                                                    }}
                                                                >
                                                                    <EditIcon />
                                                                </IconButton>
                                                            </span>
                                                        </Tooltip>
                                                        <Tooltip
                                                            title={!isEditable ? `This movement request is ${finalStateInfo?.message || 'not editable'}` : "Delete"}>
                                                            <span>
                                                                <IconButton
                                                                    onClick={() => isEditable && handleOpenDeleteDialog(request.publicId)}
                                                                    color="error"
                                                                    disabled={!isEditable}
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
                                                            <IconButton onClick={() => handleOpenDialog(request.publicId)}>
                                                                <VisibilityIcon />
                                                            </IconButton>
                                                        </Tooltip>
                                                    </TableCell>
                                                </TableRow>
                                            );
                                        })}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        )}

                        {pagination.totalPages > 0 && (
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                                <Pagination
                                    count={pagination.totalPages}
                                    page={pagination.currentPage + 1}
                                    onChange={handlePageChange}
                                    color="primary"
                                    showFirstButton
                                    showLastButton
                                />
                            </Box>
                        )}
                    </>
                )}
            </Box>

            <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="lg" fullWidth>
                <DialogTitle>Edit Movement Request - {currentEdit?.publicId}</DialogTitle>
                <DialogContent>
                    <Box sx={{ pt: 2 }}>
                        <Grid container spacing={2}>
                            {/* Basic Information */}
                            <Grid item xs={12}>
                                <Typography variant="h6" gutterBottom color="primary">
                                    Basic Information
                                </Typography>
                                <Divider sx={{ mb: 2 }} />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="employeeId"
                                    label="Employee ID"
                                    value={editValues.employeeId}
                                    onChange={handleEditChange}
                                    fullWidth
                                    disabled={true}
                                    margin="normal"
                                />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="userId"
                                    label="User ID"
                                    value={editValues.userId}
                                    disabled={true}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <FormControl fullWidth margin="normal">
                                    <InputLabel>Movement Type</InputLabel>
                                    <Select
                                        name="movementType"
                                        value={editValues.movementType}
                                        onChange={handleEditChange}
                                        label="Movement Type"
                                    >
                                        {Object.entries(MOVEMENT_TYPES).map(([key, value]) => (
                                            <MenuItem key={key} value={key}>
                                                {value}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Grid>

                            {(isAdmin || useAdmin) && (
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth margin="normal">
                                        <InputLabel>Request Status</InputLabel>
                                        <Select
                                            name="requestStatus"
                                            value={editValues.requestStatus}
                                            onChange={handleEditChange}
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
                            )}

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="category"
                                    label="Category"
                                    value={editValues.category}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    name="destination"
                                    label="Destination"
                                    value={editValues.destination}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <Typography variant="h6" gutterBottom color="primary" sx={{ mt: 2 }}>
                                    Date & Time Information
                                </Typography>
                                <Divider sx={{ mb: 2 }} />
                            </Grid>

                            <Grid item xs={12} sm={4}>
                                <TextField
                                    name="happenDate"
                                    label="Happen Date"
                                    type="date"
                                    value={editValues.happenDate}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>

                            <Grid item xs={12} sm={4}>
                                <TextField
                                    name="reqDate"
                                    label="Request Date"
                                    type="date"
                                    value={editValues.reqDate}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>

                            <Grid item xs={12} sm={4}>
                                <TextField
                                    name="logTime"
                                    label="Log Time"
                                    type="datetime-local"
                                    value={editValues.logTime}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="inTime"
                                    label="In Time"
                                    type="time"
                                    value={editValues.inTime}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="outTime"
                                    label="Out Time"
                                    type="time"
                                    value={editValues.outTime}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                    InputLabelProps={{ shrink: true }}
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <Typography variant="h6" gutterBottom color="primary" sx={{ mt: 2 }}>
                                    Additional Information
                                </Typography>
                                <Divider sx={{ mb: 2 }} />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="attSync"
                                    label="Attendance Sync"
                                    type="number"
                                    value={editValues.attSync}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                />
                            </Grid>

                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="attendance"
                                    label="Attendance ID"
                                    value={editValues.attendance}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    name="comment"
                                    label="Comment"
                                    value={editValues.comment}
                                    onChange={handleEditChange}
                                    fullWidth
                                    margin="normal"
                                    multiline
                                    rows={3}
                                    placeholder="Add any additional comments or notes..."
                                />
                            </Grid>

                            {/* Enum Information Display */}
                            <Grid item xs={12}>
                                <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                                    <Typography variant="body2" color="textSecondary">
                                        <strong>Selected Movement Type:</strong> {MOVEMENT_TYPES[editValues.movementType] || 'None'}
                                    </Typography>
                                    {(isAdmin || useAdmin) && (
                                        <Typography variant="body2" color="textSecondary">
                                            <strong>Request Status:</strong> {REQUEST_STATUSES[editValues.requestStatus] || 'None'}
                                        </Typography>
                                    )}
                                </Box>
                            </Grid>
                        </Grid>
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setEditDialogOpen(false)} startIcon={<CloseIcon />}>
                        Cancel
                    </Button>
                    <Button onClick={handleSaveEdit} variant="contained" color="primary" startIcon={<SaveIcon />}>
                        Save All Changes
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
                <DialogTitle>Confirm Delete</DialogTitle>
                <DialogContent>
                    <Typography>
                        Are you sure you want to delete this movement request? This action cannot be undone.
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
                    <Button onClick={handleConfirmDelete} variant="contained" color="error">
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
                <DialogTitle>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <VisibilityIcon color="primary" />
                        <Typography variant="h6">Movement Request Details</Typography>
                    </Box>
                </DialogTitle>
                <DialogContent>
                    {viewMovementData && (
                        <Box sx={{ pt: 2 }}>
                            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                Basic Information
                            </Typography>
                            <TableContainer component={Paper} sx={{ mb: 3 }}>
                                <Table size="small">
                                    <TableBody>
                                        <TableRow>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Request ID</TableCell>
                                            <TableCell>{viewMovementData.publicId}</TableCell>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Employee ID</TableCell>
                                            <TableCell>{viewMovementData.employeeId}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Type</TableCell>
                                            <TableCell>{MOVEMENT_TYPES[viewMovementData.movementType] || viewMovementData.type}</TableCell>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Destination</TableCell>
                                            <TableCell>{viewMovementData.destination || "Not specified"}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Date</TableCell>
                                            <TableCell>{viewMovementData.startDate || "Not specified"}</TableCell>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Request Date</TableCell>
                                            <TableCell>{viewMovementData.endDate || "Not specified"}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>IN-TIME</TableCell>
                                            <TableCell>{viewMovementData.inTime || "Not specified"}</TableCell>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>OUT-TIME</TableCell>
                                            <TableCell>{viewMovementData.outTime || "Not specified"}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                            <TableCell>{viewMovementData.status}</TableCell>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Edited</TableCell>
                                            <TableCell>{viewMovementData.isEdited ? 'Yes' : 'No'}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Comment</TableCell>
                                            <TableCell colSpan={3}>{viewMovementData.comment || "No comment"}</TableCell>
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
                                                                <TableCell>{inOutData.morning?.punchTypeTime || "Not recorded"}</TableCell>
                                                            </TableRow>
                                                            <TableRow>
                                                                <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Punch In (Server Time)</TableCell>
                                                                <TableCell>{inOutData.morning?.punchTime ? formatDate(inOutData.morning.punchTime) : "Not recorded"}</TableCell>
                                                            </TableRow>
                                                            <TableRow>
                                                                <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                                                <TableCell>
                                                                    {inOutData.morning?.inOutValue === 1 ? (
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
                                                                <TableCell>{inOutData.evening?.punchTypeTime || "Not recorded"}</TableCell>
                                                            </TableRow>
                                                            <TableRow>
                                                                <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Punch Out (Server Time)</TableCell>
                                                                <TableCell>{inOutData.evening?.punchTime ? formatDate(inOutData.evening.punchTime) : "Not recorded"}</TableCell>
                                                            </TableRow>
                                                            <TableRow>
                                                                <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                                                <TableCell>
                                                                    {inOutData.evening?.inOutValue === 0 ? (
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

                            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 4 }}>
                                Approval History
                            </Typography>
                            {viewMovementData.adminsTra && viewMovementData.adminsTra.length > 0 ? (
                                <TableContainer component={Paper} sx={{ mb: 4 }}>
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>Profile</TableCell>
                                                <TableCell>Admin</TableCell>
                                                <TableCell>Employee ID</TableCell>
                                                <TableCell>SLT ID</TableCell>
                                                <TableCell>Approval Date</TableCell>
                                                <TableCell>Priority</TableCell>
                                                <TableCell>Status</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {viewMovementData.adminsTra.map((admin, index) => (
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
                                                    <TableCell>{`${admin.firstName || ''} ${admin.lastName || ''}`}</TableCell>
                                                    <TableCell>{admin.employeeId}</TableCell>
                                                    <TableCell>{admin.sltId}</TableCell>
                                                    <TableCell>{formatDate(admin.approvedDate)}</TableCell>
                                                    <TableCell>{admin.highestRolePriority}</TableCell>
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
                    <Button onClick={() => setViewDialogOpen(false)} color="primary">
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default ManageMovementRequests;