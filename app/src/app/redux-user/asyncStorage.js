// app/redux-user/asyncStorage.js
import { encryptData, decryptData } from '../utils/cryptoUtils';

// Create a custom storage engine that works with redux-persist and handles encryption
class AsyncCryptoStorage {
    constructor() {
        // Make sure we're in a browser environment
        this.isClient = typeof window !== 'undefined';
        this.storage = this.isClient ? window.sessionStorage : null;
    }

    getItem(key) {
        return new Promise((resolve) => {
            if (!this.isClient) {
                resolve(null);
                return;
            }

            const item = this.storage.getItem(key);

            if (!item) {
                resolve(null);
                return;
            }

            // Check if this looks like our encrypted data before trying to decrypt
            try {
                // Simple check for Base64 content that could be our encrypted data
                if (item && typeof item === 'string' &&
                    (item.startsWith('eyJ') ||
                        item.includes('+') ||
                        item.includes('/'))) {
                    // Try to decrypt
                    decryptData(item)
                        .then(decrypted => resolve(decrypted))
                        .catch(err => {
                            console.error('Decryption failed:', err);
                            resolve(item); // Return original if decryption fails
                        });
                } else {
                    resolve(item); // Not encrypted, return as is
                }
            } catch (error) {
                console.error('Error in getItem:', error);
                resolve(item); // Return original on error
            }
        });
    }

    setItem(key, value) {
        return new Promise((resolve) => {
            if (!this.isClient) {
                resolve();
                return;
            }

            // Only encrypt objects
            if (value && typeof value === 'object') {
                encryptData(value)
                    .then(encrypted => {
                        this.storage.setItem(key, encrypted);
                        resolve();
                    })
                    .catch(err => {
                        console.error('Encryption failed:', err);
                        // Fall back to storing as JSON
                        try {
                            this.storage.setItem(key, JSON.stringify(value));
                        } catch (e) {
                            console.error('JSON stringify failed:', e);
                        }
                        resolve();
                    });
            } else {
                // Store primitive values directly
                try {
                    this.storage.setItem(key, value);
                } catch (e) {
                    console.error('Storage setItem failed:', e);
                }
                resolve();
            }
        });
    }

    removeItem(key) {
        return new Promise((resolve) => {
            if (this.isClient) {
                this.storage.removeItem(key);
            }
            resolve();
        });
    }
}

export default new AsyncCryptoStorage();