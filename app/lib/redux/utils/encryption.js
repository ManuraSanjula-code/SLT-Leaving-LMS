import CryptoJS from 'crypto-js';

const SECRET_KEY = process.env.NEXT_PUBLIC_ENCRYPTION_KEY || 'your-secure-fallback-key';

export const encryptData = (data) => {
    if (!data) return null;
    try {
        return CryptoJS.AES.encrypt(JSON.stringify(data), SECRET_KEY).toString();
    } catch (error) {
        console.error('Encryption error:', error);
        return data;
    }
};

export const decryptData = (encryptedData) => {
    if (!encryptedData) return null;

    if (typeof encryptedData === 'object') return encryptedData;

    try {
        const bytes = CryptoJS.AES.decrypt(encryptedData, SECRET_KEY);
        return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
    } catch (error) {
        console.error('Decryption error:', error);
        return null;
    }
};

export const hashData = (data) => {
    return CryptoJS.SHA256(JSON.stringify(data) + SECRET_KEY).toString();
};