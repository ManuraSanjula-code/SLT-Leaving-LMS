import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchAttendanceData = createAsyncThunk(
    'attendance/fetchAttendanceData',
    async ({ userId, startDate, endDate, dateRangeMode }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');

            if (!userId) {
                userId = sessionStorage.getItem('userId');
            }

            if (!userId) {
                return rejectWithValue('User ID not found in session storage');
            }

            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            let url;
            if (dateRangeMode) {
                url = `http://localhost:8080/lms/in-out/${startDate}/${endDate}/${userId}/${empId}`;
            } else {
                url = `http://localhost:8080/lms/in-out/${startDate}/${userId}/${empId}`;
            }

            const response = await fetch(url, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error: ${response.status}`);
            }

            const data = await response.json();
            return data;
        } catch (error) {
            return rejectWithValue(`Failed to fetch attendance data: ${error.message}`);
        }
    }
);

// Create the attendance slice
const attendanceSlice = createSlice({
    name: 'attendance',
    initialState: {
        attendanceData: [],
        loading: false,
        error: null,
        filters: {
            startDate: formatDate(new Date()),
            endDate: formatDate(new Date()),
            dateRangeMode: false
        },
        employeeInfo: {
            employeeName: ''
        }
    },
    reducers: {
        // Update filters
        setStartDate: (state, action) => {
            state.filters.startDate = action.payload;
        },
        setEndDate: (state, action) => {
            state.filters.endDate = action.payload;
        },
        toggleDateRangeMode: (state) => {
            state.filters.dateRangeMode = !state.filters.dateRangeMode;
        },
        // Update employee info
        setEmployeeName: (state, action) => {
            state.employeeInfo.employeeName = action.payload;
        },
        // Reset error
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchAttendanceData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchAttendanceData.fulfilled, (state, action) => {
                state.loading = false;
                state.attendanceData = action.payload;
            })
            .addCase(fetchAttendanceData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

// Helper function for formatting date
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Export actions
export const {
    setStartDate,
    setEndDate,
    toggleDateRangeMode,
    setEmployeeName,
    clearError
} = attendanceSlice.actions;

// Export selectors
export const selectAttendanceData = state => state.attendance.attendanceData;
export const selectFilters = state => state.attendance.filters;
export const selectLoading = state => state.attendance.loading;
export const selectError = state => state.attendance.error;
export const selectEmployeeInfo = state => state.attendance.employeeInfo;

// Export helpers for processing attendance data
export const attendanceHelpers = {
    // Format time string to display in readable format
    formatTime: (timeString) => {
        if (!timeString) return '--:--';
        return timeString;
    },

    // Format ISO date string
    formatISODate: (isoString) => {
        if (!isoString) return '';
        const date = new Date(isoString);
        return formatDate(date);
    },

    // Calculate duration between punch-in and punch-out
    calculateDuration: (punchInTime, punchOutTime) => {
        if (!punchInTime || !punchOutTime) return '--:--';

        const [inHours, inMinutes] = punchInTime.split(':').map(Number);
        const [outHours, outMinutes] = punchOutTime.split(':').map(Number);

        let durationMinutes = (outHours * 60 + outMinutes) - (inHours * 60 + inMinutes);
        if (durationMinutes < 0) durationMinutes += 24 * 60; // Handle overnight shifts

        const hours = Math.floor(durationMinutes / 60);
        const minutes = durationMinutes % 60;

        return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
    },

    // Process data to group punch-ins and punch-outs by date
    processAttendanceData: (attendanceData) => {
        const processedData = {};

        attendanceData.forEach(record => {
            // Determine the date from either punchInMoa or punchInEv
            const dateString = attendanceHelpers.formatISODate(record.punchInMoa || record.punchInEv);
            if (!dateString) return;

            if (!processedData[dateString]) {
                processedData[dateString] = {
                    date: dateString,
                    records: [],
                    totalHours: 0
                };
            }

            processedData[dateString].records.push(record);
        });

        // Sort by date
        return Object.values(processedData).sort((a, b) =>
            new Date(b.date) - new Date(a.date)
        );
    },

    // Group data to pair in-out records
    groupInOutPairs: (dateRecords) => {
        const pairs = [];
        let currentPair = {};

        // Sort by time
        const sortedRecords = [...dateRecords].sort((a, b) => {
            const timeA = a.timeMoa || a.timeEve || '';
            const timeB = b.timeMoa || b.timeEve || '';
            return timeA.localeCompare(timeB);
        });

        sortedRecords.forEach(record => {
            if (record.inOut === 1) { // Punch in
                if (Object.keys(currentPair).length > 0 && !currentPair.out) {
                    // Complete the previous pair if it's missing out
                    pairs.push({...currentPair, out: null});
                }
                currentPair = {in: record, out: null};
            } else if (record.inOut === 0) { // Punch out
                if (Object.keys(currentPair).length > 0 && currentPair.in && !currentPair.out) {
                    currentPair.out = record;
                    pairs.push({...currentPair});
                    currentPair = {};
                } else {
                    // Orphaned punch out
                    pairs.push({in: null, out: record});
                }
            }
        });

        // Add any remaining incomplete pair
        if (Object.keys(currentPair).length > 0) {
            pairs.push(currentPair);
        }

        return pairs;
    },

    // Calculate attendance summary
    calculateSummary: (attendanceData) => {
        let totalWorkingDays = 0;
        let totalWorkHours = 0;

        const processedData = attendanceHelpers.processAttendanceData(attendanceData);

        processedData.forEach(dayData => {
            const pairs = attendanceHelpers.groupInOutPairs(dayData.records);
            let dayHasValidPair = false;

            pairs.forEach(pair => {
                if (pair.in && pair.out) {
                    const inTime = pair.in.timeMoa || pair.in.timeEve;
                    const outTime = pair.out.timeMoa || pair.out.timeEve;

                    if (inTime && outTime) {
                        dayHasValidPair = true;

                        // Calculate hours
                        const [inHours, inMinutes] = inTime.split(':').map(Number);
                        const [outHours, outMinutes] = outTime.split(':').map(Number);

                        let durationMinutes = (outHours * 60 + outMinutes) - (inHours * 60 + inMinutes);
                        if (durationMinutes < 0) durationMinutes += 24 * 60; // Handle overnight shifts

                        totalWorkHours += durationMinutes / 60;
                    }
                }
            });

            if (dayHasValidPair) {
                totalWorkingDays++;
            }
        });

        return {
            totalWorkingDays,
            totalWorkHours: Math.round(totalWorkHours * 100) / 100,
            averageDailyHours: totalWorkingDays ?
                Math.round((totalWorkHours / totalWorkingDays) * 100) / 100 : 0
        };
    }
};

// Export reducer
export default attendanceSlice.reducer;