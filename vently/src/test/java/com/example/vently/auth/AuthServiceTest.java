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
    void testRegister_ShouldCreateUserAndSendVerificationEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPassword("password123");
        request.setRole(Role.VOLUNTEER);

        // Create a user with ID set (simulating what the database would do)
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
        when(subscriptionRepository.findByUserId(any())).thenReturn(Optional.empty()); // No subscription yet

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
        assertEquals(SubscriptionTier.FREE, response.getSubscriptionTier()); // Defaults to FREE
        verify(userRepository, times(2)).save(any(User.class)); // Once for register, once for verification token
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testSendVerificationEmail_ShouldGenerateTokenAndSetExpiration() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.sendVerificationEmail("test@example.com");

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(argThat(user -> 
            user.getVerificationToken() != null && 
            user.getVerificationTokenExpiresAt() != null &&
            user.getVerificationTokenExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    void testSendVerificationEmail_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            authService.sendVerificationEmail("nonexistent@example.com")
        );
    }

    @Test
    void testVerifyEmail_ShouldMarkEmailAsVerified() {
        // Arrange
        String token = "valid-token";
        testUser.setVerificationToken(token);
        testUser.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.verifyEmail(token);

        // Assert
        verify(userRepository).findByVerificationToken(token);
        verify(userRepository).save(argThat(user -> 
            user.getEmailVerified() == true &&
            user.getVerificationToken() == null &&
            user.getVerificationTokenExpiresAt() == null
        ));
    }

    @Test
    void testVerifyEmail_ShouldThrowException_WhenTokenInvalid() {
        // Arrange
        when(userRepository.findByVerificationToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            authService.verifyEmail("invalid-token")
        );
    }

    @Test
    void testVerifyEmail_ShouldThrowException_WhenTokenExpired() {
        // Arrange
        String token = "expired-token";
        testUser.setVerificationToken(token);
        testUser.setVerificationTokenExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            authService.verifyEmail(token)
        );
    }
}
