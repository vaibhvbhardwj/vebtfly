package com.example.vently.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.vently.admin.dto.UserFilterRequest;
import com.example.vently.audit.AuditService;
import com.example.vently.notification.NotificationService;
import com.example.vently.user.AccountStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminService adminService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(Role.VOLUNTEER);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser.setNoShowCount(0);
        testUser.setVerificationBadge(false);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFullName("Admin User");
        adminUser.setRole(Role.ADMIN);
        adminUser.setAccountStatus(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void testGetAllUsers() {
        // Arrange
        UserFilterRequest filterRequest = new UserFilterRequest();
        filterRequest.setRole(Role.VOLUNTEER);
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(java.util.List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<User> result = adminService.getAllUsers(filterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getId(), result.getContent().get(0).getId());
    }
}