'use client'
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
    Box,
    Paper,
    TablePagination,
    Typography,
    Chip,
    Grid,
    CircularProgress,
    Button,
    Alert,
    Stack
} from '@mui/material';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import RefreshIcon from '@mui/icons-material/Refresh';
import EnhancedDatePicker from './EnhancedDatePicker';
import SearchFilterComponent from './SearchFilterComponent';
import AttendanceTableComponent from './AttendanceTableComponent';
import {
    fetchAttendanceData,
    setSelectedDate,
    setSearchTerm,
    setFilterStatus,
    setPage,
    setRowsPerPage,
    applyFilters,
    selectAttendanceState,
    selectPaginatedData
} from '../../lib/redux/redux-roster/attendanceSlice';

const AttendanceTable = () => {
    const dispatch = useDispatch();
    const {
        selectedDate = new Date().toISOString(),
        loading = false,
        error = null,
        searchTerm = '',
        filterStatus = 'all',
        page = 0,
        rowsPerPage = 5,
        filteredData = []
    } = useSelector(selectAttendanceState);

    const paginatedData = useSelector(selectPaginatedData);
    const selectedDateObj = new Date(selectedDate);

    useEffect(() => {
        if (selectedDate && !isNaN(selectedDateObj.getTime())) {
            dispatch(fetchAttendanceData(selectedDateObj));
        }
    }, [dispatch, selectedDate]);

    useEffect(() => {
        dispatch(applyFilters());
    }, [dispatch, searchTerm, filterStatus]);

    const handleDateChange = (newDate) => {
        if (newDate && !isNaN(newDate.getTime())) {
            dispatch(setSelectedDate(newDate));
        }
    };

    const handleSearchChange = (event) => {
        dispatch(setSearchTerm(event.target.value));
    };

    const handleStatusFilterChange = (event) => {
        dispatch(setFilterStatus(event.target.value));
    };

    const handleChangePage = (event, newPage) => {
        dispatch(setPage(newPage));
    };

    const handleChangeRowsPerPage = (event) => {
        dispatch(setRowsPerPage(parseInt(event.target.value, 10)));
    };

    const handleRefresh = () => {
        if (selectedDate) {
            dispatch(fetchAttendanceData(selectedDateObj));
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return "N/A";
        try {
            const date = new Date(dateString);
            return isNaN(date.getTime()) ? "Invalid Date" :
                `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        } catch {
            return "Invalid Date";
        }
    };

    const getStatusChip = (item) => {
        if (!item) return <Chip label="Unknown" size="small" />;
        if (item.isAbsent) return <Chip label="Absent" color="error" size="small" />;
        if (item.isLate) return <Chip label="Late" color="warning" size="small" />;
        if (item.isFullLeave || item.isHalfDay || item.isShortLeave) {
            return <Chip label="On Leave" color="info" size="small" />;
        }
        return <Chip label="Present" color="success" size="small" />;
    };

    return (
        <Box sx={{ width: '100%', mb: 4 }}>
            <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 2 }}>
                <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold', color: 'primary.main' }}>
                    Employee Attendance Records
                </Typography>

                <Grid container spacing={3} sx={{ mb: 3, alignItems: 'center' }}>
                    <Grid item xs={12} md={4}>
                        <EnhancedDatePicker
                            selectedDate={selectedDateObj}
                            onChange={handleDateChange}
                            disabled={loading}
                        />
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <Button
                            variant="contained"
                            onClick={handleRefresh}
                            disabled={loading}
                            fullWidth
                            sx={{ height: 56, boxShadow: 2 }}
                            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <RefreshIcon />}
                        >
                            {loading ? 'Loading...' : 'Refresh Data'}
                        </Button>
                    </Grid>
                    <Grid item xs={12} md={5}>
                        <Stack direction="row" spacing={1} alignItems="center" sx={{
                            height: 56,
                            p: 2,
                            bgcolor: 'primary.light',
                            color: 'primary.contrastText',
                            borderRadius: 1,
                            boxShadow: 1
                        }}>
                            <CalendarTodayIcon />
                            <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                                Viewing: {formatDate(selectedDate)}
                            </Typography>
                        </Stack>
                    </Grid>
                </Grid>

                {error && (
                    <Alert severity={error.includes('No records') ? 'info' : 'error'} sx={{ mb: 3 }}>
                        {error}
                    </Alert>
                )}

                <SearchFilterComponent
                    searchTerm={searchTerm}
                    onSearchChange={handleSearchChange}
                    filterStatus={filterStatus}
                    onFilterStatusChange={handleStatusFilterChange}
                    isLoading={loading}
                />
            </Paper>

            <Paper elevation={3} sx={{ width: '100%', overflow: 'hidden', position: 'relative', borderRadius: 2 }}>
                {loading && (
                    <Box sx={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        backgroundColor: 'rgba(255, 255, 255, 0.8)',
                        zIndex: 1,
                    }}>
                        <CircularProgress size={40} thickness={4} />
                    </Box>
                )}

                <AttendanceTableComponent
                    data={paginatedData}
                    isLoading={loading}
                    formatDate={formatDate}
                    getStatusChip={getStatusChip}
                    rowsPerPage={rowsPerPage}
                />

                <TablePagination
                    rowsPerPageOptions={[5, 10, 25]}
                    component="div"
                    count={filteredData.length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    onPageChange={handleChangePage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                    sx={{ borderTop: '1px solid rgba(0, 0, 0, 0.12)' }}
                />
            </Paper>
        </Box>
    );
};

export default AttendanceTable;