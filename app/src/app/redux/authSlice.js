import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  jwt: null,
  userId: null,
  userDetails: {
    firstName: '',
    lastName: '',
    email: '',
    profilePic: '', // Profile picture URL
    roles: [], // User roles (e.g., ROLE_ADMIN, ROLE_EMPLOYEE)
    phone: null, // Phone number
    gender: null, // Gender
    active: 0, // Active status
    addresses: [], // Addresses array
    sections: [], // Sections array
    profiles: [], // Profiles array
    isSltEmp: null, // Is SLT employee
    isSltIntern: null, // Is SLT intern
    defaultAddress: 0, // Default address index
  },
  loading: false,
  successMessage: null,
  errorMessage: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action) => {
      state.jwt = action.payload.jwt;
      state.userId = action.payload.userId;
      state.errorMessage = null;
    },
    setUserDetails: (state, action) => {
      console.log('Setting user details:', action.payload); // Log the payload
      state.userDetails = action.payload; // Update userDetails with server response
      state.loading = false;
      state.successMessage = 'User details fetched successfully!';
      state.errorMessage = null;
    },
    setLoading: (state, action) => {
      state.loading = action.payload;
    },
    setError: (state, action) => {
      state.loading = false;
      state.errorMessage = action.payload;
      state.successMessage = null;
    },
    clearAuth: (state, bool) => {
      state.jwt = null;
      state.userId = null;
      state.userDetails = {
        firstName: '',
        lastName: '',
        email: '',
        profilePic: '',
        roles: [],
        phone: null,
        gender: null,
        active: 0,
        addresses: [],
        sections: [],
        profiles: [],
        isSltEmp: null,
        isSltIntern: null,
        defaultAddress: 0,
      };
      state.loading = false;
      state.successMessage = null;
      state.errorMessage = null;
    },
  },
});

export const { setCredentials, setUserDetails, setLoading, setError, clearAuth } = authSlice.actions;
export default authSlice.reducer;