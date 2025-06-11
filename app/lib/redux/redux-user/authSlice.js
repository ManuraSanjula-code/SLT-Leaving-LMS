import {createSlice} from '@reduxjs/toolkit';

const initialState = {
    jwt: null,
    userId: null,
    userDetails: {
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
        isAuthenticated: false,
        integrityHash: null,
        highestRolePriority: 0,
        roaster: false
    },
    loading: false,
    successMessage: null,
    errorMessage: null,
    error: null,
    _expires: null
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setIntegrityHash: (state, action) => {
            state.integrityHash = action.payload;
        },
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
            const sanitizedDetails = {...action.payload};

            if (!Array.isArray(sanitizedDetails.roles)) {
                sanitizedDetails.roles = [];
            }

            if (sanitizedDetails.email && typeof sanitizedDetails.email === 'string') {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(sanitizedDetails.email)) {
                    sanitizedDetails.email = '';
                }
            }

            if (sanitizedDetails.profilePic && typeof sanitizedDetails.profilePic === 'string') {
                if (!sanitizedDetails.profilePic.startsWith('/') &&
                    !sanitizedDetails.profilePic.startsWith('http://localhost')){
                    sanitizedDetails.profilePic = '';
                }
            }

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
            state.errorMessage = action.payload ?
                String(action.payload).replace(/<[^>]*>/g, '') :
                'An unknown error occurred';
            state.successMessage = null;
        },

        clearAuth: (state) => {
            Object.assign(state, initialState);
        },

        oginStart: (state) => {
            state.loading = true;
            state.error = null;
        },
        loginSuccess: (state, action) => {
            state.loading = false;
            state.userDetails = action.payload.user;
            state.token = action.payload.token;
            state._expires = Date.now() + (24 * 60 * 60 * 1000);

            if (typeof window !== 'undefined') {
                const fingerprint = CryptoJS.SHA256(
                    navigator.userAgent +
                    (action.payload.token ? action.payload.token.substr(0, 10) : '')
                ).toString();
                state._fingerprint = fingerprint;
            }
        },
        loginFailure: (state, action) => {
            state.loading = false;
            state.error = action.payload;
            state.userDetails = null;
            state.token = null;
            state._expires = null;
        },
        logout: (state) => {
            return initialState;
        },
        extendSession: (state) => {
            if (state.token) {
                state._expires = Date.now() + (24 * 60 * 60 * 1000);
            }
        }
    },
});

export const {
    setCredentials,
    setUserDetails,
    setLoading,
    setError,
    clearAuth,
    setIntegrityHash,
    loginStart,
    loginSuccess,
    loginFailure,
    logout,
    extendSession
} =
    authSlice.actions;
export default authSlice.reducer;