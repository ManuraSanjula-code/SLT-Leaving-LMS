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

            const data = await response.json();
            if (data.content && Array.isArray(data.content)) {
                data.content.sort((a, b) => {
                    const dateA = new Date(a.date);
                    const dateB = new Date(b.date);
                    return dateB - dateA;
                });
            }
            return data;
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
    }
});

export const { setPage, setPageSize, clearError, clearSelection } = unauthorizedLeavesSlice.actions;
export default unauthorizedLeavesSlice.reducer;