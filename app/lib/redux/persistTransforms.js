import { createTransform } from 'redux-persist';
import { encryptData, decryptData } from './utils/encryption';

const ENCRYPTED_SLICES = ['auth', 'management'];

export const encryptionTransform = createTransform(
    (inboundState, key) => {
        if (ENCRYPTED_SLICES.includes(key)) {
            return encryptData(inboundState);
        }
        return inboundState;
    },
    (outboundState, key) => {
        if (ENCRYPTED_SLICES.includes(key)) {
            return decryptData(outboundState);
        }
        return outboundState;
    },
    { whitelist: ENCRYPTED_SLICES }
);