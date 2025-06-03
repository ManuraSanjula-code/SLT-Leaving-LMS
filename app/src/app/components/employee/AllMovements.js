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

const ManageMovementRequests = ({ isAdmin = false, userId = null }) => {
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

    // Local UI state
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [currentEdit, setCurrentEdit] = useState(null);
    const [editValues, setEditValues] = useState({
        happenDate: "",
        destination: "",
        movementType: ""
    });
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteMovementId, setDeleteMovementId] = useState(null);
    const [viewDialogOpen, setViewDialogOpen] = useState(false);
    const [viewMovementData, setViewMovementData] = useState(null);

    // Fetch movement data
    useEffect(() => {
        if (userId == null) userId = sessionStorage.getItem('userId');
        console.log(userId)
        if (userId) {
            dispatch(fetchMovementRequests({
                isAdmin,
                userId,
                page: pagination.currentPage,
                size: pagination.pageSize
            }));
        }
    }, [dispatch, isAdmin, userId, pagination.currentPage, pagination.pageSize]);

    const isApproved = (request) => {
        return request.status === "Approved" || request.accepted === true;
    };

    const handleSelect = (id) => {
        const request = movementRequests.find(req => req.id === id);
        if (request && isApproved(request)) return;

        if (selected.includes(id)) {
            dispatch(setSelected(selected.filter(item => item !== id)));
        } else {
            dispatch(setSelected([...selected, id]));
        }
    };

    const handleSelectAll = () => {
        const selectableRequests = filteredMovementRequests.filter(request => !isApproved(request));

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
                if (request && request.publicId && !isApproved(request)) {
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

    const handleOpenEditDialog = (request) => {
        setCurrentEdit(request);
        setEditValues({
            happenDate: request.happenDate ? new Date(request.happenDate).toISOString().split('T')[0] : "",
            destination: request.destination || "",
            movementType: request.type || ""
        });
        setEditDialogOpen(true);
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditValues(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSaveEdit = async () => {
        try {
            if (!currentEdit || !currentEdit.publicId) {
                throw new Error("Invalid movement request data");
            }

            if (isApproved(currentEdit)) {
                throw new Error("Cannot edit approved movement requests");
            }

            await dispatch(updateMovementRequest({
                publicId: currentEdit.publicId,
                happenDate: editValues.happenDate,
                destination: editValues.destination,
                movementType: editValues.movementType
            })).unwrap();

            setEditDialogOpen(false);
        } catch (err) {
            console.error("Error updating movement request:", err);
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

            if (request.startDate && userId) {
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
                                disabled={selected.length === 0}
                            >
                                Delete Selected ({selected.length})
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
                                                        selected.length < filteredMovementRequests.filter(req => !isApproved(req)).length
                                                    }
                                                    checked={
                                                        selected.length === filteredMovementRequests.filter(req => !isApproved(req)).length &&
                                                        filteredMovementRequests.filter(req => !isApproved(req)).length > 0
                                                    }
                                                    onChange={handleSelectAll}
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
                                        {filteredMovementRequests.map((request) => (
                                            <TableRow key={request.id}>
                                                <TableCell padding="checkbox">
                                                    <Tooltip
                                                        title={isApproved(request) ? "Approved movements cannot be selected" : ""}>
                            <span>
                              <Checkbox
                                  checked={selected.includes(request.id)}
                                  onChange={() => handleSelect(request.id)}
                                  disabled={isApproved(request) || request.reject}
                              />
                            </span>
                                                    </Tooltip>
                                                </TableCell>
                                                <TableCell>{request.employeeId || ""}</TableCell>
                                                <TableCell>{request.type || "Unknown"}</TableCell>
                                                <TableCell>{request.destination || "Not specified"}</TableCell>
                                                <TableCell>{request.startDate || "Not specified"}</TableCell>
                                                <TableCell>{request.endDate || "Not specified"}</TableCell>
                                                <TableCell>{request.status || "Unknown"}</TableCell>
                                                <TableCell>
                                                    <Tooltip
                                                        title={isApproved(request) ? "Approved movements cannot be edited" : "Edit"}>
                            <span>
                              <IconButton
                                  onClick={() => !isApproved(request) && handleOpenEditDialog(request)}
                                  color="primary"
                                  disabled={isApproved(request) || request.reject}
                              >
                                <EditIcon />
                              </IconButton>
                            </span>
                                                    </Tooltip>
                                                    <Tooltip
                                                        title={isApproved(request) ? "Approved movements cannot be deleted" : "Delete"}>
                            <span>
                              <IconButton
                                  onClick={() => !isApproved(request) && handleOpenDeleteDialog(request.publicId)}
                                  color="error"
                                  disabled={isApproved(request) || request.reject}
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
                                        ))}
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

            {/* Edit Dialog */}
            <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)}>
                <DialogTitle>Edit Movement Request</DialogTitle>
                <DialogContent>
                    <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
                        <TextField
                            name="happenDate"
                            label="Date"
                            type="date"
                            value={editValues.happenDate}
                            onChange={handleEditChange}
                            fullWidth
                            InputLabelProps={{ shrink: true }}
                        />

                        <TextField
                            name="destination"
                            label="Destination"
                            value={editValues.destination}
                            onChange={handleEditChange}
                            fullWidth
                        />

                        <FormControl fullWidth margin="normal">
                            <InputLabel>Movement Type</InputLabel>
                            <Select
                                name="movementType"
                                value={editValues.movementType}
                                onChange={handleEditChange}
                                label="Movement Type"
                            >
                                <MenuItem value="ABSENT">Absent</MenuItem>
                                <MenuItem value="LATEWORK">Late Work</MenuItem>
                                <MenuItem value="UNSUCCESSFUL">Unsuccessful</MenuItem>
                                <MenuItem value="UNAUTHORIZED">Unauthorized</MenuItem>
                                <MenuItem value="REMOTEWORK">Remote Work</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setEditDialogOpen(false)} startIcon={<CloseIcon />}>
                        Cancel
                    </Button>
                    <Button onClick={handleSaveEdit}  variant="contained" color="primary" startIcon={<SaveIcon />}>
                        Save
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Confirmation Dialog */}
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

            {/* View Details Dialog */}
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
                                            <TableCell>{viewMovementData.type}</TableCell>
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
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Status</TableCell>
                                            <TableCell>{viewMovementData.status}</TableCell>
                                            <TableCell component="th" scope="row" sx={{ fontWeight: 'bold' }}>Comment</TableCell>
                                            <TableCell>{viewMovementData.comment || "No comment"}</TableCell>
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

                            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                Approval History
                            </Typography>
                            {viewMovementData.adminsTra && viewMovementData.adminsTra.length > 0 ? (
                                <TableContainer component={Paper}>
                                    <Table>
                                        <TableHead>
                                            <TableRow>
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
                                                    <TableCell>{`${admin.firstName || ''} ${admin.lastName || ''}`}</TableCell>
                                                    <TableCell>{admin.employeeId}</TableCell>
                                                    <TableCell>{admin.sltId}</TableCell>
                                                    <TableCell>{formatDate(admin.approvedDate)}</TableCell>
                                                    <TableCell>{admin.highestRolePriority}</TableCell>
                                                    <TableCell>
                                                        {admin.accepted ? (
                                                            <Typography color="primary">Approved</Typography>
                                                        ) : (
                                                            <Typography color="error">Rejected</Typography>
                                                        )}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </TableContainer>
                            ) : (
                                <Alert severity="info">No approval history available.</Alert>
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