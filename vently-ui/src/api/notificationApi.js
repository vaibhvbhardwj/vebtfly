import axios from './axios';

const API_BASE = '/api/v1/notifications';

export const notificationApi = {
  // Fetch notifications with pagination
  getNotifications: async (page = 1, pageSize = 20) => {
    const response = await axios.get(API_BASE, {
      params: {
        page: page - 1,
        size: pageSize,
      },
    });
    return response.data;
  },

  // Get unread count
  getUnreadCount: async () => {
    const response = await axios.get(`${API_BASE}/unread-count`);
    return response.data;
  },

  // Mark notification as read
  markAsRead: async (notificationId) => {
    const response = await axios.put(`${API_BASE}/${notificationId}/read`);
    return response.data;
  },

  // Mark all as read
  markAllAsRead: async () => {
    const response = await axios.put(`${API_BASE}/read-all`);
    return response.data;
  },

  // Delete notification
  deleteNotification: async (notificationId) => {
    const response = await axios.delete(`${API_BASE}/${notificationId}`);
    return response.data;
  },

  // Get notification preferences
  getPreferences: async () => {
    const response = await axios.get(`${API_BASE}/preferences`);
    return response.data;
  },

  // Update notification preferences
  updatePreferences: async (preferences) => {
    const response = await axios.put(`${API_BASE}/preferences`, preferences);
    return response.data;
  },
};
