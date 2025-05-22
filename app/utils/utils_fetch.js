export default async function fetchWithHeaders(url, options = {}) {
    const userId = sessionStorage.getItem('userId');
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'X-User-ID': userId || '', // Replace with your server header
    };

    const finalOptions = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...(options.headers || {}),
        },
    };

    return fetch(url, finalOptions);
}