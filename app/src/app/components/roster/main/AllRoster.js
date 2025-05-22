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
        <>
            <Container maxWidth="lg">
                <Box my={4}>
                    <Typography variant="h4" component="h1" gutterBottom>
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
                        <Box display="flex" justifyContent="center" my={4}>
                            <CircularProgress />
                        </Box>
                    ) : error ? (
                        <Paper style={{ padding: '16px', backgroundColor: '#ffebee' }}>
                            <Typography color="error">{error}</Typography>
                        </Paper>
                    ) : rosterData ? (
                        <Box>
                            <TeamSummary
                                teamAttendanceSummary={rosterData.teamAttendanceSummary}
                                selectedDate={selectedDate}
                            />

                            <EmployeeTable
                                employeeAttendanceDetails={rosterData.employeeAttendanceDetails}
                            />
                        </Box>
                    ) : (
                        <Paper style={{ padding: '24px', textAlign: 'center' }}>
                            <Typography variant="body1">No roster data available</Typography>
                        </Paper>
                    )}
                </Box>
            </Container>
        </>
    );
};

export default RosterDisplay;