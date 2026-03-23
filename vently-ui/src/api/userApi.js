import axios from './axios';

const API_BASE = '/api/v1/users';

export const userApi = {
  // Get current user profile
  getProfile: async () => {
    const response = await axios.get(`${API_BASE}/profile`);
    return response.data;
  },

  // Get user by ID
  getUserById: async (userId) => {
    const response = await axios.get(`${API_BASE}/${userId}`);
    return response.data;
  },

  // Update profile
  updateProfile: async (profileData) => {
    const response = await axios.put(`${API_BASE}/profile`, profileData);
    return response.data;
  },

  // Upload profile picture
  uploadProfilePicture: async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axios.post(`${API_BASE}/profile-picture`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Get user statistics
  getUserStatistics: async (userId) => {
    const response = await axios.get(`${API_BASE}/${userId}/statistics`);
    return response.data;
  },
};
