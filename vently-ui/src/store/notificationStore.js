import { create } from 'zustand';
import api from '../api/axiosConfig';

export const useNotificationStore = create((set, get) => ({
  notifications: [],
  unreadCount: 0,
  loading: false,
  error: null,
  pagination: {
    page: 1,
    pageSize: 20,
    total: 0,
  },

  // Fetch notifications
  fetchNotifications: async (page = 1) => {
    set({ loading: true, error: null });
    try {
      const response = await api.get('/notifications', {
        params: { page: page - 1, size: get().pagination.pageSize },
      });
      const data = response.data;
      set({
        notifications: data.content || [],
        pagination: {
          page,
          pageSize: get().pagination.pageSize,
          total: data.totalElements || 0,
        },
        loading: false,
      });
    } catch (err) {
      set({ error: err.message, loading: false });
    }
  },

  // Fetch unread count
  fetchUnreadCount: async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) return;
      const response = await api.get('/notifications/unread-count');
      set({ unreadCount: response.data?.count || 0 });
    } catch (err) {
      // Silently fail - don't crash the app
    }
  },

  // Mark notification as read
  markAsRead: async (notificationId) => {
    try {
      await api.put(`/notifications/${notificationId}/read`);
      set((state) => ({
        notifications: state.notifications.map((n) =>
          n.id === notificationId ? { ...n, read: true } : n
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      }));
    } catch (err) {
      console.error('Error marking notification as read:', err);
    }
  },

  // Mark all as read
  markAllAsRead: async () => {
    try {
      await api.put('/notifications/read-all');
      set((state) => ({
        notifications: state.notifications.map((n) => ({ ...n, read: true })),
        unreadCount: 0,
      }));
    } catch (err) {
      console.error('Error marking all as read:', err);
    }
  },

  // Delete notification
  deleteNotification: async (notificationId) => {
    try {
      await api.delete(`/notifications/${notificationId}`);
      set((state) => ({
        notifications: state.notifications.filter((n) => n.id !== notificationId),
      }));
    } catch (err) {
      console.error('Error deleting notification:', err);
    }
  },

  // Set pagination
  setPagination: (page) => {
    set((state) => ({
      pagination: { ...state.pagination, page },
    }));
  },

  // Clear error
  clearError: () => {
    set({ error: null });
  },
}));
