import { encryptData, decryptData } from '../../utils/cryptoUtils';

class AsyncCryptoStorage {
    constructor() {
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

            try {
                if (item && typeof item === 'string' &&
                    (item.startsWith('eyJ') ||
                        item.includes('+') ||
                        item.includes('/'))) {
                    // Try to decrypt
                    decryptData(item)
                        .then(decrypted => resolve(decrypted))
                        .catch(err => {
                            console.error('Decryption failed:', err);
                            resolve(item);
                        });
                } else {
                    resolve(item);
                }
            } catch (error) {
                console.error('Error in getItem:', error);
                resolve(item);
            }
        });
    }

    setItem(key, value) {
        return new Promise((resolve) => {
            if (!this.isClient) {
                resolve();
                return;
            }

            if (value && typeof value === 'object') {
                encryptData(value)
                    .then(encrypted => {
                        this.storage.setItem(key, encrypted);
                        resolve();
                    })
                    .catch(err => {
                        console.error('Encryption failed:', err);
                        try {
                            this.storage.setItem(key, JSON.stringify(value));
                        } catch (e) {
                            console.error('JSON stringify failed:', e);
                        }
                        resolve();
                    });
            } else {
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