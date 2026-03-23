package com.example.vently.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.vently.user.User;
import com.example.vently.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for AuditService
 * Requirements: 30.1, 30.2, 30.3, 30.4, 30.7
 */
@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditService auditService;

    private User testUser;
    private User adminUser;
    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("Test User")
                .role(Role.VOLUNTEER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();

        testAuditLog = AuditLog.builder()
                .id(1L)
                .user(testUser)
                .action("LOGIN_SUCCESS")
                .entityType("USER")
                .entityId(1L)
                .details("{\"email\":\"user@example.com\",\"success\":true}")
                .ipAddress("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Test logAuthentication for successful login
     * Requirements: 30.1
     */
    @Test
    void testLogAuthenticationSuccess() throws Exception {
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"email\":\"user@example.com\",\"success\":true}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logAuthentication(testUser, ipAddress, true);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(1L) &&
            log.getAction().equals("LOGIN_SUCCESS") &&
            log.getIpAddress().equals(ipAddress)
        ));
    }

    /**
     * Test logAuthentication for failed login
     * Requirements: 30.1
     */
    @Test
    void testLogAuthenticationFailure() throws Exception {
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"email\":\"user@example.com\",\"success\":false}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logAuthentication(testUser, ipAddress, false);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(1L) &&
            log.getAction().equals("LOGIN_FAILED") &&
            log.getIpAddress().equals(ipAddress)
        ));
    }

    /**
     * Test logPaymentTransaction for deposit
     * Requirements: 30.2
     */
    @Test
    void testLogPaymentTransactionDeposit() throws Exception {
        Long eventId = 100L;
        String amount = "500.00";
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":100,\"amount\":\"500.00\",\"transactionType\":\"DEPOSIT\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logPaymentTransaction(testUser, eventId, amount, "DEPOSIT", ipAddress);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(1L) &&
            log.getAction().equals("PAYMENT_DEPOSIT") &&
            log.getEntityType().equals("PAYMENT") &&
            log.getEntityId().equals(eventId)
        ));
    }

    /**
     * Test logPaymentTransaction for release
     * Requirements: 30.2
     */
    @Test
    void testLogPaymentTransactionRelease() throws Exception {
        Long eventId = 100L;
        String amount = "450.00";
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":100,\"amount\":\"450.00\",\"transactionType\":\"RELEASE\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logPaymentTransaction(testUser, eventId, amount, "RELEASE", ipAddress);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(1L) &&
            log.getAction().equals("PAYMENT_RELEASE") &&
            log.getEntityType().equals("PAYMENT")
        ));
    }

    /**
     * Test logPaymentTransaction for refund
     * Requirements: 30.2
     */
    @Test
    void testLogPaymentTransactionRefund() throws Exception {
        Long eventId = 100L;
        String amount = "500.00";
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":100,\"amount\":\"500.00\",\"transactionType\":\"REFUND\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logPaymentTransaction(testUser, eventId, amount, "REFUND", ipAddress);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(1L) &&
            log.getAction().equals("PAYMENT_REFUND")
        ));
    }

    /**
     * Test logAdminAction for user suspension
     * Requirements: 30.3
     */
    @Test
    void testLogAdminActionSuspendUser() throws Exception {
        Long targetUserId = 1L;
        Map<String, Object> details = new HashMap<>();
        details.put("durationInDays", 30);
        details.put("reason", "Policy violation");
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"durationInDays\":30,\"reason\":\"Policy violation\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logAdminAction(adminUser, "SUSPEND_USER", targetUserId, details, ipAddress);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(2L) &&
            log.getAction().equals("ADMIN_SUSPEND_USER") &&
            log.getEntityId().equals(targetUserId)
        ));
    }

    /**
     * Test logAdminAction for user ban
     * Requirements: 30.3
     */
    @Test
    void testLogAdminActionBanUser() throws Exception {
        Long targetUserId = 1L;
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "Severe policy violation");
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"reason\":\"Severe policy violation\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logAdminAction(adminUser, "BAN_USER", targetUserId, details, ipAddress);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(2L) &&
            log.getAction().equals("ADMIN_BAN_USER")
        ));
    }

    /**
     * Test logEventStateTransition
     * Requirements: 30.4
     */
    @Test
    void testLogEventStateTransition() throws Exception {
        Long eventId = 100L;
        String fromStatus = "DRAFT";
        String toStatus = "PUBLISHED";
        String ipAddress = "192.168.1.1";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":100,\"fromStatus\":\"DRAFT\",\"toStatus\":\"PUBLISHED\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logEventStateTransition(testUser, eventId, fromStatus, toStatus, ipAddress);

        verify(auditLogRepository, times(1)).save(argThat(log ->
            log.getUser().getId().equals(1L) &&
            log.getAction().equals("EVENT_STATE_TRANSITION") &&
            log.getEntityType().equals("EVENT") &&
            log.getEntityId().equals(eventId)
        ));
    }

    /**
     * Test searchAuditLogs with all filters
     * Requirements: 30.7
     */
    @Test
    void testSearchAuditLogsWithAllFilters() {
        Long userId = 1L;
        String action = "LOGIN_SUCCESS";
        String entityType = "USER";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 20);

        Page<AuditLog> expectedPage = new PageImpl<>(java.util.List.of(testAuditLog), pageable, 1);
        when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(expectedPage);

        Page<AuditLog> result = auditService.searchAuditLogs(userId, action, entityType, startDate, endDate, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testAuditLog.getId(), result.getContent().get(0).getId());
        verify(auditLogRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    /**
     * Test searchAuditLogs with partial filters (nulls)
     * Requirements: 30.7
     */
    @Test
    void testSearchAuditLogsWithPartialFilters() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        Page<AuditLog> expectedPage = new PageImpl<>(java.util.List.of(testAuditLog), pageable, 1);
        when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(expectedPage);

        Page<AuditLog> result = auditService.searchAuditLogs(userId, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    /**
     * Test getAuditLogsByUser
     */
    @Test
    void testGetAuditLogsByUser() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLog> expectedPage = new PageImpl<>(java.util.List.of(testAuditLog), pageable, 1);

        when(auditLogRepository.findByUserId(userId, pageable)).thenReturn(expectedPage);

        Page<AuditLog> result = auditService.getAuditLogsByUser(userId, pageable);

        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).findByUserId(userId, pageable);
    }

    /**
     * Test getAuditLogsByAction
     */
    @Test
    void testGetAuditLogsByAction() {
        String action = "LOGIN_SUCCESS";
        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLog> expectedPage = new PageImpl<>(java.util.List.of(testAuditLog), pageable, 1);

        when(auditLogRepository.findByAction(action, pageable)).thenReturn(expectedPage);

        Page<AuditLog> result = auditService.getAuditLogsByAction(action, pageable);

        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).findByAction(action, pageable);
    }

    /**
     * Test countByAction
     */
    @Test
    void testCountByAction() {
        String action = "LOGIN_SUCCESS";

        when(auditLogRepository.countByAction(action)).thenReturn(5L);

        Long result = auditService.countByAction(action);

        assertEquals(5L, result);
        verify(auditLogRepository, times(1)).countByAction(action);
    }

    /**
     * Test countByUserId
     */
    @Test
    void testCountByUserId() {
        Long userId = 1L;

        when(auditLogRepository.countByUserId(userId)).thenReturn(10L);

        Long result = auditService.countByUserId(userId);

        assertEquals(10L, result);
        verify(auditLogRepository, times(1)).countByUserId(userId);
    }
}
