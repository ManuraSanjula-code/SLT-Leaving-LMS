import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

const API_BASE_URL = 'http://192.168.3.20:8080/api';

const processTeamData = (data, teamId) => ({
    id: data?.id || teamId,
    name: data?.name || 'Unknown Team',
    shortName: data?.shortName || 'UT',
    active: data?.active ?? true
});

const processEmployeeData = (data, id) => ({
    id: data?.id || id,
    employeeId: data?.employeeId || 'N/A',
    name: data?.name || 'Unknown Employee',
    shortName: data?.shortName || 'UE',
    mobileNo: data?.mobileNo || '',
    teamId: data?.teamId || null,
    active: data?.active ?? true
});

// Thunk for fetching roster data
export const fetchRosterData = createAsyncThunk(
    'rosterManagement/fetchRosterData',
    async ({ month, year }, { rejectWithValue }) => {
        try {
            const response = await fetch(`${API_BASE_URL}/roster/${month}/${year}`);

            if (!response.ok) {
                throw new Error(`Server error: ${response.status}`);
            }
            const contentLength = response.headers.get('Content-Length');
            if (contentLength === '0' || !response.body) {
                return [];
            } 
            const data = await response.json();

            if (!data?.teams?.length) {
                return rejectWithValue({
                    message: `No roster data for ${month}/${year}`,
                    isEmpty: true
                });
            }

            return data;
        } catch (error) {
            return rejectWithValue({
                message: error.message,
                isEmpty: false
            });
        }
    }
);

// Thunk for fetching team details
export const fetchTeamDetails = createAsyncThunk(
    'rosterManagement/fetchTeamDetails',
    async (teamIds) => {
        const teamPromises = teamIds.map(async (teamId) => {
            try {
                const response = await fetch(`${API_BASE_URL}/teams/${teamId}`);
                if (!response.ok) return processTeamData(null, teamId);
                const data = await response.json();
                return processTeamData(data, teamId);
            } catch {
                return processTeamData(null, teamId);
            }
        });
        return await Promise.all(teamPromises);
    }
);

// Thunk for fetching employee details
export const fetchEmployeeDetails = createAsyncThunk(
    'rosterManagement/fetchEmployeeDetails',
    async (employeeIds) => {
        const employeePromises = employeeIds.map(async (id) => {
            try {
                const response = await fetch(`${API_BASE_URL}/employees/${id}`);
                if (!response.ok) return processEmployeeData(null, id);
                const data = await response.json();
                return processEmployeeData(data, id);
            } catch {
                return processEmployeeData(null, id);
            }
        });
        const results = await Promise.all(employeePromises);
        return results.reduce((acc, emp) => ({ ...acc, [emp.id]: emp }), {});
    }
);

// Filter function for teams
const filterTeams = (teams, teamDetails, employees, searchTerm) => {
    if (!searchTerm) return teams;
    const searchLower = searchTerm.toLowerCase();

    return teams.filter(team => {
        const teamDetail = teamDetails.find(t => t.id === team.teamId) ||
            processTeamData(null, team.teamId);

        return (
            teamDetail.name.toLowerCase().includes(searchLower) ||
            teamDetail.shortName.toLowerCase().includes(searchLower) ||
            team.employees.some(emp => {
                const employee = employees[emp.employeeId] ||
                    processEmployeeData(null, emp.employeeId);
                return (
                    employee.name.toLowerCase().includes(searchLower) ||
                    employee.employeeId.toLowerCase().includes(searchLower) ||
                    employee.shortName.toLowerCase().includes(searchLower)
                );
            })
        );
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
    notification: {
        open: false,
        message: '',
        severity: 'info'
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
        navigateToCurrentMonth: (state) => {
            const now = new Date();
            state.currentMonth = now.getMonth() + 1;
            state.currentYear = now.getFullYear();
        },
        clearRosterData: (state) => {
            state.roster = null;
            state.teams = [];
            state.employees = {};
            state.filteredTeams = [];
        },
        closeNotification: (state) => {
            state.notification.open = false;
        },
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchRosterData.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.roster = null;
                state.filteredTeams = [];
            })
            .addCase(fetchRosterData.fulfilled, (state, action) => {
                state.loading = false;
                state.roster = action.payload;
                state.filteredTeams = filterTeams(
                    action.payload.teams,
                    state.teams,
                    state.employees,
                    state.searchTerm
                );
            })
            .addCase(fetchRosterData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.roster = null;
                state.filteredTeams = [];
            })
            .addCase(fetchTeamDetails.fulfilled, (state, action) => {
                state.teams = action.payload;
                if (state.roster) {
                    state.filteredTeams = filterTeams(
                        state.roster.teams,
                        action.payload,
                        state.employees,
                        state.searchTerm
                    );
                }
            })
            .addCase(fetchEmployeeDetails.fulfilled, (state, action) => {
                state.employees = action.payload;
                if (state.roster) {
                    state.filteredTeams = filterTeams(
                        state.roster.teams,
                        state.teams,
                        action.payload,
                        state.searchTerm
                    );
                }
            });
    }
});

// Export actions and reducer
export const {
    setSearchTerm,
    navigateToPreviousMonth,
    navigateToNextMonth,
    navigateToCurrentMonth,
    clearRosterData,
    closeNotification,
    clearError
} = rosterManagementSlice.actions;

export default rosterManagementSlice.reducer;

export const selectRosterManagementState = (state) => state.rosterManagement;