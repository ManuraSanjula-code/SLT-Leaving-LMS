import React, { useState, useEffect } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Typography,
    Box,
    Alert,
    CircularProgress,
    TextField,
    Button,
    Card,
    CardContent,
    Chip,
    Grid
} from '@mui/material';

const ShiftRosterTable = () => {
    const [rosterData, setRosterData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
    const [selectedMonth, setSelectedMonth] = useState(new Date().toLocaleString('default', { month: 'long' }));

    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];

    const fetchRosterData = async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`http://localhost:8080/api/roster/shift-roster/${selectedYear}/${selectedMonth}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            // Check if data is empty or invalid
            if (!data || Object.keys(data).length === 0) {
                throw new Error('No data found for the selected month and year');
            }

            // Validate required fields
            if (!data.dutyTurn || !data.dates) {
                throw new Error('Invalid data structure received from server');
            }

            setRosterData(data);
        } catch (err) {
            setError(err.message);
            setRosterData(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRosterData();
    }, [selectedYear, selectedMonth]); // Auto-fetch when year or month changes

    const handleFetchData = () => {
        fetchRosterData();
    };

    const getTeamForDateAndShift = (date, shift) => {
        if (!rosterData || !rosterData.dutyTurn || !rosterData.dutyTurn[shift]) {
            return 'N/A';
        }

        const shiftData = rosterData.dutyTurn[shift].find(item => item.date === date.toString());
        return shiftData ? shiftData.team : 'N/A';
    };

    const getDayDutyForDate = (date) => {
        if (!rosterData || !rosterData.dayDuty || !rosterData.dayDuty['Day Duty']) {
            return 'N/A';
        }

        const dayDutyData = rosterData.dayDuty['Day Duty'].find(item => item.date === date.toString());
        return dayDutyData ? dayDutyData.team : 'N/A';
    };

    const isOffDay = (date) => {
        if (!rosterData || !rosterData.offDay || !rosterData.offDay['Off Day']) {
            return false;
        }

        return rosterData.offDay['Off Day'].some(item => item.date === date.toString());
    };

    const getTeamColor = (team) => {
        const colors = {
            'T 1': '#e3f2fd',
            'T 1 ROT': '#bbdefb',
            'T 2': '#e8f5e8',
            'T 2 ROT': '#c8e6c8',
            'T 3': '#fff3e0',
            'T 3 ROT': '#ffcc02',
            'Na': '#f5f5f5'
        };
        return colors[team] || '#ffffff';
    };

    const renderTableContent = () => {
        if (!rosterData || !rosterData.dates) {
            return null;
        }

        const shifts = ['00:00 - 08:00', '08:00 - 16:00', '16:00 - 24:00'];

        return (
            <TableContainer component={Paper} elevation={3}>
                <Table sx={{ minWidth: 800 }} stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell
                                align="center"
                                sx={{
                                    fontWeight: 'bold',
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    minWidth: 80
                                }}
                            >
                                Date
                            </TableCell>
                            {shifts.map((shift) => (
                                <TableCell
                                    key={shift}
                                    align="center"
                                    sx={{
                                        fontWeight: 'bold',
                                        backgroundColor: '#1976d2',
                                        color: 'white',
                                        minWidth: 120
                                    }}
                                >
                                    {shift}
                                </TableCell>
                            ))}
                            <TableCell
                                align="center"
                                sx={{
                                    fontWeight: 'bold',
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    minWidth: 100
                                }}
                            >
                                Day Duty
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    fontWeight: 'bold',
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    minWidth: 80
                                }}
                            >
                                Status
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {rosterData.dates.map((date, index) => {
                            const dateNum = parseInt(date);
                            const offDay = isOffDay(dateNum);

                            return (
                                <TableRow
                                    key={date}
                                    sx={{
                                        '&:nth-of-type(odd)': { backgroundColor: '#f9f9f9' },
                                        backgroundColor: offDay ? '#ffebee' : undefined
                                    }}
                                >
                                    <TableCell
                                        align="center"
                                        sx={{
                                            fontWeight: 'bold',
                                            backgroundColor: offDay ? '#ffcdd2' : undefined
                                        }}
                                    >
                                        {date}
                                    </TableCell>
                                    {shifts.map((shift) => {
                                        const team = getTeamForDateAndShift(dateNum, shift);
                                        return (
                                            <TableCell
                                                key={shift}
                                                align="center"
                                                sx={{ backgroundColor: getTeamColor(team) }}
                                            >
                                                <Chip
                                                    label={team}
                                                    size="small"
                                                    variant="outlined"
                                                    sx={{
                                                        fontWeight: 'bold',
                                                        backgroundColor: 'rgba(255,255,255,0.7)'
                                                    }}
                                                />
                                            </TableCell>
                                        );
                                    })}
                                    <TableCell align="center">
                                        <Chip
                                            label={getDayDutyForDate(dateNum)}
                                            size="small"
                                            color="primary"
                                            variant="outlined"
                                        />
                                    </TableCell>
                                    <TableCell align="center">
                                        <Chip
                                            label={offDay ? "OFF" : "DUTY"}
                                            size="small"
                                            color={offDay ? "error" : "success"}
                                            variant="filled"
                                        />
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>
        );
    };

    return (
        <Box sx={{ p: 3 }}>
            <Card sx={{ mb: 3 }}>
                <CardContent>
                    <Typography variant="h4" component="h1" gutterBottom align="center">
                        IPTV NOC Team Shift Duty Roster
                    </Typography>

                    <Grid container spacing={3} alignItems="center" justifyContent="center" sx={{ mb: 2 }}>
                        <Grid item>
                            <TextField
                                type="number"
                                label="Year"
                                value={selectedYear}
                                onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                                InputProps={{
                                    inputProps: {
                                        min: 2020,
                                        max: 2030
                                    }
                                }}
                                variant="outlined"
                                size="medium"
                                sx={{ minWidth: 100 }}
                            />
                        </Grid>
                        <Grid item>
                            <TextField
                                select
                                label="Month"
                                value={selectedMonth}
                                onChange={(e) => setSelectedMonth(e.target.value)}
                                SelectProps={{
                                    native: true,
                                }}
                                variant="outlined"
                                size="medium"
                                sx={{ minWidth: 140 }}
                            >
                                {months.map((month) => (
                                    <option key={month} value={month}>
                                        {month}
                                    </option>
                                ))}
                            </TextField>
                        </Grid>
                        <Grid item>
                            <Button
                                variant="contained"
                                onClick={handleFetchData}
                                disabled={loading}
                                size="medium"
                                sx={{ px: 3 }}
                            >
                                {loading ? 'Loading...' : 'Refresh Data'}
                            </Button>
                        </Grid>
                    </Grid>

                    <Typography variant="body2" align="center" color="text.secondary" sx={{ mb: 2 }}>
                        📅 Data automatically loads when you change month or year
                    </Typography>

                    {rosterData && (
                        <Typography variant="h6" align="center" color="text.secondary" gutterBottom>
                            {rosterData.title || `${selectedMonth} ${selectedYear} Shift Roster`}
                        </Typography>
                    )}
                </CardContent>
            </Card>

            {loading && (
                <Box display="flex" justifyContent="center" alignItems="center" sx={{ py: 4 }}>
                    <CircularProgress />
                    <Typography variant="body1" sx={{ ml: 2 }}>
                        Loading shift roster data...
                    </Typography>
                </Box>
            )}

            {error && (
                <Alert severity="error" sx={{ mb: 3 }}>
                    <Typography variant="h6">Error Loading Data</Typography>
                    <Typography variant="body2">{error}</Typography>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                        Please check:
                        <br />• Server is running on localhost:8080
                        <br />• API endpoint is accessible
                        <br />• Selected month and year have data available
                        <br />• Network connection is stable
                    </Typography>
                </Alert>
            )}

            {!loading && !error && rosterData && renderTableContent()}

            {!loading && !error && !rosterData && (
                <Alert severity="info" sx={{ textAlign: 'center' }}>
                    <Typography variant="h6">No Data</Typography>
                    <Typography variant="body2">
                        Please select a month and year, then click "Load Roster" to fetch the shift schedule.
                    </Typography>
                </Alert>
            )}

            {/* Legend */}
            {rosterData && (
                <Card sx={{ mt: 3 }}>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Team Legend
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                            {['T 1', 'T 1 ROT', 'T 2', 'T 2 ROT', 'T 3', 'T 3 ROT', 'Na'].map((team) => (
                                <Chip
                                    key={team}
                                    label={team}
                                    sx={{
                                        backgroundColor: getTeamColor(team),
                                        border: '1px solid #ccc'
                                    }}
                                />
                            ))}
                        </Box>
                    </CardContent>
                </Card>
            )}
        </Box>
    );
};

export default ShiftRosterTable;