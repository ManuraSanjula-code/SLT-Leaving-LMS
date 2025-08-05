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
                url = `http://192.168.3.20:8080/lms/in-out/${startDate}/${endDate}/${userId}/${empId}`;
            } else {
                url = `http://192.168.3.20:8080/lms/in-out/${startDate}/${userId}/${empId}`;
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
            
            const sortedData = data.sort((a, b) => {
                const dateA = new Date(a.punchTime);
                const dateB = new Date(b.punchTime);
                return dateB - dateA;
            });
            
            return sortedData;
        } catch (error) {
            return rejectWithValue(`Failed to fetch attendance data: ${error.message}`);
        }
    }
);

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
        setStartDate: (state, action) => {
            state.filters.startDate = action.payload;
        },
        setEndDate: (state, action) => {
            state.filters.endDate = action.payload;
        },
        toggleDateRangeMode: (state) => {
            state.filters.dateRangeMode = !state.filters.dateRangeMode;
        },
        setEmployeeName: (state, action) => {
            state.employeeInfo.employeeName = action.payload;
        },
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

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

export const {
    setStartDate,
    setEndDate,
    toggleDateRangeMode,
    setEmployeeName,
    clearError
} = attendanceSlice.actions;

export const selectAttendanceData = state => state.attendance.attendanceData;
export const selectFilters = state => state.attendance.filters;
export const selectLoading = state => state.attendance.loading;
export const selectError = state => state.attendance.error;
export const selectEmployeeInfo = state => state.attendance.employeeInfo;

export const attendanceHelpers = {
    formatTime: (timeString) => {
        if (!timeString) return '--:--';
        return timeString;
    },

    formatISODate: (isoString) => {
        if (!isoString) return '';
        const date = new Date(isoString);
        return formatDate(date);
    },

    calculateDuration: (punchInTime, punchOutTime) => {
        if (!punchInTime || !punchOutTime) return '--:--';

        const [inHours, inMinutes] = punchInTime.split(':').map(Number);
        const [outHours, outMinutes] = punchOutTime.split(':').map(Number);

        let durationMinutes = (outHours * 60 + outMinutes) - (inHours * 60 + inMinutes);
        if (durationMinutes < 0) durationMinutes += 24 * 60;

        const hours = Math.floor(durationMinutes / 60);
        const minutes = durationMinutes % 60;

        return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
    },

    processAttendanceData: (attendanceData) => {
        const processedData = {};

        attendanceData.forEach(record => {
            const dateString = attendanceHelpers.formatISODate(record.date);
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

        return Object.values(processedData).sort((a, b) =>
            new Date(b.date) - new Date(a.date)
        );
    },

    groupInOutPairs: (dateRecords) => {
        const pairs = [];
        let currentPair = {};

        const sortedRecords = [...dateRecords].sort((a, b) => {
            const timeA = a.punchTypeTime || '';
            const timeB = b.punchTypeTime || '';
            return timeA.localeCompare(timeB);
        });

        sortedRecords.forEach(record => {
            if (record.inOutValue === 1) { // Punch in
                if (Object.keys(currentPair).length > 0 && !currentPair.out) {
                    pairs.push({...currentPair, out: null});
                }
                currentPair = {in: record, out: null};
            } else if (record.inOutValue === 0) { // Punch out
                if (Object.keys(currentPair).length > 0 && currentPair.in && !currentPair.out) {
                    currentPair.out = record;
                    pairs.push({...currentPair});
                    currentPair = {};
                } else {
                    pairs.push({in: null, out: record});
                }
            }
        });

        if (Object.keys(currentPair).length > 0) {
            pairs.push(currentPair);
        }

        return pairs;
    },

    calculateSummary: (attendanceData) => {
        let totalWorkingDays = 0;
        let totalWorkHours = 0;

        const processedData = attendanceHelpers.processAttendanceData(attendanceData);

        processedData.forEach(dayData => {
            const pairs = attendanceHelpers.groupInOutPairs(dayData.records);
            let dayHasValidPair = false;

            pairs.forEach(pair => {
                if (pair.in && pair.out) {
                    const inTime = pair.in.punchTypeTime;
                    const outTime = pair.out.punchTypeTime;

                    if (inTime && outTime) {
                        dayHasValidPair = true;

                        const [inHours, inMinutes, inSeconds] = inTime.split(':').map(Number);
                        const [outHours, outMinutes, outSeconds] = outTime.split(':').map(Number);

                        let durationMinutes = (outHours * 60 + outMinutes) - (inHours * 60 + inMinutes);
                        if (durationMinutes < 0) durationMinutes += 24 * 60;

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

export default attendanceSlice.reducer;