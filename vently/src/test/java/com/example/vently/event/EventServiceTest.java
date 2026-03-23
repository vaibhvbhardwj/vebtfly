package com.example.vently.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.dto.EventFilterDto;
import com.example.vently.event.dto.EventResponseDto;
import com.example.vently.payment.Payment;
import com.example.vently.payment.PaymentRepository;
import com.example.vently.payment.PaymentStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private com.example.vently.subscription.SubscriptionService subscriptionService;
    
    @Mock
    private com.example.vently.rating.RatingRepository ratingRepository;

    @Mock
    private com.example.vently.audit.AuditService auditService;

    @Mock
    private com.example.vently.notification.NotificationService notificationService;

    @InjectMocks
    private EventService eventService;

    private User organizer;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
                .id(1L)
                .email("organizer@example.com")
                .fullName("Test Organizer")
                .role(Role.ORGANIZER)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .location("Test Location")
                .date(LocalDate.now().plusDays(10))
                .time(LocalTime.of(10, 0))
                .requiredVolunteers(5)
                .paymentPerVolunteer(new BigDecimal("50.00"))
                .status(EventStatus.DRAFT)
                .organizer(organizer)
                .build();
    }

    @Test
    void testCreateEvent_ShouldSetDraftStatusAndOrganizer() {
        // Arrange
        Event newEvent = Event.builder()
                .title("New Event")
                .description("Description")
                .location("Location")
                .date(LocalDate.now().plusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(3)
                .paymentPerVolunteer(new BigDecimal("30.00"))
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);
        when(eventRepository.save(any(Event.class))).thenReturn(newEvent);

        // Act
        Event result = eventService.createEvent(newEvent, organizer);

        // Assert
        assertNotNull(result);
        assertEquals(EventStatus.DRAFT, result.getStatus());
        assertEquals(organizer, result.getOrganizer());
        verify(subscriptionService).canCreateEvent(organizer.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testCreateEvent_WithInvalidData_ShouldThrowException() {
        // Arrange
        Event invalidEvent = Event.builder()
                .title("")
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidEvent, organizer);
        });
    }

    @Test
    void testUpdateEvent_ForDraftEvent_ShouldSucceed() {
        // Arrange
        Event updatedEvent = Event.builder()
                .title("Updated Title")
                .description("Updated Description")
                .location("Updated Location")
                .date(LocalDate.now().plusDays(15))
                .time(LocalTime.of(15, 0))
                .requiredVolunteers(10)
                .paymentPerVolunteer(new BigDecimal("75.00"))
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event result = eventService.updateEvent(1L, updatedEvent);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testUpdateEvent_ForPublishedEvent_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        Event updatedEvent = Event.builder()
                .title("Updated Title")
                .description("Updated Description")
                .location("Updated Location")
                .date(LocalDate.now().plusDays(15))
                .time(LocalTime.of(15, 0))
                .requiredVolunteers(10)
                .paymentPerVolunteer(new BigDecimal("75.00"))
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event result = eventService.updateEvent(1L, updatedEvent);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", testEvent.getTitle());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testPublishEvent_FromDraft_ShouldSucceed() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event result = eventService.publishEvent(1L);

        // Assert
        assertNotNull(result);
        assertEquals(EventStatus.PUBLISHED, result.getStatus());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testPublishEvent_FromNonDraft_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.publishEvent(1L);
        });
    }

    @Test
    void testCancelEvent_MoreThan7DaysBefore_Should100PercentRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);
        testEvent.setDate(LocalDate.now().plusDays(10));
        
        Payment payment = Payment.builder()
                .id(1L)
                .event(testEvent)
                .organizer(organizer)
                .amount(new BigDecimal("250.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Test cancellation");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        assertEquals("Test cancellation", testEvent.getCancellationReason());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelEvent_Between3And7Days_Should50PercentRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);
        testEvent.setDate(LocalDate.now().plusDays(5));
        
        Payment payment = Payment.builder()
                .id(1L)
                .event(testEvent)
                .organizer(organizer)
                .amount(new BigDecimal("250.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Test cancellation");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelEvent_LessThan3Days_ShouldNoRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);
        testEvent.setDate(LocalDate.now().plusDays(2));
        
        Payment payment = Payment.builder()
                .id(1L)
                .event(testEvent)
                .organizer(organizer)
                .amount(new BigDecimal("250.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Test cancellation");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        // Payment should still be saved but with 0% refund
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testCancelEvent_ShouldUpdateApplicationsToCancel() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        
        Application app1 = Application.builder()
                .id(1L)
                .event(testEvent)
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        Application app2 = Application.builder()
                .id(2L)
                .event(testEvent)
                .status(ApplicationStatus.ACCEPTED)
                .build();

        List<Application> applications = List.of(app1, app2);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(applicationRepository.findByEventId(1L)).thenReturn(applications);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Test cancellation");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        verify(applicationRepository, times(2)).save(any(Application.class));
    }

    @Test
    void testTransitionEventStatus_ValidTransition_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.DRAFT);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.PUBLISHED);

        // Assert
        assertEquals(EventStatus.PUBLISHED, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_InvalidTransition_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.DRAFT);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.transitionEventStatus(testEvent, EventStatus.COMPLETED);
        });
    }

    @Test
    void testTransitionEventStatus_FromCompletedState_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.COMPLETED);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.transitionEventStatus(testEvent, EventStatus.CANCELLED);
        });
    }

    @Test
    void testGetEventById_ShouldReturnEvent() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        Event result = eventService.getEventById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
    }

    @Test
    void testGetEventById_NotFound_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEventById(999L);
        });
    }

    @Test
    void testGetOrganizerEvents_ShouldReturnEventsList() {
        // Arrange
        List<Event> events = List.of(testEvent);
        when(eventRepository.findByOrganizerId(1L)).thenReturn(events);

        // Act
        List<Event> result = eventService.getOrganizerEvents(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent.getId(), result.get(0).getId());
    }

    // ==================== Event Validation Tests ====================

    @Test
    void testCreateEvent_WithMissingTitle_ShouldThrowException() {
        // Arrange
        Event invalidEvent = Event.builder()
                .title("")
                .description("Description")
                .location("Location")
                .date(LocalDate.now().plusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(3)
                .paymentPerVolunteer(new BigDecimal("30.00"))
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidEvent, organizer);
        });
    }

    @Test
    void testCreateEvent_WithMissingLocation_ShouldThrowException() {
        // Arrange
        Event invalidEvent = Event.builder()
                .title("Event Title")
                .description("Description")
                .location("")
                .date(LocalDate.now().plusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(3)
                .paymentPerVolunteer(new BigDecimal("30.00"))
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidEvent, organizer);
        });
    }

    @Test
    void testCreateEvent_WithInvalidVolunteerCount_ShouldThrowException() {
        // Arrange
        Event invalidEvent = Event.builder()
                .title("Event Title")
                .description("Description")
                .location("Location")
                .date(LocalDate.now().plusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(0)
                .paymentPerVolunteer(new BigDecimal("30.00"))
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidEvent, organizer);
        });
    }

    @Test
    void testCreateEvent_WithInvalidPayment_ShouldThrowException() {
        // Arrange
        Event invalidEvent = Event.builder()
                .title("Event Title")
                .description("Description")
                .location("Location")
                .date(LocalDate.now().plusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(3)
                .paymentPerVolunteer(BigDecimal.ZERO)
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidEvent, organizer);
        });
    }

    @Test
    void testCreateEvent_WithPastDate_ShouldThrowException() {
        // Arrange
        Event invalidEvent = Event.builder()
                .title("Event Title")
                .description("Description")
                .location("Location")
                .date(LocalDate.now().minusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(3)
                .paymentPerVolunteer(new BigDecimal("30.00"))
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidEvent, organizer);
        });
    }

    @Test
    void testCreateEvent_SubscriptionLimitExceeded_ShouldThrowException() {
        // Arrange
        Event newEvent = Event.builder()
                .title("New Event")
                .description("Description")
                .location("Location")
                .date(LocalDate.now().plusDays(5))
                .time(LocalTime.of(14, 0))
                .requiredVolunteers(3)
                .paymentPerVolunteer(new BigDecimal("30.00"))
                .build();

        when(subscriptionService.canCreateEvent(organizer.getId())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.createEvent(newEvent, organizer);
        });
    }

    // ==================== State Transition Tests ====================

    @Test
    void testTransitionEventStatus_DraftToPublished_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.DRAFT);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.PUBLISHED);

        // Assert
        assertEquals(EventStatus.PUBLISHED, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_PublishedToDepositPaid_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.DEPOSIT_PAID);

        // Assert
        assertEquals(EventStatus.DEPOSIT_PAID, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_DepositPaidToInProgress_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.IN_PROGRESS);

        // Assert
        assertEquals(EventStatus.IN_PROGRESS, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_InProgressToCompleted_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.IN_PROGRESS);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.COMPLETED);

        // Assert
        assertEquals(EventStatus.COMPLETED, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_DraftToCancelled_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.DRAFT);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.CANCELLED);

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_PublishedToCancelled_ShouldSucceed() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);

        // Act
        eventService.transitionEventStatus(testEvent, EventStatus.CANCELLED);

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
    }

    @Test
    void testTransitionEventStatus_InvalidTransitionDraftToInProgress_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.DRAFT);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.transitionEventStatus(testEvent, EventStatus.IN_PROGRESS);
        });
    }

    @Test
    void testTransitionEventStatus_InvalidTransitionPublishedToCompleted_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.transitionEventStatus(testEvent, EventStatus.COMPLETED);
        });
    }

    // ==================== Cancellation Refund Logic Tests ====================

    @Test
    void testCancelEvent_MoreThan7DaysBefore_ShouldCalculate100PercentRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);
        testEvent.setDate(LocalDate.now().plusDays(10));
        
        Payment payment = Payment.builder()
                .id(1L)
                .event(testEvent)
                .organizer(organizer)
                .amount(new BigDecimal("250.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Organizer request");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        assertEquals("Organizer request", testEvent.getCancellationReason());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelEvent_Between3And7Days_ShouldCalculate50PercentRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);
        testEvent.setDate(LocalDate.now().plusDays(5));
        
        Payment payment = Payment.builder()
                .id(1L)
                .event(testEvent)
                .organizer(organizer)
                .amount(new BigDecimal("250.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Organizer request");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelEvent_LessThan3Days_ShouldCalculate0PercentRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.DEPOSIT_PAID);
        testEvent.setDate(LocalDate.now().plusDays(2));
        
        Payment payment = Payment.builder()
                .id(1L)
                .event(testEvent)
                .organizer(organizer)
                .amount(new BigDecimal("250.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        eventService.cancelEvent(1L, "Organizer request");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testCancelEvent_BeforeDepositPaid_ShouldNotProcessRefund() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        testEvent.setDate(LocalDate.now().plusDays(10));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(applicationRepository.findByEventId(1L)).thenReturn(new ArrayList<>());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        doNothing().when(auditService).logEventStateTransition(any(), anyLong(), anyString(), anyString(), anyString());

        // Act
        eventService.cancelEvent(1L, "Organizer request");

        // Assert
        assertEquals(EventStatus.CANCELLED, testEvent.getStatus());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testCancelEvent_AlreadyCompleted_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.COMPLETED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.cancelEvent(1L, "Organizer request");
        });
    }

    @Test
    void testCancelEvent_AlreadyCancelled_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.cancelEvent(1L, "Organizer request");
        });
    }

    // ==================== Filtering and Search Tests ====================

    @Test
    void testGetPublishedEvents_WithFilters_ShouldReturnFilteredResults() {
        // Arrange
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
                .location("Test Location")
                .build();

        List<Event> events = List.of(testEvent);
        org.springframework.data.domain.Page<Event> eventPage = 
            new org.springframework.data.domain.PageImpl<>(events, pageable, 1);

        when(eventRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), 
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(eventPage);
        when(ratingRepository.calculateAverageRating(organizer.getId())).thenReturn(4.5);
        when(ratingRepository.countByRatedUserId(organizer.getId())).thenReturn(10L);

        // Act
        org.springframework.data.domain.Page<EventResponseDto> result = 
            eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetPublishedEvents_WithSortByPayment_ShouldSortCorrectly() {
        // Arrange
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
                .sortBy("payment")
                .build();

        List<Event> events = List.of(testEvent);
        org.springframework.data.domain.Page<Event> eventPage = 
            new org.springframework.data.domain.PageImpl<>(events, pageable, 1);

        when(eventRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), 
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(eventPage);
        when(ratingRepository.calculateAverageRating(organizer.getId())).thenReturn(4.5);
        when(ratingRepository.countByRatedUserId(organizer.getId())).thenReturn(10L);

        // Act
        org.springframework.data.domain.Page<EventResponseDto> result = 
            eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetPublishedEvents_WithSortByDate_ShouldSortCorrectly() {
        // Arrange
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
                .sortBy("date")
                .build();

        List<Event> events = List.of(testEvent);
        org.springframework.data.domain.Page<Event> eventPage = 
            new org.springframework.data.domain.PageImpl<>(events, pageable, 1);

        when(eventRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), 
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(eventPage);
        when(ratingRepository.calculateAverageRating(organizer.getId())).thenReturn(4.5);
        when(ratingRepository.countByRatedUserId(organizer.getId())).thenReturn(10L);

        // Act
        org.springframework.data.domain.Page<EventResponseDto> result = 
            eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
    }
}
