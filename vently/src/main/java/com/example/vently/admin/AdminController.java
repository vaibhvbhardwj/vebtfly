package com.example.vently.admin;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.admin.dto.UserFilterRequest;
import com.example.vently.admin.dto.SuspendUserRequest;
import com.example.vently.errorlog.ErrorLog;
import com.example.vently.errorlog.ErrorLogService;
import com.example.vently.admin.dto.AdjustNoShowRequest;
import com.example.vently.admin.dto.PlatformAnalyticsDTO;
import com.example.vently.admin.dto.UserGrowthTrendDTO;
import com.example.vently.admin.dto.RevenueMetricsDTO;
import com.example.vently.admin.dto.DisputeMetricsDTO;
import com.example.vently.admin.dto.AverageRatingsDTO;
import com.example.vently.admin.dto.NoShowStatisticsDTO;
import com.example.vently.audit.AuditLog;
import com.example.vently.audit.AuditService;
import com.example.vently.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ErrorLogService errorLogService;

    /**
     * GET /api/v1/admin/users
     * Get all users with pagination and filters
     * Requirements: 20.1, 24.4, 24.5
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(UserFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/v1/admin/users - filters: role={}, status={}, verification={}", 
            filterRequest.getRole(), filterRequest.getStatus(), filterRequest.getVerificationBadge());

        filterRequest.setPage(page);
        filterRequest.setSize(size);
        
        Page<User> users = adminService.getAllUsers(filterRequest);

        java.util.List<com.example.vently.admin.dto.AdminUserDto> dtos = users.getContent()
                .stream()
                .map(com.example.vently.admin.dto.AdminUserDto::from)
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos);
        response.put("totalElements", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        response.put("currentPage", users.getNumber());
        response.put("pageSize", users.getSize());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/admin/users/{id}/suspend
     * Suspend a user account
     * Requirements: 20.3, 24.4, 24.5
     */
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendUser(
            @PathVariable Long id,
            @RequestBody SuspendUserRequest request) {
        
        log.info("POST /api/v1/admin/users/{}/suspend - duration: {} days, reason: {}", 
            id, request.getDurationInDays(), request.getReason());
        
        User suspendedUser = adminService.suspendUser(id, request.getDurationInDays(), request.getReason());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User suspended successfully");
        response.put("userId", suspendedUser.getId());
        response.put("suspendedUntil", suspendedUser.getSuspendedUntil());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/admin/users/{id}/ban
     * Permanently ban a user account
     * Requirements: 20.4, 24.4, 24.5
     */
    @PostMapping("/users/{id}/ban")
    public ResponseEntity<Map<String, Object>> banUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String reason = request.get("reason");
        log.info("POST /api/v1/admin/users/{}/ban - reason: {}", id, reason);
        
        User bannedUser = adminService.banUser(id, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User banned successfully");
        response.put("userId", bannedUser.getId());
        response.put("accountStatus", bannedUser.getAccountStatus());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/admin/users/{id}/verify
     * Grant verification badge to user
     * Requirements: 20.6, 24.4, 24.5
     */
    @PostMapping("/users/{id}/verify")
    public ResponseEntity<Map<String, Object>> grantVerificationBadge(@PathVariable Long id) {
        log.info("POST /api/v1/admin/users/{}/verify", id);
        
        User verifiedUser = adminService.grantVerificationBadge(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Verification badge granted successfully");
        response.put("userId", verifiedUser.getId());
        response.put("verificationBadge", verifiedUser.getVerificationBadge());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/admin/users/{id}/reset-password
     * Reset user password and send temporary password
     * Requirements: 20.7, 24.4, 24.5
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id) {
        log.info("POST /api/v1/admin/users/{}/reset-password", id);
        
        String temporaryPassword = adminService.resetPassword(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset successfully. Temporary password sent to user email.");
        response.put("userId", id);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/admin/users/{id}/no-shows
     * Adjust no-show count for a user
     * Requirements: 11.7, 24.4, 24.5
     */
    @PutMapping("/users/{id}/no-shows")
    public ResponseEntity<Map<String, Object>> adjustNoShowCount(
            @PathVariable Long id,
            @RequestBody AdjustNoShowRequest request) {
        
        log.info("PUT /api/v1/admin/users/{}/no-shows - newCount: {}, reason: {}", 
            id, request.getNewCount(), request.getReason());
        
        User updatedUser = adminService.adjustNoShowCount(id, request.getNewCount(), request.getReason());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "No-show count adjusted successfully");
        response.put("userId", updatedUser.getId());
        response.put("noShowCount", updatedUser.getNoShowCount());
        response.put("accountStatus", updatedUser.getAccountStatus());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/analytics
     * Get platform analytics with date range filtering
     * Requirements: 21.1, 21.2, 21.3, 21.7
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        
        log.info("GET /api/v1/admin/analytics - startDate: {}, endDate: {}", startDate, endDate);
        
        PlatformAnalyticsDTO platformAnalytics = analyticsService.getPlatformAnalytics(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("platformAnalytics", platformAnalytics);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/analytics/user-growth
     * Get user growth trends
     * Requirements: 21.4
     */
    @GetMapping("/analytics/user-growth")
    public ResponseEntity<Map<String, Object>> getUserGrowthTrends(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        
        log.info("GET /api/v1/admin/analytics/user-growth - startDate: {}, endDate: {}", startDate, endDate);
        
        var trends = analyticsService.getUserGrowthTrends(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("trends", trends);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/analytics/revenue
     * Get revenue metrics
     * Requirements: 21.3
     */
    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueMetrics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        
        log.info("GET /api/v1/admin/analytics/revenue - startDate: {}, endDate: {}", startDate, endDate);
        
        RevenueMetricsDTO revenueMetrics = analyticsService.getRevenueMetrics(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("revenueMetrics", revenueMetrics);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/analytics/disputes
     * Get dispute metrics
     * Requirements: 21.8
     */
    @GetMapping("/analytics/disputes")
    public ResponseEntity<Map<String, Object>> getDisputeMetrics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        
        log.info("GET /api/v1/admin/analytics/disputes - startDate: {}, endDate: {}", startDate, endDate);
        
        DisputeMetricsDTO disputeMetrics = analyticsService.getDisputeMetrics(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("disputeMetrics", disputeMetrics);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/analytics/ratings
     * Get average ratings for volunteers and organizers
     * Requirements: 21.5
     */
    @GetMapping("/analytics/ratings")
    public ResponseEntity<Map<String, Object>> getAverageRatings() {
        log.info("GET /api/v1/admin/analytics/ratings");
        
        AverageRatingsDTO averageRatings = analyticsService.getAverageRatings();
        
        Map<String, Object> response = new HashMap<>();
        response.put("averageRatings", averageRatings);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/analytics/no-shows
     * Get no-show statistics and trends
     * Requirements: 21.6
     */
    @GetMapping("/analytics/no-shows")
    public ResponseEntity<Map<String, Object>> getNoShowStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        
        log.info("GET /api/v1/admin/analytics/no-shows - startDate: {}, endDate: {}", startDate, endDate);
        
        NoShowStatisticsDTO noShowStats = analyticsService.getNoShowStatistics(startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("noShowStatistics", noShowStats);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/audit-logs
     * Get audit logs with pagination and filtering
     * Requirements: 30.7
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /api/v1/admin/audit-logs - userId: {} action: {} entityType: {} startDate: {} endDate: {} page: {} size: {}",
            userId, action, entityType, startDate, endDate, page, size);
        
        // Limit page size to prevent performance issues
        if (size > 100) {
            size = 100;
        }
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by("timestamp").descending());
        
        // Convert LocalDate to LocalDateTime for query
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        
        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(23, 59, 59);
        }
        
        Page<AuditLog> auditLogs = auditService.searchAuditLogs(
            userId, action, entityType, startDateTime, endDateTime, pageable);

        java.util.List<com.example.vently.audit.AuditLogDto> dtos = auditLogs.getContent()
                .stream()
                .map(com.example.vently.audit.AuditLogDto::from)
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("auditLogs", dtos);
        response.put("totalElements", auditLogs.getTotalElements());
        response.put("totalPages", auditLogs.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/errors/{traceId}
     * Look up a persisted error log by trace ID
     */
    @GetMapping("/errors/{traceId}")
    public ResponseEntity<Map<String, Object>> getErrorByTraceId(@PathVariable String traceId) {
        log.info("GET /api/v1/admin/errors/{}", traceId);

        return errorLogService.findByTraceId(traceId)
                .map(errorLog -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("traceId", errorLog.getTraceId());
                    response.put("errorType", errorLog.getErrorType());
                    response.put("message", errorLog.getMessage());
                    response.put("path", errorLog.getPath());
                    response.put("stackTrace", errorLog.getStackTrace());
                    response.put("userId", errorLog.getUserId());
                    response.put("ipAddress", errorLog.getIpAddress());
                    response.put("httpStatus", errorLog.getHttpStatus());
                    response.put("timestamp", errorLog.getTimestamp());
                    response.put("found", true);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("found", false);
                    response.put("message", "No error log found for trace ID: " + traceId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
}
