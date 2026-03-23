import { useNotificationStore } from '../store/notificationStore';
import { useCallback, useEffect, useRef } from 'react';

export const useNotifications = (pollInterval = 30000) => {
  const {
    notifications,
    unreadCount,
    loading,
    error,
    pagination,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    setPagination,
    clearError,
  } = useNotificationStore();

  const pollIntervalRef = useRef(null);

  const handleFetchNotifications = useCallback(
    async (page = 1) => {
      try {
        await fetchNotifications(page);
      } catch (err) {
        console.error('Error fetching notifications:', err);
      }
    },
    [fetchNotifications]
  );

  const handleFetchUnreadCount = useCallback(async () => {
    try {
      await fetchUnreadCount();
    } catch (err) {
      console.error('Error fetching unread count:', err);
    }
  }, [fetchUnreadCount]);

  const handleMarkAsRead = useCallback(
    async (notificationId) => {
      try {
        await markAsRead(notificationId);
      } catch (err) {
        console.error('Error marking notification as read:', err);
      }
    },
    [markAsRead]
  );

  const handleMarkAllAsRead = useCallback(async () => {
    try {
      await markAllAsRead();
    } catch (err) {
      console.error('Error marking all as read:', err);
    }
  }, [markAllAsRead]);

  const handleDeleteNotification = useCallback(
    async (notificationId) => {
      try {
        await deleteNotification(notificationId);
      } catch (err) {
        console.error('Error deleting notification:', err);
      }
    },
    [deleteNotification]
  );

  const handleSetPagination = useCallback(
    (page) => {
      setPagination(page);
    },
    [setPagination]
  );

  const handleClearError = useCallback(() => {
    clearError();
  }, [clearError]);

  // Start polling for notifications
  const startPolling = useCallback(() => {
    handleFetchUnreadCount();
    pollIntervalRef.current = setInterval(() => {
      handleFetchUnreadCount();
    }, pollInterval);
  }, [handleFetchUnreadCount, pollInterval]);

  // Stop polling
  const stopPolling = useCallback(() => {
    if (pollIntervalRef.current) {
      clearInterval(pollIntervalRef.current);
      pollIntervalRef.current = null;
    }
  }, []);

  // Start polling on mount - only once
  useEffect(() => {
    startPolling();
    return () => stopPolling();
  }, []);

  return {
    notifications,
    unreadCount,
    loading,
    error,
    pagination,
    fetchNotifications: handleFetchNotifications,
    fetchUnreadCount: handleFetchUnreadCount,
    markAsRead: handleMarkAsRead,
    markAllAsRead: handleMarkAllAsRead,
    deleteNotification: handleDeleteNotification,
    setPagination: handleSetPagination,
    clearError: handleClearError,
    startPolling,
    stopPolling,
  };
};
