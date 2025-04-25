// app/redux-user/store.js
import { configureStore, combineReducers } from '@reduxjs/toolkit';
import { persistReducer, persistStore, createTransform, FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER } from 'redux-persist';

import authReducer from './authSlice';
import managementReducer from '../redux-lms/managementSlice';
import { persistTransform } from './sanitizeState';
import asyncStorage from './asyncStorage';

const rootReducer = combineReducers({
    auth: authReducer,
    management: managementReducer,
});

const persistConfig = {
    key: 'root',
    storage: asyncStorage,
    whitelist: ['auth'],
    transforms: [createTransform(persistTransform.in, persistTransform.out)],
    // Add this to improve error handling
    writeFailHandler: (err) => {
        console.warn('Redux persist write failure:', err);
    },
};

// Create a function to handle server/client context
const createPersistedReducer = () => {
    // Check if we're on client or server
    if (typeof window === 'undefined') {
        // On server, return regular reducer without persistence
        return rootReducer;
    }
    // On client, return persisted reducer
    return persistReducer(persistConfig, rootReducer);
};

// Create store with conditionally persisted reducer
export const store = configureStore({
    reducer: createPersistedReducer(),
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: {
                // Ignore these actions in serializable check
                ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
                // Ignore these paths in serializable check
                ignoredPaths: ['auth.userDetails.addresses'],
            },
        }),
});

// Only create persistor on client side
export const persistor = typeof window !== 'undefined' ? persistStore(store) : null;