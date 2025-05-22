// middleware/encryptionMiddleware.js
import { encryptData, decryptData, hashData } from '../utils/encryption';

export const encryptionMiddleware = store => next => action => {
    // Process the action normally first
    const result = next(action);

    // Get updated state after the action
    const state = store.getState();

    // Handle additional security measures beyond what redux-persist handles
    // For example, you could log security events or perform integrity checks

    // Example: Generate integrity hash for sensitive data
    if (state.auth && state.auth.userDetails) {
        // Add a hash to verify data hasn't been tampered with
        // (This is separate from persistence encryption)
        const dataHash = hashData(state.auth.userDetails);

        // We could dispatch an action to store this hash
        // This is just an example - integrate as needed with your auth slice
        if (!state.auth.integrityHash || state.auth.integrityHash !== dataHash) {
            store.dispatch({
                type: 'auth/setIntegrityHash',
                payload: dataHash
            });
        }
    }

    return result;
};