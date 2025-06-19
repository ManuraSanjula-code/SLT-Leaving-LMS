import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Async thunk for fetching roster data
export const fetchRosterData = createAsyncThunk(
    'rosterManagement/fetchRosterData',
    async ({ month, year }, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/api/roster/${month}/${year}`);

            if (!response.ok) {
                throw new Error(`Failed to fetch roster data. Status: ${response.status}`);
            }

            const data = await response.json();

            if (!data || !data.teams || data.teams.length === 0) {
                return rejectWithValue("No roster data found for the selected period");
            }

            return data;
        } catch (error) {
            console.error('Error fetching roster data:', error);
            return rejectWithValue(`Error: ${error.message || "Failed to fetch roster data"}`);
        }
    }
);

// Async thunk for fetching team details
export const fetchTeamDetails = createAsyncThunk(
    'rosterManagement/fetchTeamDetails',
    async (teamIds, { rejectWithValue }) => {
        try {
            const teamPromises = teamIds.map(teamId =>
                fetch(`http://192.168.3.20:8080/api/teams/${teamId}`)
                    .then(res => {
                        if (!res.ok) {
                            throw new Error(`Failed to fetch team details for ${teamId}. Status: ${res.status}`);
                        }
                        return res.json();
                    })
                    .catch(err => {
                        console.error(`Error fetching team ${teamId}:`, err);
                        return { id: teamId, name: 'Team Data Unavailable', shortName: 'Error' };
                    })
            );

            const teamResults = await Promise.all(teamPromises);
            return teamResults;
        } catch (error) {
            console.error('Error fetching team details:', error);
            return rejectWithValue(`Error: ${error.message || "Failed to fetch team details"}`);
        }
    }
);

// Async thunk for fetching employee details
export const fetchEmployeeDetails = createAsyncThunk(
    'rosterManagement/fetchEmployeeDetails',
    async (employeeIds, { rejectWithValue }) => {
        try {
            const employeePromises = employeeIds.map(id =>
                fetch(`http://192.168.3.20:8080/api/employees/${id}`)
                    .then(res => {
                        if (!res.ok) {
                            throw new Error(`Failed to fetch employee details for ${id}. Status: ${res.status}`);
                        }
                        return res.json();
                    })
                    .catch(err => {
                        console.error(`Error fetching employee ${id}:`, err);
                        return {
                            id: id,
                            name: 'Employee Data Unavailable',
                            employeeId: 'Error',
                            shortName: 'Error'
                        };
                    })
            );

            const employeeResults = await Promise.all(employeePromises);

            // Create a map of employee ID to employee details
            const employeeMap = {};
            employeeResults.forEach(emp => {
                employeeMap[emp.id] = emp;
            });

            return employeeMap;
        } catch (error) {
            console.error('Error fetching employee details:', error);
            return rejectWithValue(`Error: ${error.message || "Failed to fetch employee details"}`);
        }
    }
);

// Async thunk for updating employee data
export const updateEmployeeRoster = createAsyncThunk(
    'rosterManagement/updateEmployeeRoster',
    async (payload, { rejectWithValue }) => {
        try {
            const response = await fetch(`http://192.168.3.20:8080/api/roster/employee`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error(`Failed to update employee data. Status: ${response.status}`);
            }

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
            return rejectWithValue(`Error: ${error.message || "Failed to update employee data"}`);
        }
    }
);

