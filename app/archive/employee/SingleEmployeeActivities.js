"use client";

import React, { useEffect } from "react";
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
    Alert,
    CircularProgress,
    Card,
    CardContent,
    Divider,
    Chip,
    IconButton,
    Collapse,
    Grid,
    Badge,
    Avatar,
    Tooltip,
    Pagination,
    TableFooter,
    TablePagination, FormControlLabel, Checkbox, Button
} from "@mui/material";
import {
    ThemeProvider,
    CssBaseline,
    createTheme
} from "@mui/material";
import FilterListIcon from '@mui/icons-material/FilterList';
import SearchIcon from '@mui/icons-material/Search';
import EventNoteIcon from '@mui/icons-material/EventNote';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import PersonIcon from '@mui/icons-material/Person';
import EventIcon from '@mui/icons-material/Event';
import ErrorIcon from '@mui/icons-material/Error';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import BlockIcon from '@mui/icons-material/Block';
import FirstPageIcon from '@mui/icons-material/FirstPage';
import LastPageIcon from '@mui/icons-material/LastPage';
import KeyboardArrowLeftIcon from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import { TrashIcon, X} from "lucide-react";
import Dialog from "@mui/material/Dialog";
import {Edit as EditIcon, StopRounded} from "@mui/icons-material";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContentText from "@mui/material/DialogContentText";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import {
    fetchEmployeeActivities,
    createEmployeeActivity,
    updateEmployeeActivity,
    deleteEmployeeActivity,
    deleteEmployeeDeActivity,
    setPage,
    setRowsPerPage,
    setSearchTerm,
    setFilterType,
    setFilterStatus,
    setStartDateFilter,
    setEndDateFilter,
    setFormData,
    resetFormData,
    setEditMode,
    setEditId,
    setShowModal,
    setShowFilters,
    setOpenDeleteDialog,
    setIdToDelete,
    clearError,
    handleFormChange
} from "../../../../lib/redux/redux-lms/employee-activities/employeeActivitiesSlice";

// Custom theme with better colors
const theme = createTheme({
    palette: {
        primary: {
            main: '#3f51b5',
        },
        secondary: {
            main: '#f50057',
        },
        background: {
            default: '#f5f7fa',
        },
    },
    typography: {
        fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
        h4: {
            fontWeight: 500,
        }
    },
    components: {
        MuiTableHead: {
            styleOverrides: {
                root: {
                    backgroundColor: '#f1f5fb',
                }
            }
        },
        MuiTableCell: {
            styleOverrides: {
                head: {
                    fontWeight: 600,
                }
            }
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    boxShadow: '0 2px 12px 0 rgba(0,0,0,0.1)',
                }
            }
        }
    }
});

const formatDate = (dateString) => {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleDateString();
};

// Helper function to format time from Time object to HH:MM format for form inputs
const formatTimeForInput = (timeString) => {
    if (!timeString) return "";

    // If it's already in HH:MM or HH:MM:SS format, format it correctly
    if (typeof timeString === 'string') {
        // Handle various time formats
        const parts = timeString.split(':');
        if (parts.length >= 2) {
            return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
        }
    }

    return "";
};

// Helper function to format date for input fields
const formatDateForInput = (dateString) => {
    if (!dateString) return new Date().toISOString().split('T')[0];

    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            // If invalid date, return today's date
            return new Date().toISOString().split('T')[0];
        }
        return date.toISOString().split('T')[0];
    } catch (error) {
        console.error("Error formatting date:", error);
        return new Date().toISOString().split('T')[0];
    }
};

