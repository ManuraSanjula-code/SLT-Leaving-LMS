import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

const initialState = {
    activities: [],
    loading: false,
    error: null,
    page: 0,
    rowsPerPage: 5,
    formData: {
        date: new Date().toISOString().split('T')[0],
        employeeId: '',
        arrivalDate: new Date().toISOString().split('T')[0],
        arrivalTime: '',
        leftTime: '',
        attendanceType: 'FULL_DAY',
        leaveStatus: null,
        payStatus: null,
        resolve: null,
        isLate: false,
        isLateCovered: false,
        isUnauthorized: false,
        isUnSuccessful: false,
        isHoliday: false,
        isResolved: false,
        hasIssues: false,
        isManual: true,
        isActive: true,
        issueDescription: '',
        dueDateForUA: '',
        terminalId: '',
    },
    isEditMode: false,
    editId: null,
    openDeleteDialog: false,
    idToDelete: null,
    showModal: false,
    showFilters: false,
    searchTerm: "",
    filterType: "all",
    filterStatus: "all",
    filterActive: "all",
    startDateFilter: "",
    endDateFilter: ""
};

export const fetchEmployeeActivities = createAsyncThunk(
    'employeeActivities/fetch',
    async ({userId, isAdmin, page, rowsPerPage}, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');

            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }

            const response = await fetch(`http://192.168.3.20:8080/lms/${userId}/${empId}?page=${page}&size=${rowsPerPage}`, {
                credentials: 'include',
            });

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error("User not found. Please check your credentials.");
                }
                throw new Error(`Error fetching data: ${response.statusText}`);
            }

            const data = await response.json();
            return data.content || [];
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const createEmployeeActivity = createAsyncThunk(
    'employeeActivities/create',
    async (formData, { rejectWithValue }) => {
        try {
            const submissionData = prepareFormData(formData);
            const empId = sessionStorage.getItem('userId');

            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch('http://192.168.3.20:8080/lms/' + empId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionData),
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => null);
                throw new Error(errorData?.message || 'Failed to submit attendance');
            }

            return await response.json();
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const updateEmployeeActivity = createAsyncThunk(
    'employeeActivities/update',
    async ({ id, formData }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const submissionData = prepareFormData(formData);
            const response = await fetch(`http://192.168.3.20:8080/lms/attendance/${id}/${empId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionData),
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => null);
                throw new Error(errorData?.message || 'Failed to update attendance');
            }

            return await response.json();
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const deleteEmployeeActivity = createAsyncThunk(
    'employeeActivities/delete',
    async (id, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/attendance/${id}/${empId}`, {
                method: 'DELETE',
                credentials: 'include',
            });

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error("ID not found. Please check your credentials.");
                }
                throw new Error(`Error deleting data: ${response.statusText}`);
            }

            return id;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const deleteEmployeeDeActivity = createAsyncThunk(
    'employeeActivities/deleteDe',
    async (id, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://192.168.3.20:8080/lms/attendance/de/${id}/${empId}`, {
                method: 'DELETE',
                credentials: 'include',
            });

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error("ID not found. Please check your credentials.");
                }
                throw new Error(`Error deleting data: ${response.statusText}`);
            }

            return id;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

const prepareFormData = (formData) => {
    const submissionData = { ...formData };

    // Handle time formatting
    if (submissionData.arrivalTime) {
        if (!submissionData.arrivalTime.includes(':')) {
            submissionData.arrivalTime = submissionData.arrivalTime + ":00:00";
        } else if (submissionData.arrivalTime.split(':').length === 2) {
            submissionData.arrivalTime = submissionData.arrivalTime + ":00";
        }
    } else {
        submissionData.arrivalTime = "00:00:00";
    }

    if (submissionData.leftTime) {
        if (!submissionData.leftTime.includes(':')) {
            submissionData.leftTime = submissionData.leftTime + ":00:00";
        } else if (submissionData.leftTime.split(':').length === 2) {
            submissionData.leftTime = submissionData.leftTime + ":00";
        }
    } else {
        submissionData.leftTime = "00:00:00";
    }

    // Handle date formatting
    if (submissionData.date) {
        submissionData.date = new Date(submissionData.date).toISOString();
    } else {
        submissionData.date = new Date().toISOString();
    }

    if (submissionData.arrivalDate) {
        submissionData.arrivalDate = new Date(submissionData.arrivalDate).toISOString();
    } else {
        submissionData.arrivalDate = new Date().toISOString();
    }

    if (submissionData.dueDateForUA && submissionData.isUnauthorized) {
        submissionData.dueDateForUA = new Date(submissionData.dueDateForUA).toISOString();
    } else if (submissionData.isUnauthorized) {
        const dueDate = new Date();
        dueDate.setDate(dueDate.getDate() + 7);
        submissionData.dueDateForUA = dueDate.toISOString();
    } else {
        submissionData.dueDateForUA = null;
    }

    // Convert null values to proper enum values or null
    if (!submissionData.leaveStatus || submissionData.leaveStatus === '') {
        submissionData.leaveStatus = null;
    }
    if (!submissionData.payStatus || submissionData.payStatus === '') {
        submissionData.payStatus = null;
    }
    if (!submissionData.resolve || submissionData.resolve === '') {
        submissionData.resolve = null;
    }

    return submissionData;
};

