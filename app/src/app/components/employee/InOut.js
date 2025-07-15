'use client'

import React, { useEffect, useState } from 'react';
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
    selectEmployeeInfo
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
    Alert,
    Pagination,
    FormControl,
    InputLabel,
    Select,
    MenuItem
} from '@mui/material';

const AttendanceTracker = ({ userId }) => {
    const dispatch = useDispatch();

    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(5);

    const attendanceData = useSelector(selectAttendanceData);
    const { startDate, endDate, dateRangeMode } = useSelector(selectFilters);
    const loading = useSelector(selectLoading);
    const error = useSelector(selectError);
    const { employeeName } = useSelector(selectEmployeeInfo);

    useEffect(() => {
        
    }, [attendanceData]);

    if (!userId) {
        userId = sessionStorage.getItem('userId');
    }

    useEffect(() => {
        dispatch(fetchAttendanceData({
            userId,
            startDate,
            endDate,
            dateRangeMode
        }));
    }, [dispatch, userId, startDate, endDate, dateRangeMode]);

    const handleStartDateChange = (e) => {
        dispatch(setStartDate(e.target.value));
    };

    const handleEndDateChange = (e) => {
        dispatch(setEndDate(e.target.value));
    };

    const handleToggleDateRangeMode = () => {
        dispatch(toggleDateRangeMode());
    };

    const handleRefresh = () => {
        dispatch(fetchAttendanceData({
            userId,
            startDate,
            endDate,
            dateRangeMode
        }));
    };

    // Format time - use punchTypeTime which contains the actual time
    const formatTime = (timeString) => {
        if (!timeString) return '--:--';
        try {
            // If it's already in HH:MM:SS format, convert to 12-hour format
            const [hours, minutes, seconds] = timeString.split(':').map(Number);
            const date = new Date();
            date.setHours(hours, minutes, seconds || 0);

            return date.toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: true
            });
        } catch {
            return timeString; // Return as-is if parsing fails
        }
    };

    // Format full date and time using punchTime
    const formatFullDateTime = (isoString) => {
        if (!isoString) return '--:--';
        try {
            const date = new Date(isoString);
            return date.toLocaleString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: true
            });
        } catch {
            return '--:--';
        }
    };

    // Group records into in/out pairs
    const groupInOutPairs = (records) => {
        const pairs = [];
        let currentPair = {};

        // Sort records by punchTypeTime (the actual time)
        const sortedRecords = [...records].sort((a, b) => {
            const timeA = a.punchTypeTime || '00:00:00';
            const timeB = b.punchTypeTime || '00:00:00';
            return timeA.localeCompare(timeB);
        });

        sortedRecords.forEach(record => {
            if (record.inOutValue === 1) { // Punch in
                if (currentPair.in) {
                    pairs.push({...currentPair, out: null});
                }
                currentPair = { in: record, out: null };
            } else if (record.inOutValue === 0) { // Punch out
                if (currentPair.in) {
                    currentPair.out = record;
                    pairs.push({...currentPair});
                    currentPair = {};
                } else {
                    pairs.push({ in: null, out: record });
                }
            }
        });

        if (currentPair.in) {
            pairs.push(currentPair);
        }

        return pairs;
    };

    // Calculate duration between two time strings (HH:MM:SS format)
    const calculateDuration = (startTime, endTime) => {
        if (!startTime || !endTime) return '--:--';
        try {
            const [startHours, startMinutes, startSeconds] = startTime.split(':').map(Number);
            const [endHours, endMinutes, endSeconds] = endTime.split(':').map(Number);

            let startTotalMinutes = startHours * 60 + startMinutes;
            let endTotalMinutes = endHours * 60 + endMinutes;

            // Handle overnight shifts
            if (endTotalMinutes < startTotalMinutes) {
                endTotalMinutes += 24 * 60;
            }

            const diffMinutes = endTotalMinutes - startTotalMinutes;
            const hours = Math.floor(diffMinutes / 60);
            const minutes = diffMinutes % 60;

            return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
        } catch {
            return '--:--';
        }
    };

    // Process attendance data by date
    const processAttendanceData = (data) => {
        if (!data || !Array.isArray(data)) return [];

        const groupedByDate = {};

        data.forEach(record => {
            const date = new Date(record.date);
            const dateKey = date.toISOString().split('T')[0];

            if (!groupedByDate[dateKey]) {
                groupedByDate[dateKey] = {
                    date: dateKey,
                    records: []
                };
            }

            groupedByDate[dateKey].records.push(record);
        });

        return Object.values(groupedByDate).sort((a, b) =>
            new Date(b.date) - new Date(a.date)
        );
    };

    // Calculate summary statistics
    const calculateSummary = (data) => {
        const processed = processAttendanceData(data);
        let totalWorkingDays = 0;
        let totalMinutes = 0;

        processed.forEach(day => {
            const pairs = groupInOutPairs(day.records);
            let hasValidPair = false;

            pairs.forEach(pair => {
                if (pair.in && pair.out) {
                    const duration = calculateDuration(pair.in.punchTypeTime, pair.out.punchTypeTime);
                    if (duration !== '--:--') {
                        hasValidPair = true;
                        const [hours, minutes] = duration.split(':').map(Number);
                        totalMinutes += hours * 60 + minutes;
                    }
                }
            });

            if (hasValidPair) totalWorkingDays++;
        });

        const totalHours = (totalMinutes / 60).toFixed(2);
        const avgHours = totalWorkingDays > 0 ? (totalMinutes / (totalWorkingDays * 60)).toFixed(2) : 0;

        return {
            totalWorkingDays,
            totalWorkHours: totalHours,
            averageDailyHours: avgHours
        };
    };

    const processedData = processAttendanceData(attendanceData);
    const summary = calculateSummary(attendanceData);

    const totalPages = Math.ceil(processedData.length / pageSize);
    const startIndex = (page - 1) * pageSize;
    const paginatedData = processedData.slice(startIndex, startIndex + pageSize);

    const handlePageChange = (event, newPage) => {
        setPage(newPage);
    };

    const handlePageSizeChange = (event) => {
        setPageSize(event.target.value);
        setPage(1);
    };

    useEffect(() => {
        setPage(1);
    }, [attendanceData]);

    return (
        <Box sx={{
            minHeight: '100vh',
            backgroundColor: '#ffffff',
            color: '#000000',
            p: 3
        }}>
            <Typography variant="h4" gutterBottom sx={{ color: '#000000' }}>
                Attendance Tracker
            </Typography>

            {(employeeName || userId) && (
                <Paper sx={{ p: 2, mb: 3, backgroundColor: '#ffffff', boxShadow: 1 }}>
                    <Typography variant="h6" sx={{ color: '#000000' }}>
                        {employeeName || 'Employee Dashboard'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ color: '#666666' }}>
                        Employee ID: {userId || 'N/A'}
                    </Typography>
                </Paper>
            )}

            <Paper sx={{ p: 2, mb: 3, backgroundColor: '#ffffff', boxShadow: 1 }}>
                <Grid container spacing={2} alignItems="center">
                    <Grid item xs={12} sm={3}>
                        <TextField
                            label="Start Date"
                            type="date"
                            value={startDate}
                            onChange={handleStartDateChange}
                            fullWidth
                            InputLabelProps={{ shrink: true }}
                            sx={{
                                '& .MuiOutlinedInput-root': {
                                    backgroundColor: '#ffffff',
                                    color: '#000000'
                                },
                                '& .MuiInputLabel-root': {
                                    color: '#000000'
                                }
                            }}
                        />
                    </Grid>

                    {dateRangeMode && (
                        <Grid item xs={12} sm={3}>
                            <TextField
                                label="End Date"
                                type="date"
                                value={endDate}
                                onChange={handleEndDateChange}
                                fullWidth
                                InputLabelProps={{ shrink: true }}
                                sx={{
                                    '& .MuiOutlinedInput-root': {
                                        backgroundColor: '#ffffff',
                                        color: '#000000'
                                    },
                                    '& .MuiInputLabel-root': {
                                        color: '#000000'
                                    }
                                }}
                            />
                        </Grid>
                    )}

                    <Grid item xs={12} sm={dateRangeMode ? 6 : 9}>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                            <Button
                                variant="outlined"
                                onClick={handleToggleDateRangeMode}
                                sx={{
                                    backgroundColor: '#ffffff',
                                    color: '#1976d2',
                                    borderColor: '#1976d2',
                                    '&:hover': {
                                        backgroundColor: '#f5f5f5',
                                        borderColor: '#1976d2'
                                    }
                                }}
                            >
                                {dateRangeMode ? 'Single Day' : 'Date Range'}
                            </Button>
                            <Button
                                variant="contained"
                                onClick={handleRefresh}
                                sx={{
                                    backgroundColor: '#1976d2',
                                    '&:hover': {
                                        backgroundColor: '#1565c0'
                                    }
                                }}
                            >
                                Refresh
                            </Button>
                        </Box>
                    </Grid>
                </Grid>
            </Paper>

            <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12} sm={4}>
                    <Card sx={{ backgroundColor: '#ffffff', boxShadow: 1 }}>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom sx={{ color: '#666666' }}>
                                Working Days
                            </Typography>
                            <Typography variant="h4" sx={{ color: '#000000' }}>
                                {summary.totalWorkingDays}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={4}>
                    <Card sx={{ backgroundColor: '#ffffff', boxShadow: 1 }}>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom sx={{ color: '#666666' }}>
                                Total Hours
                            </Typography>
                            <Typography variant="h4" sx={{ color: '#000000' }}>
                                {summary.totalWorkHours}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={4}>
                    <Card sx={{ backgroundColor: '#ffffff', boxShadow: 1 }}>
                        <CardContent>
                            <Typography color="text.secondary" gutterBottom sx={{ color: '#666666' }}>
                                Avg. Daily Hours
                            </Typography>
                            <Typography variant="h4" sx={{ color: '#000000' }}>
                                {summary.averageDailyHours}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {error && (
                <Alert
                    severity="error"
                    sx={{
                        mb: 3,
                        backgroundColor: '#ffebee',
                        color: '#c62828',
                        '& .MuiAlert-icon': {
                            color: '#c62828'
                        }
                    }}
                >
                    {error}
                </Alert>
            )}

            {loading && (
                <Box sx={{
                    display: 'flex',
                    justifyContent: 'center',
                    my: 3,
                    backgroundColor: '#ffffff',
                    p: 2,
                    borderRadius: 1
                }}>
                    <CircularProgress sx={{ color: '#1976d2' }} />
                </Box>
            )}

            {!loading && processedData.length > 0 ? (
                <>
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        mb: 2,
                        backgroundColor: '#ffffff',
                        p: 2,
                        borderRadius: 1,
                        boxShadow: 1
                    }}>
                        <Typography variant="body1" sx={{ color: '#000000' }}>
                            Showing {startIndex + 1}-{Math.min(startIndex + pageSize, processedData.length)} of {processedData.length} records
                        </Typography>
                        <FormControl size="small">
                            <InputLabel sx={{ color: '#000000' }}>Rows per page</InputLabel>
                            <Select
                                value={pageSize}
                                onChange={handlePageSizeChange}
                                label="Rows per page"
                                sx={{
                                    backgroundColor: '#ffffff',
                                    color: '#000000',
                                    '& .MuiOutlinedInput-notchedOutline': {
                                        borderColor: '#cccccc'
                                    }
                                }}
                            >
                                <MenuItem value={5}>5</MenuItem>
                                <MenuItem value={10}>10</MenuItem>
                                <MenuItem value={25}>25</MenuItem>
                                <MenuItem value={50}>50</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>

                    {paginatedData.map((dayData) => (
                        <Paper key={dayData.date} sx={{ mb: 3, backgroundColor: '#ffffff', boxShadow: 1 }}>
                            <Box sx={{ p: 2, bgcolor: '#1976d2', color: '#ffffff' }}>
                                <Typography variant="h6">
                                    {new Date(dayData.date).toLocaleDateString('en-US', {
                                        weekday: 'long',
                                        year: 'numeric',
                                        month: 'long',
                                        day: 'numeric'
                                    })}
                                </Typography>
                            </Box>

                            <TableContainer sx={{ backgroundColor: '#ffffff' }}>
                                <Table>
                                    <TableHead>
                                        <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>No.</TableCell>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold', minWidth: '180px' }}>Punch In</TableCell>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Type</TableCell>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold', minWidth: '180px' }}>Punch Out</TableCell>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Type</TableCell>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Duration</TableCell>
                                            <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Terminal</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {groupInOutPairs(dayData.records).map((pair, index) => (
                                            <TableRow
                                                key={index}
                                                sx={{
                                                    backgroundColor: index % 2 === 0 ? '#ffffff' : '#fafafa',
                                                    '&:hover': {
                                                        backgroundColor: '#f0f0f0'
                                                    }
                                                }}
                                            >
                                                <TableCell sx={{ color: '#000000' }}>{index + 1}</TableCell>
                                                <TableCell sx={{ color: '#000000', whiteSpace: 'nowrap' }}>
                                                    {pair.in ? formatTime(pair.in.punchTypeTime) : '--:--'}
                                                </TableCell>
                                                <TableCell>
                                                    {pair.in ? (
                                                        <Chip
                                                            label={pair.in.inOutType}
                                                            size="small"
                                                            color={pair.in.morning ? 'primary' : 'secondary'}
                                                        />
                                                    ) : '-'}
                                                </TableCell>
                                                <TableCell sx={{ color: '#000000', whiteSpace: 'nowrap' }}>
                                                    {pair.out ? formatTime(pair.out.punchTypeTime) : '--:--'}
                                                </TableCell>
                                                <TableCell>
                                                    {pair.out ? (
                                                        <Chip
                                                            label={pair.out.inOutType}
                                                            size="small"
                                                            color={pair.out.morning ? 'primary' : 'secondary'}
                                                        />
                                                    ) : '-'}
                                                </TableCell>
                                                <TableCell sx={{ color: '#000000' }}>
                                                    {pair.in && pair.out ?
                                                        calculateDuration(pair.in.punchTypeTime, pair.out.punchTypeTime) :
                                                        '--:--'
                                                    }
                                                </TableCell>
                                                <TableCell sx={{ color: '#000000' }}>
                                                    {(pair.in?.terminalID || pair.out?.terminalID || '').trim() || 'N/A'}
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Paper>
                    ))}

                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        mt: 3,
                        backgroundColor: '#ffffff',
                        p: 2,
                        borderRadius: 1,
                        boxShadow: 1
                    }}>
                        <Pagination
                            count={totalPages}
                            page={page}
                            onChange={handlePageChange}
                            color="primary"
                            showFirstButton
                            showLastButton
                            sx={{
                                '& .MuiPaginationItem-root': {
                                    color: '#000000'
                                }
                            }}
                        />
                    </Box>
                </>
            ) : !loading ? (
                <Paper sx={{ p: 4, textAlign: 'center', backgroundColor: '#ffffff', boxShadow: 1 }}>
                    <Typography variant="h6" color="text.secondary" sx={{ color: '#666666' }}>
                        No attendance records found
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ color: '#666666' }}>
                        Try selecting a different date or date range
                    </Typography>
                </Paper>
            ) : null}
        </Box>
    );
};

export default AttendanceTracker;