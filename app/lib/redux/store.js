import { configureStore, combineReducers } from '@reduxjs/toolkit';
import {
    persistReducer,
    persistStore,
    createTransform,
    FLUSH,
    REHYDRATE,
    PAUSE,
    PERSIST,
    PURGE,
    REGISTER
} from 'redux-persist';
import CryptoJS from 'crypto-js';

// Import all reducers
import authReducer from './redux-user/authSlice';
import managementReducer from './redux-lms/user/managementSlice';
import unsuccessfulLeavesReducer from './redux-lms/unsuccessful-leaves/unsuccessfulLeavesSlice';
import unauthorizedLeavesSlice from './redux-lms/unauthorized-leaves/unauthorizedLeavesSlice';
import employeeActivitiesReducer from './redux-lms/employee-activities/employeeActivitiesSlice';
import activityRecordsReducer from './redux-lms/employee-activities/admin/employeeActivitiesSlice';
import noPay from './redux-lms/no-pay/noPaySlice';
import movementReducer from './redux-lms/movement/admin/movementSlice';
import movementReducerNo from './redux-lms/movement/movementSlice';
import movementRequestReducer from './redux-lms/movement/req/movementRequestSlice';
import leaveReducer from './redux-lms/leave/admin/leaveSlice';
import attendanceReducer from './redux-lms/in-outs/attendanceSlice';
import leaveApplicationReducer from './redux-lms/leave/apply/leaveSlice';
import leaveReducerNo from './redux-lms/leave/leaveSlice';
import rosterReducer from './redux-roster/rosterSlice';
import attendanceRosterReducer from './redux-roster/attendanceSlice';
import rosterManagementReducer from './redux-roster/rosterManagementSlice';

// Import middleware
import { authMiddleware, apiErrorMiddleware } from "./middleware/middleware";
import { encryptionMiddleware } from './middleware/encryptionMiddleware';
import { encryptionTransform } from './persistTransforms';
import { persistTransform } from './sanitizeState';
import asyncStorage from './asyncStorage';

// Enhanced encryption transform for better security
const enhancedEncryptionTransform = createTransform(
    // State to storage (encrypt)
    (inboundState, key) => {
        if (!inboundState) return inboundState;

        // Use environment variable for key or fallback for development
        const secretKey = process.env.NEXT_PUBLIC_REDUX_SECRET_KEY || 'fallback-key-for-dev-only';

        try {
            // Convert state to string for encryption
            const stringState = JSON.stringify(inboundState);

            // Add timestamp for freshness check
            const stateWithTimestamp = JSON.stringify({
                state: inboundState,
                timestamp: Date.now()
            });

            // Encrypt with AES
            return CryptoJS.AES.encrypt(stateWithTimestamp, secretKey).toString();
        } catch (error) {
            console.error('Encryption error:', error);
            return null; // Prevent storage of unencrypted data
        }
    },

    // Storage to state (decrypt)
    (outboundState, key) => {
        if (!outboundState) return outboundState;

        const secretKey = process.env.NEXT_PUBLIC_REDUX_SECRET_KEY || 'fallback-key-for-dev-only';

        try {
            // Decrypt the state
            const decrypted = CryptoJS.AES.decrypt(outboundState, secretKey);
            const stringState = decrypted.toString(CryptoJS.enc.Utf8);

            // Parse the state with timestamp
            const parsedData = JSON.parse(stringState);

            // Check if the state is too old (optional, 24 hours here)
            const MAX_AGE = 24 * 60 * 60 * 1000; // 24 hours
            if (Date.now() - parsedData.timestamp > MAX_AGE) {
                console.warn('Persisted state expired');
                return null; // Return null to use initial state
            }

            return parsedData.state;
        } catch (error) {
            console.error('Decryption error:', error);
            // If decryption fails, state might be tampered with
            return null; // Use initial state instead
        }
    },

    // Specify which reducers to apply this transform to
    { whitelist: ['auth'] }
);

