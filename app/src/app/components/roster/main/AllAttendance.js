import React, { useState, useEffect, useCallback } from 'react';
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
    const [dateInput, setDateInput] = useState(formatDate(new Date()));
    const [page, setPage] = useState(1);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [employeeFilter, setEmployeeFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('all');
    const [attendanceData, setAttendanceData] = useState(null);
    const [filteredData, setFilteredData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        document.body.style.backgroundColor = '#ffffff';
        document.documentElement.style.backgroundColor = '#ffffff';
        document.body.style.color = '#000000';
    }, []);

    const fetchAttendanceData = async (dateString) => {
        try {
            setLoading(true);
            setError(null);

            const response = await fetch(`http://192.168.3.20:8080/api/attendance/${dateString}`);

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

    const applyFilters = useCallback(() => {
        if (!attendanceData) return;

        let result = [...attendanceData.content];

        if (employeeFilter) {
            result = result.filter(record =>
                record.employeeId.toLowerCase().includes(employeeFilter.toLowerCase())
            );
        }

        if (statusFilter !== 'all') {
            result = result.filter(record => {
                if (statusFilter === 'present') return record.attendanceType === 'FULL_DAY';
                if (statusFilter === 'absent') return record.attendanceType === 'ABSENT';
                if (statusFilter === 'halfday') return record.attendanceType === 'HALF_DAY';
                return true;
            });
        }

        setFilteredData(result);
        setPage(1);
    }, [attendanceData, employeeFilter, statusFilter]);

    const resetFilters = () => {
        setEmployeeFilter('');
        setStatusFilter('all');
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
    }, [applyFilters]);

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
        switch (record.attendanceType) {
            case 'ABSENT':
                return <Chip label="Absent" color="error" size="small" />;
            case 'HALF_DAY':
                return <Chip label="Half Day" color="warning" size="small" />;
            case 'FULL_DAY':
                return <Chip label="Present" color="success" size="small" />;
            default:
                return <Chip label={record.attendanceType} color="default" size="small" />;
        }
    };

    const getIssueChip = (record) => {
        if (record.hasIssues || record.isLate || record.isUnauthorized) {
            return <Chip label="Yes" color="error" size="small" />;
        }
        return <Chip label="No" color="success" size="small" />;
    };

    const paginatedData = filteredData.slice(
        (page - 1) * rowsPerPage,
        page * rowsPerPage
    );

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

                {/* Filter Section */}
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
                                onChange={(e) => setEmployeeFilter(e.target.value)}
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
                                    onChange={(e) => setStatusFilter(e.target.value)}
                                    sx={{
                                        backgroundColor: '#ffffff !important'
                                    }}
                                >
                                    <MenuItem value="all">All Status</MenuItem>
                                    <MenuItem value="present">Present</MenuItem>
                                    <MenuItem value="absent">Absent</MenuItem>
                                    <MenuItem value="halfday">Half Day</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} sx={{ display: 'flex', alignItems: 'center' }}>
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
                                                colSpan={6}
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
                                count={Math.ceil(filteredData.length / rowsPerPage)}
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