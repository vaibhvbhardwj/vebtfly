package com.example.vently.auth;

import java.time.LocalDateTime;

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
import com.example.vently.notification.EmailService;
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
    private final EmailService emailService;

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
                .phone(request.getPhone())
                .build();
        var savedUser = repository.save(user);
        
        // Send email OTP for verification
        sendEmailOtp(savedUser.getEmail());
        
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
     * Generates a 6-digit OTP and sends it to the user's email for verification.
     */
    public void sendEmailOtp(String email) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.format("%06d", (int)(Math.random() * 1000000));
        user.setEmailOtp(otp);
        user.setEmailOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        repository.save(user);

        // Send via SES if configured, otherwise log
        String subject = "Your Ventfly verification code";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px">
              <h2 style="color:#807aeb">Verify your email</h2>
              <p>Hi %s,</p>
              <p>Use the code below to verify your email address. It expires in 10 minutes.</p>
              <div style="font-size:36px;font-weight:700;letter-spacing:8px;color:#111827;background:#ebf2fa;padding:20px;border-radius:12px;text-align:center;margin:24px 0">%s</div>
              <p style="color:#6B7280;font-size:13px">If you didn't create a Ventfly account, ignore this email.</p>
            </div>
            """.formatted(user.getFullName(), otp);

        try {
            emailService.sendRawEmail(email, subject, html);
        } catch (Exception e) {
            log.warn("Could not send email OTP via SES, OTP for {}: {}", email, otp);
        }
        log.info("Email OTP for {}: {}", email, otp);
    }

    /**
     * Verifies the 6-digit email OTP.
     */
    public void verifyEmailOtp(String email, String otp) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailOtp() == null || user.getEmailOtpExpiresAt() == null) {
            throw new com.example.vently.exception.ValidationException("No OTP requested. Please request a new one.");
        }
        if (LocalDateTime.now().isAfter(user.getEmailOtpExpiresAt())) {
            throw new com.example.vently.exception.ValidationException("OTP has expired. Please request a new one.");
        }
        if (!user.getEmailOtp().equals(otp.trim())) {
            throw new com.example.vently.exception.ValidationException("Invalid OTP. Please try again.");
        }

        user.setEmailVerified(true);
        user.setEmailOtp(null);
        user.setEmailOtpExpiresAt(null);
        repository.save(user);
        log.info("Email verified via OTP for: {}", email);
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
