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

const ATTENDANCE_TYPE_LABELS = {
    'FULL_DAY': 'Full Day',
    'HALF_DAY': 'Half Day',
    'ABSENT': 'Absent'
};

const LEAVE_STATUS_LABELS = {
    'NO_LEAVE': 'No Leave',
    'FULL_LEAVE': 'Full Leave',
    'SHORT_LEAVE': 'Short Leave',
    'LEAVE_REQUESTED': 'Leave Requested',
    'LEAVE_APPROVED': 'Leave Approved'
};

const PAY_STATUS_LABELS = {
    'NO_PAY': 'No Pay'
};

const RESOLVE_TYPE_LABELS = {
    'VIA_MOVEMENT': 'Via Movement',
    'VIA_LEAVE': 'Via Leave'
};

const ActivityDetailsDialog = React.memo(({ open, onClose, activity, getActivityType, getStatusText, getStatusColor }) => {
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
        { key: 'isLate', label: 'Late' },
        { key: 'isLateCovered', label: 'Late Covered' },
        { key: 'isUnauthorized', label: 'Unauthorized' },
        { key: 'isUnSuccessful', label: 'Unsuccessful' },
        { key: 'isHoliday', label: 'Holiday' },
        { key: 'isResolved', label: 'Resolved' },
        { key: 'hasIssues', label: 'Has Issues' },
        { key: 'isManual', label: 'Manual Entry' },
        { key: 'isActive', label: 'Active' },
        { key: 'isFullDay', label: 'Full Day' },
        { key: 'isHalfDay', label: 'Half Day' },
        { key: 'isAbsent', label: 'Absent' },
        { key: 'isNoPay', label: 'No Pay' }
    ];

    const enumFields = [
        { key: 'attendanceType', label: 'Attendance Type', mapping: ATTENDANCE_TYPE_LABELS },
        { key: 'leaveStatus', label: 'Leave Status', mapping: LEAVE_STATUS_LABELS },
        { key: 'payStatus', label: 'Pay Status', mapping: PAY_STATUS_LABELS },
        { key: 'resolve', label: 'Resolve Type', mapping: RESOLVE_TYPE_LABELS }
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
            <DialogTitle sx={{
                bgcolor: 'primary.main',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <InfoIcon />
                    Activity Details - {activity.employeeId}
                </Box>
                <IconButton onClick={onClose} sx={{ color: 'white' }} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent sx={{ p: 3 }}>
                <Grid container spacing={3}>
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
                                    <ListItem>
                                        <ListItemText
                                            primary="Terminal ID"
                                            secondary={activity.terminalId || "-"}
                                        />
                                    </ListItem>
                                </List>
                            </CardContent>
                        </Card>
                    </Grid>

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

                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom color="primary">
                                    Attendance & Leave Information
                                </Typography>
                                <Grid container spacing={2}>
                                    {enumFields.map((field) => {
                                        const value = activity[field.key];
                                        const displayValue = value ? (field.mapping[value] || value) : 'Not Set';
                                        return (
                                            <Grid item xs={6} md={3} key={field.key}>
                                                <Typography variant="body2" color="textSecondary">
                                                    {field.label}
                                                </Typography>
                                                <Chip
                                                    label={displayValue}
                                                    size="small"
                                                    color={value ? "info" : "default"}
                                                    variant="outlined"
                                                    sx={{ mt: 0.5 }}
                                                />
                                            </Grid>
                                        );
                                    })}
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>

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
                                                        {inOut.employeeId || "-"}
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
                                                        {inOut.terminalId ? inOut.terminalId.trim() : "-"}
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
                                            </Grid>
                                        </Box>
                                    ))}
                                </CardContent>
                            </Card>
                        </Grid>
                    )}

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
                                            {activity.terminalId ? activity.terminalId.trim() : "-"}
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
                                            label={activity.isManual ? "Manual" : "Automatic"}
                                            size="small"
                                            color={activity.isManual ? "warning" : "info"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Record Status
                                        </Typography>
                                        <Chip
                                            label={activity.isActive !== false ? "Active" : "Inactive"}
                                            size="small"
                                            color={activity.isActive !== false ? "success" : "error"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Late Cover
                                        </Typography>
                                        <Chip
                                            label={activity.isLateCovered ? "Yes" : "No"}
                                            size="small"
                                            color={activity.isLateCovered ? "warning" : "default"}
                                            variant="outlined"
                                        />
                                    </Grid>
                                    <Grid item xs={6} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            ETL Run Time
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                                            {formatDate(activity.etlRunTime)}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </DialogContent>

            <DialogActions sx={{ p: 2 }}>
                <Button onClick={onClose} variant="contained">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
});
ActivityDetailsDialog.displayName = 'ActivityDetailsDialog';
const EmployeeActivities = () => {
    const dispatch = useDispatch();
    const router = useRouter();

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

    const isFiltering = useSelector(selectIsFiltering);

    const [anchorEl, setAnchorEl] = React.useState(null);
    const [selectedEmployeeId, setSelectedEmployeeId] = React.useState(null);
    const [selectedActivity, setSelectedActivity] = React.useState(null);
    const open = Boolean(anchorEl);

    const [detailsDialogOpen, setDetailsDialogOpen] = React.useState(false);

    useEffect(() => {
        if (!isFiltering) {
            dispatch(fetchActivityRecords({ page, rowsPerPage }));
        }
    }, [dispatch, page, rowsPerPage, isFiltering]);

    const handleChangePage = (event, newPage) => {
        dispatch(setPage(newPage));
    };

    const handleChangeRowsPerPage = (event) => {
        dispatch(setRowsPerPage(parseInt(event.target.value, 10)));
    };

    const handleSearchTermChange = (event) => {
        dispatch(setSearchTerm(event.target.value));
    };

    const handleFilterTypeChange = (event) => {
        dispatch(setFilterType(event.target.value));
    };

    const handleFilterStatusChange = (event) => {
        dispatch(setFilterStatus(event.target.value));
    };

    const handleFilterIssueChange = (event) => {
        dispatch(setFilterIssue(event.target.value));
    };

    const handleClearFilters = () => {
        dispatch(clearFilters());
    };

    const handleMenuClick = useCallback((event, activity) => {
        setAnchorEl(event.currentTarget);
        setSelectedEmployeeId(activity.employeeId);
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

    const handleViewDetails = useCallback(() => {
        if (selectedActivity) {
            setDetailsDialogOpen(true);
        }
        handleMenuClose();
    }, [selectedActivity, handleMenuClose]);

    const handleCloseDetailsDialog = useCallback(() => {
        setDetailsDialogOpen(false);
        setSelectedActivity(null);
    }, []);

    const getStatusColor = (activity) => {
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "success";
        if (activity.isUnSuccessful || activity.isUnauthorized) return "error";
        if (activity.hasIssues && !activity.isResolved) return "warning";
        return "success";
    };

    const getStatusText = (activity) => {
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "Leave Approved";
        if (activity.leaveStatus === 'LEAVE_REQUESTED') return "Leave Requested";
        if (activity.isUnSuccessful) return "Unsuccessful";
        if (activity.isUnauthorized) return "Unauthorized";
        if (activity.hasIssues && !activity.isResolved) return "Has Issues";
        if (activity.isResolved) return "Resolved";
        return "Normal";
    };

    const getActivityType = (activity) => {
        if (activity.attendanceType === 'ABSENT') return "Absent";
        if (activity.leaveStatus === 'FULL_LEAVE') return "Full Leave";
        if (activity.leaveStatus === 'SHORT_LEAVE') return "Short Leave";
        if (activity.attendanceType === 'HALF_DAY') return "Half Day";
        if (activity.isLate && activity.attendanceType === 'FULL_DAY') return "Late (Full Day)";
        if (activity.isLate) return "Late";
        if (activity.attendanceType === 'FULL_DAY') return "Present";
        return "Present";
    };

    const getAttendanceStatus = (activity) => {
        if (activity.isUnauthorized) return "Unauthorized";
        if (activity.attendanceType === 'ABSENT') return "Absent";
        if (activity.isUnSuccessful) return "Unsuccessful";
        if (activity.hasIssues && !activity.isResolved) return "Has Issues";
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "Leave Approved";
        if (activity.leaveStatus === 'FULL_LEAVE' || activity.leaveStatus === 'SHORT_LEAVE') return "On Leave";
        if (activity.isLate) return "Late";
        return "Present";
    };

    const getAttendanceStatusColor = (activity) => {
        if (activity.isUnauthorized || activity.attendanceType === 'ABSENT' || activity.isUnSuccessful) return "error";
        if (activity.hasIssues && !activity.isResolved) return "warning";
        if (activity.leaveStatus === 'LEAVE_APPROVED' || activity.leaveStatus === 'FULL_LEAVE' || activity.leaveStatus === 'SHORT_LEAVE') return "info";
        if (activity.isLate) return "warning";
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
                                            {activity.userId}
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