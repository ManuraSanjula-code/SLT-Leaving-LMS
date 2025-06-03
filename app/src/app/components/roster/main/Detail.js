'use client'

import React, { useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Typography, Snackbar, Alert } from '@mui/material';

import RosterHeader from "../../roster/RosterHeader";
import TeamRosterCard from "../../roster/TeamRosterCard";
import ErrorState from "../../roster/ErrorState";
import EmptyState from "../../roster/EmptyState";

import {
    fetchRosterData,
    setSearchTerm,
    navigateToPreviousMonth,
    navigateToNextMonth,
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

    const getMonthName = useCallback((month) => {
        const date = new Date();
        date.setMonth(month - 1);
        return date.toLocaleString('en-US', { month: 'long' });
    }, []);

    const handleSearchChange = useCallback((event) => {
        dispatch(setSearchTerm(event.target.value));
    }, [dispatch]);

    const handlePreviousMonth = useCallback(() => {
        dispatch(navigateToPreviousMonth());
    }, [dispatch]);

    const handleNextMonth = useCallback(() => {
        dispatch(navigateToNextMonth());
    }, [dispatch]);

    const handleCloseNotification = useCallback(() => {
        dispatch(closeNotification());
    }, [dispatch]);

    const handleRetry = useCallback(() => {
        dispatch(fetchRosterData({ month: currentMonth, year: currentYear }));
    }, [dispatch, currentMonth, currentYear]);

    // Fetch roster data when month or year changes
    useEffect(() => {
        dispatch(fetchRosterData({ month: currentMonth, year: currentYear }));
    }, [dispatch, currentMonth, currentYear]);

    // Loading state for initial load
    if (loading && !roster) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <Typography variant="h5">Loading roster data...</Typography>
            </Box>
        );
    }

    // Error state
    if (error && !roster) {
        return (
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
        );
    }

    // Empty state
    if (!roster?.teams?.length) {
        return (
            <EmptyState
                currentMonth={currentMonth}
                currentYear={currentYear}
                onPreviousMonth={handlePreviousMonth}
                onNextMonth={handleNextMonth}
                getMonthName={getMonthName}
            />
        );
    }

    const renderTeamCards = () => {
        if (filteredTeams.length === 0) {
            return (
                <Typography variant="h6" sx={{ textAlign: 'center', mt: 5 }}>
                    No results found for "{searchTerm}"
                </Typography>
            );
        }

        return filteredTeams.map((team) => {
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
        });
    };

    return (
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

            {/* Content */}
            {loading && roster ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
                    <Typography variant="h5">Loading updated data...</Typography>
                </Box>
            ) : (
                renderTeamCards()
            )}
        </Box>
    );
};

export default RosterManagement;