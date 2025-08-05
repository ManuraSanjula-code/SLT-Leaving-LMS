import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
    Container,
    Typography,
    TextField,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    CircularProgress,
    Box,
    Chip,
    InputAdornment,
    Pagination,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Button,
    Grid
} from '@mui/material';
import {
    CalendarToday,
    Search,
    FilterList,
    Refresh
} from '@mui/icons-material';

import {
    fetchAttendanceData,
    setDateInput,
    setPage,
    setRowsPerPage,
    setEmployeeFilter,
    setStatusFilter,
    applyFilters,
    resetFilters,
} from '../../../../../lib/redux/redux-roster/attendanceSlice';

const AttendanceComponent = () => {
    const dispatch = useDispatch();
    
    const { 
        attendanceData, 
        loading, 
        error, 
        dateInput, 
        page, 
        rowsPerPage, 
        employeeFilter, 
        statusFilter, 
        filteredData 
    } = useSelector(state => state.attendanceRoster);

    const paginatedData = filteredData.slice(
        (page - 1) * rowsPerPage,
        page * rowsPerPage
    );
    const totalPages = Math.ceil(filteredData.length / rowsPerPage);

    useEffect(() => {
        document.body.style.backgroundColor = '#ffffff';
        document.documentElement.style.backgroundColor = '#ffffff';
        document.body.style.color = '#000000';
    }, []);

    useEffect(() => {
        dispatch(fetchAttendanceData(dateInput));
    }, [dispatch, dateInput]);

    useEffect(() => {
        dispatch(applyFilters());
    }, [dispatch, employeeFilter, statusFilter, attendanceData]);

    const handleDateChange = (e) => {
        dispatch(setDateInput(e.target.value));
    };

    const handlePageChange = (event, newPage) => {
        dispatch(setPage(newPage));
    };

    const handleRowsPerPageChange = (event) => {
        dispatch(setRowsPerPage(parseInt(event.target.value, 10)));
    };

    const handleEmployeeFilterChange = (e) => {
        dispatch(setEmployeeFilter(e.target.value));
    };

    const handleStatusFilterChange = (e) => {
        dispatch(setStatusFilter(e.target.value));
    };

    const handleResetFilters = () => {
        dispatch(resetFilters());
    };

    const formatDisplayDate = (dateString) => {
        const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    };

    const getStatusChip = (record) => {
        if (record.leaveStatus) {
            return <Chip label={`Leave (${record.leaveStatus})`} color="info" size="small" />;
        }
        switch (record.attendanceType) {
            case 'ABSENT':
                return <Chip label="Absent" color="error" size="small" />;
            case 'HALF_DAY':
                return <Chip label="Half Day" color="warning" size="small" />;
            case 'FULL_DAY':
                return <Chip label="Present" color="success" size="small" />;
            default:
                return <Chip label={record.attendanceType || '--'} color="default" size="small" />;
        }
    };

    const getIssueChip = (record) => {
        if (record.hasIssues || record.isLate || record.isUnauthorized) {
            return <Chip label="Yes" color="error" size="small" />;
        }
        return <Chip label="No" color="success" size="small" />;
    };

    const getRosterType = (rosterType) => {
        if (!rosterType) return '--';
        return rosterType.split('_').map(word => 
            word.charAt(0) + word.slice(1).toLowerCase()
        ).join(' ');
    };

    return (
        <div style={{
            backgroundColor: '#ffffff',
            minHeight: '100vh',
            width: '100%',
            position: 'relative'
        }}>
            <Container
                maxWidth="lg"
                sx={{
                    mt: 4,
                    mb: 4,
                    backgroundColor: '#ffffff !important',
                    minHeight: '100vh',
                    color: '#000000 !important'
                }}
            >
                <Box sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    mb: 3,
                    backgroundColor: '#ffffff'
                }}>
                    <Typography
                        variant="h4"
                        component="h1"
                        sx={{ color: '#000000 !important' }}
                    >
                        Attendance Records
                    </Typography>
                    <TextField
                        type="date"
                        value={dateInput}
                        onChange={handleDateChange}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <CalendarToday />
                                </InputAdornment>
                            ),
                        }}
                        sx={{
                            width: 220,
                            '& .MuiOutlinedInput-root': {
                                backgroundColor: '#ffffff !important'
                            }
                        }}
                    />
                </Box>

                <Paper sx={{
                    p: 2,
                    mb: 3,
                    backgroundColor: '#ffffff !important',
                    color: '#000000 !important'
                }}>
                    <Typography
                        variant="h6"
                        gutterBottom
                        sx={{
                            display: 'flex',
                            alignItems: 'center',
                            color: '#000000 !important'
                        }}
                    >
                        <FilterList sx={{ mr: 1 }} /> Filters
                    </Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6} md={4}>
                            <TextField
                                fullWidth
                                label="Search Employee ID"
                                value={employeeFilter}
                                onChange={handleEmployeeFilterChange}
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <Search />
                                        </InputAdornment>
                                    ),
                                }}
                                sx={{
                                    '& .MuiOutlinedInput-root': {
                                        backgroundColor: '#ffffff !important'
                                    }
                                }}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6} md={4}>
                            <FormControl fullWidth>
                                <InputLabel>Status</InputLabel>
                                <Select
                                    value={statusFilter}
                                    label="Status"
                                    onChange={handleStatusFilterChange}
                                    sx={{
                                        backgroundColor: '#ffffff !important'
                                    }}
                                >
                                    <MenuItem value="all">All Status</MenuItem>
                                    <MenuItem value="present">Present</MenuItem>
                                    <MenuItem value="absent">Absent</MenuItem>
                                    <MenuItem value="halfday">Half Day</MenuItem>
                                    <MenuItem value="leave">Leave</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} sx={{ display: 'flex', alignItems: 'center' }}>
                            <Button
                                variant="outlined"
                                startIcon={<Refresh />}
                                onClick={handleResetFilters}
                                fullWidth
                                sx={{ height: '56px' }}
                            >
                                Reset Filters
                            </Button>
                        </Grid>
                    </Grid>
                </Paper>

                {loading && (
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        mt: 4,
                        backgroundColor: '#ffffff'
                    }}>
                        <CircularProgress />
                    </Box>
                )}

                {error && (
                    <Box sx={{
                        p: 2,
                        backgroundColor: '#ffebee !important',
                        borderRadius: 1,
                        mb: 2,
                        border: '1px solid #ffcdd2'
                    }}>
                        <Typography sx={{ color: '#d32f2f !important' }}>{error}</Typography>
                    </Box>
                )}

                {attendanceData && !loading && (
                    <Box sx={{ backgroundColor: '#ffffff' }}>
                        <Typography
                            variant="subtitle1"
                            sx={{
                                mb: 2,
                                color: '#000000 !important'
                            }}
                        >
                            Showing {filteredData.length} records for {formatDisplayDate(dateInput)}
                            {filteredData.length !== attendanceData.content.length && (
                                <Typography
                                    component="span"
                                    sx={{ color: '#666666 !important' }}
                                >
                                    {' '}(filtered from {attendanceData.content.length})
                                </Typography>
                            )}
                        </Typography>

                        <TableContainer
                            component={Paper}
                            sx={{
                                backgroundColor: '#ffffff !important'
                            }}
                        >
                            <Table sx={{ minWidth: 650 }} aria-label="attendance table">
                                <TableHead>
                                    <TableRow>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Employee ID</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Arrival Time</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Left Time</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Status</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Leave Status</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Roster Type</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Late</TableCell>
                                        <TableCell sx={{ backgroundColor: '#f5f5f5 !important', color: '#000000 !important' }}>Issues</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {paginatedData.length > 0 ? (
                                        paginatedData.map((record) => (
                                            <TableRow
                                                key={record.id}
                                                hover
                                                sx={{
                                                    backgroundColor: '#ffffff !important',
                                                    '&:hover': {
                                                        backgroundColor: '#f9f9f9 !important'
                                                    }
                                                }}
                                            >
                                                <TableCell sx={{ color: '#000000 !important' }}>{record.employeeId}</TableCell>
                                                <TableCell sx={{ color: '#000000 !important' }}>{record.arrivalTime || '--'}</TableCell>
                                                <TableCell sx={{ color: '#000000 !important' }}>{record.leftTime || '--'}</TableCell>
                                                <TableCell>
                                                    {getStatusChip(record)}
                                                </TableCell>
                                                <TableCell sx={{ color: '#000000 !important' }}>
                                                    {record.leaveStatus || '--'}
                                                </TableCell>
                                                <TableCell sx={{ color: '#000000 !important' }}>
                                                    {getRosterType(record.rosterType)}
                                                </TableCell>
                                                <TableCell>
                                                    {record.isLate ? (
                                                        <Chip label="Yes" color="error" size="small" />
                                                    ) : (
                                                        <Chip label="No" color="success" size="small" />
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    {getIssueChip(record)}
                                                </TableCell>
                                            </TableRow>
                                        ))
                                    ) : (
                                        <TableRow sx={{ backgroundColor: '#ffffff !important' }}>
                                            <TableCell
                                                colSpan={8}
                                                align="center"
                                                sx={{ color: '#000000 !important' }}
                                            >
                                                No records found matching your filters
                                            </TableCell>
                                        </TableRow>
                                    )}
                                </TableBody>
                            </Table>
                        </TableContainer>

                        {/* Pagination */}
                        <Box sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            mt: 2,
                            backgroundColor: '#ffffff'
                        }}>
                            <FormControl sx={{ m: 1, minWidth: 120 }} size="small">
                                <Select
                                    value={rowsPerPage}
                                    onChange={handleRowsPerPageChange}
                                    sx={{
                                        backgroundColor: '#ffffff !important'
                                    }}
                                >
                                    <MenuItem value={5}>5 per page</MenuItem>
                                    <MenuItem value={10}>10 per page</MenuItem>
                                    <MenuItem value={25}>25 per page</MenuItem>
                                    <MenuItem value={50}>50 per page</MenuItem>
                                </Select>
                            </FormControl>
                            <Pagination
                                count={totalPages}
                                page={page}
                                onChange={handlePageChange}
                                color="primary"
                                showFirstButton
                                showLastButton
                            />
                            <Typography sx={{ color: '#000000 !important' }}>
                                Showing {(page - 1) * rowsPerPage + 1}-
                                {Math.min(page * rowsPerPage, filteredData.length)} of {filteredData.length}
                            </Typography>
                        </Box>
                    </Box>
                )}
            </Container>
        </div>
    );
};

export default AttendanceComponent;