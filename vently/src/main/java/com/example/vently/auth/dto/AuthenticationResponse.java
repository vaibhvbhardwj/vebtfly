package com.example.vently.auth.dto;

import com.example.vently.subscription.SubscriptionTier;
import com.example.vently.user.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response containing user details and JWT token.
 * Returned after successful registration or login.
 * 
 * Requirements: 1.3 - JWT token with role-based claims
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private Boolean emailVerified;
    private SubscriptionTier subscriptionTier;
}
