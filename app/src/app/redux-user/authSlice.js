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
      // Validate input
      if (!action.payload?.jwt || !action.payload?.userId) {
        console.error('Invalid credentials provided');
        return;
      }

      state.jwt = action.payload.jwt;
      state.userId = action.payload.userId;
      state.errorMessage = null;
    },

    setUserDetails: (state, action) => {
      // Sanitize incoming user details
      const sanitizedDetails = { ...action.payload };

      // Ensure roles is always an array
      if (!Array.isArray(sanitizedDetails.roles)) {
        sanitizedDetails.roles = [];
      }

      // Validate email format if present
      if (sanitizedDetails.email && typeof sanitizedDetails.email === 'string') {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(sanitizedDetails.email)) {
          sanitizedDetails.email = '';
        }
      }

      // Sanitize profile picture URL if needed
      if (sanitizedDetails.profilePic && typeof sanitizedDetails.profilePic === 'string') {
        // Ensure only relative paths or your domain URLs are accepted
        if (!sanitizedDetails.profilePic.startsWith('/') &&
            !sanitizedDetails.profilePic.startsWith('http://localhost') &&
            !sanitizedDetails.profilePic.startsWith('https://yourdomain.com')) {
          sanitizedDetails.profilePic = '';
        }
      }

      // Ensure addresses is always an array
      if (!Array.isArray(sanitizedDetails.addresses)) {
        sanitizedDetails.addresses = [];
      }

      console.log('Setting sanitized user details');
      state.userDetails = sanitizedDetails;
      state.loading = false;
      state.successMessage = 'User details fetched successfully!';
      state.errorMessage = null;
    },

    setLoading: (state, action) => {
      state.loading = Boolean(action.payload);
    },

    setError: (state, action) => {
      state.loading = false;
      // Sanitize error message to prevent injection attacks
      state.errorMessage = action.payload ?
          String(action.payload).replace(/<[^>]*>/g, '') :
          'An unknown error occurred';
      state.successMessage = null;
    },

    clearAuth: (state) => {
      // Reset to initial state
      Object.assign(state, initialState);
    },
  },
});

export const { setCredentials, setUserDetails, setLoading, setError, clearAuth } = authSlice.actions;
export default authSlice.reducer;