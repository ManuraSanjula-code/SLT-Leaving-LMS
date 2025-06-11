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
import absentEmployeesReducer from './redux-lms/absent/absentEmployeesSlice'

import { authMiddleware, apiErrorMiddleware } from "./middleware/middleware";
import { encryptionMiddleware } from './middleware/encryptionMiddleware';
import { encryptionTransform } from './persistTransforms';
import { persistTransform } from './sanitizeState';
import asyncStorage from './asyncStorage';

const enhancedEncryptionTransform = createTransform(
    (inboundState, key) => {
        if (!inboundState) return inboundState;

        const secretKey = process.env.NEXT_PUBLIC_REDUX_SECRET_KEY || 'fallback-key-for-dev-only';

        try {
            const stringState = JSON.stringify(inboundState);

            const stateWithTimestamp = JSON.stringify({
                state: inboundState,
                timestamp: Date.now()
            });

            return CryptoJS.AES.encrypt(stateWithTimestamp, secretKey).toString();
        } catch (error) {
            console.error('Encryption error:', error);
            return null;
        }
    },

    (outboundState, key) => {
        if (!outboundState) return outboundState;

        const secretKey = process.env.NEXT_PUBLIC_REDUX_SECRET_KEY || 'fallback-key-for-dev-only';

        try {
            const decrypted = CryptoJS.AES.decrypt(outboundState, secretKey);
            const stringState = decrypted.toString(CryptoJS.enc.Utf8);

            const parsedData = JSON.parse(stringState);

            const MAX_AGE = 24 * 60 * 60 * 1000;
            if (Date.now() - parsedData.timestamp > MAX_AGE) {
                console.warn('Persisted state expired');
                return null;
            }

            return parsedData.state;
        } catch (error) {
            console.error('Decryption error:', error);
            return null;
        }
    },

    { whitelist: ['auth'] }
);

const validationTransform = createTransform(
    (inboundState) => inboundState,

    (outboundState, key) => {
        if (!outboundState) return outboundState;

        if (key === 'auth') {
            const isValid =
                typeof outboundState === 'object' &&
                outboundState !== null &&
                outboundState.hasOwnProperty('userDetails') &&
                outboundState.hasOwnProperty('token') &&
                typeof outboundState.token === 'string';

            if (!isValid) {
                console.warn('Invalid auth state structure detected');
                return null;
            }

            const tokenRegex = /^[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*$/;
            if (!tokenRegex.test(outboundState.token)) {
                console.warn('Invalid token format detected');
                return null;
            }
        }

        return outboundState;
    },

    { whitelist: ['auth'] }
);

function calculateIntegrity(state) {
    if (!state) return '';

    const stateForHash = {...state};
    delete stateForHash._integrity;

    return CryptoJS.SHA256(JSON.stringify(stateForHash)).toString();
}

// Integrity check transform
const integrityTransform = createTransform(
    (inboundState, key) => {
        if (!inboundState || key !== 'auth') return inboundState;

        return {
            ...inboundState,
            _integrity: calculateIntegrity(inboundState)
        };
    },

    (outboundState, key) => {
        if (!outboundState || key !== 'auth' || !outboundState._integrity) return outboundState;

        const calculatedHash = calculateIntegrity(outboundState);

        if (calculatedHash !== outboundState._integrity) {
            console.warn('State integrity check failed - possible tampering detected');
            return null;
        }

        const cleanState = {...outboundState};
        delete cleanState._integrity;
        return cleanState;
    },

    { whitelist: ['auth'] }
);

const rootReducer = combineReducers({
    auth: authReducer,
    management: managementReducer,
    unsuccessfulLeaves: unsuccessfulLeavesReducer,
    unauthorizedLeaves: unauthorizedLeavesSlice,
    employeeActivities: employeeActivitiesReducer,
    activityRecords: activityRecordsReducer,
    absentEmployees: absentEmployeesReducer,
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

const securityMiddleware = store => next => action => {
    if (action.type === REHYDRATE && action.key === 'auth' && action.payload) {
        const authState = action.payload;

        if (authState._expires && Date.now() > authState._expires) {
            console.warn('Stored authentication expired');
            // Override payload with null to trigger initial state
            action.payload = null;
        }
    }

    return next(action);
};

const persistConfig = {
    key: 'root-v1.0.3',
    storage: asyncStorage,
    whitelist: ['auth'],
    transforms: [
        validationTransform,
        integrityTransform,
        persistTransform,
        enhancedEncryptionTransform || encryptionTransform
    ],
    writeFailHandler: (err) => {
        console.warn('Redux persist write failure:', err);
    },
    stateReconciler: (inboundState, originalState, reducedState, config) => {
        const safeState = {...reducedState};

        if (inboundState && config.whitelist) {
            config.whitelist.forEach(key => {
                if (inboundState[key] !== undefined) {
                    safeState[key] = inboundState[key];
                }
            });
        }

        return safeState;
    },
    timeout: typeof window !== 'undefined' ? 2000 : 0,
    debug: process.env.NODE_ENV !== 'production' && process.env.REDUX_PERSIST_DEBUG === 'true'
};

const createPersistedReducer = () => {
    if (typeof window === 'undefined') {
        return rootReducer;
    }
    return persistReducer(persistConfig, rootReducer);
};

export const store = configureStore({
    reducer: createPersistedReducer(),
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: {
                ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
                ignoredPaths: ['auth.userDetails.addresses'],
            },
            immutableCheck: {
                warnAfter: 300,
                ignoredPaths: ['auth.userDetails.someSpecialField']
            },
        }).concat(
            authMiddleware,
            apiErrorMiddleware,
            encryptionMiddleware,
            securityMiddleware
        ),
    devTools: process.env.NODE_ENV !== 'production',
});

export const persistor = typeof window !== 'undefined' ? persistStore(store) : null;

export const secureLogout = () => {
    if (typeof window !== 'undefined' && persistor) {
        store.dispatch({ type: 'auth/logout' });
        persistor.purge();
    }
};

export const handleStateMigration = () => {
    if (typeof window !== 'undefined') {
        const oldVersion = localStorage.getItem('redux-version');
        const currentVersion = '1.0.3'; // Should match key version in persistConfig

        if (oldVersion && oldVersion !== currentVersion) {
            console.log('Redux state version changed, migrating...');
            localStorage.setItem('redux-version', currentVersion);
        } else if (!oldVersion) {
            localStorage.setItem('redux-version', currentVersion);
        }
    }
};

if (typeof window !== 'undefined') {
    handleStateMigration();
}

export const isPersistenceEnabled = () =>
    typeof window !== 'undefined' &&
    persistConfig.whitelist.length > 0;


if (typeof window !== 'undefined') {
    window.__REDUX_DEVTOOLS_EXTENSION__ = () => function(createStore) {
        return function(...args) {
            const realStore = createStore(...args);
            return {
                ...realStore,
                dispatch: realStore.dispatch,
                getState: () => ({
                    notice: "State viewing is disabled in production",
                    auth: { isAuthenticated: true },
                    app: { initialized: true }
                })
            };
        };
    };
}