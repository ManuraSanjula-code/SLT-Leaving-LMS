import React, { useState, useEffect } from 'react';
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

// Helper function to format date as YYYY-MM-DD
const formatDate = (date) => {
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
};

const AttendanceComponent = () => {
    // Set today's date as initial date (formatted as YYYY-MM-DD)
    const [dateInput, setDateInput] = useState(formatDate(new Date()));

    // Pagination state
    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(10);

    // Filter states
    const [employeeFilter, setEmployeeFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('all');
    const [shiftFilter, setShiftFilter] = useState('all');

    // Data state
    const [attendanceData, setAttendanceData] = useState(null);
    const [filteredData, setFilteredData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Fetch attendance data
    const fetchAttendanceData = async (dateString) => {
        try {
            setLoading(true);
            setError(null);

            const response = await fetch(`http://localhost:8080/api/attendance/${dateString}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            setAttendanceData(data);
            setFilteredData(data.content);
        } catch (err) {
            setError(err.message);
            console.error("Error fetching attendance data:", err);
        } finally {
            setLoading(false);
        }
    };

    // Apply filters
    const applyFilters = () => {
        if (!attendanceData) return;

        let result = [...attendanceData.content];

        // Employee ID filter
        if (employeeFilter) {
            result = result.filter(record =>
                record.employeeID.toLowerCase().includes(employeeFilter.toLowerCase())
            );
        }

        // Status filter
        if (statusFilter !== 'all') {
            result = result.filter(record => {
                if (statusFilter === 'present') return !record.isAbsent && !record.isFullLeave && !record.isHalfDay;
                if (statusFilter === 'absent') return record.isAbsent;
                if (statusFilter === 'leave') return record.isFullLeave;
                if (statusFilter === 'halfday') return record.isHalfDay;
                return true;
            });
        }

        // Shift filter
        if (shiftFilter !== 'all') {
            result = result.filter(record =>
                record.shiftCode.toLowerCase().includes(shiftFilter.toLowerCase())
            );
        }

        setFilteredData(result);
        setPage(1); // Reset to first page when filters change
    };

    // Reset all filters
    const resetFilters = () => {
        setEmployeeFilter('');
        setStatusFilter('all');
        setShiftFilter('all');
        if (attendanceData) {
            setFilteredData(attendanceData.content);
        }
        setPage(1);
    };

    useEffect(() => {
        fetchAttendanceData(dateInput);
    }, [dateInput]);

    useEffect(() => {
        applyFilters();
    }, [employeeFilter, statusFilter, shiftFilter]);

    const handleDateChange = (e) => {
        setDateInput(e.target.value);
    };

    const handlePageChange = (event, newPage) => {
        setPage(newPage);
    };

    const handleRowsPerPageChange = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(1);
    };

    const formatDisplayDate = (dateString) => {
        const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    };

    const getStatusChip = (record) => {
        if (record.isAbsent) return <Chip label="Absent" color="error" size="small" />;
        if (record.isFullLeave) return <Chip label="Full Leave" color="warning" size="small" />;
        if (record.isHalfDay) return <Chip label="Half Day" color="info" size="small" />;
        if (record.isShortLeave) return <Chip label="Short Leave" color="info" size="small" variant="outlined" />;
        return <Chip label="Present" color="success" size="small" />;
    };

    // Calculate paginated data
    const paginatedData = filteredData.slice(
        (page - 1) * rowsPerPage,
        page * rowsPerPage
    );

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h4" component="h1">
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
                    sx={{ width: 220 }}
                />
            </Box>

            {/* Filter Section */}
            <Paper sx={{ p: 2, mb: 3 }}>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                    <FilterList sx={{ mr: 1 }} /> Filters
                </Typography>
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={6} md={3}>
                        <TextField
                            fullWidth
                            label="Search Employee ID"
                            value={employeeFilter}
                            onChange={(e) => setEmployeeFilter(e.target.value)}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Search />
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                        <FormControl fullWidth>
                            <InputLabel>Status</InputLabel>
                            <Select
                                value={statusFilter}
                                label="Status"
                                onChange={(e) => setStatusFilter(e.target.value)}
                            >
                                <MenuItem value="all">All Status</MenuItem>
                                <MenuItem value="present">Present</MenuItem>
                                <MenuItem value="absent">Absent</MenuItem>
                                <MenuItem value="leave">Full Leave</MenuItem>
                                <MenuItem value="halfday">Half Day</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                        <FormControl fullWidth>
                            <InputLabel>Shift</InputLabel>
                            <Select
                                value={shiftFilter}
                                label="Shift"
                                onChange={(e) => setShiftFilter(e.target.value)}
                            >
                                <MenuItem value="all">All Shifts</MenuItem>
                                <MenuItem value="T 1">T 1</MenuItem>
                                <MenuItem value="T 2">T 2</MenuItem>
                                <MenuItem value="T 3">T 3</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3} sx={{ display: 'flex', alignItems: 'center' }}>
                        <Button
                            variant="outlined"
                            startIcon={<Refresh />}
                            onClick={resetFilters}
                            fullWidth
                            sx={{ height: '56px' }}
                        >
                            Reset Filters
                        </Button>
                    </Grid>
                </Grid>
            </Paper>

            {loading && (
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                    <CircularProgress />
                </Box>
            )}

            {error && (
                <Box sx={{ p: 2, backgroundColor: 'error.light', borderRadius: 1, mb: 2 }}>
                    <Typography color="error">{error}</Typography>
                </Box>
            )}

            {attendanceData && !loading && (
                <>
                    <Typography variant="subtitle1" sx={{ mb: 2 }}>
                        Showing {filteredData.length} records for {formatDisplayDate(dateInput)}
                        {filteredData.length !== attendanceData.content.length && (
                            <Typography component="span" color="text.secondary">
                                {' '}(filtered from {attendanceData.content.length})
                            </Typography>
                        )}
                    </Typography>

                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 650 }} aria-label="attendance table">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Employee ID</TableCell>
                                    <TableCell>Shift</TableCell>
                                    <TableCell>Shift Time</TableCell>
                                    <TableCell>Arrival Time</TableCell>
                                    <TableCell>Left Time</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell>Issues</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {paginatedData.length > 0 ? (
                                    paginatedData.map((record) => (
                                        <TableRow key={record.id} hover>
                                            <TableCell>{record.employeeID}</TableCell>
                                            <TableCell>{record.shiftCode}</TableCell>
                                            <TableCell>{record.shiftTime}</TableCell>
                                            <TableCell>{record.arrivalTime || '--'}</TableCell>
                                            <TableCell>{record.leftTime || '--'}</TableCell>
                                            <TableCell>
                                                {getStatusChip(record)}
                                            </TableCell>
                                            <TableCell>
                                                {record.issues ? (
                                                    <Chip label="Yes" color="error" size="small" />
                                                ) : (
                                                    <Chip label="No" color="success" size="small" />
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={7} align="center">
                                            No records found matching your filters
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    {/* Pagination */}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
                        <FormControl sx={{ m: 1, minWidth: 120 }} size="small">
                            <Select
                                value={rowsPerPage}
                                onChange={handleRowsPerPageChange}
                            >
                                <MenuItem value={5}>5 per page</MenuItem>
                                <MenuItem value={10}>10 per page</MenuItem>
                                <MenuItem value={25}>25 per page</MenuItem>
                                <MenuItem value={50}>50 per page</MenuItem>
                            </Select>
                        </FormControl>
                        <Pagination
                            count={Math.ceil(filteredData.length / rowsPerPage)}
                            page={page}
                            onChange={handlePageChange}
                            color="primary"
                            showFirstButton
                            showLastButton
                        />
                        <Typography>
                            Showing {(page - 1) * rowsPerPage + 1}-
                            {Math.min(page * rowsPerPage, filteredData.length)} of {filteredData.length}
                        </Typography>
                    </Box>
                </>
            )}
        </Container>
    );
};

export default AttendanceComponent;