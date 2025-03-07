"use client"

import React from 'react';
import {
  Container,
  CssBaseline,
  Box,
  Typography,
  Grid,
  Paper,
} from '@mui/material';
import {
  AccessTime as AttendanceIcon,
  EventAvailable as LeaveIcon,
  CalendarToday as CalendarIcon,
  Person as UserIcon,
} from '@mui/icons-material';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';

// Mock data for the dashboard
const leaveData = [
  { name: 'Sick Leave', value: 5 },
  { name: 'Casual Leave', value: 3 },
  { name: 'Earned Leave', value: 7 },
];

const attendanceData = [
  { name: 'Jan', attendance: 22 },
  { name: 'Feb', attendance: 20 },
  { name: 'Mar', attendance: 23 },
  { name: 'Apr', attendance: 21 },
];

const Dashboard = () => {
  return (
    <Container maxWidth="lg">
      <CssBaseline />
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Leave Management Dashboard
        </Typography>
        <Grid container spacing={3}>
          {/* Total Leaves Card */}
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <LeaveIcon fontSize="large" color="primary" />
              <Typography variant="h6">Total Leaves</Typography>
              <Typography variant="h4">15</Typography>
            </Paper>
          </Grid>
          {/* Total Attendance Card */}
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <AttendanceIcon fontSize="large" color="secondary" />
              <Typography variant="h6">Total Attendance</Typography>
              <Typography variant="h4">86</Typography>
            </Paper>
          </Grid>
          {/* Leave Balance Card */}
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <CalendarIcon fontSize="large" color="success" />
              <Typography variant="h6">Leave Balance</Typography>
              <Typography variant="h4">10</Typography>
            </Paper>
          </Grid>
          {/* User Info Card */}
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <UserIcon fontSize="large" color="action" />
              <Typography variant="h6">User</Typography>
              <Typography variant="h4">John Doe</Typography>
            </Paper>
          </Grid>
          {/* Leave Distribution Pie Chart */}
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <Typography variant="h6" gutterBottom>
                Leave Distribution
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={leaveData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    fill="#8884d8"
                    paddingAngle={5}
                    dataKey="value"
                    label
                  >
                    {leaveData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={['#0088FE', '#00C49F', '#FFBB28'][index % 3]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </Paper>
          </Grid>
          {/* Attendance Bar Chart */}
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <Typography variant="h6" gutterBottom>
                Monthly Attendance
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={attendanceData}>
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="attendance" fill="#82ca9d" />
                </BarChart>
              </ResponsiveContainer>
            </Paper>
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default Dashboard;