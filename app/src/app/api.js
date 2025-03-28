import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // Replace with your API URL
  withCredentials: true, // Set withCredentials globally for all requests
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response) {
        switch (error.response.status) {
          case 401:
            console.error('Unauthorized - redirecting to login');
            localStorage.clear();
            window.location.href = '/login';
            break;
          case 403:
            console.error('Forbidden - redirecting to unauthorized');
            window.location.href = '/unauthorized';
            break;
          default:
            console.error('API Error:', error.response.data);
        }
      } else {
        console.error('Network Error:', error.message);
      }
      return Promise.reject(error);
    }
);

export const fetchData = async (endpoint, jwt) => {
  try {
    const response = await apiClient.get(endpoint, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to fetch data');
  }
};

export const fetchData_ = async (userId, jwt) => {
  try {
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

export const putUserData = async (endpoint, payload) => {
  try {
    const response = await apiClient.put(endpoint, payload, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to update user data');
  }
};

export const putUserProfile = async (endpoint, file) => {
  try {
    const formData = new FormData();
    formData.append('image', file); // Append the file to FormData

    const response = await apiClient.put(endpoint, formData, {
      headers: {
        'Content-Type': 'multipart/form-data', // Set the correct Content-Type for file uploads
      },
    });
    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to upload profile picture');
  }
};