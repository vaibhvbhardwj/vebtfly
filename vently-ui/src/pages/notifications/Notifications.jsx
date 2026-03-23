import React, { useEffect, useState } from 'react';
import { useNotifications } from '../../hooks/useNotifications';
import { formatRelativeTime } from '../../utils/formatters';
import { Pagination } from '../../components/shared/Pagination';

const Notifications = () => {
  const {
    notifications,
    unreadCount,
    loading,
    error,
    pagination,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    setPagination,
  } = useNotifications();

  const [filter, setFilter] = useState('all'); // all, unread

  useEffect(() => {
    fetchNotifications(pagination.page);
  }, [pagination.page]);

  const handleMarkAsRead = async (notificationId) => {
    await markAsRead(notificationId);
  };

  const handleMarkAllAsRead = async () => {
    await markAllAsRead();
  };

  const handleDelete = async (notificationId) => {
    await deleteNotification(notificationId);
  };

  const handlePageChange = (newPage) => {
    setPagination(newPage);
  };

  // Group notifications by date
  const groupedNotifications = notifications.reduce((groups, notification) => {
    const date = new Date(notification.createdAt);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    let groupKey = 'Older';
    if (date.toDateString() === today.toDateString()) {
      groupKey = 'Today';
    } else if (date.toDateString() === yesterday.toDateString()) {
      groupKey = 'Yesterday';
    }

    if (!groups[groupKey]) {
      groups[groupKey] = [];
    }
    groups[groupKey].push(notification);
    return groups;
  }, {});

  const filteredNotifications = filter === 'unread' 
    ? Object.entries(groupedNotifications).reduce((acc, [key, notifs]) => {
        acc[key] = notifs.filter(n => !n.read);
        return acc;
      }, {})
    : groupedNotifications;

  const displayOrder = ['Today', 'Yesterday', 'Older'];

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-8 animate-fade-in">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-[#111827] mb-1">Notifications</h1>
          <p className="text-[#6B7280]">
            {unreadCount > 0 ? `${unreadCount} unread notification${unreadCount !== 1 ? 's' : ''}` : 'All caught up!'}
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl">
            <p className="text-[#EF4444] text-sm">{error}</p>
          </div>
        )}

        <div className="mb-6 flex items-center justify-between gap-4">
          <div className="flex gap-2">
            {['all', 'unread'].map((f) => (
              <button key={f} onClick={() => setFilter(f)}
                className={`px-4 py-2 rounded-xl font-medium text-sm transition ${filter === f ? 'bg-[#807aeb] text-white' : 'bg-white text-[#6B7280] border border-[#807aeb]/10 hover:border-[#807aeb]/30'}`}>
                {f.charAt(0).toUpperCase() + f.slice(1)}
              </button>
            ))}
          </div>
          {unreadCount > 0 && (
            <button onClick={handleMarkAllAsRead}
              className="px-4 py-2 bg-white text-[#6B7280] border border-[#807aeb]/10 hover:border-[#807aeb]/30 rounded-xl font-medium text-sm transition">
              Mark all as read
            </button>
          )}
        </div>

        {loading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="bg-white rounded-2xl p-4 animate-pulse border border-[#807aeb]/10">
                <div className="h-4 bg-[#ebf2fa] rounded w-3/4 mb-2" />
                <div className="h-3 bg-[#ebf2fa] rounded w-full mb-2" />
                <div className="h-3 bg-[#ebf2fa] rounded w-1/2" />
              </div>
            ))}
          </div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-5xl mb-4">🔔</div>
            <p className="text-[#6B7280] text-lg">No notifications</p>
          </div>
        ) : (
          <div className="space-y-6">
            {displayOrder.map((dateGroup) => {
              const groupNotifications = filteredNotifications[dateGroup];
              if (!groupNotifications || groupNotifications.length === 0) return null;
              return (
                <div key={dateGroup}>
                  <h2 className="text-xs font-semibold text-[#6B7280] uppercase tracking-wide mb-3">{dateGroup}</h2>
                  <div className="space-y-2">
                    {groupNotifications.map((notification) => (
                      <div key={notification.id}
                        className={`p-4 rounded-2xl border transition ${!notification.read ? 'bg-[#807aeb]/5 border-[#807aeb]/20 border-l-4 border-l-[#807aeb]' : 'bg-white border-[#807aeb]/10 hover:border-[#807aeb]/20'}`}>
                        <div className="flex items-start justify-between gap-4">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="text-[#111827] font-semibold text-sm">{notification.title}</h3>
                              {!notification.read && <span className="w-2 h-2 bg-[#807aeb] rounded-full" />}
                            </div>
                            <p className="text-[#6B7280] text-sm mb-1">{notification.message}</p>
                            <p className="text-xs text-[#6B7280]/60">{formatRelativeTime(notification.createdAt)}</p>
                          </div>
                          <div className="flex items-center gap-1 flex-shrink-0">
                            {!notification.read && (
                              <button onClick={() => handleMarkAsRead(notification.id)}
                                className="p-1.5 text-[#6B7280] hover:text-[#807aeb] hover:bg-[#807aeb]/10 rounded-lg transition" title="Mark as read">
                                ✓
                              </button>
                            )}
                            <button onClick={() => handleDelete(notification.id)}
                              className="p-1.5 text-[#6B7280] hover:text-[#EF4444] hover:bg-red-50 rounded-lg transition" title="Delete">
                              ✕
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {!loading && notifications.length > 0 && (
          <div className="mt-8">
            <Pagination
              currentPage={pagination.page}
              totalPages={Math.ceil(pagination.total / pagination.pageSize)}
              onPageChange={handlePageChange}
            />
          </div>
        )}
      </div>
    </div>
  );
};


export default Notifications;