// State validation transform to prevent injection attacks
const validationTransform = createTransform(
    // No changes when saving state
    (inboundState) => inboundState,

    // Validate when loading state
    (outboundState, key) => {
        // Skip validation for null states
        if (!outboundState) return outboundState;

        // Specific validation for auth state
        if (key === 'auth') {
            // Check for required properties in auth state
            const isValid =
                typeof outboundState === 'object' &&
                outboundState !== null &&
                outboundState.hasOwnProperty('userDetails') &&
                outboundState.hasOwnProperty('token') &&
                typeof outboundState.token === 'string';

            if (!isValid) {
                console.warn('Invalid auth state structure detected');
                return null; // Return null to use initial state
            }

            // Validate token format (example - adjust to your token format)
            const tokenRegex = /^[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*$/;
            if (!tokenRegex.test(outboundState.token)) {
                console.warn('Invalid token format detected');
                return null;
            }
        }

        return outboundState;
    },

    // Apply to all whitelisted reducers
    { whitelist: ['auth'] }
);

// Utility function to calculate state integrity hash
function calculateIntegrity(state) {
    if (!state) return '';

    // Create a deterministic string from state, excluding _integrity field
    const stateForHash = {...state};
    delete stateForHash._integrity;

    return CryptoJS.SHA256(JSON.stringify(stateForHash)).toString();
}

// Integrity check transform
const integrityTransform = createTransform(
    // Add integrity hash when saving
    (inboundState, key) => {
        if (!inboundState || key !== 'auth') return inboundState;

        // Add integrity hash to the state
        return {
            ...inboundState,
            _integrity: calculateIntegrity(inboundState)
        };
    },

    // Verify integrity hash when loading
    (outboundState, key) => {
        if (!outboundState || key !== 'auth' || !outboundState._integrity) return outboundState;

        // Calculate hash of current state
        const calculatedHash = calculateIntegrity(outboundState);

        // Compare with stored hash
        if (calculatedHash !== outboundState._integrity) {
            console.warn('State integrity check failed - possible tampering detected');
            return null; // Return null to use initial state
        }

        // Remove integrity hash before returning
        const cleanState = {...outboundState};
        delete cleanState._integrity;
        return cleanState;
    },

    // Apply only to auth
    { whitelist: ['auth'] }
);

const rootReducer = combineReducers({
    auth: authReducer,
    management: managementReducer,
    unsuccessfulLeaves: unsuccessfulLeavesReducer,
    unauthorizedLeaves: unauthorizedLeavesSlice,
    employeeActivities: employeeActivitiesReducer,
    activityRecords: activityRecordsReducer,
    noPay: noPay,
    movement: movementReducer,
    movementRequest: movementRequestReducer,
    leave: leaveReducer,
    leaveNo: leaveReducerNo,
    attendance: attendanceReducer,
    leaveApplication: leaveApplicationReducer,
    movementNo: movementReducerNo,
    roster: rosterReducer,
    rosterManagement: rosterManagementReducer,
    attendanceRoster: attendanceRosterReducer
});

// Security middleware
const securityMiddleware = store => next => action => {
    // Handle REHYDRATE action for security checks
    if (action.type === REHYDRATE && action.key === 'auth' && action.payload) {
        const authState = action.payload;

        // Check expiration if exists
        if (authState._expires && Date.now() > authState._expires) {
            console.warn('Stored authentication expired');
            // Override payload with null to trigger initial state
            action.payload = null;
        }
    }

    return next(action);
};

// Configure persistence with enhanced security
const persistConfig = {
    key: 'root-v1.0.3', // Version your storage - update on breaking changes
    storage: asyncStorage,
    whitelist: ['auth'], // Only persist auth state
    transforms: [
        validationTransform,      // Validate state structure
        integrityTransform,       // Check for tampering
        persistTransform,         // Your existing sanitization
        enhancedEncryptionTransform || encryptionTransform // Use enhanced encryption if available
    ],
    writeFailHandler: (err) => {
        console.warn('Redux persist write failure:', err);
        // Optional: notify user of storage issues
    },
    // Custom state reconciler
    stateReconciler: (inboundState, originalState, reducedState, config) => {
        // Custom reconciliation logic for additional security
        const safeState = {...reducedState};

        // Only merge whitelisted reducers from persisted state
        if (inboundState && config.whitelist) {
            config.whitelist.forEach(key => {
                if (inboundState[key] !== undefined) {
                    safeState[key] = inboundState[key];
                }
            });
        }

        return safeState;
    },
    // Prevent persisting during SSR
    timeout: typeof window !== 'undefined' ? 2000 : 0,
    // Debug mode in development only
    debug: process.env.NODE_ENV !== 'production' && process.env.REDUX_PERSIST_DEBUG === 'true'
};

// Special handling for server-side rendering
const createPersistedReducer = () => {
    if (typeof window === 'undefined') {
        // On server, don't use persistence
        return rootReducer;
    }

    // On client, use persisted reducer
    return persistReducer(persistConfig, rootReducer);
};

// Configure the store with security middleware
export const store = configureStore({
    reducer: createPersistedReducer(),
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: {
                ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
                ignoredPaths: ['auth.userDetails.addresses'],
            },
            // Add immutability checks
            immutableCheck: {
                warnAfter: 300,
                // Ignore certain paths that might contain non-serializable data
                ignoredPaths: ['auth.userDetails.someSpecialField']
            },
        }).concat(
            authMiddleware,
            apiErrorMiddleware,
            encryptionMiddleware,
            securityMiddleware
        ),
    // Disable devTools in production for security
    devTools: process.env.NODE_ENV !== 'production',
});

