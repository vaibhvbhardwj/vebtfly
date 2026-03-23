package com.example.vently.user;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.rating.RatingRepository;
import com.example.vently.service.S3Service;
import com.example.vently.service.SnsService;
import com.example.vently.user.dto.EmailPreferencesRequest;
import com.example.vently.user.dto.EmailPreferencesResponse;
import com.example.vently.user.dto.UserProfileDto;
import com.example.vently.user.dto.UserStatisticsDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired(required = false)
    private SnsService snsService;

    // Allowed file types for profile pictures
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png");

    // Max file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Get user profile with ratings and basic statistics
     * Requirements: 2.1, 2.2, 2.3, 2.7
     */
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Double averageRating = ratingRepository.calculateAverageRating(userId);
        Long totalRatings = ratingRepository.countByRatedUserId(userId);

        // Parse gallery photos from JSON
        List<String> galleryPhotos = new java.util.ArrayList<>();
        if (user.getGalleryPhotos() != null && !user.getGalleryPhotos().isEmpty()) {
            try {
                galleryPhotos = new java.util.ArrayList<>(Arrays.asList(
                        user.getGalleryPhotos().replaceAll("[\\[\\]\"]", "").split(",")));
                galleryPhotos.removeIf(String::isBlank);
            } catch (Exception e) {
                // If parsing fails, return empty list
                galleryPhotos = new java.util.ArrayList<>();
            }
        }

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .bio(user.getBio())
                .phone(user.getPhone())
                .phoneVerified(user.getPhoneVerified())
                .profilePictureUrl(user.getProfilePictureUrl())
                .galleryPhotos(galleryPhotos)
                .verificationBadge(user.getVerificationBadge())
                .noShowCount(user.getNoShowCount())
                .accountStatus(user.getAccountStatus())
                .skills(user.getSkills())
                .availability(user.getAvailability())
                .experience(user.getExperience())
                .organizationName(user.getOrganizationName())
                .organizationDetails(user.getOrganizationDetails())
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .build();
    }

    /**
     * Update user profile information
     * Requirements: 2.1, 2.2, 2.3
     */
    @Transactional
    public UserProfileDto updateProfile(Long userId, UserProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update common fields
        if (dto.getFullName() != null) {
            // Lock fullName once set
            if (user.getFullName() == null || user.getFullName().isBlank()) {
                user.setFullName(dto.getFullName());
            }
        }
        if (dto.getGender() != null) {
            // Lock gender once set
            if (user.getGender() == null || user.getGender().isBlank()) {
                user.setGender(dto.getGender());
            }
        }
        if (dto.getDateOfBirth() != null) {
            // Lock dateOfBirth once set
            if (user.getDateOfBirth() == null) {
                user.setDateOfBirth(dto.getDateOfBirth());
            }
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        // Update volunteer-specific fields
        if (user.getRole() == Role.VOLUNTEER) {
            if (dto.getSkills() != null) {
                user.setSkills(dto.getSkills());
            }
            if (dto.getAvailability() != null) {
                user.setAvailability(dto.getAvailability());
            }
            if (dto.getExperience() != null) {
                user.setExperience(dto.getExperience());
            }
        }

        // Update organizer-specific fields
        if (user.getRole() == Role.ORGANIZER) {
            if (dto.getOrganizationName() != null) {
                user.setOrganizationName(dto.getOrganizationName());
            }
            if (dto.getOrganizationDetails() != null) {
                user.setOrganizationDetails(dto.getOrganizationDetails());
            }
        }

        User savedUser = userRepository.save(user);

        // Return updated profile
        return getUserProfile(savedUser.getId());
    }

    /**
     * Get user statistics including events/applications count and ratings
     * Requirements: 2.7
     */
    @Transactional(readOnly = true)
    public UserStatisticsDto getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Double averageRating = ratingRepository.calculateAverageRating(userId);
        Long totalRatings = ratingRepository.countByRatedUserId(userId);

        UserStatisticsDto.UserStatisticsDtoBuilder statsBuilder = UserStatisticsDto.builder()
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .noShowCount(user.getNoShowCount());

        // Calculate volunteer-specific statistics
        if (user.getRole() == Role.VOLUNTEER) {
            long totalApplications = applicationRepository.countByVolunteerId(userId);
            long confirmedApplications = applicationRepository.countByVolunteerIdAndStatus(
                    userId, ApplicationStatus.CONFIRMED);

            statsBuilder
                    .totalApplications(totalApplications)
                    .confirmedApplications(confirmedApplications);
        }

        // Calculate organizer-specific statistics
        if (user.getRole() == Role.ORGANIZER) {
            long totalEvents = eventRepository.findByOrganizerId(userId).size();
            long completedEvents = eventRepository.findByOrganizerId(userId).stream()
                    .filter(event -> event.getStatus() == EventStatus.COMPLETED)
                    .count();

            statsBuilder
                    .totalEvents(totalEvents)
                    .completedEvents(completedEvents);
        }

        return statsBuilder.build();
    }

    /**
     * Check and update account status if suspension has expired
     * Requirements: 11.3, 11.4, 20.5
     */
    @Transactional
    public void checkAndUpdateSuspensionStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // If account is suspended and suspension period has expired, reactivate
        if (user.getAccountStatus() == AccountStatus.SUSPENDED && 
            user.getSuspendedUntil() != null && 
            LocalDateTime.now().isAfter(user.getSuspendedUntil())) {
            
            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setSuspendedUntil(null);
            userRepository.save(user);
        }
    }

    /**
     * Upload profile picture to S3 and update user profile
     * Requirements: 2.5, 26.7
     */
    @Transactional
    public String uploadProfilePicture(Long userId, MultipartFile file) {
        // Check if S3Service is available
        if (s3Service == null || !s3Service.isConfigured()) {
            throw new RuntimeException("S3 service is not configured. Please configure AWS credentials.");
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of 5MB. File size: " + file.getSize() + " bytes");
        }

        // Validate file type (jpg, jpeg, png)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only JPG, JPEG, and PNG files are allowed. Received: " + contentType);
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        try {
            // Upload to S3
            String fileUrl = s3Service.uploadFile(file, "profile-pictures");

            // Update user profile picture URL
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage(), e);
        }
    }

    /**
     * Get user email notification preferences
     * Requirements: 18.7
     */
    @Transactional(readOnly = true)
    public EmailPreferencesResponse getEmailPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return EmailPreferencesResponse.fromUser(user);
    }

    /**
     * Update user email notification preferences
     * Requirements: 18.7
     */
    @Transactional
    public EmailPreferencesResponse updateEmailPreferences(Long userId, EmailPreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update email preferences
        user.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        user.setNotifyOnApplicationStatus(request.getNotifyOnApplicationStatus());
        user.setNotifyOnEventCancellation(request.getNotifyOnEventCancellation());
        user.setNotifyOnPayment(request.getNotifyOnPayment());
        user.setNotifyOnDisputeResolution(request.getNotifyOnDisputeResolution());

        userRepository.save(user);

        return EmailPreferencesResponse.fromUser(user);
    }

    /**
     * Upload gallery photo for user (max 3 photos)
     * Requirements: 2.5, 26.7
     */
    @Transactional
    public void uploadGalleryPhoto(Long userId, MultipartFile file) {
        // Check if S3Service is available
        if (s3Service == null || !s3Service.isConfigured()) {
            throw new RuntimeException("S3 service is not configured. Please configure AWS credentials.");
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of 5MB. File size: " + file.getSize() + " bytes");
        }

        // Validate file type (jpg, jpeg, png)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only JPG, JPEG, and PNG files are allowed. Received: " + contentType);
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        try {
            // Parse existing gallery photos
            List<String> photos = new java.util.ArrayList<>();
            if (user.getGalleryPhotos() != null && !user.getGalleryPhotos().isEmpty()) {
                photos = new java.util.ArrayList<>(Arrays.asList(
                        user.getGalleryPhotos().replaceAll("[\\[\\]\"]", "").split(",")));
                photos.removeIf(String::isBlank);
            }

            // Check max 3 photos limit
            if (photos.size() >= 3) {
                throw new IllegalArgumentException("Maximum 3 photos allowed in gallery");
            }

            // Upload to S3
            String fileUrl = s3Service.uploadFile(file, "gallery-photos");
            photos.add(fileUrl);

            // Convert to JSON array format
            String galleryJson = "[\"" + String.join("\",\"", photos) + "\"]";
            user.setGalleryPhotos(galleryJson);
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload gallery photo: " + e.getMessage(), e);
        }
    }

    /**
     * Remove gallery photo for user
     * Requirements: 2.5, 26.7
     */
    @Transactional
    public void removeGalleryPhoto(Long userId, String photoUrl) {        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getGalleryPhotos() == null || user.getGalleryPhotos().isEmpty()) {
            throw new IllegalArgumentException("No gallery photos to remove");
        }

        try {
            // Parse existing gallery photos
            List<String> photos = new java.util.ArrayList<>(Arrays.asList(
                    user.getGalleryPhotos().replaceAll("[\\[\\]\"]", "").split(",")));
            photos.removeIf(String::isBlank);

            // Remove the photo
            if (!photos.remove(photoUrl)) {
                throw new IllegalArgumentException("Photo not found in gallery");
            }

            // Convert back to JSON array format
            if (photos.isEmpty()) {
                user.setGalleryPhotos(null);
            } else {
                String galleryJson = "[\"" + String.join("\",\"", photos) + "\"]";
                user.setGalleryPhotos(galleryJson);
            }

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove gallery photo: " + e.getMessage(), e);
        }
    }

    /**
     * Send OTP to user's phone number for verification.
     * Returns the OTP string if SNS is not configured or fails (dev/fallback mode),
     * returns null if SMS was sent successfully via SNS.
     */
    @Transactional
    public String sendPhoneOtp(Long userId, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Normalize phone to E.164 format (add +91 for India if not present)
        String normalizedPhone = normalizePhone(phone);

        // Check if this phone is already verified on another account
        long existingCount = userRepository.countVerifiedPhoneOnOtherAccounts(normalizedPhone, userId);
        if (existingCount > 0) {
            throw new IllegalArgumentException("This phone number is already linked to another account.");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", (int)(Math.random() * 1000000));

        user.setPhone(normalizedPhone);
        user.setPhoneOtp(otp);
        user.setPhoneOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        user.setPhoneVerified(false);
        userRepository.save(user);

        if (snsService != null && snsService.isConfigured()) {
            try {
                snsService.sendSms(normalizedPhone, "Your Vently verification code is: " + otp + ". Valid for 10 minutes.");
                System.out.println("[SNS] OTP SMS sent to " + normalizedPhone);
                return null; // SMS sent — don't expose OTP
            } catch (Exception e) {
                System.err.println("[SNS] Failed to send SMS to " + normalizedPhone + ": " + e.getMessage());
                System.out.println("[FALLBACK] OTP for " + normalizedPhone + " is: " + otp);
                return otp; // Return OTP so frontend can show it
            }
        } else {
            System.out.println("[DEV] Phone OTP for " + normalizedPhone + ": " + otp);
            return otp; // Return OTP in dev mode
        }
    }

    /**
     * Verify OTP entered by user
     */
    @Transactional
    public void verifyPhoneOtp(Long userId, String otp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPhoneOtp() == null || user.getPhoneOtpExpiresAt() == null) {
            throw new IllegalArgumentException("No OTP requested. Please request a new OTP.");
        }
        if (LocalDateTime.now().isAfter(user.getPhoneOtpExpiresAt())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }
        if (!user.getPhoneOtp().equals(otp.trim())) {
            throw new IllegalArgumentException("Invalid OTP. Please try again.");
        }

        user.setPhoneVerified(true);
        user.setPhoneOtp(null);
        user.setPhoneOtpExpiresAt(null);
        userRepository.save(user);
    }

    /**
     * Mark phone as verified after Firebase token validation.
     * Called by the backend after verifying the Firebase ID token.
     */
    @Transactional
    public void markPhoneVerified(Long userId, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setPhoneOtp(null);
        user.setPhoneOtpExpiresAt(null);
        userRepository.save(user);
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+")) return digits;
        if (digits.startsWith("0")) digits = digits.substring(1);
        // Default to India (+91) if no country code
        if (digits.length() == 10) return "+91" + digits;
        return "+" + digits;
    }

}