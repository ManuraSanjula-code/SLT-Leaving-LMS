"use client";

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
    Container,
    Paper,
    Typography,
    Button,
    Grid,
    Box,
    CircularProgress
} from '@mui/material';
import NavBar from "../../navbar/NavBar";
import DatePicker from "../../roster/DatePicker";
import TeamSummary from "../../roster/TeamSummary";
import EmployeeTable from "../../roster/EmployeeTable";
import { fetchRosterData, setSelectedDate } from "../../../../../lib/redux/redux-roster/rosterSlice";

const RosterDisplay = () => {
    const dispatch = useDispatch();
    const { rosterData, loading, error, selectedDate } = useSelector(state => state.roster);

    useEffect(() => {
        // Fetch data when component mounts
        dispatch(fetchRosterData(selectedDate));
    }, [dispatch, selectedDate]);

    const handleDateChange = (newDate) => {
        // Update selected date in Redux store
        dispatch(setSelectedDate(newDate));
    };

    const handleRefresh = () => {
        // Manually trigger data refresh
        dispatch(fetchRosterData(selectedDate));
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
                    backgroundColor: '#ffffff !important',
                    minHeight: '100vh',
                    color: '#000000 !important',
                    paddingTop: 2,
                    paddingBottom: 2
                }}
            >
                <Box my={4} sx={{ backgroundColor: '#ffffff' }}>
                    <Typography
                        variant="h4"
                        component="h1"
                        gutterBottom
                        sx={{ color: '#000000 !important' }}
                    >
                        Team Attendance Roster
                    </Typography>

                    <Box mb={3}>
                        <Grid container spacing={2} alignItems="center">
                            <Grid item>
                                <DatePicker selectedDate={selectedDate} onChange={handleDateChange} />
                            </Grid>
                            <Grid item>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    onClick={handleRefresh}
                                >
                                    Refresh Data
                                </Button>
                            </Grid>
                        </Grid>
                    </Box>

                    {loading ? (
                        <Box
                            display="flex"
                            justifyContent="center"
                            my={4}
                            sx={{ backgroundColor: '#ffffff' }}
                        >
                            <CircularProgress />
                        </Box>
                    ) : error ? (
                        <Paper sx={{
                            padding: 2,
                            backgroundColor: '#ffebee !important',
                            color: '#d32f2f !important',
                            border: '1px solid #ffcdd2'
                        }}>
                            <Typography sx={{ color: '#d32f2f !important' }}>{error}</Typography>
                        </Paper>
                    ) : rosterData ? (
                        <Box sx={{ backgroundColor: '#ffffff' }}>
                            <TeamSummary
                                teamAttendanceSummary={rosterData.teamAttendanceSummary}
                                selectedDate={selectedDate}
                            />

                            <EmployeeTable
                                employeeAttendanceDetails={rosterData.employeeAttendanceDetails}
                            />
                        </Box>
                    ) : (
                        <Paper sx={{
                            padding: 3,
                            textAlign: 'center',
                            backgroundColor: '#ffffff !important',
                            color: '#000000 !important',
                            border: '1px solid #e0e0e0'
                        }}>
                            <Typography
                                variant="body1"
                                sx={{ color: '#000000 !important' }}
                            >
                                No roster data available
                            </Typography>
                        </Paper>
                    )}
                </Box>
            </Container>
        </div>
    );
};

export default RosterDisplay;