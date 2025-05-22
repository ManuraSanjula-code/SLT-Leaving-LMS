// persistTransforms.js
import { createTransform } from 'redux-persist';
import { encryptData, decryptData } from './utils/encryption';

// List of state slices that should be encrypted
const ENCRYPTED_SLICES = ['auth', 'management']; // Add any other sensitive slices

// Create the encryption transform
export const encryptionTransform = createTransform(
    // Transform state on its way to being serialized and persisted
    (inboundState, key) => {
        // Only encrypt slices in our list
        if (ENCRYPTED_SLICES.includes(key)) {
            return encryptData(inboundState);
        }
        return inboundState;
    },
    // Transform state being rehydrated (coming back from storage)
    (outboundState, key) => {
        if (ENCRYPTED_SLICES.includes(key)) {
            return decryptData(outboundState);
        }
        return outboundState;
    },
    // Configuration to specify which reducers this transform gets applied to
    { whitelist: ENCRYPTED_SLICES }
);