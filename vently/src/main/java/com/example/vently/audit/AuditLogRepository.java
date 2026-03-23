package com.example.vently.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /**
     * Find all audit logs for a specific user
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all audit logs for a specific action type
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find all audit logs for a specific entity type
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find all audit logs for a specific entity
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find all audit logs from a specific IP address
     */
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * Find audit logs within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find audit logs by user and action type
     */
    Page<AuditLog> findByUserIdAndAction(Long userId, String action, Pageable pageable);

    /**
     * Find audit logs by user within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdAndTimestampBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find audit logs by action within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByActionAndTimestampBetween(
        @Param("action") String action,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find audit logs by entity type within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByEntityTypeAndTimestampBetween(
        @Param("entityType") String entityType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );



    /**
     * Count audit logs by action type
     */
    Long countByAction(String action);

    /**
     * Count audit logs for a user
     */
    Long countByUserId(Long userId);

    /**
     * Count audit logs within a date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    Long countByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recent audit logs for a user (for activity feed)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find authentication attempts from an IP address
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.action LIKE 'LOGIN%' ORDER BY a.timestamp DESC")
    List<AuditLog> findAuthenticationAttemptsByIp(@Param("ipAddress") String ipAddress);

    /**
     * Find failed login attempts for a user within a time window
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = 'LOGIN_FAILED' AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttempts(
        @Param("userId") Long userId,
        @Param("since") LocalDateTime since
    );

    /**
     * Find all admin actions within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action LIKE 'ADMIN_%' AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findAdminActions(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find payment-related audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = 'PAYMENT' ORDER BY a.timestamp DESC")
    Page<AuditLog> findPaymentLogs(Pageable pageable);
}
