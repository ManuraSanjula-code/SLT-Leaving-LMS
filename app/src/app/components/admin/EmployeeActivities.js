"use client";

import React, { useEffect, useCallback } from "react";
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
    IconButton,
    Menu,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemText,
    Grid,
    Divider,
} from "@mui/material";
import { ThemeProvider, CssBaseline, createTheme } from "@mui/material";
import {
    MoreHoriz as MoreHorizIcon,
    Person as PersonIcon,
    Event as EventIcon,
    AccessTime as AccessTimeIcon,
    Description as DescriptionIcon,
    Info as InfoIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon,
    Close as CloseIcon,
    Visibility as VisibilityIcon,
} from '@mui/icons-material';
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
import { useRouter } from 'next/navigation';

const theme = createTheme();

// Optimized Activity Details Dialog Component - Using React.memo for performance
const ActivityDetailsDialog = React.memo(({ open, onClose, activity, getActivityType, getStatusText, getStatusColor }) => {
    // Early return if dialog is not open to prevent unnecessary renders
    if (!open || !activity) return null;

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const formatTime = (timeString) => {
        if (!timeString) return "-";
        return timeString;
    };

    const booleanFields = [
        { key: 'fullDay', label: 'Full Day' },
        { key: 'late', label: 'Late' },
        { key: 'lateCover', label: 'Late Cover' },
        { key: 'halfDay', label: 'Half Day' },
        { key: 'fullLeave', label: 'Full Leave' },
        { key: 'shortLeave', label: 'Short Leave' },
        { key: 'absent', label: 'Absent' },
        { key: 'unSuccessful', label: 'Unsuccessful' },
        { key: 'noPay', label: 'No Pay' },
        { key: 'nopay', label: 'No Pay (Alt)' },
        { key: 'issues', label: 'Issues' },
        { key: 'unAuthorized', label: 'Unauthorized' },
        { key: 'resolve', label: 'Resolved' },
        { key: 'leaveSuccess', label: 'Leave Success' },
        { key: 'leaveReq', label: 'Leave Request' },
        { key: 'active', label: 'Active' },
        { key: 'manual', label: 'Manual Entry' }
    ];

    return (
        <Dialog
            open={open}
            onClose={onClose}
            fullWidth
            maxWidth="md"
            PaperProps={{
                sx: {
                    borderRadius: 2,
                    boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
                }
            }}
        >
            {/* Dialog Header */}
            <DialogTitle sx={{
                bgcolor: 'primary.main',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <InfoIcon />
                    Activity Details - {activity.employeeID}
                </Box>
                <IconButton onClick={onClose} sx={{ color: 'white' }} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            {/* Dialog Content */}
            <DialogContent sx={{ p: 3 }}>
                <Grid container spacing={3}>
                    {/* Basic Information Card */}
                    <Grid item xs={12} md={6}>
                        <Card variant="outlined" sx={{ height: '100%' }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom color="primary">
                                    <PersonIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                    Basic Information
                                </Typography>
                                <List dense>
                                    <ListItem>
                                        <ListItemText
                                            primary="Employee ID"
                                            secondary={activity.employeeID || "-"}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="User ID"
                                            secondary={activity.userId || "-"}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Public ID"
                                            secondary={activity.publicId || "-"}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Date"
                                            secondary={formatDate(activity.date)}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Arrival Date"
                                            secondary={formatDate(activity.arrivalDate)}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Due Date (UA)"
                                            secondary={formatDate(activity.dueDateForUA)}
                                        />
                                    </ListItem>
                                </List>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Time Information Card */}
                    <Grid item xs={12} md={6}>
                        <Card variant="outlined" sx={{ height: '100%' }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom color="primary">
                                    <AccessTimeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                    Time Information
                                </Typography>
                                <List dense>
                                    <ListItem>
                                        <ListItemText
                                            primary="Arrival Time"
                                            secondary={formatTime(activity.arrivalTime)}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Left Time"
                                            secondary={formatTime(activity.leftTime)}
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Activity Type"
                                            secondary={
                                                <Chip
                                                    label={getActivityType(activity)}
                                                    size="small"
                                                    color="primary"
                                                    variant="outlined"
                                                />
                                            }
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemText
                                            primary="Activity Status"
                                            secondary={
                                                <Chip
                                                    label={getStatusText(activity)}
                                                    size="small"
                                                    color={getStatusColor(activity)}
                                                    variant="outlined"
                                                />
                                            }
                                        />
                                    </ListItem>
                                </List>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Issue Description */}
                    {activity.issueDescription && (
                        <Grid item xs={12}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" gutterBottom color="primary">
                                        <DescriptionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                        Issue Description
                                    </Typography>
                                    <Typography variant="body2" sx={{
                                        bgcolor: 'grey.50',
                                        p: 2,
                                        borderRadius: 1,
                                        whiteSpace: 'pre-wrap'
                                    }}>
                                        {activity.issueDescription}
                                    </Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                    )}

                    {/* Status Flags */}
                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom color="primary">
                                    Status Flags
                                </Typography>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    {booleanFields.map((field) => {
                                        const isTrue = activity[field.key];
                                        return (
                                            <Chip
                                                key={field.key}
                                                label={field.label}
                                                icon={isTrue ? <CheckCircleIcon /> : <CancelIcon />}
                                                color={isTrue ? "success" : "default"}
                                                variant={isTrue ? "filled" : "outlined"}
                                                size="small"
                                                sx={{
                                                    opacity: isTrue ? 1 : 0.6,
                                                    '& .MuiChip-icon': {
                                                        fontSize: '0.75rem'
                                                    }
                                                }}
                                            />
                                        );
                                    })}
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* In-Out Records */}
                    {activity.inOutDTOs && activity.inOutDTOs.length > 0 && (
                        <Grid item xs={12}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" gutterBottom color="primary">
                                        <EventIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                        In-Out Records
                                    </Typography>
                                    {activity.inOutDTOs.map((inOut, index) => (
                                        <Box key={index} sx={{ mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                                            <Grid container spacing={2}>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Employee ID
                                                    </Typography>
                                                    <Typography variant="body2">
                                                        {inOut.employeeID || "-"}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Type
                                                    </Typography>
                                                    <Chip
                                                        label={inOut.moaning ? "Morning IN" : inOut.evening ? "Evening OUT" : "Unknown"}
                                                        size="small"
                                                        color={inOut.moaning ? "success" : inOut.evening ? "error" : "default"}
                                                        variant="outlined"
                                                    />
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Time
                                                    </Typography>
                                                    <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                        {inOut.timeMoa || inOut.timeEve || "-"}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Terminal
                                                    </Typography>
                                                    <Typography variant="body2" sx={{
                                                        fontFamily: 'monospace',
                                                        fontSize: '0.8rem',
                                                        bgcolor: 'grey.100',
                                                        p: 0.5,
                                                        borderRadius: 0.5,
                                                        display: 'inline-block'
                                                    }}>
                                                        {inOut.terminalID ? inOut.terminalID.trim() : "-"}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        In/Out Status
                                                    </Typography>
                                                    <Chip
                                                        label={inOut.inOut === 1 ? "IN" : "OUT"}
                                                        size="small"
                                                        color={inOut.inOut === 1 ? "success" : "error"}
                                                        variant="filled"
                                                    />
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Date
                                                    </Typography>
                                                    <Typography variant="body2">
                                                        {formatDate(inOut.date)}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Punch In Morning
                                                    </Typography>
                                                    <Typography variant="body2">
                                                        {formatDate(inOut.punchInMoa)}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Punch In Evening
                                                    </Typography>
                                                    <Typography variant="body2">
                                                        {formatDate(inOut.punchInEv)}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Past Status
                                                    </Typography>
                                                    <Chip
                                                        label={inOut.past ? "Past" : "Current"}
                                                        size="small"
                                                        color={inOut.past ? "warning" : "info"}
                                                        variant="outlined"
                                                    />
                                                </Grid>

                                                {/* Access Log Details */}
                                                {inOut.accessLog && (
                                                    <Grid item xs={12}>
                                                        <Divider sx={{ my: 1 }} />
                                                        <Typography variant="subtitle2" color="primary" gutterBottom>
                                                            Access Log Details
                                                        </Typography>
                                                        <Grid container spacing={2}>
                                                            <Grid item xs={6} md={3}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    Log Date
                                                                </Typography>
                                                                <Typography variant="body2">
                                                                    {inOut.accessLog.logDate || "-"}
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item xs={6} md={3}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    Log Time
                                                                </Typography>
                                                                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                                    {inOut.accessLog.logTime ? inOut.accessLog.logTime.trim() : "-"}
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item xs={6} md={3}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    Terminal ID
                                                                </Typography>
                                                                <Typography variant="body2" sx={{
                                                                    fontFamily: 'monospace',
                                                                    fontSize: '0.75rem'
                                                                }}>
                                                                    {inOut.accessLog.terminalID ? inOut.accessLog.terminalID.trim() : "-"}
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item xs={6} md={3}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    In/Out
                                                                </Typography>
                                                                <Typography variant="body2">
                                                                    {inOut.accessLog.inOut ? inOut.accessLog.inOut.trim() : "-"}
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item xs={6} md={3}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    Read Status
                                                                </Typography>
                                                                <Typography variant="body2">
                                                                    {inOut.accessLog.readStatus || "-"}
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item xs={6} md={3}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    Processed
                                                                </Typography>
                                                                <Chip
                                                                    label={inOut.accessLog.processed ? "Yes" : "No"}
                                                                    size="small"
                                                                    color={inOut.accessLog.processed ? "success" : "error"}
                                                                    variant="outlined"
                                                                />
                                                            </Grid>
                                                            <Grid item xs={12} md={6}>
                                                                <Typography variant="body2" color="textSecondary">
                                                                    ETL Run Time
                                                                </Typography>
                                                                <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                                                                    {formatDate(inOut.accessLog.etlRunTime)}
                                                                </Typography>
                                                            </Grid>
                                                        </Grid>
                                                    </Grid>
                                                )}
                                            </Grid>
                                        </Box>
                                    ))}
                                </CardContent>
                            </Card>
                        </Grid>
                    )}

                    {/* Edited By Information */}
                    {activity.editedByDTOs && activity.editedByDTOs.length > 0 && (
                        <Grid item xs={12}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" gutterBottom color="primary">
                                        Edit History
                                    </Typography>
                                    {activity.editedByDTOs.map((edit, index) => (
                                        <Box key={index} sx={{ mb: 1, p: 1, bgcolor: 'grey.50', borderRadius: 1 }}>
                                            <Typography variant="body2">
                                                Edited by: {edit.editorName || edit.editorId || "Unknown"}
                                            </Typography>
                                            <Typography variant="body2" color="textSecondary">
                                                Date: {formatDate(edit.editDate)}
                                            </Typography>
                                        </Box>
                                    ))}
                                </CardContent>
                            </Card>
                        </Grid>
                    )}

                    {/* System Information */}
                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom color="primary">
                                    System Information
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid item xs={12} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Terminal ID
                                        </Typography>
                                        <Typography variant="body2" sx={{
                                            fontFamily: 'monospace',
                                            fontSize: '0.8rem',
                                            bgcolor: 'grey.100',
                                            p: 0.5,
                                            borderRadius: 0.5,
                                            wordBreak: 'break-all'
                                        }}>
                                            {activity.terminalID ? activity.terminalID.trim() : "-"}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Record ID
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                                            {activity.id || "-"}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Entry Type
                                        </Typography>
                                        <Chip
                                            label={activity.manual ? "Manual" : "Automatic"}
                                            size="small"
                                            color={activity.manual ? "warning" : "info"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Record Status
                                        </Typography>
                                        <Chip
                                            label={activity.active !== false ? "Active" : "Inactive"}
                                            size="small"
                                            color={activity.active !== false ? "success" : "error"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Late Cover
                                        </Typography>
                                        <Chip
                                            label={activity.lateCover ? "Yes" : "No"}
                                            size="small"
                                            color={activity.lateCover ? "warning" : "default"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Leave Request
                                        </Typography>
                                        <Chip
                                            label={activity.leaveReq ? "Yes" : "No"}
                                            size="small"
                                            color={activity.leaveReq ? "info" : "default"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </DialogContent>

            {/* Dialog Actions */}
            <DialogActions sx={{ p: 2 }}>
                <Button onClick={onClose} variant="contained">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
});

const EmployeeActivities = () => {
    const dispatch = useDispatch();
    const router = useRouter();

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

    // Menu state
    const [anchorEl, setAnchorEl] = React.useState(null);
    const [selectedEmployeeId, setSelectedEmployeeId] = React.useState(null);
    const [selectedActivity, setSelectedActivity] = React.useState(null);
    const open = Boolean(anchorEl);

    // Details dialog state
    const [detailsDialogOpen, setDetailsDialogOpen] = React.useState(false);

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

    // Menu handlers
    const handleMenuClick = useCallback((event, activity) => {
        setAnchorEl(event.currentTarget);
        setSelectedEmployeeId(activity.userId);
        setSelectedActivity(activity);
    }, []);

    const handleMenuClose = useCallback(() => {
        setAnchorEl(null);
        setSelectedEmployeeId(null);
        setSelectedActivity(null);
    }, []);

    const handleMenuItemClick = useCallback((path) => {
        handleMenuClose();
        router.push(path);
    }, [router, handleMenuClose]);

    // View details handler - optimized with useCallback
    const handleViewDetails = useCallback(() => {
        if (selectedActivity) {
            setDetailsDialogOpen(true);
        }
        handleMenuClose();
    }, [selectedActivity, handleMenuClose]);

    // Close details dialog
    const handleCloseDetailsDialog = useCallback(() => {
        setDetailsDialogOpen(false);
        setSelectedActivity(null);
    }, []);

    // Get status chip color
    const getStatusColor = (activity) => {
        if (activity.leaveSuccess) return "success";
        if (activity.unSuccessful || activity.unAuthorized) return "error";
        if (activity.issues && !activity.resolve) return "warning";
        return "success";
    };

    // Get status text
    const getStatusText = (activity) => {
        if (activity.leaveSuccess) return "Approved";
        if (activity.unSuccessful) return "Unsuccessful";
        if (activity.unAuthorized) return "Unauthorized";
        if (activity.issues && !activity.resolve) return "Has Issues";
        if (activity.resolve) return "Resolved";
        return "Normal";
    };

    // Get activity type - simplified for -admin view
    const getActivityType = (activity) => {
        if (activity.absent) return "Absent";
        if (activity.fullLeave) return "Full Leave";
        if (activity.shortLeave) return "Short Leave";
        if (activity.halfDay) return "Half Day";
        if (activity.late && activity.fullDay) return "Late (Full Day)";
        if (activity.late) return "Late";
        if (activity.fullDay) return "Present";
        return "Present";
    };

    // Get attendance status for -admin view
    const getAttendanceStatus = (activity) => {
        if (activity.unAuthorized) return "Unauthorized";
        if (activity.absent) return "Absent";
        if (activity.unSuccessful) return "Unsuccessful";
        if (activity.issues && !activity.resolve) return "Has Issues";
        if (activity.leaveSuccess) return "Leave Approved";
        if (activity.fullLeave || activity.shortLeave) return "On Leave";
        if (activity.late) return "Late";
        return "Present";
    };

    // Get status color for attendance
    const getAttendanceStatusColor = (activity) => {
        if (activity.unAuthorized || activity.absent || activity.unSuccessful) return "error";
        if (activity.issues && !activity.resolve) return "warning";
        if (activity.leaveSuccess || activity.fullLeave || activity.shortLeave) return "info";
        if (activity.late) return "warning";
        return "success";
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

                {/* Action Menu */}
                <Menu
                    id="employee-actions-menu"
                    aria-labelledby="employee-actions-button"
                    anchorEl={anchorEl}
                    open={open}
                    onClose={handleMenuClose}
                    anchorOrigin={{
                        vertical: 'top',
                        horizontal: 'left',
                    }}
                    transformOrigin={{
                        vertical: 'top',
                        horizontal: 'left',
                    }}
                >
                    <MenuItem onClick={handleViewDetails}>
                        <VisibilityIcon sx={{ mr: 1 }} fontSize="small" />
                        View Details
                    </MenuItem>
                    <MenuItem onClick={() => handleMenuItemClick(`/all-movements/${selectedEmployeeId}`)}>
                        Movements
                    </MenuItem>
                    <MenuItem onClick={() => handleMenuItemClick(`/all-leaves/${selectedEmployeeId}`)}>
                        Leave
                    </MenuItem>
                    <MenuItem onClick={() => handleMenuItemClick(`/single-employee-activities/${selectedEmployeeId}`)}>
                        Attendance
                    </MenuItem>
                    <MenuItem onClick={() => handleMenuItemClick(`/in-outs/${selectedEmployeeId}`)}>
                        In-Outs
                    </MenuItem>
                </Menu>

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
                        <InputLabel>Attendance Type</InputLabel>
                        <Select
                            value={filterType}
                            onChange={handleFilterTypeChange}
                            label="Attendance Type"
                        >
                            <MenuItem value="all">All Types</MenuItem>
                            <MenuItem value="fullDay">Present</MenuItem>
                            <MenuItem value="late">Late</MenuItem>
                            <MenuItem value="halfDay">Half Day</MenuItem>
                            <MenuItem value="absent">Absent</MenuItem>
                            <MenuItem value="fullLeave">Full Leave</MenuItem>
                            <MenuItem value="shortLeave">Short Leave</MenuItem>
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
                            <MenuItem value="Approved">Leave Approved</MenuItem>
                            <MenuItem value="Pending">Pending</MenuItem>
                            <MenuItem value="Not Approved">Issues/Unauthorized</MenuItem>
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

                {/* Table of Employee Activities - Clean Admin View */}
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell><strong>Employee ID</strong></TableCell>
                                <TableCell><strong>Date</strong></TableCell>
                                <TableCell><strong>In Time</strong></TableCell>
                                <TableCell><strong>Out Time</strong></TableCell>
                                <TableCell><strong>Attendance</strong></TableCell>
                                <TableCell><strong>Status</strong></TableCell>
                                <TableCell align="center"><strong>Actions</strong></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredActivities.map((activity) => (
                                <TableRow key={activity.id} hover>
                                    <TableCell>
                                        <Typography variant="body1" fontWeight="medium">
                                            {activity.employeeID}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2">
                                            {new Date(activity.arrivalDate).toLocaleDateString('en-US', {
                                                weekday: 'short',
                                                month: 'short',
                                                day: 'numeric'
                                            })}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" sx={{
                                            fontFamily: 'monospace',
                                            color: activity.arrivalTime ? 'text.primary' : 'text.secondary'
                                        }}>
                                            {activity.arrivalTime || 'Not Recorded'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Typography variant="body2" sx={{
                                            fontFamily: 'monospace',
                                            color: activity.leftTime ? 'text.primary' : 'text.secondary'
                                        }}>
                                            {activity.leftTime || 'Not Recorded'}
                                        </Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={getActivityType(activity)}
                                            size="small"
                                            variant="outlined"
                                            sx={{
                                                minWidth: '80px',
                                                fontWeight: 'medium'
                                            }}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={getAttendanceStatus(activity)}
                                            color={getAttendanceStatusColor(activity)}
                                            size="small"
                                            sx={{
                                                minWidth: '90px',
                                                fontWeight: 'medium'
                                            }}
                                        />
                                    </TableCell>
                                    <TableCell align="center">
                                        <IconButton
                                            onClick={(event) => handleMenuClick(event, activity)}
                                            aria-label="more actions"
                                            size="small"
                                            sx={{
                                                bgcolor: 'action.hover',
                                                '&:hover': {
                                                    bgcolor: 'primary.light',
                                                    color: 'white'
                                                }
                                            }}
                                        >
                                            <MoreHorizIcon />
                                        </IconButton>
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

                {/* Activity Details Dialog */}
                <ActivityDetailsDialog
                    open={detailsDialogOpen}
                    onClose={handleCloseDetailsDialog}
                    activity={selectedActivity}
                    getActivityType={getActivityType}
                    getStatusText={getStatusText}
                    getStatusColor={getStatusColor}
                />
            </Box>
        </ThemeProvider>
    );
};

export default EmployeeActivities;