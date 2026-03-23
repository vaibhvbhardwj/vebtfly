package com.example.vently.event;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationService;
import com.example.vently.application.dto.ApplicationResponseDto;
import com.example.vently.event.dto.EventCreateDto;
import com.example.vently.event.dto.EventFilterDto;
import com.example.vently.event.dto.EventResponseDto;
import com.example.vently.event.dto.EventUpdateDto;
import com.example.vently.service.S3Service;
import com.example.vently.user.User;

import java.util.stream.Collectors;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for event management endpoints
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 24.2
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ApplicationService applicationService;
    private final S3Service s3Service;

    /**
     * Create a new event (ORGANIZER only)
     * Requirements: 3.1, 24.2
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody EventCreateDto eventDto,
            @AuthenticationPrincipal User organizer
    ) {
        EventResponseDto createdEvent = eventService.createEventFromDto(eventDto, organizer);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Update an existing event (ORGANIZER only, own events only)
     * Requirements: 3.4, 24.2
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDto eventDto,
            @AuthenticationPrincipal User organizer
    ) {
        EventResponseDto updatedEvent = eventService.updateEventFromDto(id, eventDto, organizer);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Publish an event (ORGANIZER only, own events only)
     * Requirements: 3.3, 24.2
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> publishEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User organizer
    ) {
        EventResponseDto publishedEvent = eventService.publishEventAndReturnDto(id, organizer);
        return ResponseEntity.ok(publishedEvent);
    }

    /**
     * Start an event: DEPOSIT_PAID → IN_PROGRESS, auto-generates attendance codes and notifies volunteers
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> startEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User organizer
    ) {
        EventResponseDto startedEvent = eventService.startEvent(id, organizer);
        return ResponseEntity.ok(startedEvent);
    }

    /**
     * Cancel an event (ORGANIZER only, own events only)
     * Requirements: 3.6, 14.1, 24.2
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> cancelEvent(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelled by organizer") String reason,
            @AuthenticationPrincipal User organizer
    ) {
        eventService.cancelEventByOrganizer(id, reason, organizer);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all published events with filtering and pagination
     * Requirements: 4.1, 4.2, 4.3, 4.6
     */
    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> getPublishedEvents(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String minPayment,
            @RequestParam(required = false) String maxPayment,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "date") String sortBy
    ) {
        // Build filter DTO from request parameters
        EventFilterDto filters = EventFilterDto.builder()
            .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
            .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
            .location(location)
            .minPayment(minPayment != null ? new java.math.BigDecimal(minPayment) : null)
            .maxPayment(maxPayment != null ? new java.math.BigDecimal(maxPayment) : null)
            .category(category)
            .searchQuery(searchQuery)
            .sortBy(sortBy)
            .build();
        
        Page<EventResponseDto> events = eventService.getPublishedEvents(pageable, filters);
        return ResponseEntity.ok(events);
    }

    /**
     * Get event details by ID
     * Requirements: 4.4
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        EventResponseDto event = eventService.getEventDetailsDto(id);
        return ResponseEntity.ok(event);
    }

    /**
     * Get organizer's own events (ORGANIZER only)
     * Requirements: 3.5, 24.2
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventResponseDto>> getMyEvents(
            @AuthenticationPrincipal User organizer
    ) {
        List<EventResponseDto> events = eventService.getOrganizerEventsDto(organizer.getId());
        return ResponseEntity.ok(events);
    }

    /**
     * Get applications for a specific event (ORGANIZER only)
     * Requirements: 6.1
     */
    @GetMapping("/{id}/applications")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<ApplicationResponseDto>> getEventApplications(
            @PathVariable Long id,
            @AuthenticationPrincipal User organizer
    ) {
        // Get applications and verify organizer owns the event
        List<Application> applications = applicationService.getEventApplications(id);
        
        // Verify the organizer owns the event (check first application if exists)
        if (!applications.isEmpty() && 
            !applications.get(0).getEvent().getOrganizer().getId().equals(organizer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<ApplicationResponseDto> responseDtos = applications.stream()
            .map(this::toApplicationResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Upload event image to S3 (ORGANIZER only)
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> uploadEventImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User organizer
    ) {
        try {
            String imageUrl = s3Service.uploadFile(file, "event-images");
            EventUpdateDto dto = EventUpdateDto.builder().imageUrl(imageUrl).build();
            eventService.updateEventFromDto(id, dto, organizer);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Failed to upload image";
            HttpStatus status = msg.contains("not configured") ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("error", msg));
        }
    }

    /**
     * Convert Application entity to ApplicationResponseDto
     */
    private ApplicationResponseDto toApplicationResponseDto(Application application) {
        return ApplicationResponseDto.builder()
            .id(application.getId())
            .eventId(application.getEvent().getId())
            .eventTitle(application.getEvent().getTitle())
            .eventLocation(application.getEvent().getLocation())
            .eventDateTime(application.getEvent().getEventDateTime())
            .volunteerId(application.getVolunteer().getId())
            .volunteerName(application.getVolunteer().getFullName())
            .volunteerEmail(application.getVolunteer().getEmail())
            .volunteerGender(application.getVolunteer().getGender())
            .status(application.getStatus())
            .appliedAt(application.getAppliedAt())
            .acceptedAt(application.getAcceptedAt())
            .confirmedAt(application.getConfirmedAt())
            .declinedAt(application.getDeclinedAt())
            .confirmationDeadline(application.getConfirmationDeadline())
            .updatedAt(application.getUpdatedAt())
            .build();
    }

    // TODO: This endpoint needs to be refactored to use the Application entity
    // Applications are now managed through the ApplicationController (Phase 3)
    /*
    // GOAL: Only VOLUNTEER can apply
    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<String> applyToEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User volunteer
    ) {
        eventService.applyToEvent(id, volunteer);
        return ResponseEntity.ok("Successfully applied to the event!");
    }
    */
}
