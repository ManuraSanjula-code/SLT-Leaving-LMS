import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true,
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response) {
        switch (error.response.status) {
          case 401:
            console.error('Unauthorized - redirecting to login');
            // Clear all auth data
            sessionStorage.clear();
            window.location.href = '/login';
            break;
          case 403:
            console.error('Forbidden - redirecting to unauthorized');
            window.location.href = '/unauthorized';
            break;
          default:
            // Sanitize error message
            const safeErrorMessage = 'API Error: ' + (error.response.status || 'Unknown');
            console.error(safeErrorMessage);
        }
      } else {
        console.error('Network Error:', error.message);
      }
      return Promise.reject(error);
    }
);

// Add request interceptor for security headers
apiClient.interceptors.request.use(
    (config) => {
      // Add security headers
      config.headers = {
        ...config.headers,
        'X-Content-Type-Options': 'nosniff',
        'X-Frame-Options': 'DENY',
      };

      // Add CSRF token if available
      const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken;
      }

      return config;
    },
    (error) => Promise.reject(error)
);

export const fetchData = async (endpoint, jwt) => {
  try {
    if (!endpoint || typeof endpoint !== 'string') {
      throw new Error('Invalid endpoint');
    }

    // Sanitize endpoint to prevent path traversal
    const sanitizedEndpoint = endpoint.replace(/\.\./g, '');

    const response = await apiClient.get(sanitizedEndpoint, {
      headers: jwt ? { Authorization: `Bearer ${jwt}` } : {},
    });

    // Validate response data
    if (!response.data) {
      throw new Error('Empty response received');
    }

    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to fetch data');
  }
};

export const fetchData_ = async (userId, jwt) => {
  try {
    if (!userId || !jwt) {
      throw new Error('Missing userId or JWT');
    }

    // Validate userId to prevent injection
    if (!/^\d+$/.test(userId)) {
      throw new Error('Invalid userId format');
    }

    const response = await apiClient.get(`/users/${userId}`, {
      headers: { Authorization: `Bearer ${jwt}` },
    });

    // Validate response structure
    if (!response.data || !response.data.roles) {
      throw new Error('Invalid user data response');
    }

    return response.data;
  } catch (error) {
    console.error('User details fetch failed:', error);
    throw error;
  }
};

export const putUserData = async (endpoint, payload, jwt) => {
  try {
    if (!endpoint || typeof endpoint !== 'string') {
      throw new Error('Invalid endpoint');
    }

    // Sanitize endpoint
    const sanitizedEndpoint = endpoint.replace(/\.\./g, '');

    // Validate payload
    if (!payload || typeof payload !== 'object') {
      throw new Error('Invalid payload');
    }

    const response = await apiClient.put(sanitizedEndpoint, payload, {
      headers: {
        'Content-Type': 'application/json',
        ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
      },
    });

    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to update user data');
  }
};

export const putUserProfile = async (endpoint, file, jwt) => {
  try {
    if (!endpoint || !file) {
      throw new Error('Missing endpoint or file');
    }

    // Validate file type
    const validImageTypes = ['image/jpeg', 'image/png', 'image/gif'];
    if (!validImageTypes.includes(file.type)) {
      throw new Error('Invalid file type. Only JPEG, PNG, and GIF are allowed.');
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      throw new Error('File too large. Maximum size is 5MB.');
    }

    const formData = new FormData();
    formData.append('image', file);

    const response = await apiClient.put(endpoint, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
      },
    });

    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to upload profile picture');
  }
};
