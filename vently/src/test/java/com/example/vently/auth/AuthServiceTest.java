package com.example.vently.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.vently.auth.dto.AuthenticationResponse;
import com.example.vently.auth.dto.RegisterRequest;
import com.example.vently.notification.EmailService;
import com.example.vently.subscription.SubscriptionRepository;
import com.example.vently.subscription.SubscriptionTier;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private com.example.vently.audit.AuditService auditService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .role(Role.VOLUNTEER)
                .emailVerified(false)
                .build();
    }

    @Test
    void testRegister_ShouldCreateUserAndSendEmailOtp() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPassword("password123");
        request.setRole(Role.VOLUNTEER);

        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .role(Role.VOLUNTEER)
                .emailVerified(false)
                .build();

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(subscriptionRepository.findByUserId(any())).thenReturn(Optional.empty());

        // Act
        AuthenticationResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals(Role.VOLUNTEER, response.getRole());
        assertEquals(false, response.getEmailVerified());
        assertEquals(SubscriptionTier.FREE, response.getSubscriptionTier());
        // save called twice: once for register, once for OTP
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void testSendEmailOtp_ShouldSetOtpAndExpiry() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.sendEmailOtp("test@example.com");

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(argThat(user ->
            user.getEmailOtp() != null &&
            user.getEmailOtp().length() == 6 &&
            user.getEmailOtpExpiresAt() != null &&
            user.getEmailOtpExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    void testSendEmailOtp_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            authService.sendEmailOtp("nonexistent@example.com")
        );
    }

    @Test
    void testVerifyEmailOtp_ShouldMarkEmailAsVerified() {
        // Arrange
        testUser.setEmailOtp("123456");
        testUser.setEmailOtpExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.verifyEmailOtp("test@example.com", "123456");

        // Assert
        verify(userRepository).save(argThat(user ->
            user.getEmailVerified() == true &&
            user.getEmailOtp() == null &&
            user.getEmailOtpExpiresAt() == null
        ));
    }

    @Test
    void testVerifyEmailOtp_ShouldThrowException_WhenOtpInvalid() {
        // Arrange
        testUser.setEmailOtp("123456");
        testUser.setEmailOtpExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(Exception.class, () ->
            authService.verifyEmailOtp("test@example.com", "000000")
        );
    }

    @Test
    void testVerifyEmailOtp_ShouldThrowException_WhenOtpExpired() {
        // Arrange
        testUser.setEmailOtp("123456");
        testUser.setEmailOtpExpiresAt(LocalDateTime.now().minusMinutes(1)); // expired

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(Exception.class, () ->
            authService.verifyEmailOtp("test@example.com", "123456")
        );
    }

    @Test
    void testVerifyEmailOtp_ShouldThrowException_WhenNoOtpRequested() {
        // Arrange — no OTP set on user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(Exception.class, () ->
            authService.verifyEmailOtp("test@example.com", "123456")
        );
    }
}
