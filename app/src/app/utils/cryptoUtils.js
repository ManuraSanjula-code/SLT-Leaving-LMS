// app/utils/cryptoUtils.js
const CRYPTO_KEY = 'redux-secure-key-v1';
const SALT = 'your-app-specific-salt';

// Generate a crypto key from a password
async function generateKey(password) {
    try {
        if (typeof window === 'undefined') return null; // Server-side check

        const encoder = new TextEncoder();
        const keyMaterial = await window.crypto.subtle.importKey(
            'raw',
            encoder.encode(password),
            { name: 'PBKDF2' },
            false,
            ['deriveBits', 'deriveKey']
        );

        return window.crypto.subtle.deriveKey(
            {
                name: 'PBKDF2',
                salt: encoder.encode(SALT),
                iterations: 100000,
                hash: 'SHA-256'
            },
            keyMaterial,
            { name: 'AES-GCM', length: 256 },
            false,
            ['encrypt', 'decrypt']
        );
    } catch (error) {
        console.error('Key generation error:', error);
        return null;
    }
}

// Get encryption key
async function getEncryptionKey() {
    // In production, use a more secure method to get/store this password
    const password = 'your-secure-app-password';
    return generateKey(password);
}

// Encrypt data
export async function encryptData(data) {
    try {
        if (typeof window === 'undefined') return null; // Server-side check

        const key = await getEncryptionKey();
        if (!key) return JSON.stringify(data); // Fallback if key generation fails

        const encoder = new TextEncoder();
        const encoded = encoder.encode(JSON.stringify(data));

        // Generate a random initialization vector
        const iv = window.crypto.getRandomValues(new Uint8Array(12));

        const encrypted = await window.crypto.subtle.encrypt(
            { name: 'AES-GCM', iv },
            key,
            encoded
        );

        // Combine IV and encrypted data
        const combinedData = new Uint8Array(iv.length + encrypted.byteLength);
        combinedData.set(iv, 0);
        combinedData.set(new Uint8Array(encrypted), iv.length);

        // Convert to base64 string for storage
        return btoa(String.fromCharCode.apply(null, new Uint8Array(combinedData)));
    } catch (error) {
        console.error('Encryption error:', error);
        // Fallback to JSON string
        return JSON.stringify(data);
    }
}

// Decrypt data
export async function decryptData(encryptedData) {
    try {
        if (typeof window === 'undefined' || !encryptedData) return null;

        const key = await getEncryptionKey();
        if (!key) return null;

        // Convert from base64 to array buffer
        const binary = atob(encryptedData);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }

        // Extract IV (first 12 bytes)
        const iv = bytes.slice(0, 12);
        const data = bytes.slice(12);

        const decrypted = await window.crypto.subtle.decrypt(
            { name: 'AES-GCM', iv },
            key,
            data
        );

        const decoder = new TextDecoder();
        return JSON.parse(decoder.decode(decrypted));
    } catch (error) {
        console.error('Decryption error:', error);
        // Try to parse as JSON in case it wasn't encrypted
        try {
            return JSON.parse(encryptedData);
        } catch (e) {
            return null;
        }
    }
}