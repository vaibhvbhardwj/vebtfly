package com.example.vently.notification;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/notifications
     * Get paginated notifications for the authenticated user
     * 
     * @param page          Page number (default: 0)
     * @param size          Page size (default: 20)
     * @param unreadOnly    Filter for unread notifications only (default: false)
     * @param userDetails   Authenticated user
     * @return Page of notifications
     */
    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("GET /api/v1/notifications - user: {}, page: {}, size: {}, unreadOnly: {}", 
                userDetails.getUsername(), page, size, unreadOnly);
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUnreadNotifications(user.getId(), page, size);
        } else {
            notifications = notificationService.getUserNotifications(user.getId(), page, size);
        }
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * PUT /api/v1/notifications/{id}/read
     * Mark a specific notification as read
     * 
     * @param id          Notification ID
     * @param userDetails Authenticated user
     * @return Success response
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("PUT /api/v1/notifications/{}/read - user: {}", id, userDetails.getUsername());
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean success = notificationService.markAsRead(id, user.getId());
        
        if (!success) {
            log.warn("Failed to mark notification {} as read for user {}", id, user.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Notification not found or already read"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/notifications/read-all
     * Mark all notifications as read for the authenticated user
     * 
     * @param userDetails Authenticated user
     * @return Success response with count
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("PUT /api/v1/notifications/read-all - user: {}", userDetails.getUsername());
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        int count = notificationService.markAllAsRead(user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        response.put("count", count);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/notifications/unread-count
     * Get count of unread notifications for the authenticated user
     * 
     * @param userDetails Authenticated user
     * @return Unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("GET /api/v1/notifications/unread-count - user: {}", userDetails.getUsername());
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Long count = notificationService.getUnreadCount(user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/notifications/{id}
     * Delete a specific notification
     * 
     * @param id          Notification ID
     * @param userDetails Authenticated user
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("DELETE /api/v1/notifications/{} - user: {}", id, userDetails.getUsername());
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean success = notificationService.deleteNotification(id, user.getId());
        
        if (!success) {
            log.warn("Failed to delete notification {} for user {}", id, user.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Notification not found"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification deleted successfully");
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}
