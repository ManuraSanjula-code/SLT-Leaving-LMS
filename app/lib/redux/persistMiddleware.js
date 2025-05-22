import {decryptFromStorage} from '../../utils/securityUtils';
import { encryptData, decryptData } from '../../utils/cryptoUtils';

export const secureTransform = {
    in: async (state, key) => {
        // Only encrypt auth data
        if (key === 'auth') {
            // Sanitize state before encryption
            const sanitizedState = { ...state };

            // Don't store sensitive data
            if (sanitizedState.userDetails) {
                // Remove or mask sensitive fields
                sanitizedState.userDetails = {
                    ...sanitizedState.userDetails,
                    // Remove sensitive data or replace with masked versions
                };
            }

            // Encrypt the sanitized state
            return await encryptData(sanitizedState);
        }
        return state;
    },
    out: async (state, key) => {
        // Decrypt auth data from storage
        if (key === 'auth' && state) {
            try {
                return await decryptData(state) || state;
            } catch (error) {
                console.error('Failed to decrypt auth state', error);
                return null;
            }
        }
        return state;
    }
};