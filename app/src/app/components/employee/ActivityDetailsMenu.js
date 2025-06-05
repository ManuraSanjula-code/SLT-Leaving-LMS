import React, { useState } from 'react';
import {
    Menu,
    MenuItem,
    IconButton,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    Typography,
    Box,
    Grid,
    Chip,
    Divider,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Card,
    CardContent,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Paper
} from '@mui/material';
import {
    MoreVert as MoreVertIcon,
    Visibility as VisibilityIcon,
    Person as PersonIcon,
    Event as EventIcon,
    AccessTime as AccessTimeIcon,
    Description as DescriptionIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon,
    Info as InfoIcon,
    Login as LoginIcon,
    Logout as LogoutIcon,
    Edit as EditHistoryIcon,
    Comment as CommentIcon
} from '@mui/icons-material';
import {EditIcon, TrashIcon} from "lucide-react";

// Activity Details Menu Component
const ActivityDetailsMenu = ({ activity, onEdit, onDelete, isAdmin }) => {
    const [anchorEl, setAnchorEl] = useState(null);
    const [showDetailsDialog, setShowDetailsDialog] = useState(false);
    const open = Boolean(anchorEl);

    const handleClick = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleViewDetails = () => {
        setShowDetailsDialog(true);
        handleClose();
    };

    const handleEdit = () => {
        onEdit(activity);
        handleClose();
    };

    const handleDelete = () => {
        onDelete(activity.publicId);
        handleClose();
    };

    return (
        <>
            <IconButton
                aria-label="more"
                id="activity-menu-button"
                aria-controls={open ? 'activity-menu' : undefined}
                aria-expanded={open ? 'true' : undefined}
                aria-haspopup="true"
                onClick={handleClick}
                size="small"
            >
                <MoreVertIcon />
            </IconButton>

            <Menu
                id="activity-menu"
                MenuListProps={{
                    'aria-labelledby': 'activity-menu-button',
                }}
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                PaperProps={{
                    style: {
                        maxHeight: 48 * 4.5,
                        width: '20ch',
                    },
                }}
            >
                <MenuItem onClick={handleViewDetails}>
                    <ListItemIcon>
                        <VisibilityIcon fontSize="small" />
                    </ListItemIcon>
                    View Details
                </MenuItem>

                {isAdmin && (
                    <MenuItem onClick={handleEdit}>
                        <ListItemIcon>
                            <EditIcon fontSize="small" />
                        </ListItemIcon>
                        Edit
                    </MenuItem>
                )}


                {isAdmin && activity.active && (
                    <MenuItem onClick={handleDelete} sx={{ color: 'error.main' }}>
                        <ListItemIcon>
                            <TrashIcon size={16} />
                        </ListItemIcon>
                        Delete
                    </MenuItem>
                )}
            </Menu>

            {/* Activity Details Dialog */}
            <ActivityDetailsDialog
                open={showDetailsDialog}
                onClose={() => setShowDetailsDialog(false)}
                activity={activity}
            />
        </>
    );
};

