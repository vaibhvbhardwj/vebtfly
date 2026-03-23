package com.example.vently.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.application.dto.ApplicationDto;
import com.example.vently.application.dto.ApplicationResponseDto;
import com.example.vently.event.Event;
import com.example.vently.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for application management
 * Requirements: 5.1, 5.6, 6.1, 6.3, 6.4, 7.2, 7.3, 24.3
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Submit a new application (Volunteer only)
     * Requirements: 5.1
     */
    @PostMapping
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<ApplicationResponseDto> submitApplication(
            Authentication authentication,
            @Valid @RequestBody ApplicationDto applicationDto) {
        User currentUser = (User) authentication.getPrincipal();
        Application application = applicationService.submitApplication(
            currentUser.getId(),
            applicationDto.getEventId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(application));
    }

    /**
     * Withdraw an application (Volunteer only)
     * Requirements: 5.6
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<Void> withdrawApplication(
            Authentication authentication,
            @PathVariable Long id) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Verify the application belongs to the current user
        Application application = applicationService.getApplicationById(id);
        if (!application.getVolunteer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        applicationService.withdrawApplication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Accept an application (Organizer only)
     * Requirements: 6.3
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApplicationResponseDto> acceptApplication(
            Authentication authentication,
            @PathVariable Long id) {
        User currentUser = (User) authentication.getPrincipal();
        applicationService.acceptApplication(id, currentUser.getId());
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(toResponseDto(application));
    }

    /**
     * Reject an application (Organizer only)
     * Requirements: 6.4
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApplicationResponseDto> rejectApplication(
            Authentication authentication,
            @PathVariable Long id) {
        User currentUser = (User) authentication.getPrincipal();
        applicationService.rejectApplication(id, currentUser.getId());
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(toResponseDto(application));
    }

    /**
     * Confirm an application (Volunteer only)
     * Requirements: 7.2
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<ApplicationResponseDto> confirmApplication(
            Authentication authentication,
            @PathVariable Long id) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Verify the application belongs to the current user
        Application application = applicationService.getApplicationById(id);
        if (!application.getVolunteer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        applicationService.confirmApplication(id, currentUser.getId());
        application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(toResponseDto(application));
    }

    /**
     * Decline an application (Volunteer only)
     * Requirements: 7.3
     */
    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<ApplicationResponseDto> declineApplication(
            Authentication authentication,
            @PathVariable Long id) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Verify the application belongs to the current user
        Application application = applicationService.getApplicationById(id);
        if (!application.getVolunteer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        applicationService.declineApplication(id, currentUser.getId());
        application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(toResponseDto(application));
    }

    /**
     * Get current volunteer's applications (Volunteer only)
     * Requirements: 5.8
     */
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<Page<ApplicationResponseDto>> getMyApplications(
            Authentication authentication,
            Pageable pageable) {
        User currentUser = (User) authentication.getPrincipal();
        Page<Application> applications = applicationService.getVolunteerApplications(
            currentUser.getId(),
            pageable
        );
        Page<ApplicationResponseDto> responseDtos = applications.map(this::toResponseDto);
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Get applications for a specific event (Organizer only)
     * Note: This endpoint is also available at /api/v1/events/{id}/applications
     * Requirements: 6.1
     */
    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<ApplicationResponseDto>> getEventApplications(
            Authentication authentication,
            @PathVariable Long eventId) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Get applications and verify organizer owns the event
        List<Application> applications = applicationService.getEventApplications(eventId);
        
        // Verify the organizer owns the event (check first application if exists)
        if (!applications.isEmpty() && 
            !applications.get(0).getEvent().getOrganizer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<ApplicationResponseDto> responseDtos = applications.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Convert Application entity to ApplicationResponseDto
     */
    private ApplicationResponseDto toResponseDto(Application application) {
        User volunteer = application.getVolunteer();
        Event event = application.getEvent();
        LocalDateTime eventDateTime = null;
        try {
            if (event.getDate() != null && event.getTime() != null) {
                eventDateTime = event.getEventDateTime();
            }
        } catch (Exception ignored) {}

        // Parse gallery photos
        java.util.List<String> galleryPhotos = new java.util.ArrayList<>();
        if (volunteer.getGalleryPhotos() != null && !volunteer.getGalleryPhotos().isEmpty()) {
            try {
                galleryPhotos = new java.util.ArrayList<>(java.util.Arrays.asList(
                    volunteer.getGalleryPhotos().replaceAll("[\\[\\]\"]", "").split(",")));
                galleryPhotos.removeIf(String::isBlank);
            } catch (Exception ignored) {}
        }

        return ApplicationResponseDto.builder()
            .id(application.getId())
            .eventId(event.getId())
            .eventTitle(event.getTitle())
            .eventLocation(event.getLocation())
            .eventDateTime(eventDateTime)
            .volunteerId(volunteer.getId())
            .volunteerName(volunteer.getFullName())
            .volunteerEmail(volunteer.getEmail())
            .volunteerGender(volunteer.getGender())
            .volunteerDateOfBirth(volunteer.getDateOfBirth())
            .volunteerProfilePictureUrl(volunteer.getProfilePictureUrl())
            .volunteerGalleryPhotos(galleryPhotos)
            .volunteerAverageRating(0.0)
            .volunteerRatingCount(0)
            .volunteerVerificationBadge(volunteer.getVerificationBadge())
            .volunteerNoShowCount(volunteer.getNoShowCount())
            .status(application.getStatus())
            .appliedAt(application.getAppliedAt())
            .acceptedAt(application.getAcceptedAt())
            .confirmedAt(application.getConfirmedAt())
            .declinedAt(application.getDeclinedAt())
            .confirmationDeadline(application.getConfirmationDeadline())
            .updatedAt(application.getUpdatedAt())
            .build();
    }
}
