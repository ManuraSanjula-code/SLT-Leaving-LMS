'use client'

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
    Box,
    Typography,
    Snackbar,
    Alert
} from '@mui/material';
import RosterHeader from "../../roster/RosterHeader";
import TeamRosterCard from "../../roster/TeamRosterCard";
import ErrorState from "../../roster/ErrorState";
import EmptyState from "../../roster/EmptyState";

import {
    fetchRosterData,
    fetchTeamDetails,
    fetchEmployeeDetails,
    updateEmployeeRoster,
    setSearchTerm,
    navigateToPreviousMonth,
    navigateToNextMonth,
    startEditingEmployee,
    cancelEditing,
    updateEditData,
    closeNotification,
    selectRosterManagementState
} from '../../../../../lib/redux/redux-roster/rosterManagementSlice';

const RosterManagement = () => {
    const dispatch = useDispatch();
    const {
        roster,
        teams,
        employees,
        filteredTeams,
        currentMonth,
        currentYear,
        searchTerm,
        loading,
        error,
        editMode,
        editingEmployee,
        editData,
        notification
    } = useSelector(selectRosterManagementState);

    // Get month name helper
    const getMonthName = (month) => {
        const date = new Date();
        date.setMonth(month - 1);
        return date.toLocaleString('en-US', { month: 'long' });
    };

    // Fetch roster data when month or year changes
    useEffect(() => {
        dispatch(fetchRosterData({ month: currentMonth, year: currentYear }));
    }, [dispatch, currentMonth, currentYear]);

    // Fetch team and employee details when roster changes
    useEffect(() => {
        if (roster && roster.teams && roster.teams.length > 0) {
            // Get unique team IDs
            const teamIds = roster.teams.map(team => team.teamId);
            dispatch(fetchTeamDetails(teamIds));

            // Get unique employee IDs
            const employeeIds = roster.teams.flatMap(team =>
                team.employees.map(emp => emp.employeeId)
            );
            // Remove duplicates
            const uniqueEmployeeIds = [...new Set(employeeIds)];
            dispatch(fetchEmployeeDetails(uniqueEmployeeIds));
        }
    }, [dispatch, roster]);

    // Handle search change
    const handleSearchChange = (event) => {
        dispatch(setSearchTerm(event.target.value));
    };

    // Handle month navigation
    const handlePreviousMonth = () => {
        dispatch(navigateToPreviousMonth());
    };

    const handleNextMonth = () => {
        dispatch(navigateToNextMonth());
    };

    const handleCloseNotification = () => {
        dispatch(closeNotification());
    };

    // Retry loading data
    const handleRetry = () => {
        dispatch(fetchRosterData({ month: currentMonth, year: currentYear }));
    };

    // Handle loading state
    if (loading && !roster) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <Typography variant="h5">Loading roster data...</Typography>
            </Box>
        );
    }

    // Handle error state
    if (error && !roster) {
        return (
            <>
                <ErrorState
                    error={error}
                    loading={loading}
                    currentMonth={currentMonth}
                    currentYear={currentYear}
                    onPreviousMonth={handlePreviousMonth}
                    onNextMonth={handleNextMonth}
                    onRetry={handleRetry}
                    getMonthName={getMonthName}
                />
            </>
        );
    }

    // Handle empty data state
    if (!roster || !roster.teams || roster.teams.length === 0) {
        return (
            <>

                <EmptyState
                    currentMonth={currentMonth}
                    currentYear={currentYear}
                    onPreviousMonth={handlePreviousMonth}
                    onNextMonth={handleNextMonth}
                    getMonthName={getMonthName}
                />
            </>
        );
    }

    return (
        <>

            <Box sx={{ p: 3 }}>
                {/* Notification toast */}
                <Snackbar
                    open={notification.open}
                    autoHideDuration={6000}
                    onClose={handleCloseNotification}
                    anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
                >
                    <Alert
                        onClose={handleCloseNotification}
                        severity={notification.severity}
                        sx={{ width: '100%' }}
                    >
                        {notification.message}
                    </Alert>
                </Snackbar>

                {/* Header with Month/Year navigation */}
                <RosterHeader
                    currentMonth={currentMonth}
                    currentYear={currentYear}
                    searchTerm={searchTerm}
                    onSearchChange={handleSearchChange}
                    onPreviousMonth={handlePreviousMonth}
                    onNextMonth={handleNextMonth}
                    loading={loading}
                    editMode={editMode}
                    getMonthName={getMonthName}
                />

                {loading && roster ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
                        <Typography variant="h5">Loading updated data...</Typography>
                    </Box>
                ) : (
                    <>
                        {/* Teams and Employees */}
                        {filteredTeams.length === 0 ? (
                            <Typography variant="h6" sx={{ textAlign: 'center', mt: 5 }}>
                                No results found for "{searchTerm}"
                            </Typography>
                        ) : (
                            filteredTeams.map((team) => {
                                const teamDetails = teams.find(t => t.id === team.teamId) || {
                                    name: 'Unknown Team',
                                    shortName: 'UT'
                                };

                                return (
                                    <TeamRosterCard
                                        key={team.teamId}
                                        team={team}
                                        teamDetails={teamDetails}
                                        employees={employees}
                                        editMode={editMode}
                                        editingEmployee={editingEmployee}
                                        editData={editData}
                                        loading={loading}
                                    />
                                );
                            })
                        )}
                    </>
                )}
            </Box>
        </>
    );
};

export default RosterManagement;