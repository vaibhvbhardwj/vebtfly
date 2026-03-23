package com.example.vently.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.vently.application.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for application responses
 * Requirements: 5.1, 5.8, 6.1, 7.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponseDto {
    
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventDateTime;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerEmail;
    private String volunteerGender;
    private LocalDate volunteerDateOfBirth;
    private String volunteerProfilePictureUrl;
    private List<String> volunteerGalleryPhotos;
    private Double volunteerAverageRating;
    private Integer volunteerRatingCount;
    private Boolean volunteerVerificationBadge;
    private Integer volunteerNoShowCount;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime declinedAt;
    private LocalDateTime confirmationDeadline;
    private LocalDateTime updatedAt;
}
