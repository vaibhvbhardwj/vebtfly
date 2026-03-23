package com.example.vently.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.subscription.SubscriptionRepository;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private com.example.vently.subscription.SubscriptionService subscriptionService;

    @Mock
    private com.example.vently.notification.NotificationService notificationService;

    @InjectMocks
    private ApplicationService applicationService;

    private User organizer;
    private User volunteer;
    private Event event;
    private Application application;

    @BeforeEach
    void setUp() {
        // Create organizer
        organizer = User.builder()
            .id(1L)
            .email("organizer@test.com")
            .role(Role.ORGANIZER)
            .build();

        // Create volunteer
        volunteer = User.builder()
            .id(2L)
            .email("volunteer@test.com")
            .role(Role.VOLUNTEER)
            .build();

        // Create event
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(7);
        event = Event.builder()
            .id(1L)
            .title("Test Event")
            .organizer(organizer)
            .requiredVolunteers(5)
            .status(EventStatus.PUBLISHED)
            .date(futureDateTime.toLocalDate())
            .time(futureDateTime.toLocalTime())
            .build();

        // Create application
        application = Application.builder()
            .id(1L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .appliedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void acceptApplication_Success() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(2L);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        applicationService.acceptApplication(1L, 1L);

        // Assert
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.ACCEPTED &&
            app.getAcceptedAt() != null &&
            app.getConfirmationDeadline() != null
        ));
    }

    @Test
    void acceptApplication_NotOrganizer_ThrowsException() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.acceptApplication(1L, 999L)
        );
        
        assertTrue(exception.getMessage().contains("Only the event organizer can accept applications"));
    }

    @Test
    void acceptApplication_NotPending_ThrowsException() {
        // Arrange
        application.setStatus(ApplicationStatus.CONFIRMED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.acceptApplication(1L, 1L)
        );
        
        assertTrue(exception.getMessage().contains("Only PENDING applications can be accepted"));
    }

    @Test
    void acceptApplication_CapacityReached_ThrowsException() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.acceptApplication(1L, 1L)
        );
        
        assertTrue(exception.getMessage().contains("Event has reached maximum capacity"));
    }

    @Test
    void rejectApplication_Success() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        applicationService.rejectApplication(1L, 1L);

        // Assert
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.REJECTED
        ));
    }

    @Test
    void rejectApplication_NotOrganizer_ThrowsException() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.rejectApplication(1L, 999L)
        );
        
        assertTrue(exception.getMessage().contains("Only the event organizer can reject applications"));
    }

    @Test
    void rejectApplication_NotPending_ThrowsException() {
        // Arrange
        application.setStatus(ApplicationStatus.ACCEPTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.rejectApplication(1L, 1L)
        );
        
        assertTrue(exception.getMessage().contains("Only PENDING applications can be rejected"));
    }

    @Test
    void autoRejectExcessApplications_CapacityReached_RejectsAllPending() {
        // Arrange
        Application pendingApp1 = Application.builder()
            .id(2L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();
        
        Application pendingApp2 = Application.builder()
            .id(3L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();

        List<Application> pendingApplications = Arrays.asList(pendingApp1, pendingApp2);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);
        when(applicationRepository.findPendingApplicationsByEventId(1L)).thenReturn(pendingApplications);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        applicationService.autoRejectExcessApplications(1L);

        // Assert
        verify(applicationRepository, times(2)).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.REJECTED
        ));
    }

    @Test
    void autoRejectExcessApplications_CapacityNotReached_DoesNotReject() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(3L);

        // Act
        applicationService.autoRejectExcessApplications(1L);

        // Assert
        verify(applicationRepository, never()).findPendingApplicationsByEventId(anyLong());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void confirmApplication_Success() {
        // Arrange
        application.setAccepted(); // Set to ACCEPTED with confirmation deadline
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event)); // Mock for autoRejectExcessApplications
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(2L);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        applicationService.confirmApplication(1L, 2L);

        // Assert
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.CONFIRMED &&
            app.getConfirmedAt() != null
        ));
    }

    @Test
    void confirmApplication_NotVolunteer_ThrowsException() {
        // Arrange
        application.setAccepted();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.confirmApplication(1L, 999L)
        );
        
        assertTrue(exception.getMessage().contains("Only the applicant can confirm their application"));
    }

    @Test
    void confirmApplication_NotAccepted_ThrowsException() {
        // Arrange
        application.setStatus(ApplicationStatus.PENDING);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.confirmApplication(1L, 2L)
        );
        
        assertTrue(exception.getMessage().contains("Only ACCEPTED applications can be confirmed"));
    }

    @Test
    void confirmApplication_DeadlineExpired_ThrowsException() {
        // Arrange
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setConfirmationDeadline(LocalDateTime.now().minusHours(1)); // Expired deadline
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.confirmApplication(1L, 2L)
        );
        
        assertTrue(exception.getMessage().contains("Confirmation deadline has expired"));
    }

    @Test
    void declineApplication_Success() {
        // Arrange
        application.setAccepted();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        applicationService.declineApplication(1L, 2L);

        // Assert
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.DECLINED &&
            app.getDeclinedAt() != null
        ));
    }

    @Test
    void declineApplication_NotVolunteer_ThrowsException() {
        // Arrange
        application.setAccepted();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.declineApplication(1L, 999L)
        );
        
        assertTrue(exception.getMessage().contains("Only the applicant can decline their application"));
    }

    @Test
    void declineApplication_NotAccepted_ThrowsException() {
        // Arrange
        application.setStatus(ApplicationStatus.PENDING);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.declineApplication(1L, 2L)
        );
        
        assertTrue(exception.getMessage().contains("Only ACCEPTED applications can be declined"));
    }

    @Test
    void autoDeclineExpiredApplications_DeclinesExpiredApplications() {
        // Arrange
        Application expiredApp1 = Application.builder()
            .id(2L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.ACCEPTED)
            .confirmationDeadline(LocalDateTime.now().minusHours(1))
            .build();
        
        Application expiredApp2 = Application.builder()
            .id(3L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.ACCEPTED)
            .confirmationDeadline(LocalDateTime.now().minusHours(2))
            .build();

        List<Application> expiredApplications = Arrays.asList(expiredApp1, expiredApp2);

        when(applicationRepository.findExpiredConfirmations(any(LocalDateTime.class)))
            .thenReturn(expiredApplications);
        when(applicationRepository.save(any(Application.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        applicationService.autoDeclineExpiredApplications();

        // Assert
        verify(applicationRepository, times(2)).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.DECLINED &&
            app.getDeclinedAt() != null
        ));
    }

    // ===== ADDITIONAL TESTS FOR TASK 7.5 =====

    @Test
    void submitApplication_DuplicateApplication_ThrowsException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(volunteer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.existsByEventIdAndVolunteerId(1L, 2L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.submitApplication(2L, 1L)
        );
        
        assertTrue(exception.getMessage().contains("You have already applied to this event"));
    }

    @Test
    void submitApplication_EventCapacityReached_ThrowsException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(volunteer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.existsByEventIdAndVolunteerId(1L, 2L)).thenReturn(false);
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.submitApplication(2L, 1L)
        );
        
        assertTrue(exception.getMessage().contains("Event has reached maximum capacity"));
    }

    @Test
    void submitApplication_TierLimitExceeded_ThrowsException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(volunteer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.existsByEventIdAndVolunteerId(1L, 2L)).thenReturn(false);
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(2L);
        when(subscriptionService.canApplyToEvent(2L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.submitApplication(2L, 1L)
        );
        
        assertTrue(exception.getMessage().contains("Application limit reached"));
    }

    @Test
    void submitApplication_Success_CreatesApplication() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(volunteer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.existsByEventIdAndVolunteerId(1L, 2L)).thenReturn(false);
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(2L);
        when(subscriptionService.canApplyToEvent(2L)).thenReturn(true);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        Application result = applicationService.submitApplication(2L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.PENDING, result.getStatus());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void confirmApplication_WithinDeadline_Success() {
        // Arrange
        LocalDateTime futureDeadline = LocalDateTime.now().plusHours(24);
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setConfirmationDeadline(futureDeadline);
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(2L);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        applicationService.confirmApplication(1L, 2L);

        // Assert
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.CONFIRMED &&
            app.getConfirmedAt() != null
        ));
    }

    @Test
    void confirmApplication_JustBeforeDeadline_Success() {
        // Arrange
        LocalDateTime almostExpiredDeadline = LocalDateTime.now().plusSeconds(30);
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setConfirmationDeadline(almostExpiredDeadline);
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(2L);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Act
        applicationService.confirmApplication(1L, 2L);

        // Assert
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.CONFIRMED
        ));
    }

    @Test
    void confirmApplication_CapacityReachedAfterConfirm_AutoRejectsExcess() {
        // Arrange
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setConfirmationDeadline(LocalDateTime.now().plusHours(24));
        
        Application pendingApp = Application.builder()
            .id(2L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);
        when(applicationRepository.findPendingApplicationsByEventId(1L)).thenReturn(Arrays.asList(pendingApp));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        applicationService.confirmApplication(1L, 2L);

        // Assert - Verify save was called at least once
        verify(applicationRepository, atLeastOnce()).save(any(Application.class));
    }

    @Test
    void autoRejectExcessApplications_MultipleExcessApplications_RejectsAll() {
        // Arrange
        Application pendingApp1 = Application.builder()
            .id(2L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();
        
        Application pendingApp2 = Application.builder()
            .id(3L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();
        
        Application pendingApp3 = Application.builder()
            .id(4L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();

        List<Application> pendingApplications = Arrays.asList(pendingApp1, pendingApp2, pendingApp3);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);
        when(applicationRepository.findPendingApplicationsByEventId(1L)).thenReturn(pendingApplications);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        applicationService.autoRejectExcessApplications(1L);

        // Assert
        verify(applicationRepository, times(3)).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.REJECTED
        ));
    }

    @Test
    void autoRejectExcessApplications_CapacityNotReachedByOne_DoesNotReject() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(4L);

        // Act
        applicationService.autoRejectExcessApplications(1L);

        // Assert
        verify(applicationRepository, never()).findPendingApplicationsByEventId(anyLong());
    }

    @Test
    void autoDeclineExpiredApplications_NoExpiredApplications_DoesNothing() {
        // Arrange
        when(applicationRepository.findExpiredConfirmations(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        // Act
        applicationService.autoDeclineExpiredApplications();

        // Assert
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void autoDeclineExpiredApplications_MultipleExpiredApplications_DeclinesAll() {
        // Arrange
        List<Application> expiredApplications = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Application expiredApp = Application.builder()
                .id((long) i)
                .event(event)
                .volunteer(volunteer)
                .status(ApplicationStatus.ACCEPTED)
                .confirmationDeadline(LocalDateTime.now().minusHours(i + 1))
                .build();
            expiredApplications.add(expiredApp);
        }

        when(applicationRepository.findExpiredConfirmations(any(LocalDateTime.class)))
            .thenReturn(expiredApplications);
        when(applicationRepository.save(any(Application.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        applicationService.autoDeclineExpiredApplications();

        // Assert
        verify(applicationRepository, times(5)).save(argThat(app -> 
            app.getStatus() == ApplicationStatus.DECLINED
        ));
    }

    @Test
    void confirmApplication_CapacityExactlyReached_AutoRejectsAllPending() {
        // Arrange
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setConfirmationDeadline(LocalDateTime.now().plusHours(24));
        
        Application pendingApp1 = Application.builder()
            .id(2L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();
        
        Application pendingApp2 = Application.builder()
            .id(3L)
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);
        when(applicationRepository.findPendingApplicationsByEventId(1L)).thenReturn(Arrays.asList(pendingApp1, pendingApp2));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        applicationService.confirmApplication(1L, 2L);

        // Assert - Should save confirmed app + 2 rejected apps
        verify(applicationRepository, atLeast(3)).save(any(Application.class));
    }
}
