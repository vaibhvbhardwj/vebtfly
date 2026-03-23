package com.example.vently.notification;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create a notification for a user
     * 
     * @param user    The user to notify
     * @param type    The notification type (e.g., "APPLICATION_STATUS", "PAYMENT", "EVENT_CANCELLED")
     * @param title   The notification title
     * @param message The notification message
     * @return The created notification
     */
    @Transactional
    public Notification createNotification(User user, String type, String title, String message) {
        log.info("Creating notification for user {} with type {}", user.getId(), type);
        
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .build();
        
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification created with ID: {}", saved.getId());
        
        return saved;
    }

    /**
     * Get paginated notifications for a user
     * 
     * @param userId The user ID
     * @param page   Page number (0-indexed)
     * @param size   Page size
     * @return Page of notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        log.debug("Fetching notifications for user {} - page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get paginated unread notifications for a user
     * 
     * @param userId The user ID
     * @param page   Page number (0-indexed)
     * @param size   Page size
     * @return Page of unread notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(Long userId, int page, int size) {
        log.debug("Fetching unread notifications for user {} - page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Mark a specific notification as read
     * 
     * @param notificationId The notification ID
     * @param userId         The user ID (for security check)
     * @return true if marked successfully, false otherwise
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read for user {}", notificationId, userId);
        int updated = notificationRepository.markAsRead(notificationId, userId);
        return updated > 0;
    }

    /**
     * Mark all notifications as read for a user
     * 
     * @param userId The user ID
     * @return Number of notifications marked as read
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        int updated = notificationRepository.markAllAsRead(userId);
        log.debug("Marked {} notifications as read", updated);
        return updated;
    }

    /**
     * Get count of unread notifications for a user
     * 
     * @param userId The user ID
     * @return Count of unread notifications
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        log.debug("Getting unread count for user {}", userId);
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Delete a specific notification
     * 
     * @param notificationId The notification ID
     * @param userId         The user ID (for security check)
     * @return true if deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification {} for user {}", notificationId, userId);
        int deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
        return deleted > 0;
    }

    /**
     * Scheduled task to cleanup expired notifications (runs daily at 2 AM)
     * Deletes notifications older than 30 days
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredNotifications() {
        log.info("Starting cleanup of expired notifications");
        LocalDateTime now = LocalDateTime.now();
        int deleted = notificationRepository.deleteExpiredNotifications(now);
        log.info("Cleanup completed. Deleted {} expired notifications", deleted);
    }

    /**
     * Get total notification count for a user
     * 
     * @param userId The user ID
     * @return Total count of notifications
     */
    @Transactional(readOnly = true)
    public Long getTotalCount(Long userId) {
        log.debug("Getting total notification count for user {}", userId);
        return notificationRepository.countByUserId(userId);
    }

    /**
     * Get notifications by type for a user
     * 
     * @param userId The user ID
     * @param type   The notification type
     * @param page   Page number (0-indexed)
     * @param size   Page size
     * @return Page of notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByType(Long userId, String type, int page, int size) {
        log.debug("Fetching notifications for user {} with type {} - page: {}, size: {}", userId, type, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
    }
}
