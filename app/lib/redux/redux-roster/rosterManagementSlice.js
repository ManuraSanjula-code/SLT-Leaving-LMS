import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const API_BASE_URL = 'http://localhost:8080/api';

// Helper function for handling API errors
const handleApiError = (response, fallbackMessage) => {
    if (!response.ok) {
        throw new Error(`${fallbackMessage}. Status: ${response.status}`);
    }
};

// Async thunk for fetching roster data
export const fetchRosterData = createAsyncThunk(
    'rosterManagement/fetchRosterData',
    async ({ month, year }, { rejectWithValue }) => {
        try {
            const response = await fetch(`${API_BASE_URL}/roster/${month}/${year}`);
            handleApiError(response, 'Failed to fetch roster data');

            const data = await response.json();

            if (!data?.teams?.length) {
                return rejectWithValue("No roster data found for the selected period");
            }

            return data;
        } catch (error) {
            console.error('Error fetching roster data:', error);
            return rejectWithValue(error.message || "Failed to fetch roster data");
        }
    }
);

// Async thunk for fetching team details
export const fetchTeamDetails = createAsyncThunk(
    'rosterManagement/fetchTeamDetails',
    async (teamIds, { rejectWithValue }) => {
        try {
            const teamPromises = teamIds.map(async (teamId) => {
                try {
                    const response = await fetch(`${API_BASE_URL}/teams/${teamId}`);
                    handleApiError(response, `Failed to fetch team details for ${teamId}`);
                    return await response.json();
                } catch (error) {
                    console.error(`Error fetching team ${teamId}:`, error);
                    return {
                        id: teamId,
                        name: 'Team Data Unavailable',
                        shortName: 'Error'
                    };
                }
            });

            return await Promise.all(teamPromises);
        } catch (error) {
            console.error('Error fetching team details:', error);
            return rejectWithValue(error.message || "Failed to fetch team details");
        }
    }
);

// Async thunk for fetching employee details
export const fetchEmployeeDetails = createAsyncThunk(
    'rosterManagement/fetchEmployeeDetails',
    async (employeeIds, { rejectWithValue }) => {
        try {
            const employeePromises = employeeIds.map(async (id) => {
                try {
                    const response = await fetch(`${API_BASE_URL}/employees/${id}`);
                    handleApiError(response, `Failed to fetch employee details for ${id}`);
                    return await response.json();
                } catch (error) {
                    console.error(`Error fetching employee ${id}:`, error);
                    return {
                        id,
                        name: 'Employee Data Unavailable',
                        employeeId: 'Error',
                        shortName: 'Error'
                    };
                }
            });

            const employeeResults = await Promise.all(employeePromises);

            // Convert array to object for easier lookup
            return employeeResults.reduce((acc, emp) => {
                acc[emp.id] = emp;
                return acc;
            }, {});
        } catch (error) {
            console.error('Error fetching employee details:', error);
            return rejectWithValue(error.message || "Failed to fetch employee details");
        }
    }
);

