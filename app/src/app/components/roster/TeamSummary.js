"use client";

import React from 'react';
import { Grid, Card, CardContent, Typography } from '@mui/material';

const TeamSummary = ({ teamAttendanceSummary, selectedDate }) => {
    return (
        <>
            <Typography variant="h5" gutterBottom>
                Team Attendance Summary - {selectedDate}
            </Typography>

            <Grid container spacing={3} mb={4}>
                {Object.values(teamAttendanceSummary).map((team, index) => (
                    <Grid item xs={12} md={6} key={index}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    {team.teamName} ({team.shiftTime})
                                </Typography>
                                <Typography variant="body1">
                                    Total Employees: {team.totalEmployees}
                                </Typography>
                                <Typography variant="body1" color="primary">
                                    Present: {team.presentEmployees}
                                </Typography>
                                <Typography variant="body1" color={team.lateEmployees > 0 ? "warning" : "textSecondary"}>
                                    Late: {team.lateEmployees}
                                </Typography>
                                <Typography variant="body1" color={team.absentEmployees > 0 ? "error" : "textSecondary"}>
                                    Absent: {team.absentEmployees}
                                </Typography>
                                <Typography variant="body1">
                                    Half Day: {team.halfDayEmployees}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    );
};

export default TeamSummary;