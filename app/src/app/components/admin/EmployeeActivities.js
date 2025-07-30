"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useRouter } from 'next/navigation';
import {
    Box, Button, Card, CardContent, Chip, CircularProgress, Dialog, DialogActions,
    DialogContent, DialogTitle, Divider, FormControl, Grid, IconButton, InputLabel,
    List, ListItem, ListItemText, Menu, MenuItem, Paper, Select, Table, TableBody,
    TableCell, TableContainer, TableHead, TablePagination, TableRow, TextField, Typography
} from "@mui/material";
import {
    MoreHoriz as MoreHorizIcon,
    Close as CloseIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon,
    Info as InfoIcon,
    Person as PersonIcon,
    Event as EventIcon,
    AccessTime as AccessTimeIcon,
    Description as DescriptionIcon,
    Visibility as VisibilityIcon
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

const ActivityDetailsDialog = React.memo(({
                                              open,
                                              onClose,
                                              activity
                                          }) => {
    const formatDate = useCallback((dateString) => {
        if (!dateString) return "-";
        const date = new Date(dateString);
        return isNaN(date.getTime()) ? "-" : date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }, []);

    const formatTime = useCallback((timeString) => {
        if (!timeString) return "-";
        return timeString;
    }, []);

    const getStatusColor = useCallback((activity) => {
        if (!activity) return "default";
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "success";
        if (activity.isUnSuccessful || activity.isUnauthorized) return "error";
        if (activity.hasIssues && !activity.isResolved) return "warning";
        return "success";
    }, []);

    const getStatusText = useCallback((activity) => {
        if (!activity) return "Unknown";
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "Leave Approved";
        if (activity.leaveStatus === 'LEAVE_REQUESTED') return "Leave Requested";
        if (activity.isUnSuccessful) return "Unsuccessful";
        if (activity.isUnauthorized) return "Unauthorized";
        if (activity.hasIssues && !activity.isResolved) return "Has Issues";
        if (activity.isResolved) return "Resolved";
        return "Normal";
    }, []);

    const getActivityType = useCallback((activity) => {
        if (!activity) return "Unknown";
        if (activity.attendanceType === 'ABSENT') return "Absent";
        if (activity.leaveStatus === 'FULL_LEAVE') return "Full Leave";
        if (activity.leaveStatus === 'SHORT_LEAVE') return "Short Leave";
        if (activity.attendanceType === 'HALF_DAY') return "Half Day";
        if (activity.isLate && activity.attendanceType === 'FULL_DAY') return "Late (Full Day)";
        if (activity.isLate) return "Late";
        return "Present";
    }, []);

    const booleanFields = useMemo(() => [
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
    ], []);

    const enumFields = useMemo(() => [
        { key: 'attendanceType', label: 'Attendance Type', mapping: ATTENDANCE_TYPE_LABELS },
        { key: 'leaveStatus', label: 'Leave Status', mapping: LEAVE_STATUS_LABELS },
        { key: 'payStatus', label: 'Pay Status', mapping: PAY_STATUS_LABELS },
        { key: 'resolve', label: 'Resolve Type', mapping: RESOLVE_TYPE_LABELS }
    ], []);

    if (!activity) return null;

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
                    Activity Details - {activity.userId || activity.employeeId || "Unknown"}
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
                                            secondary={activity.userId || activity.employeeId || "-"}
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
                                            primary="Roster Type"
                                            secondary={activity.rosterType || "NONE"}
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
                                                        {inOut.employeeID || "-"}
                                                    </Typography>
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Type
                                                    </Typography>
                                                    <Chip
                                                        label={inOut.inOutType || "Unknown"}
                                                        size="small"
                                                        color={
                                                            inOut.inOutType === 'MORNING_IN' ? "success" :
                                                                inOut.inOutType === 'EVENING_OUT' ? "error" : "default"
                                                        }
                                                        variant="outlined"
                                                    />
                                                </Grid>
                                                <Grid item xs={6} md={3}>
                                                    <Typography variant="body2" color="textSecondary">
                                                        Time
                                                    </Typography>
                                                    <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                        {inOut.punchTypeTime || "-"}
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
                                            </Grid>
                                        </Box>
                                    ))}
                                </CardContent>
                            </Card>
                        </Grid>
                    )}
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
    } = useSelector(selectActivitiesData);

    const isFiltering = useSelector(selectIsFiltering);

    // State for menu and dialog
    const [anchorEl, setAnchorEl] = useState(null);
    const [selectedActivity, setSelectedActivity] = useState(null);
    const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);

    const openMenu = Boolean(anchorEl);

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

    const handleMenuClick = (event, activity) => {
        setAnchorEl(event.currentTarget);
        setSelectedActivity(activity);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    const handleViewDetails = () => {
        setDetailsDialogOpen(true);
        handleMenuClose();
    };

    const handleMenuItemClick = (path) => {
        if (selectedActivity) {
            router.push(`${path}/${selectedActivity.userId || selectedActivity.employeeId}`);
        }
        handleMenuClose();
    };

    const handleCloseDetailsDialog = () => {
        setDetailsDialogOpen(false);
    };

    const getActivityType = useCallback((activity) => {
        if (!activity) return "Unknown";
        if (activity.attendanceType === 'ABSENT') return "Absent";
        if (activity.leaveStatus === 'FULL_LEAVE') return "Full Leave";
        if (activity.leaveStatus === 'SHORT_LEAVE') return "Short Leave";
        if (activity.attendanceType === 'HALF_DAY') return "Half Day";
        if (activity.isLate && activity.attendanceType === 'FULL_DAY') return "Late (Full Day)";
        if (activity.isLate) return "Late";
        if (activity.attendanceType === 'FULL_DAY') return "Present";
        return "Present";
    }, []);

    const getAttendanceStatus = useCallback((activity) => {
        if (!activity) return "Unknown";
        if (activity.isHoliday) return "HOLIDAY";
        if (activity.isUnauthorized) return "Unauthorized";
        if (activity.attendanceType === 'ABSENT') return "Absent";
        if (activity.isUnSuccessful) return "Unsuccessful";
        if (activity.hasIssues && !activity.isResolved) return "Has Issues";
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "Leave Approved";
        if (activity.leaveStatus === 'FULL_LEAVE' || activity.leaveStatus === 'SHORT_LEAVE') return "On Leave";
        if (activity.isLate) return "Late";
        return "Present";
    }, []);

    const getAttendanceStatusColor = useCallback((activity) => {
        if (!activity) return "default";
        if (activity.isUnauthorized || activity.attendanceType === 'ABSENT' || activity.isUnSuccessful) return "error";
        if (activity.hasIssues && !activity.isResolved) return "warning";
        if (activity.leaveStatus === 'LEAVE_APPROVED' || activity.leaveStatus === 'FULL_LEAVE' || activity.leaveStatus === 'SHORT_LEAVE') return "info";
        if (activity.isLate) return "warning";
        return "success";
    }, []);

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
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
        );
    }

    return (
        <Box sx={{ p: 3, backgroundColor: "#fff", minHeight: "100vh" }}>
            <Typography variant="h4" gutterBottom color="textPrimary">
                Employee Activities
            </Typography>

            {/* Action Menu */}
            <Menu
                id="employee-actions-menu"
                anchorEl={anchorEl}
                open={openMenu}
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
                <MenuItem onClick={() => handleMenuItemClick('/all-movements')}>
                    Movements
                </MenuItem>
                <MenuItem onClick={() => handleMenuItemClick('/all-leaves')}>
                    Leave
                </MenuItem>
                <MenuItem onClick={() => handleMenuItemClick('/single-employee-activities')}>
                    Attendance
                </MenuItem>
                <MenuItem onClick={() => handleMenuItemClick('/in-outs')}>
                    In-Outs
                </MenuItem>
                <MenuItem onClick={() => handleMenuItemClick('/no-pay-leaves')}>
                   No-Pay
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
                            <TableCell><strong>Roster Type</strong></TableCell>
                            <TableCell align="center"><strong>Actions</strong></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filteredActivities.map((activity) => (
                            <TableRow key={activity.id} hover>
                                <TableCell>
                                    <Typography variant="body1" fontWeight="medium">
                                        {activity.userId || activity.employeeId}
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
                                        label={activity.leaveStatus ? LEAVE_STATUS_LABELS[activity.leaveStatus] : getAttendanceStatus(activity)}
                                        color={
                                            activity.leaveStatus === 'LEAVE_APPROVED' ? 'success' :
                                                activity.leaveStatus === 'LEAVE_REQUESTED' ? 'warning' :
                                                    getAttendanceStatusColor(activity)
                                        }
                                        size="small"
                                        sx={{
                                            minWidth: '90px',
                                            fontWeight: 'medium',
                                            textTransform: activity.leaveStatus ? 'uppercase' : 'none'
                                        }}
                                    />
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2" sx={{
                                        fontFamily: 'monospace',
                                    }}>
                                        {activity.rosterType || 'Not Recorded'}
                                    </Typography>
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
            />
        </Box>
    );
};
EmployeeActivities.displayName = "EmployeeActivities";
export default EmployeeActivities;