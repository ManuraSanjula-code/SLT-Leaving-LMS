import CryptoJS from 'crypto-js';

// Get the secret key from environment variables
const SECRET_KEY = process.env.NEXT_PUBLIC_ENCRYPTION_KEY || 'your-secure-fallback-key';

// Encrypt data
export const encryptData = (data) => {
    if (!data) return null;
    try {
        return CryptoJS.AES.encrypt(JSON.stringify(data), SECRET_KEY).toString();
    } catch (error) {
        console.error('Encryption error:', error);
        return data; // Return original data if encryption fails
    }
};

// Decrypt data
export const decryptData = (encryptedData) => {
    if (!encryptedData) return null;

    // If it's not encrypted (for backwards compatibility), return as is
    if (typeof encryptedData === 'object') return encryptedData;

    try {
        const bytes = CryptoJS.AES.decrypt(encryptedData, SECRET_KEY);
        return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
    } catch (error) {
        console.error('Decryption error:', error);
        return null;
    }
};

// Simple hash function for integrity checks
export const hashData = (data) => {
    return CryptoJS.SHA256(JSON.stringify(data) + SECRET_KEY).toString();
};