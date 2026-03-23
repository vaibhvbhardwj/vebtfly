package com.example.vently.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.user.dto.EmailPreferencesRequest;
import com.example.vently.user.dto.EmailPreferencesResponse;
import com.example.vently.user.dto.UserProfileDto;
import com.example.vently.user.dto.UserStatisticsDto;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import java.util.Map;

/**
 * REST controller for user profile management
 * Requirements: 2.1, 2.2, 2.3, 2.7, 24.6
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current authenticated user's profile
     * Requirements: 2.1, 2.2, 2.3, 2.7
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserProfileDto profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current authenticated user's profile
     * Requirements: 2.1, 2.2, 2.3
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(
            Authentication authentication,
            @RequestBody UserProfileDto profileDto) {
        User currentUser = (User) authentication.getPrincipal();
        UserProfileDto updatedProfile = userService.updateProfile(currentUser.getId(), profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Get any user's public profile by ID
     * Requirements: 2.1, 2.7
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long id) {
        UserProfileDto profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user statistics by ID
     * Requirements: 2.7
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserStatisticsDto> getUserStatistics(@PathVariable Long id) {
        UserStatisticsDto statistics = userService.getUserStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Upload profile picture for current authenticated user
     * Requirements: 2.5, 26.7
     */
    @PostMapping("/profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        User currentUser = (User) authentication.getPrincipal();
        String fileUrl = userService.uploadProfilePicture(currentUser.getId(), file);
        UserProfileDto profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Upload gallery photo for current authenticated user
     * Requirements: 2.5, 26.7
     */
    @PostMapping("/gallery-photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> uploadGalleryPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        User currentUser = (User) authentication.getPrincipal();
        userService.uploadGalleryPhoto(currentUser.getId(), file);
        UserProfileDto profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Remove gallery photo for current authenticated user
     * Requirements: 2.5, 26.7
     */
    @DeleteMapping("/gallery-photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> removeGalleryPhoto(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User currentUser = (User) authentication.getPrincipal();
        String photoUrl = request.get("photoUrl");
        userService.removeGalleryPhoto(currentUser.getId(), photoUrl);
        UserProfileDto profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Get current user's email notification preferences
     * Requirements: 18.7
     */
    @GetMapping("/email-preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailPreferencesResponse> getEmailPreferences(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        EmailPreferencesResponse preferences = userService.getEmailPreferences(currentUser.getId());
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update current user's email notification preferences
     * Requirements: 18.7
     */
    @PutMapping("/email-preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailPreferencesResponse> updateEmailPreferences(
            Authentication authentication,
            @Valid @RequestBody EmailPreferencesRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        EmailPreferencesResponse preferences = userService.updateEmailPreferences(currentUser.getId(), request);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Send OTP to phone number for verification
     */
    @PostMapping("/phone/send-otp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> sendPhoneOtp(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User currentUser = (User) authentication.getPrincipal();
        String phone = request.get("phone");
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is required"));
        }
        try {
            String devOtp = userService.sendPhoneOtp(currentUser.getId(), phone);
            if (devOtp != null) {
                return ResponseEntity.ok(Map.of("message", "OTP sent (dev mode)", "devOtp", devOtp));
            }
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already linked to another account")) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                        .body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Verify OTP for phone number
     */
    @PostMapping("/phone/verify-otp")    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> verifyPhoneOtp(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User currentUser = (User) authentication.getPrincipal();
        String otp = request.get("otp");
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP is required");
        }
        userService.verifyPhoneOtp(currentUser.getId(), otp);
        UserProfileDto profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

}

