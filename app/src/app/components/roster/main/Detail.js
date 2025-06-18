import React, { useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
    Box,
    Typography,
    Snackbar,
    Alert,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    TextField,
    Button,
    CircularProgress,
    IconButton
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import {
    fetchRosterData,
    fetchTeamDetails,
    fetchEmployeeDetails,
    setSearchTerm,
    navigateToPreviousMonth,
    navigateToNextMonth,
    navigateToCurrentMonth,
    closeNotification,
    selectRosterManagementState
} from '../../../../../lib/redux/redux-roster/rosterManagementSlice';

// Team Roster Card Component
const TeamRosterCard = ({ team, teamDetails, employees }) => (
    <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
            {teamDetails.name} ({teamDetails.shortName})
        </Typography>
        <TableContainer component={Paper}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Employee</TableCell>
                        <TableCell>ID</TableCell>
                        <TableCell>Total Shifts</TableCell>
                        <TableCell>Rotational Shifts</TableCell>
                        <TableCell>Off Days</TableCell>
                        <TableCell>Double Duties</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {team.employees.map((emp) => {
                        const employee = employees[emp.employeeId] || {
                            name: 'Unknown Employee',
                            employeeId: 'N/A',
                            shortName: 'UE'
                        };
                        return (
                            <TableRow key={emp.employeeId}>
                                <TableCell>{employee.name} ({employee.shortName})</TableCell>
                                <TableCell>{employee.employeeId}</TableCell>
                                <TableCell>{emp.totalShift}</TableCell>
                                <TableCell>{emp.rotShift}</TableCell>
                                <TableCell>{emp.offDay}</TableCell>
                                <TableCell>{emp.dduty}</TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </TableContainer>
    </Box>
);

// Roster Header Component
const RosterHeader = ({
                          currentMonth,
                          currentYear,
                          searchTerm,
                          onSearchChange,
                          onPreviousMonth,
                          onNextMonth,
                          onCurrentMonth,
                          onRefresh,
                          loading,
                          getMonthName
                      }) => (
    <Box sx={{
        mb: 4,
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        justifyContent: 'space-between',
        alignItems: { xs: 'flex-start', sm: 'center' },
        gap: 2
    }}>
        <Typography variant="h4">
            {getMonthName(currentMonth)} {currentYear}
        </Typography>
        <Box sx={{
            display: 'flex',
            gap: 2,
            alignItems: 'center',
            width: { xs: '100%', sm: 'auto' },
            flexWrap: 'wrap'
        }}>
            <TextField
                size="small"
                placeholder="Search..."
                value={searchTerm}
                onChange={onSearchChange}
                disabled={loading}
                sx={{ minWidth: 200, flexGrow: 1 }}
            />
            <IconButton onClick={onRefresh} disabled={loading}>
                <RefreshIcon />
            </IconButton>
            <Button
                variant="outlined"
                onClick={onCurrentMonth}
                disabled={loading}
                size="small"
            >
                Today
            </Button>
            <Button
                variant="contained"
                onClick={onPreviousMonth}
                disabled={loading}
                size="small"
            >
                Previous
            </Button>
            <Button
                variant="contained"
                onClick={onNextMonth}
                disabled={loading}
                size="small"
            >
                Next
            </Button>
            {loading && <CircularProgress size={24} />}
        </Box>
    </Box>
);

// Status Message Component
const StatusMessage = ({
                           title,
                           message,
                           actionText,
                           onAction,
                           loading,
                           severity = 'info'
                       }) => (
    <Box sx={{
        p: 4,
        textAlign: 'center',
        border: 1,
        borderColor: `${severity}.main`,
        borderRadius: 1,
        backgroundColor: `${severity}.light`,
        mt: 3
    }}>
        <Typography variant="h5" color={`${severity}.main`} gutterBottom>
            {title}
        </Typography>
        <Typography variant="body1" sx={{ mb: 3 }}>
            {message}
        </Typography>
        {onAction && (
            <Button
                variant="contained"
                onClick={onAction}
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : null}
                color={severity}
            >
                {actionText}
            </Button>
        )}
    </Box>
);

// Main Component
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
        notification
    } = useSelector(selectRosterManagementState);

    const getMonthName = useCallback((month) => {
        return new Date(0, month - 1).toLocaleString('en-US', { month: 'long' });
    }, []);

    // Handler functions
    const handleSearchChange = (e) => dispatch(setSearchTerm(e.target.value));
    const handlePreviousMonth = () => dispatch(navigateToPreviousMonth());
    const handleNextMonth = () => dispatch(navigateToNextMonth());
    const handleCurrentMonth = () => dispatch(navigateToCurrentMonth());
    const handleRefresh = () => dispatch(fetchRosterData({ month: currentMonth, year: currentYear }));
    const handleCloseNotification = () => dispatch(closeNotification());

    // Load data effect
    useEffect(() => {
        const loadData = async () => {
            try {
                const rosterResult = await dispatch(
                    fetchRosterData({ month: currentMonth, year: currentYear })
                ).unwrap();

                if (rosterResult?.teams?.length) {
                    const teamIds = [...new Set(rosterResult.teams.map(t => t.teamId))];
                    const employeeIds = [...new Set(
                        rosterResult.teams.flatMap(t => t.employees.map(e => e.employeeId))
                    )];

                    dispatch(fetchTeamDetails(teamIds));
                    dispatch(fetchEmployeeDetails(employeeIds));
                }
            } catch (err) {
                console.error("Data loading error:", err);
            }
        };

        loadData();
    }, [dispatch, currentMonth, currentYear]);

    // Render content based on state
    const renderContent = () => {
        if (loading && !roster) {
            return (
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                    <CircularProgress size={60} />
                </Box>
            );
        }

        if (error) {
            return (
                <StatusMessage
                    title={error.isEmpty ? "No Data Available" : "Error Occurred"}
                    message={error.message}
                    actionText="Retry"
                    onAction={handleRefresh}
                    loading={loading}
                    severity={error.isEmpty ? "info" : "error"}
                />
            );
        }

        if (!roster?.teams?.length) {
            return (
                <StatusMessage
                    title="No Roster Data"
                    message={`No roster found for ${getMonthName(currentMonth)} ${currentYear}`}
                    actionText="Refresh"
                    onAction={handleRefresh}
                    loading={loading}
                    severity="info"
                />
            );
        }

        if (filteredTeams.length === 0) {
            return (
                <Typography variant="h6" sx={{ textAlign: 'center', mt: 5 }}>
                    No results found for &quot;{searchTerm}&quot;
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
                />
            );
        });
    };

    return (
        <Box sx={{ p: 3 }}>
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

            <RosterHeader
                currentMonth={currentMonth}
                currentYear={currentYear}
                searchTerm={searchTerm}
                onSearchChange={handleSearchChange}
                onPreviousMonth={handlePreviousMonth}
                onNextMonth={handleNextMonth}
                onCurrentMonth={handleCurrentMonth}
                onRefresh={handleRefresh}
                loading={loading}
                getMonthName={getMonthName}
            />

            {renderContent()}
        </Box>
    );
};

export default RosterManagement;