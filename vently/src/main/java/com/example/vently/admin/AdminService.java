package com.example.vently.admin;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.admin.dto.UserFilterRequest;
import com.example.vently.audit.AuditService;
import com.example.vently.notification.EmailService;
import com.example.vently.notification.NotificationService;
import com.example.vently.user.AccountStatus;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    /**
     * Get all users with pagination and filters
     * Requirement 20.1: Display all user accounts with filtering options
     */
    public Page<User> getAllUsers(UserFilterRequest filterRequest) {
        int page = Optional.ofNullable(filterRequest.getPage()).orElse(0);
        int size = Optional.ofNullable(filterRequest.getSize()).orElse(20);
        
        // Limit page size to prevent performance issues
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Build specification for filtering
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (filterRequest.getRole() != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("role"), filterRequest.getRole()));
            }

            if (filterRequest.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("accountStatus"), filterRequest.getStatus()));
            }

            if (filterRequest.getVerificationBadge() != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("verificationBadge"), filterRequest.getVerificationBadge()));
            }

            return predicate;
        };

        return userRepository.findAll(spec, pageable);
    }

    /**
     * Suspend user account for specified duration
     * Requirement 20.3: Allow admins to suspend user accounts with specified duration
     */
    @Transactional
    public User suspendUser(Long userId, Integer durationInDays, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new IllegalStateException("Cannot suspend a banned user");
        }

        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.setSuspendedUntil(LocalDateTime.now().plusDays(durationInDays));
        
        User savedUser = userRepository.save(user);
        
        logger.info("User {} suspended for {} days. Reason: {}", userId, durationInDays, reason);

        // Log admin action
        Map<String, Object> details = new HashMap<>();
        details.put("durationInDays", durationInDays);
        details.put("reason", reason);
        details.put("suspendedUntil", savedUser.getSuspendedUntil());
        auditService.logAdminAction(user, "SUSPEND_USER", userId, details, "UNKNOWN");

        // Send notification
        String message = String.format(
            "Your account has been suspended for %d days. Reason: %s. You will be able to access your account after %s.",
            durationInDays,
            reason != null ? reason : "Policy violation",
            user.getSuspendedUntil()
        );
        notificationService.createNotification(user, "ACCOUNT_SUSPENDED", "Account Suspended", message);

        return savedUser;
    }

    /**
     * Permanently ban user account
     * Requirement 20.4: Allow admins to permanently ban user accounts
     */
    @Transactional
    public User banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setAccountStatus(AccountStatus.BANNED);
        user.setSuspendedUntil(null); // Clear any suspension date
        
        User savedUser = userRepository.save(user);
        
        logger.info("User {} permanently banned. Reason: {}", userId, reason);

        // Log admin action
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        auditService.logAdminAction(user, "BAN_USER", userId, details, "UNKNOWN");

        // Send notification
        String message = String.format(
            "Your account has been permanently banned. Reason: %s. If you believe this is an error, please contact support.",
            reason != null ? reason : "Severe policy violation"
        );
        notificationService.createNotification(user, "ACCOUNT_BANNED", "Account Banned", message);

        return savedUser;
    }

    /**
     * Reset user password and send temporary password via email
     * Requirement 20.7: Allow admins to reset user passwords upon request
     */
    @Transactional
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();
        
        // Hash and save the temporary password
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);
        
        logger.info("Password reset for user {}", userId);

        // Send email with temporary password
        sendPasswordResetEmail(user, temporaryPassword);

        // Send in-app notification
        notificationService.createNotification(
            user, 
            "PASSWORD_RESET",
            "Password Reset", 
            "Your password has been reset by an administrator. A temporary password has been sent to your email."
        );

        return temporaryPassword;
    }

    /**
     * Grant verification badge to user
     * Requirement 20.6: Allow admins to manually grant or revoke verification badges
     */
    @Transactional
    public User grantVerificationBadge(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.getVerificationBadge()) {
            throw new IllegalStateException("User already has verification badge");
        }

        user.setVerificationBadge(true);
        User savedUser = userRepository.save(user);
        
        logger.info("Verification badge granted to user {}", userId);

        // Log admin action
        auditService.logAdminAction(user, "GRANT_VERIFICATION", userId, null, "UNKNOWN");

        // Send notification
        notificationService.createNotification(
            user, 
            "VERIFICATION_GRANTED",
            "Verification Badge Granted", 
            "Congratulations! Your account has been verified by our admin team. Your profile now displays a verification badge."
        );

        return savedUser;
    }

    /**
     * Revoke verification badge from user
     * Requirement 20.6: Allow admins to manually grant or revoke verification badges
     */
    @Transactional
    public User revokeVerificationBadge(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (!user.getVerificationBadge()) {
            throw new IllegalStateException("User does not have verification badge");
        }

        user.setVerificationBadge(false);
        User savedUser = userRepository.save(user);
        
        logger.info("Verification badge revoked from user {}", userId);

        // Log admin action
        auditService.logAdminAction(user, "REVOKE_VERIFICATION", userId, null, "UNKNOWN");

        // Send notification
        notificationService.createNotification(
            user, 
            "VERIFICATION_REVOKED",
            "Verification Badge Revoked", 
            "Your verification badge has been revoked by our admin team."
        );

        return savedUser;
    }

    /**
     * Manually adjust no-show count for disputes
     * Requirement 11.7: Allow admins to manually adjust no-show counts in case of disputes
     */
    @Transactional
    public User adjustNoShowCount(Long userId, Integer newCount, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (newCount < 0) {
            throw new IllegalArgumentException("No-show count cannot be negative");
        }

        Integer oldCount = user.getNoShowCount();
        user.setNoShowCount(newCount);
        
        // Update account status based on new no-show count
        updateAccountStatusBasedOnNoShows(user);
        
        User savedUser = userRepository.save(user);
        
        logger.info("No-show count adjusted for user {} from {} to {}. Reason: {}", 
            userId, oldCount, newCount, reason);

        // Log admin action
        Map<String, Object> details = new HashMap<>();
        details.put("oldCount", oldCount);
        details.put("newCount", newCount);
        details.put("reason", reason);
        auditService.logAdminAction(user, "ADJUST_NO_SHOWS", userId, details, "UNKNOWN");

        // Send notification
        String message = String.format(
            "Your no-show count has been adjusted from %d to %d by an administrator. Reason: %s",
            oldCount,
            newCount,
            reason != null ? reason : "Dispute resolution"
        );
        notificationService.createNotification(user, "NO_SHOW_ADJUSTED", "No-Show Count Adjusted", message);

        return savedUser;
    }

    /**
     * Update account status based on no-show count
     * Requirement 11.3: Suspend account for 30 days when 3 no-shows
     * Requirement 11.4: Permanently ban when 5 no-shows
     */
    private void updateAccountStatusBasedOnNoShows(User user) {
        int noShowCount = user.getNoShowCount();
        
        if (noShowCount >= 5) {
            user.setAccountStatus(AccountStatus.BANNED);
            user.setSuspendedUntil(null);
            logger.info("User {} banned due to 5+ no-shows", user.getId());
        } else if (noShowCount >= 3) {
            if (user.getAccountStatus() != AccountStatus.BANNED) {
                user.setAccountStatus(AccountStatus.SUSPENDED);
                user.setSuspendedUntil(LocalDateTime.now().plusDays(30));
                logger.info("User {} suspended for 30 days due to 3+ no-shows", user.getId());
            }
        } else {
            // If count is reduced below thresholds, reactivate account
            if (user.getAccountStatus() == AccountStatus.SUSPENDED && 
                (user.getSuspendedUntil() == null || LocalDateTime.now().isAfter(user.getSuspendedUntil()))) {
                user.setAccountStatus(AccountStatus.ACTIVE);
                user.setSuspendedUntil(null);
                logger.info("User {} reactivated after no-show count adjustment", user.getId());
            }
        }
    }

    /**
     * Generate a secure random temporary password
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(TEMP_PASSWORD_CHARS.length());
            password.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        
        return password.toString();
    }

    /**
     * Send password reset email with temporary password
     */
    private void sendPasswordResetEmail(User user, String temporaryPassword) {
        String subject = "Password Reset - Vently";
        String htmlBody = buildPasswordResetEmailTemplate(user, temporaryPassword);
        
        // Use a simple email sending approach
        try {
            // In a real implementation, this would use the EmailService
            // For now, we'll log it
            logger.info("Password reset email would be sent to: {}", user.getEmail());
            logger.info("Temporary password: {}", temporaryPassword);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}", user.getEmail(), e);
        }
    }

    /**
     * Build password reset email template
     */
    private String buildPasswordResetEmailTemplate(User user, String temporaryPassword) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .password-box { background-color: #FEF3C7; padding: 15px; margin: 15px 0; border-left: 4px solid #F59E0B; font-family: monospace; font-size: 18px; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .warning { color: #DC2626; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Your password has been reset by an administrator.</p>
                        <p>Your temporary password is:</p>
                        <div class="password-box">%s</div>
                        <p class="warning">IMPORTANT: Please change this password immediately after logging in.</p>
                        <p>For security reasons, we recommend:</p>
                        <ul>
                            <li>Use a strong, unique password</li>
                            <li>Don't share your password with anyone</li>
                            <li>Enable two-factor authentication if available</li>
                        </ul>
                        <p>If you didn't request this password reset, please contact support immediately.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(user.getFullName(), temporaryPassword);
    }
}
