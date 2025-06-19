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
                ? `http://192.168.3.20:8080/lms/un-successful/${empId}`
                : `http://192.168.3.20:8080/lms/un-successful/${userId}/${empId}`;

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
            const response = await axios.post(`http://192.168.3.20:8080/lms/resolve-unauthorized/${id}/${empId}`, {}, {
                withCredentials: true
            });
            return { id, resolveType: response.data?.resolveType || 'VIA_MOVEMENT' };
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
            const promises = ids.map(id =>
                axios.post(`http://192.168.3.20:8080/lms/resolve-unauthorized/${id}/${empId}`, {}, {
                    withCredentials: true
                })
            );

            const responses = await Promise.all(promises);
            const resolvedItems = ids.map((id, index) => ({
                id,
                resolveType: responses[index].data?.resolveType || 'VIA_MOVEMENT'
            }));

            return resolvedItems;
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
            state.currentPage = 0;
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
                const { id, resolveType } = action.payload;
                state.leaves.content = state.leaves.content.map(leave =>
                    leave.id === id ? { ...leave, resolve: resolveType, isResolved: true } : leave
                );
            })
            .addCase(resolveLeave.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(bulkResolveLeaves.fulfilled, (state, action) => {
                const resolvedItems = action.payload;
                state.leaves.content = state.leaves.content.map(leave => {
                    const resolvedItem = resolvedItems.find(item => item.id === leave.id);
                    return resolvedItem ? { ...leave, resolve: resolvedItem.resolveType, isResolved: true } : leave;
                });
            })
            .addCase(bulkResolveLeaves.rejected, (state, action) => {
                state.error = action.payload;
            });
    }
});

export const { setCurrentPage, setPageSize, clearError } = unsuccessfulLeavesSlice.actions;
export default unsuccessfulLeavesSlice.reducer;