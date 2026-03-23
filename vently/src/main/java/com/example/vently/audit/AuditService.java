package com.example.vently.audit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing audit logs
 * Logs all critical actions: authentication, payments, admin actions, and state transitions
 * Requirements: 30.1, 30.2, 30.3, 30.4, 30.7
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log authentication attempt
     * Requirements: 30.1
     * 
     * @param user User attempting to authenticate
     * @param ipAddress IP address of the request
     * @param success Whether authentication was successful
     */
    @Transactional
    public void logAuthentication(User user, String ipAddress, boolean success) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("email", user.getEmail());
            details.put("success", success);
            details.put("timestamp", LocalDateTime.now());

            String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType("USER")
                    .entityId(user.getId())
                    .details(objectMapper.writeValueAsString(details))
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Authentication logged for user: {} from IP: {} - {}", 
                user.getEmail(), ipAddress, action);
        } catch (Exception e) {
            log.error("Failed to log authentication for user: {}", user.getEmail(), e);
        }
    }

    /**
     * Log payment transaction
     * Requirements: 30.2
     * 
     * @param user User making the payment
     * @param eventId Event ID associated with payment
     * @param amount Payment amount
     * @param transactionType Type of transaction (DEPOSIT, RELEASE, REFUND)
     * @param ipAddress IP address of the request
     */
    @Transactional
    public void logPaymentTransaction(User user, Long eventId, String amount, 
                                     String transactionType, String ipAddress) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("eventId", eventId);
            details.put("amount", amount);
            details.put("transactionType", transactionType);
            details.put("timestamp", LocalDateTime.now());

            String action = "PAYMENT_" + transactionType.toUpperCase();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType("PAYMENT")
                    .entityId(eventId)
                    .details(objectMapper.writeValueAsString(details))
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Payment transaction logged for user: {} - Type: {} Amount: {} Event: {}", 
                user.getEmail(), transactionType, amount, eventId);
        } catch (Exception e) {
            log.error("Failed to log payment transaction for user: {}", user.getEmail(), e);
        }
    }

    /**
     * Log admin action
     * Requirements: 30.3
     * 
     * @param admin Admin user performing the action
     * @param action Action type (SUSPEND, BAN, VERIFY, etc.)
     * @param targetUserId User ID being acted upon
     * @param details Additional details about the action
     * @param ipAddress IP address of the request
     */
    @Transactional
    public void logAdminAction(User admin, String action, Long targetUserId, 
                              Map<String, Object> details, String ipAddress) {
        try {
            if (details == null) {
                details = new HashMap<>();
            }
            details.put("targetUserId", targetUserId);
            details.put("timestamp", LocalDateTime.now());

            String actionType = "ADMIN_" + action.toUpperCase();

            AuditLog auditLog = AuditLog.builder()
                    .user(admin)
                    .action(actionType)
                    .entityType("USER")
                    .entityId(targetUserId)
                    .details(objectMapper.writeValueAsString(details))
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Admin action logged - Admin: {} Action: {} Target: {} from IP: {}", 
                admin.getEmail(), action, targetUserId, ipAddress);
        } catch (Exception e) {
            log.error("Failed to log admin action for admin: {}", admin.getEmail(), e);
        }
    }

    /**
     * Log event state transition
     * Requirements: 30.4
     * 
     * @param user User triggering the transition
     * @param eventId Event ID
     * @param fromStatus Previous status
     * @param toStatus New status
     * @param ipAddress IP address of the request
     */
    @Transactional
    public void logEventStateTransition(User user, Long eventId, String fromStatus, 
                                       String toStatus, String ipAddress) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("eventId", eventId);
            details.put("fromStatus", fromStatus);
            details.put("toStatus", toStatus);
            details.put("timestamp", LocalDateTime.now());

            String action = "EVENT_STATE_TRANSITION";

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType("EVENT")
                    .entityId(eventId)
                    .details(objectMapper.writeValueAsString(details))
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Event state transition logged - Event: {} User: {} {} -> {} from IP: {}", 
                eventId, user.getEmail(), fromStatus, toStatus, ipAddress);
        } catch (Exception e) {
            log.error("Failed to log event state transition for event: {}", eventId, e);
        }
    }

    /**
     * Search audit logs with pagination and filtering
     * Requirements: 30.7
     * 
     * @param userId Filter by user ID (optional)
     * @param action Filter by action type (optional)
     * @param entityType Filter by entity type (optional)
     * @param startDate Filter by start date (optional)
     * @param endDate Filter by end date (optional)
     * @param pageable Pagination parameters
     * @return Page of audit logs matching the criteria
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(Long userId, String action, String entityType,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         Pageable pageable) {
        log.info("Searching audit logs - userId: {} action: {} entityType: {} startDate: {} endDate: {}",
            userId, action, entityType, startDate, endDate);

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable);
    }

    /**
     * Get audit logs for a specific user
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of audit logs for the user
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit logs for a specific action type
     * 
     * @param action Action type
     * @param pageable Pagination parameters
     * @return Page of audit logs for the action
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs for a specific entity
     * 
     * @param entityType Entity type
     * @param entityId Entity ID
     * @return List of audit logs for the entity
     */
    @Transactional(readOnly = true)
    public java.util.List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get recent audit logs for a user
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of recent audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getRecentAuditLogs(Long userId, Pageable pageable) {
        return auditLogRepository.findRecentByUserId(userId, pageable);
    }

    /**
     * Get authentication attempts from an IP address
     * 
     * @param ipAddress IP address
     * @return List of authentication attempts
     */
    @Transactional(readOnly = true)
    public java.util.List<AuditLog> getAuthenticationAttemptsByIp(String ipAddress) {
        return auditLogRepository.findAuthenticationAttemptsByIp(ipAddress);
    }

    /**
     * Get failed login attempts for a user within a time window
     * 
     * @param userId User ID
     * @param since Time window start
     * @return List of failed login attempts
     */
    @Transactional(readOnly = true)
    public java.util.List<AuditLog> getFailedLoginAttempts(Long userId, LocalDateTime since) {
        return auditLogRepository.findFailedLoginAttempts(userId, since);
    }

    /**
     * Get admin actions within a date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of admin actions
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAdminActions(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findAdminActions(startDate, endDate, pageable);
    }

    /**
     * Get payment-related audit logs
     * 
     * @param pageable Pagination parameters
     * @return Page of payment logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getPaymentLogs(Pageable pageable) {
        return auditLogRepository.findPaymentLogs(pageable);
    }

    /**
     * Count audit logs by action type
     * 
     * @param action Action type
     * @return Count of audit logs
     */
    @Transactional(readOnly = true)
    public Long countByAction(String action) {
        return auditLogRepository.countByAction(action);
    }

    /**
     * Count audit logs for a user
     * 
     * @param userId User ID
     * @return Count of audit logs
     */
    @Transactional(readOnly = true)
    public Long countByUserId(Long userId) {
        return auditLogRepository.countByUserId(userId);
    }
}
