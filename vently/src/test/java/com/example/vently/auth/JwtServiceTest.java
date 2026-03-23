package com.example.vently.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.vently.user.Role;
import com.example.vently.user.User;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .password("password")
                .role(Role.VOLUNTEER)
                .emailVerified(true)
                .build();
    }

    @Test
    void testGenerateToken_ShouldIncludeRoleClaim() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        String extractedRole = jwtService.extractRole(token);
        assertEquals("ROLE_VOLUNTEER", extractedRole);
    }

    @Test
    void testGenerateToken_WithExtraClaims_ShouldIncludeRoleClaim() {
        // Arrange
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        // Act
        String token = jwtService.generateToken(extraClaims, testUser);

        // Assert
        assertNotNull(token);
        String extractedRole = jwtService.extractRole(token);
        assertEquals("ROLE_VOLUNTEER", extractedRole);
    }

    @Test
    void testGenerateToken_ForAdmin_ShouldIncludeAdminRole() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .fullName("Admin User")
                .password("password")
                .role(Role.ADMIN)
                .emailVerified(true)
                .build();

        // Act
        String token = jwtService.generateToken(adminUser);

        // Assert
        assertNotNull(token);
        String extractedRole = jwtService.extractRole(token);
        assertEquals("ROLE_ADMIN", extractedRole);
    }

    @Test
    void testGenerateToken_ForOrganizer_ShouldIncludeOrganizerRole() {
        // Arrange
        User organizerUser = User.builder()
                .id(3L)
                .email("organizer@example.com")
                .fullName("Organizer User")
                .password("password")
                .role(Role.ORGANIZER)
                .emailVerified(true)
                .build();

        // Act
        String token = jwtService.generateToken(organizerUser);

        // Assert
        assertNotNull(token);
        String extractedRole = jwtService.extractRole(token);
        assertEquals("ROLE_ORGANIZER", extractedRole);
    }

    @Test
    void testExtractUsername_ShouldReturnCorrectEmail() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    void testIsTokenValid_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_WithDifferentUser_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        User differentUser = User.builder()
                .id(2L)
                .email("different@example.com")
                .fullName("Different User")
                .password("password")
                .role(Role.VOLUNTEER)
                .emailVerified(true)
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }
}