// Activity Details Dialog Component
const ActivityDetailsDialog = ({ open, onClose, activity }) => {
    if (!activity) return null;

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const formatDateTime = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    const formatTime = (timeString) => {
        if (!timeString) return "-";
        return timeString.trim();
    };

    const getActivityType = (activity) => {
        if (activity.absent) return "Absent";
        if (activity.fullLeave) return "Full Leave";
        if (activity.halfDay) return "Half Day";
        if (activity.shortLeave) return "Short Leave";
        if (activity.late) return "Late";
        if (activity.unSuccessful) return "Swipe Error";
        if (activity.unAuthorized) return "Unauthorized";
        if (activity.fullDay) return "Full Day Present";
        return "Present";
    };

    const getActivityStatus = (activity) => {
        if (activity.unAuthorized) return "Unauthorized";
        if (activity.unSuccessful) return "Unsuccessful";
        if (activity.leaveSuccess) return "Leave Approved";
        if (activity.leaveReq) return "Leave Pending";
        if (activity.issues) return "Has Issues";
        if (activity.resolve) return "Resolved";
        return "Normal";
    };

    const getStatusColor = (activity) => {
        if (activity.unAuthorized || activity.unSuccessful) return "error";
        if (activity.leaveSuccess || activity.resolve) return "success";
        if (activity.leaveReq || activity.issues) return "warning";
        return "info";
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
        { key: 'issues', label: 'Issues' },
        { key: 'unAuthorized', label: 'Unauthorized' },
        { key: 'resolve', label: 'Resolved' },
        { key: 'leaveSuccess', label: 'Leave Success' },
        { key: 'leaveReq', label: 'Leave Request' },
        { key: 'active', label: 'Active' },
        { key: 'nopay', label: 'No Pay (alt)' },
        { key: 'manual', label: 'Manual Entry' }
    ];

    return (
        <Dialog
            open={open}
            onClose={onClose}
            fullWidth
            maxWidth="lg"
            PaperProps={{
                sx: {
                    borderRadius: 2,
                    boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
                    maxHeight: '90vh'
                }
            }}
        >
            {/* Dialog Header */}
            <DialogTitle sx={{
                bgcolor: 'primary.main',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                gap: 1
            }}>
                <InfoIcon />
                Attendance Details - {activity.employeeID} - {formatDate(activity.date)}
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
                                            primary="Terminal ID"
                                            secondary={activity.terminalID || "-"}
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
                                            secondary={formatDateTime(activity.arrivalDate)}
                                        />
                                    </ListItem>
                                    {activity.dueDateForUA && (
                                        <ListItem>
                                            <ListItemText
                                                primary="Due Date (UA)"
                                                secondary={formatDate(activity.dueDateForUA)}
                                            />
                                        </ListItem>
                                    )}
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
                                                    label={getActivityStatus(activity)}
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

                    {/* In/Out Log Details */}
                    {activity.inOutDTOs && activity.inOutDTOs.length > 0 && (
                        <Grid item xs={12}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" gutterBottom color="primary">
                                        <EventIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                        Access Log Details
                                    </Typography>
                                    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
                                        <Table size="small">
                                            <TableHead>
                                                <TableRow sx={{ bgcolor: 'grey.50' }}>
                                                    <TableCell><strong>Type</strong></TableCell>
                                                    <TableCell><strong>Time</strong></TableCell>
                                                    <TableCell><strong>Terminal</strong></TableCell>
                                                    <TableCell><strong>Log Date</strong></TableCell>
                                                    <TableCell><strong>Status</strong></TableCell>
                                                    <TableCell><strong>ETL Run</strong></TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {activity.inOutDTOs.map((inOut, index) => (
                                                    <TableRow key={index} hover>
                                                        <TableCell>
                                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                                {inOut.inOut === 1 ? (
                                                                    <>
                                                                        <LoginIcon color="success" fontSize="small" />
                                                                        <Chip label="IN" size="small" color="success" />
                                                                    </>
                                                                ) : (
                                                                    <>
                                                                        <LogoutIcon color="error" fontSize="small" />
                                                                        <Chip label="OUT" size="small" color="error" />
                                                                    </>
                                                                )}
                                                            </Box>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                                {formatTime(inOut.timeMoa || inOut.timeEve)}
                                                            </Typography>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography variant="body2" sx={{ fontSize: '0.8rem' }}>
                                                                {inOut.terminalID?.trim() || "-"}
                                                            </Typography>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography variant="body2">
                                                                {inOut.accessLog?.logDate || "-"}
                                                            </Typography>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Chip
                                                                label={inOut.accessLog?.processed ? "Processed" : "Pending"}
                                                                size="small"
                                                                color={inOut.accessLog?.processed ? "success" : "warning"}
                                                                variant="outlined"
                                                            />
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography variant="body2" sx={{ fontSize: '0.75rem' }}>
                                                                {formatDateTime(inOut.accessLog?.etlRunTime)}
                                                            </Typography>
                                                        </TableCell>
                                                    </TableRow>
                                                ))}
                                            </TableBody>
                                        </Table>
                                    </Paper>
                                </CardContent>
                            </Card>
                        </Grid>
                    )}

                    {/* Edit History */}
                    {activity.editedByDTOs && activity.editedByDTOs.length > 0 && (
                        <Grid item xs={12}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" gutterBottom color="primary">
                                        <EditHistoryIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                        Edit History
                                    </Typography>
                                    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
                                        <Table size="small">
                                            <TableHead>
                                                <TableRow sx={{ bgcolor: 'grey.50' }}>
                                                    <TableCell><strong>Editor</strong></TableCell>
                                                    <TableCell><strong>SLT ID</strong></TableCell>
                                                    <TableCell><strong>Employee ID</strong></TableCell>
                                                    <TableCell><strong>Comment</strong></TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {activity.editedByDTOs.map((editor, index) => (
                                                    <TableRow key={index} hover>
                                                        <TableCell>
                                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                                {editor.profilePicture ? (
                                                                    <img
                                                                        src={editor.profilePicture}
                                                                        alt={editor.name}
                                                                        style={{
                                                                            width: 24,
                                                                            height: 24,
                                                                            borderRadius: '50%',
                                                                            objectFit: 'cover'
                                                                        }}
                                                                    />
                                                                ) : (
                                                                    <PersonIcon sx={{ fontSize: 24, color: 'grey.500' }} />
                                                                )}
                                                                <Typography variant="body2" fontWeight="medium">
                                                                    {editor.name || "-"}
                                                                </Typography>
                                                            </Box>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                                {editor.sltId || "-"}
                                                            </Typography>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                                {editor.employeeId || "-"}
                                                            </Typography>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                                <CommentIcon sx={{ fontSize: 16, color: 'grey.600' }} />
                                                                <Typography variant="body2">
                                                                    {editor.comment || "No comment"}
                                                                </Typography>
                                                            </Box>
                                                        </TableCell>
                                                    </TableRow>
                                                ))}
                                            </TableBody>
                                        </Table>
                                    </Paper>
                                </CardContent>
                            </Card>
                        </Grid>
                    )}

                    {/* Issue Description */}
                    {activity.issueDescription && activity.issueDescription.trim() !== "" && (
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
                                        if (isTrue === undefined || isTrue === null) return null;
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
                                            Public ID
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.9rem', fontWeight: 'bold' }}>
                                            {activity.publicId || "-"}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={12} md={4}>
                                        <Typography variant="body2" color="textSecondary">
                                            Internal ID
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.9rem' }}>
                                            {activity.id || "-"}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={12} md={4}>
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
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </DialogContent>

            {/* Dialog Actions */}
            <DialogActions sx={{ p: 2, bgcolor: 'grey.50' }}>
                <Button onClick={onClose} variant="contained" size="large">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export { ActivityDetailsMenu, ActivityDetailsDialog };