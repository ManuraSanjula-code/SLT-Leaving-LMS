"use client";

import React, { useState, useEffect } from "react";
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
    TablePagination
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

const SingleEmployeeActivities = () => {
    const [activities, setActivities] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [searchTerm, setSearchTerm] = useState("");
    const [filterType, setFilterType] = useState("all");
    const [filterStatus, setFilterStatus] = useState("all");
    const [startDateFilter, setStartDateFilter] = useState("");
    const [endDateFilter, setEndDateFilter] = useState("");

    // UI state
    const [showFilters, setShowFilters] = useState(false);

    // Pagination state
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(5);

    useEffect(() => {
        const fetchEmployeeActivities = async () => {
            try {
                setLoading(true);

                // Get userId from sessionStorage
                const userId = sessionStorage.getItem("userId");

                if (!userId) {
                    setError("User ID not found. Please login again.");
                    setLoading(false);
                    return;
                }

                // Fetch data from API
                const response = await fetch(`http://localhost:8080/lms/${userId}`, {
                    credentials: 'include', // This sends all cookies with the request
                });

                if (!response.ok) {
                    if (response.status === 404) {
                        setError("User not found. Please check your credentials.");
                    } else {
                        setError(`Error fetching data: ${response.statusText}`);
                    }
                    setLoading(false);
                    return;
                }

                const data = await response.json();
                setActivities(data.content || []);
                setLoading(false);
            } catch (err) {
                console.error("Error fetching employee activities:", err);
                setError("Failed to load attendance data. Please try again later.");
                setLoading(false);
            }
        };

        fetchEmployeeActivities();
    }, []);

    // Reset to first page when filters change
    useEffect(() => {
        setPage(0);
    }, [searchTerm, filterType, filterStatus, startDateFilter, endDateFilter]);

    // Handle search input change
    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
    };

    // Handle type filter change
    const handleTypeFilterChange = (event) => {
        setFilterType(event.target.value);
    };

    // Handle status filter change
    const handleStatusFilterChange = (event) => {
        setFilterStatus(event.target.value);
    };

    // Handle page change
    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    // Handle rows per page change
    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    // Determine activity type based on properties
    const getActivityType = (activity) => {
        if (activity.absent) return "Absent";
        if (activity.fullLeave) return "Full Leave";
        if (activity.halfDay) return "Half Day";
        if (activity.shortLeave) return "Short Leave";
        if (activity.late) return "Late";
        return "Present";
    };

    // Determine activity status based on properties
    const getActivityStatus = (activity) => {
        if (activity.unAuthorized) return "Unauthorized";
        if (activity.unSuccessful) return "Unsuccessful";
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
                            Your Activities
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

                                    <Tooltip title={showFilters ? "Hide Filters" : "Show Filters"}>
                                        <IconButton
                                            onClick={() => setShowFilters(!showFilters)}
                                            color="primary"
                                        >
                                            {showFilters ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                                        </IconButton>
                                    </Tooltip>
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
                                                <Select value={filterType} onChange={handleTypeFilterChange} label="Type">
                                                    <MenuItem value="all">All Types</MenuItem>
                                                    <MenuItem value="Present">Present</MenuItem>
                                                    <MenuItem value="Late">Late</MenuItem>
                                                    <MenuItem value="Absent">Absent</MenuItem>
                                                    <MenuItem value="Full Leave">Full Leave</MenuItem>
                                                    <MenuItem value="Half Day">Half Day</MenuItem>
                                                    <MenuItem value="Short Leave">Short Leave</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Grid>

                                        <Grid item xs={12} md={3}>
                                            <FormControl variant="outlined" size="small" fullWidth>
                                                <InputLabel>Status</InputLabel>
                                                <Select value={filterStatus} onChange={handleStatusFilterChange} label="Status">
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
                                                onChange={(e) => setStartDateFilter(e.target.value)}
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
                                                onChange={(e) => setEndDateFilter(e.target.value)}
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
                                                    <TableCell sx={{ fontWeight: 500 }}>{activity.employeeID}</TableCell>
                                                    <TableCell>{formatDate(activity.arrivalDate)}</TableCell>
                                                    <TableCell>{activity.arrivalTime || "-"}</TableCell>
                                                    <TableCell>{activity.leftTime || "-"}</TableCell>
                                                    <TableCell>
                                                        <TypeBadge type={getActivityType(activity)} />
                                                    </TableCell>
                                                    <TableCell>
                                                        <StatusChip status={getActivityStatus(activity)} />
                                                    </TableCell>
                                                    <TableCell sx={{ maxWidth: 300, whiteSpace: 'normal', wordBreak: 'break-word' }}>
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
                                                </TableRow>
                                            ))
                                        ) : (
                                            <TableRow>
                                                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                                                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 2 }}>
                                                        <FilterListIcon sx={{ fontSize: 40, color: 'text.secondary', mb: 1 }} />
                                                        <Typography variant="h6" color="textSecondary">
                                                            No activities found matching the current filters
                                                        </Typography>
                                                        <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
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
                                                    colSpan={7}
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
    );
};

export default SingleEmployeeActivities;