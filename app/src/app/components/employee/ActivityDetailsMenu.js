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
    CardContent
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
    Info as InfoIcon
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

                {isAdmin && activity.manual && (
                    <MenuItem onClick={handleEdit}>
                        <ListItemIcon>
                            <EditIcon fontSize="small" />
                        </ListItemIcon>
                        Edit
                    </MenuItem>
                )}

                {isAdmin && (
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

    const formatTime = (timeString) => {
        if (!timeString) return "-";
        return timeString;
    };

    const getActivityType = (activity) => {
        if (activity.isAbsent) return "Absent";
        if (activity.isFullLeave) return "Full Leave";
        if (activity.isHalfDay) return "Half Day";
        if (activity.isShortLeave) return "Short Leave";
        if (activity.isLate) return "Late";
        if (activity.isUnSuccessful) return "Swipe error";
        if (activity.isUnAuthorized) return "Swipe error";
        return "Present";
    };

    const getActivityStatus = (activity) => {
        if (activity.isUnAuthorized) return "Unauthorized";
        if (activity.isUnSuccessful) return "Unauthorized";
        if (activity.leaveSuccess) return "Approved";
        if (activity.leaveReq) return "Pending";
        if (activity.issues) return "Issue";
        return "Normal";
    };

    const booleanFields = [
        { key: 'isFullDay', label: 'Full Day' },
        { key: 'isLate', label: 'Late' },
        { key: 'lateCover', label: 'Late Cover' },
        { key: 'isHalfDay', label: 'Half Day' },
        { key: 'isFullLeave', label: 'Full Leave' },
        { key: 'isShortLeave', label: 'Short Leave' },
        { key: 'isAbsent', label: 'Absent' },
        { key: 'isUnSuccessful', label: 'Unsuccessful' },
        { key: 'isNoPay', label: 'No Pay' },
        { key: 'issues', label: 'Issues' },
        { key: 'isUnAuthorized', label: 'Unauthorized' },
        { key: 'resolve', label: 'Resolved' },
        { key: 'leaveSuccess', label: 'Leave Success' },
        { key: 'leaveReq', label: 'Leave Request' },
        { key: 'active', label: 'Active' },
        { key: 'nopay', label: 'No Pay' },
        { key: 'viaMovement', label: 'Via Movement' },
        { key: 'viaLeave', label: 'Via Leave' },
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
                gap: 1
            }}>
                <InfoIcon />
                Activity Details - {activity.employeeID}
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
                                                    label={getActivityStatus(activity)}
                                                    size="small"
                                                    color="secondary"
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

                    {/* System Information */}
                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom color="primary">
                                    System Information
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid item xs={6} md={3}>
                                        <Typography variant="body2" color="textSecondary">
                                            Public ID
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                                            {activity.publicId || "-"}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={6} md={3}>
                                        <Typography variant="body2" color="textSecondary">
                                            Internal ID
                                        </Typography>
                                        <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                                            {activity.id || "-"}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={6} md={3}>
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
                                    <Grid item xs={6} md={3}>
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
};

export { ActivityDetailsMenu, ActivityDetailsDialog };