// Initial state
const initialState = {
    roster: null,
    teams: [],
    employees: {},
    filteredTeams: [],
    currentMonth: new Date().getMonth() + 1, // JS months are 0-indexed
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

// Create the rosterManagement slice
const rosterManagementSlice = createSlice({
    name: 'rosterManagement',
    initialState,
    reducers: {
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload;
            // Filter teams based on search term
            if (action.payload === '') {
                state.filteredTeams = state.roster?.teams || [];
                return;
            }

            if (!state.roster) return;

            const searchValue = action.payload.toLowerCase();
            const filtered = state.roster.teams.filter(team => {
                // Check if team name matches
                const teamDetails = state.teams.find(t => t.id === team.teamId);
                if (teamDetails && (
                    teamDetails.name.toLowerCase().includes(searchValue) ||
                    teamDetails.shortName.toLowerCase().includes(searchValue)
                )) {
                    return true;
                }

                // Check if any employee in the team matches
                return team.employees.some(emp => {
                    const employeeDetails = state.employees[emp.employeeId];
                    return employeeDetails && (
                        employeeDetails.name.toLowerCase().includes(searchValue) ||
                        employeeDetails.employeeId.toLowerCase().includes(searchValue) ||
                        employeeDetails.shortName.toLowerCase().includes(searchValue)
                    );
                });
            });

            state.filteredTeams = filtered;
        },
        setCurrentMonth: (state, action) => {
            state.currentMonth = action.payload;
        },
        setCurrentYear: (state, action) => {
            state.currentYear = action.payload;
        },
        navigateToPreviousMonth: (state) => {
            if (state.currentMonth === 1) {
                state.currentMonth = 12;
                state.currentYear = state.currentYear - 1;
            } else {
                state.currentMonth = state.currentMonth - 1;
            }
        },
        navigateToNextMonth: (state) => {
            if (state.currentMonth === 12) {
                state.currentMonth = 1;
                state.currentYear = state.currentYear + 1;
            } else {
                state.currentMonth = state.currentMonth + 1;
            }
        },
        startEditingEmployee: (state, action) => {
            const { teamId, employeeId } = action.payload;
            const team = state.roster.teams.find(t => t.teamId === teamId);
            const employee = team.employees.find(emp => emp.employeeId === employeeId);

            state.editingEmployee = { teamId, employeeId };
            state.editData = {
                totalShift: employee.totalShift,
                rotShift: employee.rotShift,
                offDay: employee.offDay,
                dduty: employee.dduty
            };
            state.editMode = true;
        },
        cancelEditing: (state) => {
            state.editMode = false;
            state.editingEmployee = null;
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
            // fetchRosterData reducers
            .addCase(fetchRosterData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchRosterData.fulfilled, (state, action) => {
                state.roster = action.payload;
                state.filteredTeams = action.payload.teams;
                state.loading = false;
            })
            .addCase(fetchRosterData.rejected, (state, action) => {
                state.error = action.payload;
                state.loading = false;
                state.roster = null;
                state.filteredTeams = [];
            })

            // fetchTeamDetails reducers
            .addCase(fetchTeamDetails.pending, (state) => {
                // No need to set loading=true here as we want to keep showing data
            })
            .addCase(fetchTeamDetails.fulfilled, (state, action) => {
                state.teams = action.payload;
            })
            .addCase(fetchTeamDetails.rejected, (state, action) => {
                // Just log the error, don't disrupt the UI
                console.error("Error fetching team details:", action.payload);
            })

            // fetchEmployeeDetails reducers
            .addCase(fetchEmployeeDetails.pending, (state) => {
                // No need to set loading=true here
            })
            .addCase(fetchEmployeeDetails.fulfilled, (state, action) => {
                state.employees = action.payload;
            })
            .addCase(fetchEmployeeDetails.rejected, (state, action) => {
                // Just log the error, don't disrupt the UI
                console.error("Error fetching employee details:", action.payload);
            })

            // updateEmployeeRoster reducers
            .addCase(updateEmployeeRoster.pending, (state) => {
                state.loading = true;
            })
            .addCase(updateEmployeeRoster.fulfilled, (state, action) => {
                const { teamId, employeeId, updates } = action.payload;

                // Update roster and filteredTeams
                const updateTeamEmployees = (teamsList) => {
                    return teamsList.map(team => {
                        if (team.teamId === teamId) {
                            return {
                                ...team,
                                employees: team.employees.map(emp => {
                                    if (emp.employeeId === employeeId) {
                                        return { ...emp, ...updates };
                                    }
                                    return emp;
                                })
                            };
                        }
                        return team;
                    });
                };

                if (state.roster) {
                    state.roster.teams = updateTeamEmployees(state.roster.teams);
                }

                state.filteredTeams = updateTeamEmployees(state.filteredTeams);

                // Reset edit state
                state.editMode = false;
                state.editingEmployee = null;

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

// Export actions and reducer
export const {
    setSearchTerm,
    setCurrentMonth,
    setCurrentYear,
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

// Selector
export const selectRosterManagementState = (state) => state.rosterManagement;