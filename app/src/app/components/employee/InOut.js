'use client'

import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
    fetchAttendanceData,
    setStartDate,
    setEndDate,
    toggleDateRangeMode,
    clearError,
    selectAttendanceData,
    selectFilters,
    selectLoading,
    selectError,
    selectEmployeeInfo,
    attendanceHelpers
} from '../../../../lib/redux/redux-lms/in-outs/attendanceSlice';
import {
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
    TextField,
    Button,
    Grid,
    Box,
    CircularProgress,
    Card,
    CardContent,
    Chip,
    Badge,
    Avatar,
    createTheme,
    ThemeProvider
} from '@mui/material';
import { AccessTime, CalendarToday, Person, Work } from '@mui/icons-material';

// Dark theme configuration
const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            main: '#90caf9',
        },
        secondary: {
            main: '#ce93d8',
        },
        background: {
            default: '#121212',
            paper: '#1e1e1e',
        },
        text: {
            primary: '#ffffff',
            secondary: '#b0bec5',
        },
    },
    components: {
        MuiCard: {
            styleOverrides: {
                root: {
                    backgroundImage: 'linear-gradient(rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05))',
                    borderRadius: '12px',
                }
            }
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    borderRadius: '8px',
                }
            }
        }
    }
});

const AttendanceTracker = ({ userId }) => {
    const dispatch = useDispatch();

    // Select from Redux store
    const attendanceData = useSelector(selectAttendanceData);
    const { startDate, endDate, dateRangeMode } = useSelector(selectFilters);
    const loading = useSelector(selectLoading);
    const error = useSelector(selectError);
    const { employeeName } = useSelector(selectEmployeeInfo);
    const { userDetails } = useSelector((state) => state.auth);

    // Destructure helpers for readability
    const {
        formatTime,
        processAttendanceData,
        groupInOutPairs,
        calculateDuration,
        calculateSummary
    } = attendanceHelpers;

    // If userId is not passed, get from session storage
    if (!userId) {
        userId = sessionStorage.getItem('userId');
    }

    // Fetch attendance data when component mounts or filters change
    useEffect(() => {
        dispatch(fetchAttendanceData({
            userId,
            startDate,
            endDate,
            dateRangeMode
        }));
    }, [dispatch, userId, startDate, endDate, dateRangeMode]);

    // Handle start date change
    const handleStartDateChange = (e) => {
        dispatch(setStartDate(e.target.value));
    };

    // Handle end date change
    const handleEndDateChange = (e) => {
        dispatch(setEndDate(e.target.value));
    };

    // Toggle date range mode
    const handleToggleDateRangeMode = () => {
        dispatch(toggleDateRangeMode());
    };

    // Handle refresh button click
    const handleRefresh = () => {
        dispatch(fetchAttendanceData({
            userId,
            startDate,
            endDate,
            dateRangeMode
        }));
    };

    // Process data and calculate summary
    const processedData = processAttendanceData(attendanceData);
    const summary = calculateSummary(attendanceData);

    return (
        <ThemeProvider theme={darkTheme}>
            <Box sx={{
                p: 3,
                minHeight: '100vh',
                background: 'linear-gradient(to bottom, #121212, #212121)'
            }}>
                {/* Header with Employee Info */}
                <Paper
                    elevation={3}
                    sx={{
                        p: 2,
                        mb: 3,
                        borderRadius: '12px',
                        background: 'linear-gradient(45deg, #1a237e, #283593)'
                    }}
                >
                    <Grid container alignItems="center" spacing={2}>
                        <Grid item>
                            <Avatar
                                sx={{
                                    width: 56,
                                    height: 56,
                                    bgcolor: '#5c6bc0',
                                    border: '2px solid #fff'
                                }}
                            >
                                <Person fontSize="large" />
                            </Avatar>
                        </Grid>
                        <Grid item xs>
                            <Typography variant="h5" fontWeight="bold">
                                {employeeName}
                            </Typography>
                            <Grid container spacing={1} alignItems="center">
                                <Grid item>
                                    <Chip
                                        label={`ID: ${userId}`}
                                        color="secondary"
                                        size="small"
                                        sx={{ fontWeight: 'bold' }}
                                    />
                                </Grid>
                                <Grid item>
                                    <Chip
                                        icon={<Work />}
                                        label="Full-Time"
                                        variant="outlined"
                                        size="small"
                                        sx={{ borderColor: '#fff', color: '#fff' }}
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Paper>

                <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', color: '#fff' }}>
                    Attendance Tracker
                </Typography>

                {/* Date Selection */}
                <Paper sx={{ p: 2, mb: 3, borderRadius: '12px' }}>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={4}>
                            <TextField
                                label="Start Date"
                                type="date"
                                value={startDate}
                                onChange={handleStartDateChange}
                                fullWidth
                                InputLabelProps={{shrink: true}}
                                variant="outlined"
                            />
                        </Grid>

                        {dateRangeMode && (
                            <Grid item xs={12} sm={4}>
                                <TextField
                                    label="End Date"
                                    type="date"
                                    value={endDate}
                                    onChange={handleEndDateChange}
                                    fullWidth
                                    InputLabelProps={{shrink: true}}
                                    variant="outlined"
                                />
                            </Grid>
                        )}

                        <Grid item xs={12} sm={dateRangeMode ? 4 : 8} sx={{display: 'flex', alignItems: 'center'}}>
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={handleToggleDateRangeMode}
                                sx={{ mr: 2, borderRadius: '8px' }}
                                startIcon={<CalendarToday />}
                            >
                                {dateRangeMode ? 'Single Day' : 'Date Range'}
                            </Button>

                            <Button
                                variant="contained"
                                color="secondary"
                                onClick={handleRefresh}
                                sx={{ borderRadius: '8px' }}
                            >
                                Refresh
                            </Button>
                        </Grid>
                    </Grid>
                </Paper>

                {/* Summary Cards */}
                <Grid container spacing={3} sx={{ mb: 4 }}>
                    <Grid item xs={12} md={4}>
                        <Card
                            sx={{
                                height: '100%',
                                borderLeft: '4px solid #5c6bc0'
                            }}
                        >
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Avatar sx={{ bgcolor: 'primary.dark', mr: 2 }}>
                                        <CalendarToday />
                                    </Avatar>
                                    <Typography variant="h6" color="text.secondary">
                                        Working Days
                                    </Typography>
                                </Box>
                                <Typography variant="h3" sx={{ fontWeight: 'bold' }}>
                                    {summary.totalWorkingDays}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} md={4}>
                        <Card
                            sx={{
                                height: '100%',
                                borderLeft: '4px solid #7e57c2'
                            }}
                        >
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Avatar sx={{ bgcolor: 'secondary.dark', mr: 2 }}>
                                        <AccessTime />
                                    </Avatar>
                                    <Typography variant="h6" color="text.secondary">
                                        Total Hours
                                    </Typography>
                                </Box>
                                <Typography variant="h3" sx={{ fontWeight: 'bold' }}>
                                    {summary.totalWorkHours}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} md={4}>
                        <Card
                            sx={{
                                height: '100%',
                                borderLeft: '4px solid #26a69a'
                            }}
                        >
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Avatar sx={{ bgcolor: '#26a69a', mr: 2 }}>
                                        <AccessTime />
                                    </Avatar>
                                    <Typography variant="h6" color="text.secondary">
                                        Avg. Daily Hours
                                    </Typography>
                                </Box>
                                <Typography variant="h3" sx={{ fontWeight: 'bold' }}>
                                    {summary.averageDailyHours}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>

                {/* Error Message */}
                {error && (
                    <Box sx={{ mb: 2 }}>
                        <Paper sx={{ p: 2, bgcolor: '#f44336', color: '#fff', borderRadius: '8px' }}>
                            <Typography>{error}</Typography>
                        </Paper>
                    </Box>
                )}

                {/* Loading Indicator */}
                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                        <CircularProgress size={60} thickness={5} />
                    </Box>
                ) : (
                    /* Attendance Data Table */
                    processedData.length > 0 ? (
                        <>
                            {processedData.map((dayData) => (
                                <Box key={dayData.date} sx={{ mb: 4 }}>
                                    <Paper
                                        sx={{
                                            p: 2,
                                            mb: 2,
                                            borderRadius: '12px 12px 0 0',
                                            background: 'linear-gradient(45deg, #303f9f, #3949ab)'
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <Typography variant="h6" sx={{ color: '#fff', fontWeight: 'bold' }}>
                                                {new Date(dayData.date).toLocaleDateString('en-US', {
                                                    weekday: 'long',
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric'
                                                })}
                                            </Typography>
                                            <Chip
                                                label={`${groupInOutPairs(dayData.records).length} records`}
                                                color="secondary"
                                                sx={{ fontWeight: 'bold' }}
                                            />
                                        </Box>
                                    </Paper>

                                    <TableContainer component={Paper} sx={{ borderRadius: '0 0 12px 12px' }}>
                                        <Table>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    }}>No.</TableCell>
                                                    <TableCell sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    }}>Punch In</TableCell>
                                                    <TableCell sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    }}>Type</TableCell>
                                                    <TableCell sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    }}>Punch Out</TableCell>
                                                    <TableCell sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    }}>Type</TableCell>
                                                    <TableCell sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                                    }}>Duration</TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {groupInOutPairs(dayData.records).map((pair, index) => (
                                                    <TableRow
                                                        key={index}
                                                        sx={{
                                                            '&:nth-of-type(odd)': { backgroundColor: 'rgba(255, 255, 255, 0.03)' },
                                                            '&:hover': { backgroundColor: 'rgba(144, 202, 249, 0.08)' }
                                                        }}
                                                    >
                                                        <TableCell>{index + 1}</TableCell>
                                                        <TableCell>
                                                            {pair.in ? formatTime(pair.in.timeMoa || pair.in.timeEve) : '--:--'}
                                                        </TableCell>
                                                        <TableCell>
                                                            {pair.in ?
                                                                <Chip
                                                                    label={pair.in.moaning ? 'Morning' : pair.in.evening ? 'Evening' : 'Unknown'}
                                                                    size="small"
                                                                    color={pair.in.moaning ? 'primary' : 'secondary'}
                                                                    sx={{ minWidth: '80px' }}
                                                                /> :
                                                                '-'
                                                            }
                                                        </TableCell>
                                                        <TableCell>
                                                            {pair.out ? formatTime(pair.out.timeMoa || pair.out.timeEve) : '--:--'}
                                                        </TableCell>
                                                        <TableCell>
                                                            {pair.out ?
                                                                <Chip
                                                                    label={pair.out.moaning ? 'Morning' : pair.out.evening ? 'Evening' : 'Unknown'}
                                                                    size="small"
                                                                    color={pair.out.moaning ? 'primary' : 'secondary'}
                                                                    sx={{ minWidth: '80px' }}
                                                                /> :
                                                                '-'
                                                            }
                                                        </TableCell>
                                                        <TableCell>
                                                            <Typography
                                                                fontWeight="bold"
                                                                color={pair.in && pair.out ? 'success.main' : 'text.secondary'}
                                                            >
                                                                {pair.in && pair.out ?
                                                                    calculateDuration(
                                                                        pair.in.timeMoa || pair.in.timeEve,
                                                                        pair.out.timeMoa || pair.out.timeEve
                                                                    ) :
                                                                    '--:--'
                                                                }
                                                            </Typography>
                                                        </TableCell>
                                                    </TableRow>
                                                ))}
                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                </Box>
                            ))}
                        </>
                    ) : (
                        <Paper sx={{ p: 4, textAlign: 'center', borderRadius: '12px' }}>
                            <Typography variant="h6" sx={{ color: 'text.secondary' }}>
                                No attendance records found for the selected date(s)
                            </Typography>
                        </Paper>
                    )
                )}
            </Box>
        </ThemeProvider>
    );
};

export default AttendanceTracker;