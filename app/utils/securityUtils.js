export const sanitizeHtml = (content) => {
    if (!content || typeof content !== 'string') return '';

    // Remove all HTML tags
    return content.replace(/<[^>]*>/g, '');
};

export const validateEmail = (email) => {
    if (!email || typeof email !== 'string') return false;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

export const sanitizeUrl = (url) => {
    if (!url || typeof url !== 'string') return '';

    // Allow only http/https URLs or relative paths
    if (!/^(https?:\/\/|\/)/i.test(url)) {
        return '';
    }

    // Remove any script or data URLs
    if (/^(javascript|data):/i.test(url)) {
        return '';
    }

    return url;
};

export const encryptForStorage = (data) => {
    // This is a placeholder for a proper encryption implementation
    // In a real app, use a proper encryption library
    try {
        const serialized = JSON.stringify(data);
        // Basic encoding (not encryption) for demonstration
        // In production, use a proper encryption approach
        return btoa(serialized);
    } catch (error) {
        console.error('Encryption error:', error);
        return null;
    }
};

export const decryptFromStorage = (encrypted) => {
    // This is a placeholder for a proper decryption implementation
    // In a real app, use a proper encryption library
    try {
        if (!encrypted) return null;
        // Basic decoding (not decryption) for demonstration
        const serialized = atob(encrypted);
        return JSON.parse(serialized);
    } catch (error) {
        console.error('Decryption error:', error);
        return null;
    }
};
