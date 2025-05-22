import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

const initialState = {
    leaves: {
        content: [],
        totalPages: 0,
        totalElements: 0,
        number: 0,
    },
    loading: false,
    error: null,
    page: 0,
    pageSize: 10,
};

export const fetchUnauthorizedLeaves = createAsyncThunk(
    'unauthorized-leaves/fetch',
    async ({ isAdmin, page, pageSize, userId }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const endpoint = isAdmin
                ? `http://localhost:8080/lms/un-authorized/${empId}`
                : `http://localhost:8080/lms/un-authorized/${userId}/${empId}`;

            const response = await fetch(`${endpoint}?page=${page}&size=${pageSize}`, {
                credentials: 'include',
            });

            if (!response.ok) {
                throw new Error('Failed to fetch data');
            }

            return await response.json();
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const resolveUnauthorizedLeave = createAsyncThunk(
    'unauthorized-leaves/resolve',
    async (id, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://localhost:8080/lms/resolve-unauthorized/${id}/${empId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Failed to resolve leave');
            }

            return id;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const approveUnauthorizedLeave = createAsyncThunk(
    'unauthorized-leaves/approve',
    async (id, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://localhost:8080/lms/approve-unauthorized/${id}/${empId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Failed to approve leave');
            }

            return id;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const bulkResolveUnauthorizedLeaves = createAsyncThunk(
    'unauthorized-leaves/bulkResolve',
    async (ids, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            await Promise.all(
                ids.map(id =>
                    fetch(`http://localhost:8080/lms/resolve-unauthorized/${id}/${empId}`, {
                        method: 'POST',
                        credentials: 'include',
                        headers: {
                            'Content-Type': 'application/json',
                        }
                    })
                )
            );
            return ids;
        } catch (err) {
            return rejectWithValue("Failed to resolve selected leaves. Please try again.");
        }
    }
);

export const deleteMultipleUnauthorizedLeaves = createAsyncThunk(
    'unauthorized-leaves/deleteMultiple',
    async (ids, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const response = await fetch(`http://localhost:8080/lms/un-authorized/delete-multiple/${empId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ ids }),
            });

            if (!response.ok) {
                throw new Error('Failed to delete selected leaves');
            }

            return ids;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

const unauthorizedLeavesSlice = createSlice({
    name: 'unauthorizedLeaves',
    initialState,
    reducers: {
        setPage: (state, action) => {
            state.page = action.payload;
        },
        setPageSize: (state, action) => {
            state.pageSize = action.payload;
            state.page = 0; // Reset to first page when changing page size
        },
        clearError: (state) => {
            state.error = null;
        },
        clearSelection: (state) => {
            state.selected = [];
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUnauthorizedLeaves.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchUnauthorizedLeaves.fulfilled, (state, action) => {
                state.loading = false;
                state.leaves = action.payload;
            })
            .addCase(fetchUnauthorizedLeaves.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(resolveUnauthorizedLeave.fulfilled, (state, action) => {
                // Update the resolved leave in the state
                const resolvedId = action.payload;
                state.leaves.content = state.leaves.content.map(leave =>
                    leave.id === resolvedId ? { ...leave, resolve: true } : leave
                );
            })
            .addCase(resolveUnauthorizedLeave.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(approveUnauthorizedLeave.fulfilled, (state, action) => {
                // Remove the approved leave from the state
                const approvedId = action.payload;
                state.leaves.content = state.leaves.content.filter(leave => leave.id !== approvedId);
                state.leaves.totalElements -= 1;
            })
            .addCase(approveUnauthorizedLeave.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(bulkResolveUnauthorizedLeaves.fulfilled, (state, action) => {
                // Update all resolved leaves in the state
                const resolvedIds = action.payload;
                state.leaves.content = state.leaves.content.map(leave =>
                    resolvedIds.includes(leave.id) ? { ...leave, resolve: true } : leave
                );
            })
            .addCase(bulkResolveUnauthorizedLeaves.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(deleteMultipleUnauthorizedLeaves.fulfilled, (state, action) => {
                // Remove deleted leaves from the state
                const deletedIds = action.payload;
                state.leaves.content = state.leaves.content.filter(leave => !deletedIds.includes(leave.id));
                state.leaves.totalElements -= deletedIds.length;
            })
            .addCase(deleteMultipleUnauthorizedLeaves.rejected, (state, action) => {
                state.error = action.payload;
            });
    }
});

export const { setPage, setPageSize, clearError, clearSelection } = unauthorizedLeavesSlice.actions;
export default unauthorizedLeavesSlice.reducer;