// Create persistor with SSR check
export const persistor = typeof window !== 'undefined' ? persistStore(store) : null;

// Utility function to securely logout and clear persisted state
export const secureLogout = () => {
    if (typeof window !== 'undefined' && persistor) {
        // First dispatch logout action to clear memory state
        store.dispatch({ type: 'auth/logout' });

        // Then purge the persisted state
        persistor.purge();

        // Force reload of the application to clear any in-memory state
        // Comment this out if you prefer to handle this differently
        // window.location.reload();
    }
};

// Optional: Add state migration support for handling breaking changes
export const handleStateMigration = () => {
    if (typeof window !== 'undefined') {
        // Example: detect old version and migrate or purge
        const oldVersion = localStorage.getItem('redux-version');
        const currentVersion = '1.0.3'; // Should match key version in persistConfig

        if (oldVersion && oldVersion !== currentVersion) {
            console.log('Redux state version changed, migrating...');
            // Option 1: Purge for clean slate
            // persistor.purge();

            // Option 2: Perform migration logic here
            // ...

            // Update version
            localStorage.setItem('redux-version', currentVersion);
        } else if (!oldVersion) {
            localStorage.setItem('redux-version', currentVersion);
        }
    }
};

// Initialize storage version
if (typeof window !== 'undefined') {
    handleStateMigration();
}

// Export the config for any components that need to know persistence status
export const isPersistenceEnabled = () =>
    typeof window !== 'undefined' &&
    persistConfig.whitelist.length > 0;


if (typeof window !== 'undefined') {
    window.__REDUX_DEVTOOLS_EXTENSION__ = () => function(createStore) {
        return function(...args) {
            const realStore = createStore(...args);
            // Return real store methods but with filtered/fake data
            return {
                ...realStore,
                // Only dispatch in the real store
                dispatch: realStore.dispatch,
                // Return dummy state to DevTools
                getState: () => ({
                    notice: "State viewing is disabled in production",
                    // Return minimal mock state structure
                    auth: { isAuthenticated: true },
                    app: { initialized: true }
                })
            };
        };
    };
}