// Async thunk for updating employee data
export const updateEmployeeRoster = createAsyncThunk(
    'rosterManagement/updateEmployeeRoster',
    async (payload, { rejectWithValue }) => {
        try {
            const response = await fetch(`${API_BASE_URL}/roster/employee`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            handleApiError(response, 'Failed to update employee data');

            return {
                teamId: payload.teamId,
                employeeId: payload.employeeId,
                updates: {
                    totalShift: payload.totalShift,
                    rotShift: payload.rotShift,
                    offDay: payload.offDay,
                    dduty: payload.dduty
                }
            };
        } catch (error) {
            console.error('Error updating employee data:', error);
            return rejectWithValue(error.message || "Failed to update employee data");
        }
    }
);

// Helper function to filter teams based on search term
const filterTeams = (teams, teamDetails, employees, searchTerm) => {
    if (!searchTerm) return teams;

    const searchValue = searchTerm.toLowerCase();

    return teams.filter(team => {
        // Check team name match
        const teamDetail = teamDetails.find(t => t.id === team.teamId);
        if (teamDetail && (
            teamDetail.name.toLowerCase().includes(searchValue) ||
            teamDetail.shortName.toLowerCase().includes(searchValue)
        )) {
            return true;
        }

        // Check employee match
        return team.employees.some(emp => {
            const employeeDetail = employees[emp.employeeId];
            return employeeDetail && (
                employeeDetail.name.toLowerCase().includes(searchValue) ||
                employeeDetail.employeeId.toLowerCase().includes(searchValue) ||
                employeeDetail.shortName.toLowerCase().includes(searchValue)
            );
        });
    });
};

// Helper function to update team employees
const updateTeamEmployees = (teamsList, teamId, employeeId, updates) => {
    return teamsList.map(team => {
        if (team.teamId === teamId) {
            return {
                ...team,
                employees: team.employees.map(emp =>
                    emp.employeeId === employeeId ? { ...emp, ...updates } : emp
                )
            };
        }
        return team;
    });
};

// Initial state
const initialState = {
    roster: null,
    teams: [],
    employees: {},
    filteredTeams: [],
    currentMonth: new Date().getMonth() + 1,
    currentYear: new Date().getFullYear(),
    searchTerm: '',
    loading: false,
    error: null,
    editMode: false,
    editingEmployee: null,
    editData: {},
    notification: {
        open: false,
        message: "",
        severity: "info"
    }
};

const rosterManagementSlice = createSlice({
    name: 'rosterManagement',
    initialState,
    reducers: {
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload;
            state.filteredTeams = filterTeams(
                state.roster?.teams || [],
                state.teams,
                state.employees,
                action.payload
            );
        },
        navigateToPreviousMonth: (state) => {
            if (state.currentMonth === 1) {
                state.currentMonth = 12;
                state.currentYear -= 1;
            } else {
                state.currentMonth -= 1;
            }
        },
        navigateToNextMonth: (state) => {
            if (state.currentMonth === 12) {
                state.currentMonth = 1;
                state.currentYear += 1;
            } else {
                state.currentMonth += 1;
            }
        },
        startEditingEmployee: (state, action) => {
            const { teamId, employeeId } = action.payload;
            const team = state.roster.teams.find(t => t.teamId === teamId);
            const employee = team?.employees.find(emp => emp.employeeId === employeeId);

            if (employee) {
                state.editingEmployee = { teamId, employeeId };
                state.editData = {
                    totalShift: employee.totalShift,
                    rotShift: employee.rotShift,
                    offDay: employee.offDay,
                    dduty: employee.dduty
                };
                state.editMode = true;
            }
        },
        cancelEditing: (state) => {
            state.editMode = false;
            state.editingEmployee = null;
            state.editData = {};
        },
        updateEditData: (state, action) => {
            const { field, value } = action.payload;
            state.editData[field] = parseInt(value) || 0;
        },
        closeNotification: (state) => {
            state.notification.open = false;
        },
        setNotification: (state, action) => {
            state.notification = {
                open: true,
                message: action.payload.message,
                severity: action.payload.severity || "info"
            };
        },
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            // Roster data handlers
            .addCase(fetchRosterData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchRosterData.fulfilled, (state, action) => {
                state.roster = action.payload;
                state.filteredTeams = filterTeams(
                    action.payload.teams,
                    state.teams,
                    state.employees,
                    state.searchTerm
                );
                state.loading = false;
            })
            .addCase(fetchRosterData.rejected, (state, action) => {
                state.error = action.payload;
                state.loading = false;
                state.roster = null;
                state.filteredTeams = [];
            })

            // Team details handlers
            .addCase(fetchTeamDetails.fulfilled, (state, action) => {
                state.teams = action.payload;
                // Reapply search filter with new team data
                state.filteredTeams = filterTeams(
                    state.roster?.teams || [],
                    action.payload,
                    state.employees,
                    state.searchTerm
                );
            })
            .addCase(fetchTeamDetails.rejected, (state, action) => {
                console.error("Error fetching team details:", action.payload);
            })

            // Employee details handlers
            .addCase(fetchEmployeeDetails.fulfilled, (state, action) => {
                state.employees = action.payload;
                // Reapply search filter with new employee data
                state.filteredTeams = filterTeams(
                    state.roster?.teams || [],
                    state.teams,
                    action.payload,
                    state.searchTerm
                );
            })
            .addCase(fetchEmployeeDetails.rejected, (state, action) => {
                console.error("Error fetching employee details:", action.payload);
            })

            // Employee update handlers
            .addCase(updateEmployeeRoster.pending, (state) => {
                state.loading = true;
            })
            .addCase(updateEmployeeRoster.fulfilled, (state, action) => {
                const { teamId, employeeId, updates } = action.payload;

                // Update both roster and filtered teams
                if (state.roster) {
                    state.roster.teams = updateTeamEmployees(
                        state.roster.teams, teamId, employeeId, updates
                    );
                }
                state.filteredTeams = updateTeamEmployees(
                    state.filteredTeams, teamId, employeeId, updates
                );

                // Reset edit state
                state.editMode = false;
                state.editingEmployee = null;
                state.editData = {};

                // Show success notification
                state.notification = {
                    open: true,
                    message: "Employee data updated successfully",
                    severity: "success"
                };
                state.loading = false;
            })
            .addCase(updateEmployeeRoster.rejected, (state, action) => {
                state.notification = {
                    open: true,
                    message: action.payload,
                    severity: "error"
                };
                state.loading = false;
            });
    }
});

export const {
    setSearchTerm,
    navigateToPreviousMonth,
    navigateToNextMonth,
    startEditingEmployee,
    cancelEditing,
    updateEditData,
    closeNotification,
    setNotification,
    clearError
} = rosterManagementSlice.actions;

export default rosterManagementSlice.reducer;

export const selectRosterManagementState = (state) => state.rosterManagement;