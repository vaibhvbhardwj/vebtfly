package com.example.vently.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.audit.AuditService;
import com.example.vently.event.dto.EventCreateDto;
import com.example.vently.event.dto.EventFilterDto;
import com.example.vently.event.dto.EventResponseDto;
import com.example.vently.event.dto.EventUpdateDto;
import com.example.vently.payment.Payment;
import com.example.vently.payment.PaymentRepository;
import com.example.vently.attendance.AttendanceService;
import com.example.vently.rating.RatingRepository;
import com.example.vently.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final RatingRepository ratingRepository;
    private final com.example.vently.subscription.SubscriptionService subscriptionService;
    private final com.example.vently.notification.NotificationService notificationService;
    private final AuditService auditService;
    private final AttendanceService attendanceService;

    /**
     * Create a new event with DRAFT status
     * Requirements: 3.1, 3.7, 3.8, 28.1
     */
    @Transactional
    public Event createEvent(Event event, User organizer) {
        // Check tier-based event creation limits
        if (!subscriptionService.canCreateEvent(organizer.getId())) {
            throw new IllegalStateException(
                "Event creation limit reached for your subscription tier. " +
                "Upgrade to Premium for unlimited events."
            );
        }
        
        // Validate event data
        validateEventData(event);
        
        // Set organizer and initial status
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);
        
        return eventRepository.save(event);
    }

    /**
     * Update an existing event (only allowed for DRAFT status)
     * Requirements: 3.4
     */
    @Transactional
    public Event updateEvent(Long eventId, Event updatedEvent) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        
        // Allow updates for DRAFT and PUBLISHED events (but not other statuses)
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalStateException("Only DRAFT and PUBLISHED events can be updated. Current status: " + event.getStatus());
        }
        
        // Validate updated data
        validateEventData(updatedEvent);
        
        // Update fields (excluding payment which cannot be changed)
        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setLocation(updatedEvent.getLocation());
        event.setDate(updatedEvent.getDate());
        event.setTime(updatedEvent.getTime());
        event.setRequiredVolunteers(updatedEvent.getRequiredVolunteers());
        event.setCategory(updatedEvent.getCategory());
        event.setApplicationDeadline(updatedEvent.getApplicationDeadline());
        if (updatedEvent.getImageUrl() != null) {
            event.setImageUrl(updatedEvent.getImageUrl());
        }
        
        return eventRepository.save(event);
    }

    /**
     * Publish an event (transition DRAFT → PUBLISHED)
     * Requirements: 3.3, 28.2
     */
    @Transactional
    public Event publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        
        // Validate state transition
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT events can be published. Current status: " + event.getStatus());
        }
        
        // Validate event is ready to be published
        validateEventForPublishing(event);
        
        // Transition to PUBLISHED
        transitionEventStatus(event, EventStatus.PUBLISHED);
        
        Event savedEvent = eventRepository.save(event);
        
        // Log event state transition
        auditService.logEventStateTransition(
            event.getOrganizer(),
            eventId,
            EventStatus.DRAFT.toString(),
            EventStatus.PUBLISHED.toString(),
            "UNKNOWN"
        );
        
        return savedEvent;
    }

    /**
     * Cancel an event with refund handling based on cancellation timing
     * Requirements: 3.6, 14.1, 14.2, 14.3, 14.4, 14.7
     */
    @Transactional
    public void cancelEvent(Long eventId, String reason) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        
        // Cannot cancel already completed or cancelled events
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel event with status: " + event.getStatus());
        }
        
        EventStatus previousStatus = event.getStatus();
        
        // Store cancellation reason
        event.setCancellationReason(reason);
        
        // Calculate refund percentage based on timing
        BigDecimal refundPercentage = calculateRefundPercentage(event);
        
        // Process refund if deposit was made
        if (event.getStatus() == EventStatus.DEPOSIT_PAID || 
            event.getStatus() == EventStatus.IN_PROGRESS) {
            processEventCancellationRefund(event, refundPercentage);
        }
        
        // Update all confirmed/accepted applications to CANCELLED
        List<Application> applications = applicationRepository.findByEventId(eventId);
        for (Application application : applications) {
            if (application.getStatus() == ApplicationStatus.CONFIRMED || 
                application.getStatus() == ApplicationStatus.ACCEPTED) {
                application.setStatus(ApplicationStatus.CANCELLED);
                applicationRepository.save(application);
                
                // Send notification to each volunteer
                notificationService.createNotification(
                    application.getVolunteer(),
                    "EVENT_CANCELLED",
                    "Event Cancelled",
                    String.format("The event '%s' has been cancelled. Reason: %s", 
                        event.getTitle(),
                        reason != null ? reason : "Not specified")
                );
            }
        }
        
        // Transition to CANCELLED
        transitionEventStatus(event, EventStatus.CANCELLED);
        eventRepository.save(event);
        
        // Log event state transition
        auditService.logEventStateTransition(
            event.getOrganizer(),
            eventId,
            previousStatus.toString(),
            EventStatus.CANCELLED.toString(),
            "UNKNOWN"
        );
    }

    /**
     * Transition event status with state machine validation
     * Requirements: 28.1, 28.2, 28.3, 28.4, 28.5, 28.6, 28.7
     */
    @Transactional
    public void transitionEventStatus(Event event, EventStatus newStatus) {
        EventStatus currentStatus = event.getStatus();
        
        // Validate state transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid state transition from %s to %s", currentStatus, newStatus)
            );
        }
        
        event.setStatus(newStatus);
    }

    /**
     * Get all events
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Get event by ID
     */
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
    }

    /**
     * Get events by organizer
     */
    public List<Event> getOrganizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    /**
     * Create event from DTO
     * Requirements: 3.1, 28.1
     */
    @Transactional
    public EventResponseDto createEventFromDto(EventCreateDto dto, User organizer) {
        Event event = Event.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .location(dto.getLocation())
            .date(dto.getDate())
            .time(dto.getTime())
            .requiredVolunteers(dto.getRequiredVolunteers())
            .requiredMaleVolunteers(dto.getRequiredMaleVolunteers() != null ? dto.getRequiredMaleVolunteers() : 0)
            .requiredFemaleVolunteers(dto.getRequiredFemaleVolunteers() != null ? dto.getRequiredFemaleVolunteers() : 0)
            .paymentPerVolunteer(dto.getPaymentPerVolunteer())
            .paymentPerMaleVolunteer(dto.getPaymentPerMaleVolunteer())
            .paymentPerFemaleVolunteer(dto.getPaymentPerFemaleVolunteer())
            .category(dto.getCategory())
            .applicationDeadline(dto.getApplicationDeadline())
            .imageUrl(dto.getImageUrl())
            .build();
        
        Event savedEvent = createEvent(event, organizer);
        return convertToResponseDto(savedEvent);
    }

    /**
     * Update event from DTO
     * Requirements: 3.4
     */
    @Transactional
    public EventResponseDto updateEventFromDto(Long eventId, EventUpdateDto dto, User organizer) {
        Event event = getEventById(eventId);
        
        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("You can only update your own events");
        }
        
        // Build updated event with only non-null fields from DTO (payment always preserved from existing event)
        Event updatedEvent = Event.builder()
            .title(dto.getTitle() != null ? dto.getTitle() : event.getTitle())
            .description(dto.getDescription() != null ? dto.getDescription() : event.getDescription())
            .location(dto.getLocation() != null ? dto.getLocation() : event.getLocation())
            .date(dto.getDate() != null ? dto.getDate() : event.getDate())
            .time(dto.getTime() != null ? dto.getTime() : event.getTime())
            .requiredVolunteers(dto.getRequiredVolunteers() != null ? dto.getRequiredVolunteers() : event.getRequiredVolunteers())
            .paymentPerVolunteer(event.getPaymentPerVolunteer()) // always preserve existing payment
            .category(dto.getCategory() != null ? dto.getCategory() : event.getCategory())
            .applicationDeadline(dto.getApplicationDeadline() != null ? dto.getApplicationDeadline() : event.getApplicationDeadline())
            .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : event.getImageUrl())
            .build();
        
        Event savedEvent = updateEvent(eventId, updatedEvent);
        return convertToResponseDto(savedEvent);
    }

    /**
     * Publish event and return DTO
     * Requirements: 3.3, 28.2
     */
    @Transactional
    public EventResponseDto publishEventAndReturnDto(Long eventId, User organizer) {
        Event event = getEventById(eventId);
        
        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("You can only publish your own events");
        }
        
        Event publishedEvent = publishEvent(eventId);
        return convertToResponseDto(publishedEvent);
    }

    /**
     * Cancel event with organizer verification
     * Requirements: 3.6, 14.1
     */
    @Transactional
    public void cancelEventByOrganizer(Long eventId, String reason, User organizer) {
        Event event = getEventById(eventId);
        
        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("You can only cancel your own events");
        }
        
        cancelEvent(eventId, reason);
    }

    /**
     * Start an event: transition DEPOSIT_PAID → IN_PROGRESS and auto-generate attendance codes
     */
    @Transactional
    public EventResponseDto startEvent(Long eventId, User organizer) {
        Event event = getEventById(eventId);

        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("You can only start your own events");
        }

        if (event.getStatus() != EventStatus.DEPOSIT_PAID) {
            throw new IllegalStateException("Only DEPOSIT_PAID events can be started. Current status: " + event.getStatus());
        }

        transitionEventStatus(event, EventStatus.IN_PROGRESS);
        eventRepository.save(event);

        // Auto-generate attendance codes and notify volunteers
        try {
            attendanceService.generateAttendanceCodes(eventId);
        } catch (Exception e) {
            log.warn("Could not auto-generate attendance codes for event {}: {}", eventId, e.getMessage());
        }

        auditService.logEventStateTransition(
            organizer, eventId,
            EventStatus.DEPOSIT_PAID.toString(),
            EventStatus.IN_PROGRESS.toString(),
            "UNKNOWN"
        );

        return convertToResponseDto(event);
    }

    /**
     * Get event details as DTO
     * Requirements: 4.4
     */
    public EventResponseDto getEventDetailsDto(Long eventId) {
        Event event = getEventById(eventId);
        return convertToResponseDto(event);
    }

    /**
     * Get organizer events as DTOs
     * Requirements: 3.5
     */
    public List<EventResponseDto> getOrganizerEventsDto(Long organizerId) {
        List<Event> events = getOrganizerEvents(organizerId);
        return events.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Get published events with filtering, search, and pagination
     * Requirements: 4.1, 4.2, 4.3, 4.6, 29.1, 29.4, 29.5
     */
    public Page<EventResponseDto> getPublishedEvents(Pageable pageable, EventFilterDto filters) {
        // Build specification from filters
        Specification<Event> spec = EventSpecification.buildSpecification(filters);
        
        // Apply sorting
        Pageable sortedPageable = applySorting(pageable, filters.getSortBy());
        
        // Fetch events with filters and pagination
        Page<Event> eventPage = eventRepository.findAll(spec, sortedPageable);
        
        // Convert to DTOs
        List<EventResponseDto> eventDtos = eventPage.getContent().stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
        
        return new PageImpl<>(eventDtos, sortedPageable, eventPage.getTotalElements());
    }

    /**
     * Apply sorting based on sortBy parameter
     * Requirements: 4.6
     */
    private Pageable applySorting(Pageable pageable, String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "date"; // Default sort by date
        }
        
        Sort sort;
        switch (sortBy.toLowerCase()) {
            case "payment":
                // Sort by payment amount descending
                sort = Sort.by(Sort.Direction.DESC, "paymentPerVolunteer");
                break;
            case "relevance":
                // Sort by relevance (for now, use created date as proxy)
                // In a real system, this would use search relevance scoring
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "date":
            default:
                // Sort by event date ascending (soonest first)
                sort = Sort.by(Sort.Direction.ASC, "date");
                break;
        }
        
        return org.springframework.data.domain.PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort
        );
    }

    /**
     * Convert Event entity to EventResponseDto
     * Requirements: 4.4, 4.5
     */
    private EventResponseDto convertToResponseDto(Event event) {
        User organizer = event.getOrganizer();
        
        // Calculate application count and remaining slots
        List<Application> applications = applicationRepository.findByEventId(event.getId());
        long applicationCount = applications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.PENDING || 
                          app.getStatus() == ApplicationStatus.ACCEPTED ||
                          app.getStatus() == ApplicationStatus.CONFIRMED)
            .count();
        
        long confirmedCount = applications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.CONFIRMED)
            .count();
        
        int remainingSlots = event.getRequiredVolunteers() - (int) confirmedCount;
        
        // Get organizer rating (handle null case)
        Double organizerRating = null;
        Integer ratingCount = 0;
        try {
            organizerRating = ratingRepository.calculateAverageRating(organizer.getId());
            // Count total ratings for this organizer
            ratingCount = (int) ratingRepository.countByRatedUserId(organizer.getId());
        } catch (Exception e) {
            log.warn("Could not calculate organizer rating for organizer {}: {}", organizer.getId(), e.getMessage());
            organizerRating = null;
        }
        
        // Build organizer info object
        EventResponseDto.OrganizerInfo organizerInfo = EventResponseDto.OrganizerInfo.builder()
            .id(organizer.getId())
            .name(organizer.getFullName())
            .organization(organizer.getOrganizationName())
            .averageRating(organizerRating)
            .ratingCount(ratingCount)
            .verificationBadge(organizer.getVerificationBadge())
            .build();
        
        return EventResponseDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .location(event.getLocation())
            .date(event.getDate())
            .time(event.getTime())
            .requiredVolunteers(event.getRequiredVolunteers())
            .requiredMaleVolunteers(event.getRequiredMaleVolunteers() != null ? event.getRequiredMaleVolunteers() : 0)
            .requiredFemaleVolunteers(event.getRequiredFemaleVolunteers() != null ? event.getRequiredFemaleVolunteers() : 0)
            .paymentPerVolunteer(event.getPaymentPerVolunteer())
            .paymentPerMaleVolunteer(event.getPaymentPerMaleVolunteer())
            .paymentPerFemaleVolunteer(event.getPaymentPerFemaleVolunteer())
            .status(event.getStatus())
            .category(event.getCategory())
            .applicationDeadline(event.getApplicationDeadline())
            .imageUrl(event.getImageUrl())
            .organizer(organizerInfo)
            .applicationCount((int) applicationCount)
            .confirmedCount((int) confirmedCount)
            .remainingSlots(remainingSlots)
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .build();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validate event data
     */
    private void validateEventData(Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title is required");
        }
        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Event location is required");
        }
        if (event.getDate() == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (event.getTime() == null) {
            throw new IllegalArgumentException("Event time is required");
        }
        if (event.getRequiredVolunteers() == null || event.getRequiredVolunteers() <= 0) {
            throw new IllegalArgumentException("Required volunteers must be greater than 0");
        }
        if (event.getPaymentPerVolunteer() == null || event.getPaymentPerVolunteer().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment per volunteer must be greater than 0");
        }
        
        // Validate event date is in the future
        LocalDateTime eventDateTime = event.getEventDateTime();
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date and time must be in the future");
        }
    }

    /**
     * Validate event is ready for publishing
     */
    private void validateEventForPublishing(Event event) {
        // Ensure all required fields are set
        if (event.getDescription() == null || event.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Event description is required for publishing");
        }
        
        // Ensure event date is in the future
        LocalDateTime eventDateTime = event.getEventDateTime();
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot publish event with past date");
        }
    }

    /**
     * Calculate refund percentage based on cancellation timing
     * Requirements: 14.2, 14.3, 14.4
     * 
     * >7 days before event: 100% refund
     * 3-7 days before event: 50% refund
     * <3 days before event: 0% refund
     */
    private BigDecimal calculateRefundPercentage(Event event) {
        LocalDateTime eventDateTime = event.getEventDateTime();
        LocalDateTime now = LocalDateTime.now();
        
        long daysUntilEvent = ChronoUnit.DAYS.between(now, eventDateTime);
        
        if (daysUntilEvent > 7) {
            return new BigDecimal("1.00"); // 100%
        } else if (daysUntilEvent >= 3) {
            return new BigDecimal("0.50"); // 50%
        } else {
            return BigDecimal.ZERO; // 0%
        }
    }

    /**
     * Process refund for event cancellation
     * Requirements: 14.2, 14.3, 14.4
     */
    private void processEventCancellationRefund(Event event, BigDecimal refundPercentage) {
        // Find the deposit payment for this event
        Payment payment = paymentRepository.findByEventId(event.getId())
            .orElse(null);
        
        if (payment != null && refundPercentage.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate refund amount
            BigDecimal refundAmount = payment.getAmount().multiply(refundPercentage);
            
            // Mark payment as refunded
            // Note: Actual Razorpay refund processing is handled by PaymentService
            // This is just updating the status
            payment.markRefunded();
            paymentRepository.save(payment);
        }
    }

    /**
     * Validate state transitions according to the state machine
     * Requirements: 28.7
     * 
     * Valid transitions:
     * DRAFT → PUBLISHED
     * PUBLISHED → DEPOSIT_PAID
     * DEPOSIT_PAID → IN_PROGRESS
     * IN_PROGRESS → COMPLETED
     * Any state (except COMPLETED, CANCELLED) → CANCELLED
     */
    private boolean isValidTransition(EventStatus from, EventStatus to) {
        // Cannot transition from terminal states
        if (from == EventStatus.COMPLETED || from == EventStatus.CANCELLED) {
            return false;
        }
        
        // Define valid transitions
        Set<EventStatus> validNextStates;
        
        switch (from) {
            case DRAFT:
                validNextStates = EnumSet.of(EventStatus.PUBLISHED, EventStatus.CANCELLED);
                break;
            case PUBLISHED:
                validNextStates = EnumSet.of(EventStatus.DEPOSIT_PAID, EventStatus.CANCELLED);
                break;
            case DEPOSIT_PAID:
                validNextStates = EnumSet.of(EventStatus.IN_PROGRESS, EventStatus.CANCELLED);
                break;
            case IN_PROGRESS:
                validNextStates = EnumSet.of(EventStatus.COMPLETED, EventStatus.CANCELLED);
                break;
            default:
                validNextStates = EnumSet.noneOf(EventStatus.class);
        }
        
        return validNextStates.contains(to);
    }
}
