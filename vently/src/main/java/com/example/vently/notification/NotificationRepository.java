package com.example.vently.notification;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find unread notifications for a specific user with pagination
     */
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Count unread notifications for a specific user
     */
    Long countByUserIdAndReadFalse(Long userId);

    /**
     * Mark a specific notification as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :notificationId AND n.user.id = :userId")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * Mark all notifications as read for a specific user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * Find expired notifications for cleanup
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :currentTime")
    Page<Notification> findExpiredNotifications(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Delete expired notifications (for cleanup job)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :currentTime")
    int deleteExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Delete a specific notification for a user
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id = :notificationId AND n.user.id = :userId")
    int deleteByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * Find notifications by user and type
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type, Pageable pageable);

    /**
     * Count total notifications for a user
     */
    Long countByUserId(Long userId);
}
