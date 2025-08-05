"use client";

import {format} from 'date-fns';
import React, {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
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
    Avatar,
    Tooltip,
    TableFooter,
    TablePagination,
    FormControlLabel,
    Checkbox,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions
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
import {TrashIcon, X} from "lucide-react";
import {Edit as EditIcon} from "@mui/icons-material";
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
    setFilterActive,
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
import {useRouter} from 'next/navigation';
import {MovementType} from "../../../../lib/redux/redux-lms/movement/req/movementRequestSlice";

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

const formatTimeForInput = (timeString) => {
    if (!timeString) return "";

    if (typeof timeString === 'string') {
        const parts = timeString.split(':');
        if (parts.length >= 2) {
            return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
        }
    }
    return "";
};

const formatDateForInput = (dateString) => {
    if (!dateString) return new Date().toISOString().split('T')[0];

    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return new Date().toISOString().split('T')[0];
        }
        return date.toISOString().split('T')[0];
    } catch (error) {
        console.error("Error formatting date:", error);
        return new Date().toISOString().split('T')[0];
    }
};

function TablePaginationActions(props) {
    const {count, page, rowsPerPage, onPageChange} = props;

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
        <Box sx={{flexShrink: 0, ml: 2.5}}>
            <IconButton
                onClick={handleFirstPageButtonClick}
                disabled={page === 0}
                aria-label="first page"
            >
                <FirstPageIcon/>
            </IconButton>
            <IconButton
                onClick={handleBackButtonClick}
                disabled={page === 0}
                aria-label="previous page"
            >
                <KeyboardArrowLeftIcon/>
            </IconButton>
            <IconButton
                onClick={handleNextButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label="next page"
            >
                <KeyboardArrowRightIcon/>
            </IconButton>
            <IconButton
                onClick={handleLastPageButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label="last page"
            >
                <LastPageIcon/>
            </IconButton>
        </Box>
    );
}

const StatusChip = ({activity}) => {
    const getChipProps = () => {
        if (activity.isUnauthorized) {
            return {
                color: 'error',
                icon: <BlockIcon fontSize="small"/>,
                label: 'Unauthorized'
            };
        }
        if (activity.isUnSuccessful) {
            return {
                color: 'warning',
                icon: <ErrorIcon fontSize="small"/>,
                label: 'Unsuccessful'
            };
        }
        if (activity.hasIssues) {
            return {
                color: 'warning',
                icon: <ErrorIcon fontSize="small"/>,
                label: 'Has Issues'
            };
        }
        if (activity.leaveStatus === 'LEAVE_APPROVED') {
            return {
                color: 'success',
                icon: <CheckCircleIcon fontSize="small"/>,
                label: 'Leave Approved'
            };
        }
        if (activity.leaveStatus === 'LEAVE_REQUESTED') {
            return {
                color: 'info',
                icon: <HourglassEmptyIcon fontSize="small"/>,
                label: 'Leave Requested'
            };
        }
        if (activity.leaveStatus === 'FULL_LEAVE') {
            return {
                bgcolor: '#bbdefb',
                color: '#0d47a1',
                label: 'Full Leave'
            };
        }
        if (activity.isLate && !activity.isLateCovered) {
            return {
                color: 'warning',
                icon: <AccessTimeIcon fontSize="small"/>,
                label: 'Late'
            };
        }
        if (activity.isLate && activity.isLateCovered) {
            return {
                color: 'info',
                icon: <CheckCircleIcon fontSize="small"/>,
                label: 'Late Covered'
            };
        }

        return {
            color: 'success',
            icon: <CheckCircleIcon fontSize="small"/>,
            label: 'Normal'
        };
    };

    const {color, icon, label} = getChipProps();

    return (
        <Chip
            label={label}
            color={color}
            size="small"
            icon={icon}
            sx={{fontWeight: 500}}
        />
    );
};

const TypeBadge = ({activity}) => {
    const getTypeProps = () => {
        if (activity.isUnauthorized) {
            return {
                bgcolor: '#ffebee',
                color: '#c62828',
                label: 'Unauthorized'
            };
        }
        if (activity.isUnSuccessful) {
            return {
                bgcolor: '#fff3e0',
                color: '#e65100',
                label: 'UnSuccessful'
            };
        }
        if (activity.isLate) {
            return {
                bgcolor: '#fff9c4',
                color: '#f57f17',
                label: activity.isLateCovered ? 'Late Covered' : 'Late'
            };
        }

        if (activity.attendanceType === 'ABSENT') {
            return {
                bgcolor: '#ffcdd2',
                color: '#c62828',
                label: 'Absent'
            };
        }
        if (activity.attendanceType === 'HALF_DAY') {
            return {
                bgcolor: '#c8e6c9',
                color: '#1b5e20',
                label: 'Half Day'
            };
        }
        if (activity.attendanceType === 'FULL_DAY') {
            return {
                bgcolor: '#e8f5e9',
                color: '#2e7d32',
                label: 'Full Day'
            };
        }

        if (activity.leaveStatus === 'FULL_LEAVE') {
            return {
                bgcolor: '#bbdefb',
                color: '#0d47a1',
                label: 'Full Leave'
            };
        }
        if (activity.leaveStatus === 'SHORT_LEAVE') {
            return {
                bgcolor: '#e1bee7',
                color: '#4a148c',
                label: 'Short Leave'
            };
        }
        if (activity.leaveStatus === 'LEAVE_REQUESTED') {
            return {
                bgcolor: '#fff8e1',
                color: '#ff8f00',
                label: 'Leave Requested'
            };
        }
        if (activity.leaveStatus === 'LEAVE_APPROVED') {
            return {
                bgcolor: '#e8f5e9',
                color: '#2e7d32',
                label: 'Leave Approved'
            };
        }

        if (activity.hasIssues) {
            return {
                bgcolor: '#fff3e0',
                color: '#e65100',
                label: 'Has Issues'
            };
        }
        if (activity.isHoliday) {
            return {
                bgcolor: '#e3f2fd',
                color: '#01579b',
                label: 'Holiday'
            };
        }

        return {
            bgcolor: '#f5f5f5',
            color: '#424242',
            label: 'Not Recorded'
        };
    };

    const {bgcolor, color, label} = getTypeProps();

    return (
        <Chip
            label={label}
            size="small"
            sx={{
                bgcolor,
                color,
                fontWeight: 500,
                minWidth: 100,
                justifyContent: 'center'
            }}
        />
    );
};

const SingleEmployeeActivities = ({isAdmin = false, userId = null}) => {
    const {userDetails} = useSelector((state) => state.auth);

    if (userId == null) userId = sessionStorage.getItem('userId');
    const router = useRouter();

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
        filterActive,
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

    const [anchorEl, setAnchorEl] = React.useState(false);

    useEffect(() => {
        dispatch(fetchEmployeeActivities({
            userId,
            isAdmin,
            page,
            rowsPerPage,
        }));
    }, [dispatch, userId, isAdmin, page, rowsPerPage]);


    // Handle form input changes
    const handleChange = (e) => {
        const {name, value, type, checked} = e.target;
        dispatch(handleFormChange({name, value, type, checked}));
    };

    const handleEditClick = (activity) => {
        const preparedData = {
            date: formatDateForInput(activity.date),
            employeeId: activity.employeeId || '',
            arrivalDate: formatDateForInput(activity.arrivalDate),
            arrivalTime: formatTimeForInput(activity.arrivalTime),
            leftTime: formatTimeForInput(activity.leftTime),
            attendanceType: activity.attendanceType || 'FULL_DAY',
            leaveStatus: activity.leaveStatus || null,
            payStatus: activity.payStatus || null,
            resolve: activity.resolve || null,
            isLate: activity.isLate || false,
            isLateCovered: activity.isLateCovered || false,
            isUnauthorized: activity.isUnauthorized || false,
            isUnSuccessful: activity.isUnSuccessful || false,
            isHoliday: activity.isHoliday || false,
            isResolved: activity.isResolved || false,
            hasIssues: activity.hasIssues || false,
            isManual: activity.isManual || false,
            isActive: activity.isActive !== false,
            issueDescription: activity.issueDescription || '',
            dueDateForUA: formatDateForInput(activity.dueDateForUA),
            terminalId: activity.terminalId || ''
        };

        dispatch(setFormData(preparedData));
        dispatch(setEditId(activity.publicId));
        dispatch(setEditMode(true));
        dispatch(setShowModal(true));
    };

    const handleDeleteClick = (id) => {
        dispatch(setIdToDelete(id));
        dispatch(setOpenDeleteDialog(true));
        setAnchorEl(false);
    };

    const handleDeleteDeClick = (id) => {
        dispatch(setIdToDelete(id));
        dispatch(setOpenDeleteDialog(true));
        setAnchorEl(true);
    };

    const handleCloseDeleteDialog = () => {
        dispatch(setOpenDeleteDialog(false));
        dispatch(setIdToDelete(null));
    };

    const handleAddNew = () => {
        dispatch(resetFormData());
        dispatch(setEditMode(false));
        dispatch(setEditId(null));
        dispatch(setShowModal(true));
    };

    const handleCloseModal = () => {
        dispatch(setShowModal(false));
        dispatch(setEditMode(false));
        dispatch(setEditId(null));
        dispatch(resetFormData());
    };

    const handleSubmit = () => {
        if (isEditMode && editId) {
            dispatch(updateEmployeeActivity({id: editId, formData}));
        } else {
            dispatch(createEmployeeActivity(formData));
        }
    };

    const deleteAttendance = () => {
        if (anchorEl) {
            dispatch(deleteEmployeeDeActivity(idToDelete));
        } else {
            dispatch(deleteEmployeeActivity(idToDelete));
        }
    };

    const handleSearchChange = (event) => {
        dispatch(setSearchTerm(event.target.value));
    };

    const handleTypeFilterChange = (event) => {
        dispatch(setFilterType(event.target.value));
    };

    const handleStatusFilterChange = (event) => {
        dispatch(setFilterStatus(event.target.value));
    };

    const handleActiveFilterChange = (event) => {
        dispatch(setFilterActive(event.target.value));
    };

    const handleChangePage = (event, newPage) => {
        dispatch(setPage(newPage));
    };

    const handleChangeRowsPerPage = (event) => {
        dispatch(setRowsPerPage(parseInt(event.target.value, 10)));
    };

    const getActivityType = (activity) => {
        if (activity.isUnauthorized) return "Unauthorized";
        if (activity.isUnSuccessful) return "Unsuccessful";
        if (activity.isLate) return "Late";
        if (activity.attendanceType === 'ABSENT') return "Absent";
        if (activity.leaveStatus === 'FULL_LEAVE') return "Full Leave";
        if (activity.leaveStatus === 'SHORT_LEAVE') return "Short Leave";
        if (activity.attendanceType === 'HALF_DAY') return "Half Day";
        if (activity.attendanceType === 'FULL_DAY') return "Full Day";
        if (activity.leaveStatus === 'LEAVE_REQUESTED') return "Leave Requested";
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "Leave Approved";
        return "Not Specified";
    };

    const getActivityStatus = (activity) => {
        if (activity.isUnauthorized) return "Unauthorized";
        if (activity.isUnSuccessful) return "Unsuccessful";
        if (activity.leaveStatus === 'LEAVE_APPROVED') return "Approved";
        if (activity.leaveStatus === 'LEAVE_REQUESTED') return "Pending";
        if (activity.hasIssues) return "Issue";
        if (activity.isLate && !activity.isLateCovered) return "Late";
        return "Normal";
    };

    const filteredActivities = activities.filter((activity) => {
        const activityType = getActivityType(activity);
        const activityStatus = getActivityStatus(activity);

        const matchesSearch = activity.employeeId?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesType = filterType === "all" || activityType === filterType;
        const matchesStatus = filterStatus === "all" || activityStatus === filterStatus;
        const matchesActive = filterActive === "all" ||
            (filterActive === "active" && activity.isActive !== false) ||
            (filterActive === "inactive" && activity.isActive === false);

        const activityDate = activity.date ? new Date(activity.date).toISOString().split('T')[0] : "";
        const startDate = startDateFilter || "";
        const endDate = endDateFilter || "";

        const matchesStartDate = !startDate || activityDate >= startDate;
        const matchesEndDate = !endDate || activityDate <= endDate;

        return matchesSearch && matchesType && matchesStatus && matchesActive && matchesStartDate && matchesEndDate;
    });

    const paginatedActivities = filteredActivities.slice(
        page * rowsPerPage,
        page * rowsPerPage + rowsPerPage
    );

    const handleResolveViaMovement = (activity) => {
        const happenDate = activity.date ? format(new Date(activity.date), 'yyyy-MM-dd') : '';
        const logTime = activity.date ? format(new Date(activity.date), 'yyyy-MM-dd\'T\'HH:mm') : '';

        const params = new URLSearchParams();
        params.set('employeeId', activity.employeeId || '');
        params.set('userId', activity.userId || '');
        params.set('happenDate', happenDate);
        params.set('logTime', logTime);
        params.set('inTime', activity.arrivalTime || '');
        params.set('outTime', activity.leftTime || '');
        params.set('terminalId', activity.terminalId || '');
        params.set('comment', activity.issueDescription || '');
        params.set('isLate', activity.isLate ? 'true' : 'false');
        params.set('isUnauthorized', activity.isUnauthorized ? 'true' : 'false');
        params.set('hasIssues', activity.hasIssues ? 'true' : 'false');

        let movementType = MovementType.FULLDAY;
        if (activity.isUnauthorized && activity.arrivalTime == null) {
            movementType = MovementType.HOME_TO_OFFICE;
        } else if (activity.isUnauthorized && activity.leftTime == null) {
            movementType = MovementType.OFFICE_TO_HOME;
        }

        params.set('movementType', movementType);

        router.push(`/request-movement?${params.toString()}`);
    };

    return (
        <>
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
                <Box sx={{position: 'relative'}}>
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        p: 2,
                        borderBottom: '1px solid #e0e0e0',
                        bgcolor: '#f5f5f5'
                    }}>
                        <Typography variant="h6" sx={{fontWeight: 500}}>
                            {isEditMode ? 'Edit Attendance Record' : 'Add Attendance Record'}
                        </Typography>
                        <IconButton onClick={handleCloseModal} size="small">
                            <X size={20}/>
                        </IconButton>
                    </Box>

                    <Box sx={{p: 3}}>
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
                                    InputLabelProps={{shrink: true}}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <EventIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Employee ID"
                                    name="employeeId"
                                    value={formData.employeeId}
                                    onChange={handleChange}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <PersonIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Terminal ID"
                                    name="terminalId"
                                    value={formData.terminalId}
                                    onChange={handleChange}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <PersonIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Arrival Date"
                                    type="date"
                                    name="arrivalDate"
                                    value={formData.arrivalDate}
                                    onChange={handleChange}
                                    InputLabelProps={{shrink: true}}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <EventIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Arrival Time"
                                    type="time"
                                    name="arrivalTime"
                                    value={formData.arrivalTime}
                                    onChange={handleChange}
                                    InputLabelProps={{shrink: true}}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <AccessTimeIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Left Time"
                                    type="time"
                                    name="leftTime"
                                    value={formData.leftTime}
                                    onChange={handleChange}
                                    InputLabelProps={{shrink: true}}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <AccessTimeIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Attendance Type</InputLabel>
                                    <Select
                                        name="attendanceType"
                                        value={formData.attendanceType}
                                        onChange={handleChange}
                                        label="Attendance Type"
                                    >
                                        <MenuItem value="FULL_DAY">Full Day</MenuItem>
                                        <MenuItem value="HALF_DAY">Half Day</MenuItem>
                                        <MenuItem value="ABSENT">Absent</MenuItem>
                                        <MenuItem value="NONE">NONE</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Leave Status</InputLabel>
                                    <Select
                                        name="leaveStatus"
                                        value={formData.leaveStatus || ''}
                                        onChange={handleChange}
                                        label="Leave Status"
                                    >
                                        <MenuItem value="">None</MenuItem>
                                        <MenuItem value="NO_LEAVE">No Leave</MenuItem>
                                        <MenuItem value="FULL_LEAVE">Full Leave</MenuItem>
                                        <MenuItem value="SHORT_LEAVE">Short Leave</MenuItem>
                                        <MenuItem value="LEAVE_REQUESTED">Leave Requested</MenuItem>
                                        <MenuItem value="LEAVE_APPROVED">Leave Approved</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Pay Status</InputLabel>
                                    <Select
                                        name="payStatus"
                                        value={formData.payStatus || ''}
                                        onChange={handleChange}
                                        label="Pay Status"
                                    >
                                        <MenuItem value="">Normal Pay</MenuItem>
                                        <MenuItem value="NO_PAY">No Pay</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Resolve Type</InputLabel>
                                    <Select
                                        name="resolve"
                                        value={formData.resolve || ''}
                                        onChange={handleChange}
                                        label="Resolve Type"
                                    >
                                        <MenuItem value="">None</MenuItem>
                                        <MenuItem value="VIA_MOVEMENT">Via Movement</MenuItem>
                                        <MenuItem value="VIA_LEAVE">Via Leave</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Due Date (For UA)"
                                    type="date"
                                    name="dueDateForUA"
                                    value={formData.dueDateForUA}
                                    onChange={handleChange}
                                    InputLabelProps={{shrink: true}}
                                    InputProps={{
                                        startAdornment: (
                                            <Box sx={{mr: 1, color: 'text.secondary'}}>
                                                <EventIcon fontSize="small"/>
                                            </Box>
                                        ),
                                    }}
                                />
                            </Grid>

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

                            <Grid item xs={12}>
                                <Typography variant="subtitle1" sx={{fontWeight: 500, mb: 1.5}}>
                                    Additional Options
                                </Typography>

                                <Grid container spacing={1}>
                                    {[
                                        {name: 'isLate', label: 'Late'},
                                        {name: 'isLateCovered', label: 'Late Covered'},
                                        {name: 'isUnauthorized', label: 'Unauthorized'},
                                        {name: 'isUnSuccessful', label: 'Unsuccessful'},
                                        {name: 'isHoliday', label: 'Holiday'},
                                        {name: 'isResolved', label: 'Resolved'},
                                        {name: 'hasIssues', label: 'Has Issues'},
                                        {name: 'isManual', label: 'Manual Entry'},
                                        {name: 'isActive', label: 'Active'}
                                    ].map((field) => (
                                        <Grid item xs={6} sm={4} md={3} key={field.name}>
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        checked={formData[field.name] || false}
                                                        onChange={handleChange}
                                                        name={field.name}
                                                        color="primary"
                                                        size="small"
                                                    />
                                                }
                                                label={field.label}
                                            />
                                        </Grid>
                                    ))}
                                </Grid>
                            </Grid>
                        </Grid>
                    </Box>

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
                <CssBaseline/>
                <Box sx={{p: {xs: 2, md: 4}, backgroundColor: "#f5f7fa", minHeight: "100vh"}}>
                    <Card variant="outlined" sx={{mb: 4, borderRadius: 2, overflow: 'hidden'}}>
                        <Box
                            sx={{
                                p: 3,
                                backgroundImage: 'linear-gradient(120deg, #3f51b5, #5c6bc0)',
                                display: 'flex',
                                alignItems: 'center'
                            }}
                        >
                            <Avatar sx={{bgcolor: '#fff', color: '#3f51b5', mr: 2}}>
                                <EventNoteIcon/>
                            </Avatar>
                            <Typography variant="h4" gutterBottom sx={{color: 'white', fontWeight: 'bold', mb: 0}}>
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
                                sx={{ml: 2}}
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
                            <CircularProgress size={60}/>
                            <Typography variant="h6" color="textSecondary">Loading activities...</Typography>
                        </Box>
                    ) : (
                        <>
                            <Card sx={{mb: 4, borderRadius: 2}}>
                                <CardContent sx={{pb: 2}}>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                            mb: 2
                                        }}
                                    >
                                        <Box sx={{display: 'flex', alignItems: 'center'}}>
                                            <SearchIcon sx={{color: 'action.active', mr: 1}}/>
                                            <TextField
                                                label="Search by Employee ID"
                                                variant="outlined"
                                                size="small"
                                                value={searchTerm}
                                                onChange={handleSearchChange}
                                                sx={{minWidth: 200}}
                                            />
                                        </Box>

                                        <Box sx={{display: 'flex', alignItems: 'center'}}>
                                            {isAdmin && (
                                                <Button
                                                    variant="contained"
                                                    color="primary"
                                                    startIcon={<PersonIcon/>}
                                                    onClick={handleAddNew}
                                                    sx={{mr: 1}}
                                                >
                                                    Add Record
                                                </Button>
                                            )}

                                            <Tooltip title={showFilters ? "Hide Filters" : "Show Filters"}>
                                                <IconButton
                                                    onClick={() => dispatch(setShowFilters(!showFilters))}
                                                    color="primary"
                                                >
                                                    {showFilters ? <ExpandLessIcon/> : <ExpandMoreIcon/>}
                                                </IconButton>
                                            </Tooltip>
                                        </Box>
                                    </Box>

                                    <Collapse in={showFilters}>
                                        <Divider sx={{my: 2}}/>

                                        <Box
                                            sx={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                mb: 1
                                            }}
                                        >
                                            <FilterListIcon sx={{mr: 1, color: 'action.active'}}/>
                                            <Typography variant="subtitle1" fontWeight="medium">
                                                Filters
                                            </Typography>
                                        </Box>

                                        <Grid container spacing={2} sx={{mt: 1}}>
                                            <Grid item xs={12} md={2.4}>
                                                <FormControl variant="outlined" size="small" fullWidth>
                                                    <InputLabel>Type</InputLabel>
                                                    <Select
                                                        value={filterType}
                                                        onChange={handleTypeFilterChange}
                                                        label="Type"
                                                    >
                                                        <MenuItem value="all">All Types</MenuItem>
                                                        <MenuItem value="Full Day">Full Day</MenuItem>
                                                        <MenuItem value="Late">Late</MenuItem>
                                                        <MenuItem value="Absent">Absent</MenuItem>
                                                        <MenuItem value="Full Leave">Full Leave</MenuItem>
                                                        <MenuItem value="Half Day">Half Day</MenuItem>
                                                        <MenuItem value="Short Leave">Short Leave</MenuItem>
                                                        <MenuItem value="Swipe error">Swipe Error</MenuItem>
                                                    </Select>
                                                </FormControl>
                                            </Grid>

                                            <Grid item xs={12} md={2.4}>
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
                                                        <MenuItem value="Late">Late</MenuItem>
                                                    </Select>
                                                </FormControl>
                                            </Grid>

                                            <Grid item xs={12} md={2.4}>
                                                <FormControl variant="outlined" size="small" fullWidth>
                                                    <InputLabel>Active Status</InputLabel>
                                                    <Select
                                                        value={filterActive}
                                                        onChange={handleActiveFilterChange}
                                                        label="Active Status"
                                                    >
                                                        <MenuItem value="all">All Records</MenuItem>
                                                        <MenuItem value="active">Active Only</MenuItem>
                                                        <MenuItem value="inactive">Inactive Only</MenuItem>
                                                    </Select>
                                                </FormControl>
                                            </Grid>

                                            <Grid item xs={12} md={2.4}>
                                                <TextField
                                                    label="Start Date"
                                                    type="date"
                                                    variant="outlined"
                                                    size="small"
                                                    fullWidth
                                                    value={startDateFilter}
                                                    onChange={(e) => dispatch(setStartDateFilter(e.target.value))}
                                                    InputLabelProps={{shrink: true}}
                                                />
                                            </Grid>

                                            <Grid item xs={12} md={2.4}>
                                                <TextField
                                                    label="End Date"
                                                    type="date"
                                                    variant="outlined"
                                                    size="small"
                                                    fullWidth
                                                    value={endDateFilter}
                                                    onChange={(e) => dispatch(setEndDateFilter(e.target.value))}
                                                    InputLabelProps={{shrink: true}}
                                                />
                                            </Grid>
                                        </Grid>
                                    </Collapse>
                                </CardContent>
                            </Card>

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

                            <Card sx={{borderRadius: 2}}>
                                <TableContainer>
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>
                                                    <Box sx={{display: 'flex', alignItems: 'center'}}>
                                                        <PersonIcon fontSize="small" sx={{mr: 1, opacity: 0.7}}/>
                                                        Employee ID
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{display: 'flex', alignItems: 'center'}}>
                                                        <EventIcon fontSize="small" sx={{mr: 1, opacity: 0.7}}/>
                                                        Date
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{display: 'flex', alignItems: 'center'}}>
                                                        <AccessTimeIcon fontSize="small" sx={{mr: 1, opacity: 0.7}}/>
                                                        Arrival Time
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{display: 'flex', alignItems: 'center'}}>
                                                        <AccessTimeIcon fontSize="small" sx={{mr: 1, opacity: 0.7}}/>
                                                        Left Time
                                                    </Box>
                                                </TableCell>
                                                <TableCell>Type</TableCell>
                                                <TableCell>Status</TableCell>
                                                <TableCell>Terminal</TableCell>
                                                <TableCell>Roster Type</TableCell>
                                                <TableCell>Issue Description</TableCell>
                                                <TableCell>Actions</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {paginatedActivities.length > 0 ? (
                                                paginatedActivities.map((activity) => (
                                                    (isAdmin || userDetails.sltId === activity.userId) && (
                                                        <>
                                                            <TableRow
                                                                key={activity.id}
                                                                sx={{
                                                                    '&:hover': {
                                                                        backgroundColor: '#f5f5f5',
                                                                    },
                                                                    backgroundColor: activity.isActive === false ? '#ffebee' : 'inherit'
                                                                }}
                                                            >
                                                                <TableCell sx={{fontWeight: 500}}>
                                                                    {activity.userId}
                                                                    {activity.isActive === false && (
                                                                        <Chip
                                                                            label="Inactive"
                                                                            size="small"
                                                                            color="error"
                                                                            sx={{ml: 1, fontSize: '0.7rem'}}
                                                                        />
                                                                    )}
                                                                </TableCell>
                                                                <TableCell>{formatDate(activity.arrivalDate)}</TableCell>
                                                                <TableCell>{activity.arrivalTime || "-"}</TableCell>
                                                                <TableCell>{activity.leftTime || "-"}</TableCell>
                                                                <TableCell>
                                                                    <TypeBadge activity={activity}/>
                                                                </TableCell>
                                                                <TableCell>
                                                                    <StatusChip activity={activity}/>
                                                                </TableCell>
                                                                <TableCell>{activity.terminalId || "-"}</TableCell>
                                                                <TableCell>{activity.rosterType || "NONE"}</TableCell>
                                                                <TableCell sx={{
                                                                    maxWidth: 300,
                                                                    whiteSpace: 'normal',
                                                                    wordBreak: 'break-word'
                                                                }}>
                                                                    {activity.hasIssues && activity.issueDescription ? (
                                                                        <Tooltip title={activity.issueDescription}
                                                                                 arrow>
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
                                                                    {(isAdmin && activity.isManual) ||
                                                                        (isAdmin && userDetails.highestRolePriority > 0 && userDetails.highestRolePriority < 50) && (
                                                                            <Box sx={{display: 'flex', gap: 1}}>
                                                                                <IconButton
                                                                                    color="primary"
                                                                                    onClick={() => handleEditClick(activity)}
                                                                                    size="small"
                                                                                >
                                                                                    <EditIcon fontSize="small"/>
                                                                                </IconButton>
                                                                                <IconButton
                                                                                    color="error"
                                                                                    onClick={() => handleDeleteClick(activity.publicId)}
                                                                                    size="small"
                                                                                >
                                                                                    <TrashIcon size={18}/>
                                                                                </IconButton>
                                                                            </Box>
                                                                        )}
                                                                    {isAdmin && !activity.isManual && activity.isActive && (
                                                                        <Box sx={{display: 'flex', gap: 1}}>
                                                                            <IconButton
                                                                                color="error"
                                                                                onClick={() => handleDeleteDeClick(activity.publicId)}
                                                                                size="small"
                                                                            >
                                                                                <X size={18}/>
                                                                            </IconButton>
                                                                        </Box>
                                                                    )}
                                                                    {!isAdmin && activity.isActive && activity.isUnauthorized && (
                                                                        <Box sx={{display: 'flex', gap: 1}}>
                                                                            <Button
                                                                                variant="contained"
                                                                                color="primary"
                                                                                size="small"
                                                                                onClick={() => handleResolveViaMovement(activity)}
                                                                                sx={{textTransform: 'none'}}
                                                                            >
                                                                                Resolve via Movement
                                                                            </Button>
                                                                        </Box>
                                                                    )}
                                                                </TableCell>
                                                            </TableRow>
                                                        </>
                                                    )
                                                ))
                                            ) : (
                                                <TableRow>
                                                    <TableCell colSpan={9} align="center" sx={{py: 4}}>
                                                        <Box sx={{
                                                            display: 'flex',
                                                            flexDirection: 'column',
                                                            alignItems: 'center',
                                                            py: 2
                                                        }}>
                                                            <FilterListIcon
                                                                sx={{fontSize: 40, color: 'text.secondary', mb: 1}}/>
                                                            <Typography variant="h6" color="textSecondary">
                                                                No activities found matching the current filters
                                                            </Typography>
                                                            <Typography variant="body2" color="textSecondary"
                                                                        sx={{mt: 1}}>
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
                                                        rowsPerPageOptions={[5, 10, 25, {label: 'All', value: -1}]}
                                                        colSpan={9}
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