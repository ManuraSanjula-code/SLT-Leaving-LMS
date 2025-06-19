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
                ? `http://192.168.3.20:8080/lms/un-authorized/${empId}`
                : `http://192.168.3.20:8080/lms/un-authorized/${userId}/${empId}`;

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
            const response = await fetch(`http://192.168.3.20:8080/lms/resolve-unauthorized/${id}/${empId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Failed to resolve leave');
            }

            const data = await response.json();
            return { id, resolveType: data?.resolveType || 'VIA_MOVEMENT' };
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
            const response = await fetch(`http://192.168.3.20:8080/lms/approve-unauthorized/${id}/${empId}`, {
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
            const promises = ids.map(id =>
                fetch(`http://192.168.3.20:8080/lms/resolve-unauthorized/${id}/${empId}`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                })
            );

            const responses = await Promise.all(promises);
            const resolvedItems = await Promise.all(
                responses.map(async (response, index) => {
                    const data = await response.json();
                    return {
                        id: ids[index],
                        resolveType: data?.resolveType || 'VIA_MOVEMENT'
                    };
                })
            );

            return resolvedItems;
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
            const response = await fetch(`http://192.168.3.20:8080/lms/un-authorized/delete-multiple/${empId}`, {
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
            state.page = 0;
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
                const { id, resolveType } = action.payload;
                state.leaves.content = state.leaves.content.map(leave =>
                    leave.id === id ? { ...leave, resolve: resolveType, isResolved: true } : leave
                );
            })
            .addCase(resolveUnauthorizedLeave.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(approveUnauthorizedLeave.fulfilled, (state, action) => {
                const approvedId = action.payload;
                state.leaves.content = state.leaves.content.filter(leave => leave.id !== approvedId);
                state.leaves.totalElements -= 1;
            })
            .addCase(approveUnauthorizedLeave.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(bulkResolveUnauthorizedLeaves.fulfilled, (state, action) => {
                const resolvedItems = action.payload;
                state.leaves.content = state.leaves.content.map(leave => {
                    const resolvedItem = resolvedItems.find(item => item.id === leave.id);
                    return resolvedItem ? { ...leave, resolve: resolvedItem.resolveType, isResolved: true } : leave;
                });
            })
            .addCase(bulkResolveUnauthorizedLeaves.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(deleteMultipleUnauthorizedLeaves.fulfilled, (state, action) => {
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