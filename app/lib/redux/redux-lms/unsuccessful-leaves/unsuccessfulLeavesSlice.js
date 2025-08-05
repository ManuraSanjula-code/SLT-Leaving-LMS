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
            
            const data = response.data;
            
            if (data.content && Array.isArray(data.content)) {
                data.content.sort((a, b) => {
                    const dateA = new Date(a.date);
                    const dateB = new Date(b.date);
                    return dateB - dateA;
                });
            }
            
            return data;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || "Failed to load data");
        }
    }
);



const sortLeavesByDate = (leaves) => {
    return leaves.sort((a, b) => {
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        return dateB - dateA;
    });
};

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
                if (state.leaves.content && Array.isArray(state.leaves.content)) {
                    state.leaves.content = sortLeavesByDate([...state.leaves.content]);
                }
            })
            .addCase(fetchUnsuccessfulLeaves.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

export const { setCurrentPage, setPageSize, clearError } = unsuccessfulLeavesSlice.actions;
export default unsuccessfulLeavesSlice.reducer;