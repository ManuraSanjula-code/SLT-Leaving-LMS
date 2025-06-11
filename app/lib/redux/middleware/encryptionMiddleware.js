import { encryptData, decryptData, hashData } from '../utils/encryption';

export const encryptionMiddleware = store => next => action => {
    const result = next(action);

    const state = store.getState();

    if (state.auth && state.auth.userDetails) {
        const dataHash = hashData(state.auth.userDetails);

        if (!state.auth.integrityHash || state.auth.integrityHash !== dataHash) {
            store.dispatch({
                type: 'auth/setIntegrityHash',
                payload: dataHash
            });
        }
    }

    return result;
};