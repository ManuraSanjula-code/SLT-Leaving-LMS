import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // Replace with your API URL
});

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error("Unauthorized - redirecting to login");
          localStorage.clear();
          window.location.href = '/login';
          break;
        case 403:
          console.error("Forbidden - redirecting to unauthorized");
          window.location.href = '/unauthorized';
          break;
        default:
          console.error("API Error:", error.response.data);
      }
    } else {
      console.error("Network Error:", error.message);
    }
    return Promise.reject(error);
  }
);

export const fetchData = async (endpoint, jwt) => {
  try {
    const response = await apiClient.get(endpoint, {
      headers: { Authorization: `Bearer ${jwt}` },
      withCredentials: true
    });
    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to fetch data');
  }
};


export const fetchData_ = async (userId, jwt) => {
  try {
    const response = await apiClient.get(`/users/${userId}`);
    
    // Validate response structure
    if (!response.data || !response.data.roles) {
      throw new Error("Invalid user data response");
    }
    
    return response.data;
  } catch (error) {
    console.error("User details fetch failed:", error);
    throw error;
  }
};


export const putUserData = async (endpoint, jwt, payload) => {
  try {
    const response = await apiClient.put(endpoint, payload, {
      headers: {
        'Authorization': `Bearer ${jwt}`,
        'Content-Type': 'application/json', // Ensure JSON data format
      },
    });
    return response.data;
  } catch (error) {
    console.error('API Request Failed:', error.response?.data || error.message);
    throw new Error('Failed to update user data');
  }
};

export const putUserProfile = async (endpoint, jwt, file) => {
  try {
    // Create FormData object
    const formData = new FormData();
    formData.append('image', file); // 'image' is the key expected by server

    const response = await apiClient.put(endpoint, formData, {
      headers: {
        'Authorization': `Bearer ${jwt}`,
        // Let the browser set the correct Content-Type with boundary
        // Don't set Content-Type manually when using FormData
      },
    });
    return response.data;
  } catch (error) {
    console.error('Upload Failed:', error.response?.data || error.message);
    throw new Error('Failed to upload image');
  }
};