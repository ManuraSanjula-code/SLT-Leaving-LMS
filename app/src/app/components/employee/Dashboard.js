"use client"

import React, {useState, useEffect} from 'react';
import {
    Container,
    CssBaseline,
    Box,
    Typography,
    Grid,
    Paper,
    CircularProgress,
    Alert,
} from '@mui/material';
import {
    AccessTime as AttendanceIcon,
    EventAvailable as LeaveIcon,
    CalendarToday as CalendarIcon,
    Person as UserIcon,
} from '@mui/icons-material';
import {PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer} from 'recharts';

const Dashboard = () => {
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const getUserId = () => {
        const sessionUserId = sessionStorage.getItem('userId');
        const localUserId = localStorage.getItem('userId');
        return sessionUserId || localUserId;
    };

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            const userId = getUserId();

            if (!userId) {
                throw new Error('User ID not found. Please log in again.');
            }

            const response = await fetch(`http://localhost:8080/lms/dashboard/${userId}/${userId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            setDashboardData(data);
            setError(null);
        } catch (err) {
            console.error('Error fetching dashboard data:', err);
            setError(err.message);
            setDashboardData(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const getLeaveDistributionData = () => {
        if (!dashboardData?.remainLeaveDistribution) return [];

        return Object.entries(dashboardData.remainLeaveDistribution)
            .filter(([_, value]) => value > 0)
            .map(([name, value]) => ({name, value}));
    };

    const getAttendanceData = () => {
        if (!dashboardData?.monthlyAttendanceDistribution) return [];

        return Object.entries(dashboardData.monthlyAttendanceDistribution)
            .map(([name, attendance]) => ({name, attendance}));
    };

    const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D'];

    if (loading) {
        return (
            <Container maxWidth="lg">
                <CssBaseline/>
                <Box sx={{
                    mt: 4,
                    mb: 4,
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    minHeight: '400px'
                }}>
                    <CircularProgress size={60}/>
                    <Typography variant="h6" sx={{ml: 2}}>Loading dashboard...</Typography>
                </Box>
            </Container>
        );
    }

    if (error) {
        return (
            <Container maxWidth="lg">
                <CssBaseline/>
                <Box sx={{mt: 4, mb: 4}}>
                    <Typography variant="h4" gutterBottom>
                        Leave Management Dashboard
                    </Typography>
                    <Alert severity="error" sx={{mt: 3}}>
                        <Typography variant="h6" gutterBottom>Unable to Load Dashboard</Typography>
                        <Typography variant="body1">{error}</Typography>
                        {error.includes('User ID not found') && (
                            <Typography variant="body2" sx={{mt: 1}}>
                                Please ensure you are logged in properly.
                            </Typography>
                        )}
                    </Alert>
                </Box>
            </Container>
        );
    }

    return (
        <Container maxWidth="lg">
            <CssBaseline/>
            <Box sx={{mt: 4, mb: 4}}>
                <Typography variant="h4" gutterBottom>
                    Leave Management Dashboard
                </Typography>

                <Grid container spacing={3}>
                    <Grid item xs={12} sm={6} md={3}>
                        <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                            <LeaveIcon fontSize="large" color="primary"/>
                            <Typography variant="h6">Total Leaves</Typography>
                            <Typography variant="h4">{dashboardData?.totalLeave || 0}</Typography>
                        </Paper>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                        <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                            <AttendanceIcon fontSize="large" color="secondary"/>
                            <Typography variant="h6">Total Attendance</Typography>
                            <Typography variant="h4">{dashboardData?.totalAttendance || 0}</Typography>
                        </Paper>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                        <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                            <CalendarIcon fontSize="large" color="success"/>
                            <Typography variant="h6">Leave Balance</Typography>
                            <Typography variant="h4">{dashboardData?.leaveBalance || 0}</Typography>
                        </Paper>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                        <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                            <UserIcon fontSize="large" color="action"/>
                            <Typography variant="h6">User</Typography>
                            <Typography variant="h6" sx={{textAlign: 'center', wordBreak: 'break-word'}}>
                                {dashboardData?.name || 'Unknown User'}
                            </Typography>
                        </Paper>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                            <Typography variant="h6" gutterBottom>
                                Remaining Leave Distribution
                            </Typography>
                            {getLeaveDistributionData().length > 0 ? (
                                <ResponsiveContainer width="100%" height={300}>
                                    <PieChart>
                                        <Pie
                                            data={getLeaveDistributionData()}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={80}
                                            fill="#8884d8"
                                            paddingAngle={5}
                                            dataKey="value"
                                            label={({name, value}) => `${name}: ${value}`}
                                        >
                                            {getLeaveDistributionData().map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]}/>
                                            ))}
                                        </Pie>
                                        <Tooltip/>
                                        <Legend/>
                                    </PieChart>
                                </ResponsiveContainer>
                            ) : (
                                <Typography variant="body2" color="text.secondary" sx={{mt: 2}}>
                                    No leave data available
                                </Typography>
                            )}
                        </Paper>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                            <Typography variant="h6" gutterBottom>
                                Monthly Attendance
                            </Typography>
                            {getAttendanceData().length > 0 ? (
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={getAttendanceData()}>
                                        <XAxis dataKey="name"/>
                                        <YAxis/>
                                        <Tooltip/>
                                        <Legend/>
                                        <Bar dataKey="attendance" fill="#82ca9d"/>
                                    </BarChart>
                                </ResponsiveContainer>
                            ) : (
                                <Typography variant="body2" color="text.secondary" sx={{mt: 2}}>
                                    No attendance data available
                                </Typography>
                            )}
                        </Paper>
                    </Grid>
                </Grid>
            </Box>
        </Container>
    );
};

export default Dashboard;