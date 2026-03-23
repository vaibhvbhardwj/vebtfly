import axios from './axios';

const API_BASE = '/ratings';

export const ratingApi = {
  // Submit a rating
  submitRating: async (ratingData) => {
    const response = await axios.post(API_BASE, ratingData);
    return response.data;
  },

  // Get ratings for a user
  getUserRatings: async (userId, page = 0, size = 10) => {
    const response = await axios.get(`${API_BASE}/user/${userId}`, {
      params: { page, size },
    });
    return response.data;
  },

  // Get ratings for an event
  getEventRatings: async (eventId, page = 0, size = 10) => {
    const response = await axios.get(`${API_BASE}/event/${eventId}`, {
      params: { page, size },
    });
    return response.data;
  },

  // Get average rating for a user
  getUserAverageRating: async (userId) => {
    const response = await axios.get(`/users/${userId}/average-rating`);
    return response.data;
  },
};
