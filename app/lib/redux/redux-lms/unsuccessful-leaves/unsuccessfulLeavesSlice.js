import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import axios from "axios";

const initialState = {
    leaves: {
        content: [],
        totalPages: 0,
        totalElements: 0,
        number: 0,
        size: 10
    },
    loading: false,
    error: null,
    currentPage: 0,
    pageSize: 10,
};

export const fetchUnsuccessfulLeaves = createAsyncThunk(
    'unsuccessfulLeaves/fetch',
    async ({ isAdmin, currentPage, pageSize, userId }, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            const endpoint = isAdmin
                ? `http://localhost:8080/lms/un-successful/${empId}`
                : `http://localhost:8080/lms/un-successful/${userId}/${empId}`;

            const response = await axios.get(endpoint, {
                params: {
                    page: currentPage,
                    size: pageSize
                },
                withCredentials: true
            });
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || "Failed to load data");
        }
    }
);

export const resolveLeave = createAsyncThunk(
    'unsuccessfulLeaves/resolve',
    async (id, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            await axios.post(`http://localhost:8080/lms/resolve-unauthorized/${id}/${empId}`, {}, {
                withCredentials: true
            });
            return id;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || "Failed to resolve leave");
        }
    }
);

export const bulkResolveLeaves = createAsyncThunk(
    'unsuccessfulLeaves/bulkResolve',
    async (ids, { rejectWithValue }) => {
        try {
            const empId = sessionStorage.getItem('userId');
            if (!empId) {
                return rejectWithValue('Employee ID not found in session storage');
            }
            await Promise.all(
                ids.map(id =>
                    axios.post(`http://localhost:8080/lms/resolve-unauthorized/${id}/${empId}`, {}, {
                        withCredentials: true
                    })
                )
            );
            return ids;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || "Failed to resolve selected leaves");
        }
    }
);

const unsuccessfulLeavesSlice = createSlice({
    name: 'unsuccessfulLeaves',
    initialState,
    reducers: {
        setCurrentPage: (state, action) => {
            state.currentPage = action.payload;
        },
        setPageSize: (state, action) => {
            state.pageSize = action.payload;
            state.currentPage = 0; // Reset to first page when changing page size
        },
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUnsuccessfulLeaves.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchUnsuccessfulLeaves.fulfilled, (state, action) => {
                state.loading = false;
                state.leaves = action.payload;
            })
            .addCase(fetchUnsuccessfulLeaves.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(resolveLeave.fulfilled, (state, action) => {
                // Update the resolved leave in the state
                const resolvedId = action.payload;
                state.leaves.content = state.leaves.content.map(leave =>
                    leave.id === resolvedId ? { ...leave, resolve: true } : leave
                );
            })
            .addCase(resolveLeave.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(bulkResolveLeaves.fulfilled, (state, action) => {
                // Update all resolved leaves in the state
                const resolvedIds = action.payload;
                state.leaves.content = state.leaves.content.map(leave =>
                    resolvedIds.includes(leave.id) ? { ...leave, resolve: true } : leave
                );
            })
            .addCase(bulkResolveLeaves.rejected, (state, action) => {
                state.error = action.payload;
            });
    }
});

export const { setCurrentPage, setPageSize, clearError } = unsuccessfulLeavesSlice.actions;
export default unsuccessfulLeavesSlice.reducer;