const employeeActivitiesSlice = createSlice({
    name: 'employeeActivities',
    initialState,
    reducers: {
        setPage: (state, action) => {
            state.page = action.payload;
        },
        setRowsPerPage: (state, action) => {
            state.rowsPerPage = action.payload;
            state.page = 0;
        },
        setSearchTerm: (state, action) => {
            state.searchTerm = action.payload;
            state.page = 0;
        },
        setFilterType: (state, action) => {
            state.filterType = action.payload;
            state.page = 0;
        },
        setFilterStatus: (state, action) => {
            state.filterStatus = action.payload;
            state.page = 0;
        },
        setFilterActive: (state, action) => {
            state.filterActive = action.payload;
            state.page = 0;
        },
        setStartDateFilter: (state, action) => {
            state.startDateFilter = action.payload;
            state.page = 0;
        },
        setEndDateFilter: (state, action) => {
            state.endDateFilter = action.payload;
            state.page = 0;
        },
        setFormData: (state, action) => {
            state.formData = action.payload;
        },
        resetFormData: (state) => {
            state.formData = initialState.formData;
        },
        setEditMode: (state, action) => {
            state.isEditMode = action.payload;
        },
        setEditId: (state, action) => {
            state.editId = action.payload;
        },
        setShowModal: (state, action) => {
            state.showModal = action.payload;
        },
        setShowFilters: (state, action) => {
            state.showFilters = action.payload;
        },
        setOpenDeleteDialog: (state, action) => {
            state.openDeleteDialog = action.payload;
        },
        setIdToDelete: (state, action) => {
            state.idToDelete = action.payload;
        },
        clearError: (state) => {
            state.error = null;
        },
        handleFormChange: (state, action) => {
            const { name, value, type, checked } = action.payload;
            state.formData = {
                ...state.formData,
                [name]: type === 'checkbox' ? checked : value
            };
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchEmployeeActivities.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchEmployeeActivities.fulfilled, (state, action) => {
                state.loading = false;
                state.activities = action.payload.sort((a, b) => new Date(b.date) - new Date(a.date));
            })
            .addCase(fetchEmployeeActivities.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(createEmployeeActivity.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(createEmployeeActivity.fulfilled, (state, action) => {
                state.loading = false;
                state.activities = [action.payload, ...state.activities];
                state.showModal = false;
                state.formData = initialState.formData;
            })
            .addCase(createEmployeeActivity.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(updateEmployeeActivity.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateEmployeeActivity.fulfilled, (state, action) => {
                state.loading = false;
                state.activities = state.activities.map(activity =>
                    activity.publicId === action.payload.publicId ? action.payload : activity
                );
                state.showModal = false;
                state.formData = initialState.formData;
                state.isEditMode = false;
                state.editId = null;
            })
            .addCase(updateEmployeeActivity.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(deleteEmployeeActivity.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(deleteEmployeeDeActivity.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(deleteEmployeeActivity.fulfilled, (state, action) => {
                state.loading = false;
                state.activities = state.activities.filter(activity => activity.publicId !== action.payload);
                state.openDeleteDialog = false;
                state.idToDelete = null;
            })
            .addCase(deleteEmployeeActivity.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.openDeleteDialog = false;
                state.idToDelete = null;
            })
            .addCase(deleteEmployeeDeActivity.fulfilled, (state, action) => {
                state.loading = false;
                state.activities = state.activities.filter(activity => activity.publicId !== action.payload);
                state.openDeleteDialog = false;
                state.idToDelete = null;
            })
            .addCase(deleteEmployeeDeActivity.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
                state.openDeleteDialog = false;
                state.idToDelete = null;
            });
    }
});

export const {
    setPage,
    setRowsPerPage,
    setSearchTerm,
    setFilterType,
    setFilterStatus,
    setFilterActive,
    setStartDateFilter,
    setEndDateFilter,
    setFormData,
    resetFormData,
    setEditMode,
    setEditId,
    setShowModal,
    setShowFilters,
    setOpenDeleteDialog,
    setIdToDelete,
    clearError,
    handleFormChange
} = employeeActivitiesSlice.actions;

export default employeeActivitiesSlice.reducer;