// Custom pagination actions component
function TablePaginationActions(props) {
    const { count, page, rowsPerPage, onPageChange } = props;

    const handleFirstPageButtonClick = (event) => {
        onPageChange(event, 0);
    };

    const handleBackButtonClick = (event) => {
        onPageChange(event, page - 1);
    };

    const handleNextButtonClick = (event) => {
        onPageChange(event, page + 1);
    };

    const handleLastPageButtonClick = (event) => {
        onPageChange(event, Math.max(0, Math.ceil(count / rowsPerPage) - 1));
    };

    return (
        <Box sx={{ flexShrink: 0, ml: 2.5 }}>
            <IconButton
                onClick={handleFirstPageButtonClick}
                disabled={page === 0}
                aria-label="first page"
            >
                <FirstPageIcon />
            </IconButton>
            <IconButton
                onClick={handleBackButtonClick}
                disabled={page === 0}
                aria-label="previous page"
            >
                <KeyboardArrowLeftIcon />
            </IconButton>
            <IconButton
                onClick={handleNextButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label="next page"
            >
                <KeyboardArrowRightIcon />
            </IconButton>
            <IconButton
                onClick={handleLastPageButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label="last page"
            >
                <LastPageIcon />
            </IconButton>
        </Box>
    );
}

// Status chip component for better visual representation
const StatusChip = ({ status }) => {
    const getChipProps = () => {
        switch (status) {
            case 'Unauthorized':
                return {
                    color: 'error',
                    icon: <BlockIcon fontSize="small" />,
                    label: status
                };
            case 'Unsuccessful':
                return {
                    color: 'warning',
                    icon: <ErrorIcon fontSize="small" />,
                    label: status
                };
            case 'Approved':
                return {
                    color: 'success',
                    icon: <CheckCircleIcon fontSize="small" />,
                    label: status
                };
            case 'Pending':
                return {
                    color: 'info',
                    icon: <HourglassEmptyIcon fontSize="small" />,
                    label: status
                };
            case 'Issue':
                return {
                    color: 'warning',
                    icon: <ErrorIcon fontSize="small" />,
                    label: status
                };
            default:
                return {
                    color: 'default',
                    icon: <CheckCircleIcon fontSize="small" />,
                    label: 'Normal'
                };
        }
    };

    const { color, icon, label } = getChipProps();

    return (
        <Chip
            label={label}
            color={color}
            size="small"
            icon={icon}
            sx={{ fontWeight: 500 }}
        />
    );
};

// Type badge component for better visual representation
const TypeBadge = ({ type }) => {
    const getTypeProps = () => {
        switch (type) {
            case 'Absent':
                return {
                    bgcolor: '#ffcdd2',
                    color: '#c62828'
                };
            case 'Late':
                return {
                    bgcolor: '#fff9c4',
                    color: '#f57f17'
                };
            case 'Full Leave':
                return {
                    bgcolor: '#bbdefb',
                    color: '#0d47a1'
                };
            case 'Half Day':
                return {
                    bgcolor: '#c8e6c9',
                    color: '#1b5e20'
                };
            case 'Short Leave':
                return {
                    bgcolor: '#e1bee7',
                    color: '#4a148c'
                };
            default:
                return {
                    bgcolor: '#e8f5e9',
                    color: '#2e7d32'
                };
        }
    };

    const { bgcolor, color } = getTypeProps();

    return (
        <Chip
            label={type}
            size="small"
            sx={{
                bgcolor,
                color,
                fontWeight: 500
            }}
        />
    );
};

const SingleEmployeeActivities = ({ isAdmin = false, userId = null }) => {
    // Get userId from sessionStorage if not provided
    if (userId == null) userId = sessionStorage.getItem('userId');

    // Use Redux state and dispatch
    const dispatch = useDispatch();
    const {
        activities,
        loading,
        error,
        page,
        rowsPerPage,
        searchTerm,
        filterType,
        filterStatus,
        startDateFilter,
        endDateFilter,
        formData,
        isEditMode,
        editId,
        showModal,
        showFilters,
        openDeleteDialog,
        idToDelete
    } = useSelector(state => state.employeeActivities);

    // Fetch data on component mount
    useEffect(() => {
        dispatch(fetchEmployeeActivities(userId));
    }, [dispatch, userId]);

    // Handle form input changes
    const handleChange = (e) => {
        const { name, value, type, checked, terminalID } = e.target;
        dispatch(handleFormChange({ name, value, type, checked, terminalID }));
    };
    const [anchorEl, setAnchorEl] = React.useState(false);

    // Handle edit button click
    const handleEditClick = (activity) => {
        const preparedData = {
            date: formatDateForInput(activity.date),
            employeeID: activity.employeeID || '',
            isFullDay: activity.isFullDay || false,
            arrivalDate: formatDateForInput(activity.arrivalDate),
            arrivalTime: formatTimeForInput(activity.arrivalTime),
            leftTime: formatTimeForInput(activity.leftTime),
            isLate: activity.isLate || false,
            lateCover: activity.lateCover || false,
            isHalfDay: activity.isHalfDay || false,
            isFullLeave: activity.isFullLeave || false,
            isShortLeave: activity.isShortLeave || false,
            isAbsent: activity.isAbsent || false,
            isUnSuccessful: activity.isUnSuccessful || false,
            isNoPay: activity.isNoPay || false,
            issues: activity.issues || false,
            isUnAuthorized: activity.isUnAuthorized || false,
            resolve: activity.resolve || false,
            leaveSuccess: activity.leaveSuccess || false,
            leaveReq: activity.leaveReq || false,
            issueDescription: activity.issueDescription || '',
            dueDateForUA: formatDateForInput(activity.dueDateForUA),
            active: activity.active !== false, // Default to true if undefined
            nopay: activity.nopay || false,
            viaMovement: activity.viaMovement || false,
            viaLeave: activity.viaLeave || false
        };

        dispatch(setFormData(preparedData));
        dispatch(setEditId(activity.publicId));
        dispatch(setEditMode(true));
        dispatch(setShowModal(true));
    };

    const handleDeleteClick = (id) => {
        dispatch(setIdToDelete(id));
        dispatch(setOpenDeleteDialog(true));
    };

    const handleDeleteDeClick = (id) => {
        dispatch(setIdToDelete(id));
        dispatch(setOpenDeleteDialog(true));
        setAnchorEl(true)
    };

    // Function to handle confirmation dialog close
    const handleCloseDeleteDialog = () => {
        dispatch(setOpenDeleteDialog(false));
        dispatch(setIdToDelete(null));
    };

    // Function to open the modal for creating a new record
    const handleAddNew = () => {
        dispatch(resetFormData());
        dispatch(setEditMode(false));
        dispatch(setEditId(null));
        dispatch(setShowModal(true));
    };

    // Function to close the modal and reset form
    const handleCloseModal = () => {
        dispatch(setShowModal(false));
        dispatch(setEditMode(false));
        dispatch(setEditId(null));
        dispatch(resetFormData());
    };

    // Function to handle form submission (create or update)
    const handleSubmit = () => {
        if (isEditMode && editId) {
            dispatch(updateEmployeeActivity({ id: editId, formData }));
        } else {
            dispatch(createEmployeeActivity(formData));
        }
    };

    // Function to delete an attendance record
    const deleteAttendance = () => {
        if(anchorEl)
            dispatch(deleteEmployeeDeActivity(idToDelete));
        else
            dispatch(deleteEmployeeActivity(idToDelete));
    };

    // Handle search input change
    const handleSearchChange = (event) => {
        dispatch(setSearchTerm(event.target.value));
    };

    // Handle type filter change
    const handleTypeFilterChange = (event) => {
        dispatch(setFilterType(event.target.value));
    };

    // Handle status filter change
    const handleStatusFilterChange = (event) => {
        dispatch(setFilterStatus(event.target.value));
    };

    // Handle page change
    const handleChangePage = (event, newPage) => {
        dispatch(setPage(newPage));
    };

    // Handle rows per page change
    const handleChangeRowsPerPage = (event) => {
        dispatch(setRowsPerPage(parseInt(event.target.value, 10)));
    };

    // Determine activity type based on properties
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

    // Determine activity status based on properties
    const getActivityStatus = (activity) => {
        if (activity.isUnAuthorized) return "Unauthorized";
        if (activity.isUnSuccessful) return "Unauthorized";
        if (activity.leaveSuccess) return "Approved";
        if (activity.leaveReq) return "Pending";
        if (activity.issues) return "Issue";
        return "Normal";
    };

    // Filter activities based on search term, type, status, and date range
    const filteredActivities = activities.filter((activity) => {
        const activityType = getActivityType(activity);
        const activityStatus = getActivityStatus(activity);

        const matchesSearch = activity.employeeID?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesType = filterType === "all" || activityType === filterType;
        const matchesStatus = filterStatus === "all" || activityStatus === filterStatus;

        // Convert date strings to Date objects for comparison
        const activityDate = activity.date ? new Date(activity.date).toISOString().split('T')[0] : "";
        const startDate = startDateFilter || "";
        const endDate = endDateFilter || "";

        const matchesStartDate = !startDate || activityDate >= startDate;
        const matchesEndDate = !endDate || activityDate <= endDate;

        return matchesSearch && matchesType && matchesStatus && matchesStartDate && matchesEndDate;
    });

    // Get current page of data
    const paginatedActivities = filteredActivities.slice(
        page * rowsPerPage,
        page * rowsPerPage + rowsPerPage
    );

    return (
        <>
            {/* Delete Confirmation Dialog */}
            <Dialog
                open={openDeleteDialog}
                onClose={handleCloseDeleteDialog}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">
                    {"Confirm Deletion"}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        Are you sure you want to delete this attendance record? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDeleteDialog} color="primary">
                        Cancel
                    </Button>
                    <Button
                        onClick={deleteAttendance}
                        color="error"
                        variant="contained"
                        autoFocus
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Form Modal Dialog */}
            <Dialog
                open={showModal}
                onClose={handleCloseModal}
                fullWidth
                maxWidth="md"
                PaperProps={{
                    sx: {
                        borderRadius: 2,
                        boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
                    }
                }}
            >
                <Box sx={{ position: 'relative' }}>
                    {/* Dialog Header */}
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        p: 2,
                        borderBottom: '1px solid #e0e0e0',
                        bgcolor: '#f5f5f5'
                    }}>
                        <Typography variant="h6" sx={{ fontWeight: 500 }}>
                            {isEditMode ? 'Edit Attendance Record' : 'Add Attendance Record'}
                        </Typography>
                        <IconButton onClick={handleCloseModal} size="small">
                            <X size={20} />
                        </IconButton>
                    </Box>

                    {/* Dialog Content */}
                    <Box sx={{ p: 3 }}>
                        <Grid container spacing={2}>
                            {/* Date */}
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Date"
                                    type="date"
                                    name="date"
                                    value={formData.date}
                                    onChange={handleChange}
                                    InputLabelProps={{ shrink: true }}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <EventIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            {/* Employee ID */}
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Employee ID"
                                    name="employeeID"
                                    value={formData.employeeID}
                                    onChange={handleChange}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <PersonIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Terminal ID"
                                    name="terminalID"
                                    value={formData.terminalID}
                                    onChange={handleChange}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <PersonIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            {/* Arrival Date */}
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Arrival Date"
                                    type="date"
                                    name="arrivalDate"
                                    value={formData.arrivalDate}
                                    onChange={handleChange}
                                    InputLabelProps={{ shrink: true }}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <EventIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            {/* Arrival Time */}
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Arrival Time"
                                    type="time"
                                    name="arrivalTime"
                                    value={formData.arrivalTime}
                                    onChange={handleChange}
                                    InputLabelProps={{ shrink: true }}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <AccessTimeIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            {/* Left Time */}
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Left Time"
                                    type="time"
                                    name="leftTime"
                                    value={formData.leftTime}
                                    onChange={handleChange}
                                    InputLabelProps={{ shrink: true }}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <AccessTimeIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            {/* Due Date For UA */}
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Due Date (For UA)"
                                    type="date"
                                    name="dueDateForUA"
                                    value={formData.dueDateForUA}
                                    onChange={handleChange}
                                    InputLabelProps={{ shrink: true }}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{ mr: 1, color: 'text.secondary' }}>
                                                <EventIcon fontSize="small" />
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            {/* Issue Description */}
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Issue Description"
                                    name="issueDescription"
                                    value={formData.issueDescription}
                                    onChange={handleChange}
                                    multiline
                                    rows={2}
                                />
                            </Grid>

                            {/* Status Options */}
                            <Grid item xs={12}>
                                <Typography variant="subtitle1" sx={{ fontWeight: 500, mb: 1.5 }}>
                                    Status Options
                                </Typography>

                                <Grid container spacing={1}>
                                    {/* First Row */}
                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isFullDay}
                                                    onChange={handleChange}
                                                    name="isFullDay"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Full Day"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isLate}
                                                    onChange={handleChange}
                                                    name="isLate"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Late"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.lateCover}
                                                    onChange={handleChange}
                                                    name="lateCover"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Late Cover"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isHalfDay}
                                                    onChange={handleChange}
                                                    name="isHalfDay"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Half Day"
                                        />
                                    </Grid>

                                    {/* Second Row */}
                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isFullLeave}
                                                    onChange={handleChange}
                                                    name="isFullLeave"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Full Leave"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isShortLeave}
                                                    onChange={handleChange}
                                                    name="isShortLeave"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Short Leave"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isAbsent}
                                                    onChange={handleChange}
                                                    name="isAbsent"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Absent"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isUnSuccessful}
                                                    onChange={handleChange}
                                                    name="isUnSuccessful"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Unsuccessful"
                                        />
                                    </Grid>

                                    {/* Third Row */}
                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isNoPay}
                                                    onChange={handleChange}
                                                    name="isNoPay"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="No Pay"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.issues}
                                                    onChange={handleChange}
                                                    name="issues"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Issues"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.isUnAuthorized}
                                                    onChange={handleChange}
                                                    name="isUnAuthorized"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Unauthorized"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.resolve}
                                                    onChange={handleChange}
                                                    name="resolve"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Resolved"
                                        />
                                    </Grid>

                                    {/* Fourth Row */}
                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.leaveSuccess}
                                                    onChange={handleChange}
                                                    name="leaveSuccess"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Leave Success"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.leaveReq}
                                                    onChange={handleChange}
                                                    name="leaveReq"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Leave Request"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.active}
                                                    onChange={handleChange}
                                                    name="active"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Active"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.nopay}
                                                    onChange={handleChange}
                                                    name="nopay"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="No Pay"
                                        />
                                    </Grid>

                                    {/* Fifth Row */}
                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.viaMovement}
                                                    onChange={handleChange}
                                                    name="viaMovement"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Via Movement"
                                        />
                                    </Grid>

                                    <Grid item xs={6} sm={4} md={3}>
                                        <FormControlLabel
                                            control={
                                                <Checkbox
                                                    checked={formData.viaLeave}
                                                    onChange={handleChange}
                                                    name="viaLeave"
                                                    color="primary"
                                                    size="small"
                                                />
                                            }
                                            label="Via Leave"
                                        />
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Box>

                    {/* Dialog Actions */}
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'flex-end',
                        p: 2,
                        borderTop: '1px solid #e0e0e0',
                        bgcolor: '#f5f5f5',
                        gap: 1
                    }}>
                        <Button
                            variant="outlined"
                            onClick={handleCloseModal}
                        >
                            Cancel
                        </Button>
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            color="primary"
                        >
                            {isEditMode ? 'Update Attendance' : 'Submit Attendance'}
                        </Button>
                    </Box>
                </Box>
            </Dialog>

            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Box sx={{ p: { xs: 2, md: 4 }, backgroundColor: "#f5f7fa", minHeight: "100vh" }}>
                    <Card variant="outlined" sx={{ mb: 4, borderRadius: 2, overflow: 'hidden' }}>
                        <Box
                            sx={{
                                p: 3,
                                backgroundImage: 'linear-gradient(120deg, #3f51b5, #5c6bc0)',
                                display: 'flex',
                                alignItems: 'center'
                            }}
                        >
                            <Avatar sx={{ bgcolor: '#fff', color: '#3f51b5', mr: 2 }}>
                                <EventNoteIcon />
                            </Avatar>
                            <Typography variant="h4" gutterBottom sx={{ color: 'white', fontWeight: 'bold', mb: 0 }}>
                                {isAdmin ? 'Employee Activities' : 'Your Activities'}
                            </Typography>
                        </Box>
                    </Card>

                    {error && (
                        <Alert
                            severity="error"
                            sx={{
                                mb: 3,
                                borderRadius: 2,
                                boxShadow: '0 2px 10px rgba(0,0,0,0.08)'
                            }}
                        >
                            {error}
                            <Button
                                size="small"
                                color="inherit"
                                sx={{ ml: 2 }}
                                onClick={() => dispatch(clearError())}
                            >
                                Dismiss
                            </Button>
                        </Alert>
                    )}

                    {loading ? (
                        <Box
                            display="flex"
                            justifyContent="center"
                            alignItems="center"
                            my={8}
                            flexDirection="column"
                            gap={2}
                        >
                            <CircularProgress size={60} />
                            <Typography variant="h6" color="textSecondary">Loading activities...</Typography>
                        </Box>
                    ) : (
                        <>
                            {/* Search and Filters Card */}
                            <Card sx={{ mb: 4, borderRadius: 2 }}>
                                <CardContent sx={{ pb: 2 }}>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                            mb: 2
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                            <SearchIcon sx={{ color: 'action.active', mr: 1 }} />
                                            <TextField
                                                label="Search by Employee ID"
                                                variant="outlined"
                                                size="small"
                                                value={searchTerm}
                                                onChange={handleSearchChange}
                                                sx={{ minWidth: 200 }}
                                            />
                                        </Box>

                                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                            {isAdmin && (
                                                <Button
                                                    variant="contained"
                                                    color="primary"
                                                    startIcon={<PersonIcon />}
                                                    onClick={handleAddNew}
                                                    sx={{ mr: 1 }}
                                                >
                                                    Add Record
                                                </Button>
                                            )}

                                            <Tooltip title={showFilters ? "Hide Filters" : "Show Filters"}>
                                                <IconButton
                                                    onClick={() => dispatch(setShowFilters(!showFilters))}
                                                    color="primary"
                                                >
                                                    {showFilters ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                                                </IconButton>
                                            </Tooltip>
                                        </Box>
                                    </Box>

                                    <Collapse in={showFilters}>
                                        <Divider sx={{ my: 2 }} />

                                        <Box
                                            sx={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                mb: 1
                                            }}
                                        >
                                            <FilterListIcon sx={{ mr: 1, color: 'action.active' }} />
                                            <Typography variant="subtitle1" fontWeight="medium">
                                                Filters
                                            </Typography>
                                        </Box>

                                        <Grid container spacing={2} sx={{ mt: 1 }}>
                                            <Grid item xs={12} md={3}>
                                                <FormControl variant="outlined" size="small" fullWidth>
                                                    <InputLabel>Type</InputLabel>
                                                    <Select
                                                        value={filterType}
                                                        onChange={handleTypeFilterChange}
                                                        label="Type"
                                                    >
                                                        <MenuItem value="all">All Types</MenuItem>
                                                        <MenuItem value="Present">Present</MenuItem>
                                                        <MenuItem value="Late">Late</MenuItem>
                                                        <MenuItem value="Absent">Absent</MenuItem>
                                                        <MenuItem value="Full Leave">Full Leave</MenuItem>
                                                        <MenuItem value="Half Day">Half Day</MenuItem>
                                                        <MenuItem value="Short Leave">Short Leave</MenuItem>
                                                        <MenuItem value="Swipe error">Swipe Error</MenuItem>
                                                    </Select>
                                                </FormControl>
                                            </Grid>

                                            <Grid item xs={12} md={3}>
                                                <FormControl variant="outlined" size="small" fullWidth>
                                                    <InputLabel>Status</InputLabel>
                                                    <Select
                                                        value={filterStatus}
                                                        onChange={handleStatusFilterChange}
                                                        label="Status"
                                                    >
                                                        <MenuItem value="all">All Statuses</MenuItem>
                                                        <MenuItem value="Normal">Normal</MenuItem>
                                                        <MenuItem value="Approved">Approved</MenuItem>
                                                        <MenuItem value="Pending">Pending</MenuItem>
                                                        <MenuItem value="Unauthorized">Unauthorized</MenuItem>
                                                        <MenuItem value="Unsuccessful">Unsuccessful</MenuItem>
                                                        <MenuItem value="Issue">Issue</MenuItem>
                                                    </Select>
                                                </FormControl>
                                            </Grid>

                                            <Grid item xs={12} md={3}>
                                                <TextField
                                                    label="Start Date"
                                                    type="date"
                                                    variant="outlined"
                                                    size="small"
                                                    fullWidth
                                                    value={startDateFilter}
                                                    onChange={(e) => dispatch(setStartDateFilter(e.target.value))}
                                                    InputLabelProps={{ shrink: true }}
                                                />
                                            </Grid>

                                            <Grid item xs={12} md={3}>
                                                <TextField
                                                    label="End Date"
                                                    type="date"
                                                    variant="outlined"
                                                    size="small"
                                                    fullWidth
                                                    value={endDateFilter}
                                                    onChange={(e) => dispatch(setEndDateFilter(e.target.value))}
                                                    InputLabelProps={{ shrink: true }}
                                                />
                                            </Grid>
                                        </Grid>
                                    </Collapse>
                                </CardContent>
                            </Card>

                            {/* Activities Stats */}
                            <Box
                                sx={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    mb: 2,
                                    px: 1
                                }}
                            >
                                <Typography variant="subtitle1" fontWeight="medium">
                                    {filteredActivities.length} {filteredActivities.length === 1 ? 'Activity' : 'Activities'} Found
                                </Typography>
                            </Box>

                            {/* Table of Employee Activities */}
                            <Card sx={{ borderRadius: 2 }}>
                                <TableContainer>
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                        <PersonIcon fontSize="small" sx={{ mr: 1, opacity: 0.7 }} />
                                                        SLT ID
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                        <EventIcon fontSize="small" sx={{ mr: 1, opacity: 0.7 }} />
                                                        Date
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                        <AccessTimeIcon fontSize="small" sx={{ mr: 1, opacity: 0.7 }} />
                                                        Arrival Time
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                        <AccessTimeIcon fontSize="small" sx={{ mr: 1, opacity: 0.7 }} />
                                                        Left Time
                                                    </Box>
                                                </TableCell>
                                                <TableCell>Type</TableCell>
                                                <TableCell>Status</TableCell>
                                                <TableCell>Issue Description</TableCell>
                                                <TableCell>Actions</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {paginatedActivities.length > 0 ? (
                                                paginatedActivities.map((activity) => (
                                                    <TableRow
                                                        key={activity.id}
                                                        sx={{
                                                            '&:hover': {
                                                                backgroundColor: '#f5f5f5',
                                                            }
                                                        }}
                                                    >
                                                        <TableCell
                                                            sx={{ fontWeight: 500 }}>{activity.employeeID}</TableCell>
                                                        <TableCell>{formatDate(activity.arrivalDate)}</TableCell>
                                                        <TableCell>{activity.arrivalTime || "-"}</TableCell>
                                                        <TableCell>{activity.leftTime || "-"}</TableCell>
                                                        <TableCell>
                                                            <TypeBadge type={getActivityType(activity)} />
                                                        </TableCell>
                                                        <TableCell>
                                                            <StatusChip status={getActivityStatus(activity)} />
                                                        </TableCell>
                                                        <TableCell sx={{
                                                            maxWidth: 300,
                                                            whiteSpace: 'normal',
                                                            wordBreak: 'break-word'
                                                        }}>
                                                            {activity.issues ? (
                                                                <Tooltip title={activity.issueDescription} arrow>
                                                                    <Typography
                                                                        variant="body2"
                                                                        sx={{
                                                                            display: '-webkit-box',
                                                                            WebkitLineClamp: 2,
                                                                            WebkitBoxOrient: 'vertical',
                                                                            overflow: 'hidden',
                                                                            textOverflow: 'ellipsis',
                                                                            cursor: 'pointer'
                                                                        }}
                                                                    >
                                                                        {activity.issueDescription}
                                                                    </Typography>
                                                                </Tooltip>
                                                            ) : (
                                                                "-"
                                                            )}
                                                        </TableCell>
                                                        <TableCell>
                                                            {isAdmin && activity.manual && (
                                                                <Box sx={{ display: 'flex', gap: 1 }}>
                                                                    <IconButton
                                                                        color="primary"
                                                                        onClick={() => handleEditClick(activity)}
                                                                        disabled={!activity.manual}
                                                                        size="small"
                                                                    >
                                                                        <EditIcon fontSize="small" />
                                                                    </IconButton>
                                                                    <IconButton
                                                                        color="error"
                                                                        onClick={() => handleDeleteClick(activity.publicId)}
                                                                        disabled={!activity.manual}
                                                                        size="small"
                                                                    >
                                                                        <TrashIcon size={18} />
                                                                    </IconButton>
                                                                </Box>
                                                            )}
                                                            {isAdmin && !activity.manual && (
                                                                <Box sx={{ display: 'flex', gap: 1 }}>
                                                                    <IconButton
                                                                        color="error"
                                                                        onClick={() => handleDeleteDeClick(activity.publicId)}
                                                                        disabled={activity.manual}
                                                                        size="small"
                                                                    >
                                                                        <X size={18} />
                                                                    </IconButton>
                                                                </Box>
                                                            )}
                                                        </TableCell>
                                                    </TableRow>
                                                ))
                                            ) : (
                                                <TableRow>
                                                    <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                                                        <Box sx={{
                                                            display: 'flex',
                                                            flexDirection: 'column',
                                                            alignItems: 'center',
                                                            py: 2
                                                        }}>
                                                            <FilterListIcon
                                                                sx={{ fontSize: 40, color: 'text.secondary', mb: 1 }} />
                                                            <Typography variant="h6" color="textSecondary">
                                                                No activities found matching the current filters
                                                            </Typography>
                                                            <Typography variant="body2" color="textSecondary"
                                                                        sx={{ mt: 1 }}>
                                                                Try adjusting your search or filter criteria
                                                            </Typography>
                                                        </Box>
                                                    </TableCell>
                                                </TableRow>
                                            )}
                                        </TableBody>
                                        {filteredActivities.length > 0 && (
                                            <TableFooter>
                                                <TableRow>
                                                    <TablePagination
                                                        rowsPerPageOptions={[5, 10, 25, { label: 'All', value: -1 }]}
                                                        colSpan={8}
                                                        count={filteredActivities.length}
                                                        rowsPerPage={rowsPerPage}
                                                        page={page}
                                                        SelectProps={{
                                                            inputProps: {
                                                                'aria-label': 'rows per page',
                                                            },
                                                            native: true,
                                                        }}
                                                        onPageChange={handleChangePage}
                                                        onRowsPerPageChange={handleChangeRowsPerPage}
                                                        ActionsComponent={TablePaginationActions}
                                                    />
                                                </TableRow>
                                            </TableFooter>
                                        )}
                                    </Table>
                                </TableContainer>
                            </Card>
                        </>
                    )}
                </Box>
            </ThemeProvider>
        </>
    );
};

export default SingleEmployeeActivities;