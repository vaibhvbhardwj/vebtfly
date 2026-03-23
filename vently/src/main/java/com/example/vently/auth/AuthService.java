package com.example.vently.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.vently.auth.dto.AuthenticationRequest;
import com.example.vently.auth.dto.AuthenticationResponse;
import com.example.vently.auth.dto.RegisterRequest;
import com.example.vently.audit.AuditService;
import com.example.vently.subscription.SubscriptionRepository;
import com.example.vently.subscription.SubscriptionTier;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SubscriptionRepository subscriptionRepository;
    private final AuditService auditService;

    public AuthenticationResponse register(RegisterRequest request) {
        // Block ADMIN role from public registration
        if (request.getRole() != null && request.getRole().name().equals("ADMIN")) {
            throw new com.example.vently.exception.ValidationException("ADMIN accounts cannot be created through registration");
        }

        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .gender(request.getGender())
                .build();
        var savedUser = repository.save(user);
        
        // Send verification email after successful registration
        sendVerificationEmail(savedUser.getEmail());
        
        var jwtToken = jwtService.generateToken(savedUser);
        
        // Get subscription tier (defaults to FREE if not found)
        SubscriptionTier tier = SubscriptionTier.FREE;
        try {
            tier = subscriptionRepository.findByUserId(savedUser.getId())
                    .map(subscription -> subscription.getTier())
                    .orElse(SubscriptionTier.FREE);
        } catch (Exception e) {
            log.warn("Could not fetch subscription for user: {}", savedUser.getId(), e);
            tier = SubscriptionTier.FREE;
        }
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .emailVerified(savedUser.getEmailVerified())
                .subscriptionTier(tier)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            var user = repository.findByEmail(request.getEmail()).orElseThrow();
            var jwtToken = jwtService.generateToken(user);
            
            // Get subscription tier (ADMIN has no subscription — skip lookup)
            SubscriptionTier tier = SubscriptionTier.FREE;
            if (user.getRole() != com.example.vently.user.Role.ADMIN) {
                try {
                    tier = subscriptionRepository.findByUserId(user.getId())
                            .map(subscription -> subscription.getTier())
                            .orElse(SubscriptionTier.FREE);
                } catch (Exception e) {
                    log.warn("Could not fetch subscription for user: {}", user.getId(), e);
                    tier = SubscriptionTier.FREE;
                }
            }
            
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .emailVerified(user.getEmailVerified())
                    .subscriptionTier(tier)
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }
    
    /**
     * Generates a verification token and sends a verification email to the user.
     * For now, this logs the verification link (actual email sending will be implemented in Phase 7).
     * 
     * @param email the user's email address
     */
    public void sendVerificationEmail(String email) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate a unique verification token
        String token = UUID.randomUUID().toString();
        
        // Set token and expiration (24 hours from now)
        user.setVerificationToken(token);
        user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        repository.save(user);
        
        // For now, log the verification link (actual email sending comes in Phase 7)
        String verificationLink = "http://localhost:5173/verify-email?token=" + token;
        log.info("Verification email would be sent to: {}", email);
        log.info("Verification link: {}", verificationLink);
        log.info("Token expires at: {}", user.getVerificationTokenExpiresAt());
    }
    
    /**
     * Verifies a user's email address using the provided token.
     * 
     * @param token the verification token
     * @throws RuntimeException if token is invalid or expired
     */
    public void verifyEmail(String token) {
        var user = repository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        // Check if token has expired
        if (user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }
        
        // Mark email as verified
        user.setEmailVerified(true);
        user.setVerificationToken(null); // Clear the token after use
        user.setVerificationTokenExpiresAt(null);
        repository.save(user);
        
        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    /**
     * Get client IP address from HTTP request
     * 
     * @return Client IP address or "UNKNOWN" if not available
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP address", e);
        }
        return "UNKNOWN";
    }
}
