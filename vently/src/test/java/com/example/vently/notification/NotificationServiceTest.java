package com.example.vently.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.vently.user.Role;
import com.example.vently.user.User;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.VOLUNTEER)
                .build();

        testNotification = Notification.builder()
                .id(1L)
                .user(testUser)
                .type("APPLICATION_STATUS")
                .title("Test Notification")
                .message("This is a test notification")
                .read(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("Should create notification successfully")
    void testCreateNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(
                testUser,
                "APPLICATION_STATUS",
                "Test Notification",
                "This is a test notification"
        );

        // Assert
        assertNotNull(result);
        assertEquals("APPLICATION_STATUS", result.getType());
        assertEquals("Test Notification", result.getTitle());
        assertEquals("This is a test notification", result.getMessage());
        assertEquals(testUser, result.getUser());
        assertFalse(result.getRead());

        // Verify repository interaction
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        
        Notification savedNotification = notificationCaptor.getValue();
        assertEquals("APPLICATION_STATUS", savedNotification.getType());
        assertEquals(testUser, savedNotification.getUser());
    }

    @Test
    @DisplayName("Should get user notifications with pagination")
    void testGetUserNotifications() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> notificationPage = new PageImpl<>(List.of(testNotification));
        
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(notificationPage);

        // Act
        Page<Notification> result = notificationService.getUserNotifications(1L, 0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testNotification, result.getContent().get(0));
        
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get unread notifications")
    void testGetUnreadNotifications() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> notificationPage = new PageImpl<>(List.of(testNotification));
        
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(notificationPage);

        // Act
        Page<Notification> result = notificationService.getUnreadNotifications(1L, 0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).getRead());
        
        verify(notificationRepository).findByUserIdAndReadFalseOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead() {
        // Arrange
        when(notificationRepository.markAsRead(1L, 1L)).thenReturn(1);

        // Act
        boolean result = notificationService.markAsRead(1L, 1L);

        // Assert
        assertTrue(result);
        verify(notificationRepository).markAsRead(1L, 1L);
    }

    @Test
    @DisplayName("Should return false when marking non-existent notification as read")
    void testMarkAsReadNotFound() {
        // Arrange
        when(notificationRepository.markAsRead(999L, 1L)).thenReturn(0);

        // Act
        boolean result = notificationService.markAsRead(999L, 1L);

        // Assert
        assertFalse(result);
        verify(notificationRepository).markAsRead(999L, 1L);
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void testMarkAllAsRead() {
        // Arrange
        when(notificationRepository.markAllAsRead(1L)).thenReturn(5);

        // Act
        int result = notificationService.markAllAsRead(1L);

        // Assert
        assertEquals(5, result);
        verify(notificationRepository).markAllAsRead(1L);
    }

    @Test
    @DisplayName("Should get unread count")
    void testGetUnreadCount() {
        // Arrange
        when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(3L);

        // Act
        Long result = notificationService.getUnreadCount(1L);

        // Assert
        assertEquals(3L, result);
        verify(notificationRepository).countByUserIdAndReadFalse(1L);
    }

    @Test
    @DisplayName("Should delete notification successfully")
    void testDeleteNotification() {
        // Arrange
        when(notificationRepository.deleteByIdAndUserId(1L, 1L)).thenReturn(1);

        // Act
        boolean result = notificationService.deleteNotification(1L, 1L);

        // Assert
        assertTrue(result);
        verify(notificationRepository).deleteByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent notification")
    void testDeleteNotificationNotFound() {
        // Arrange
        when(notificationRepository.deleteByIdAndUserId(999L, 1L)).thenReturn(0);

        // Act
        boolean result = notificationService.deleteNotification(999L, 1L);

        // Assert
        assertFalse(result);
        verify(notificationRepository).deleteByIdAndUserId(999L, 1L);
    }

    @Test
    @DisplayName("Should cleanup expired notifications")
    void testCleanupExpiredNotifications() {
        // Arrange
        when(notificationRepository.deleteExpiredNotifications(any(LocalDateTime.class))).thenReturn(10);

        // Act
        notificationService.cleanupExpiredNotifications();

        // Assert
        verify(notificationRepository).deleteExpiredNotifications(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get total notification count")
    void testGetTotalCount() {
        // Arrange
        when(notificationRepository.countByUserId(1L)).thenReturn(15L);

        // Act
        Long result = notificationService.getTotalCount(1L);

        // Assert
        assertEquals(15L, result);
        verify(notificationRepository).countByUserId(1L);
    }

    @Test
    @DisplayName("Should get notifications by type")
    void testGetNotificationsByType() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> notificationPage = new PageImpl<>(List.of(testNotification));
        
        when(notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
                eq(1L), eq("APPLICATION_STATUS"), any(Pageable.class)))
                .thenReturn(notificationPage);

        // Act
        Page<Notification> result = notificationService.getNotificationsByType(
                1L, "APPLICATION_STATUS", 0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("APPLICATION_STATUS", result.getContent().get(0).getType());
        
        verify(notificationRepository).findByUserIdAndTypeOrderByCreatedAtDesc(
                eq(1L), eq("APPLICATION_STATUS"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should create notification with correct default values")
    void testCreateNotificationDefaults() {
        // Arrange
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.createNotification(
                testUser,
                "PAYMENT",
                "Payment Received",
                "Your payment has been processed"
        );

        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        
        assertEquals(testUser, captured.getUser());
        assertEquals("PAYMENT", captured.getType());
        assertEquals("Payment Received", captured.getTitle());
        assertEquals("Your payment has been processed", captured.getMessage());
        assertEquals(false, captured.getRead());
    }

    @Test
    @DisplayName("Should handle multiple notification types")
    void testMultipleNotificationTypes() {
        // Arrange
        String[] types = {
            "APPLICATION_STATUS",
            "PAYMENT",
            "EVENT_CANCELLED",
            "DISPUTE_RESOLVED",
            "RATING_RECEIVED"
        };

        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act & Assert
        for (String type : types) {
            Notification result = notificationService.createNotification(
                    testUser,
                    type,
                    "Test Title",
                    "Test Message"
            );
            
            assertNotNull(result);
        }

        verify(notificationRepository, times(types.length)).save(any(Notification.class));
    }
}
