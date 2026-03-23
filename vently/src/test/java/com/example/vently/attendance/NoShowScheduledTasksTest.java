package com.example.vently.attendance;

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
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.payment.PaymentService;
import com.example.vently.user.AccountStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class NoShowScheduledTasksTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AttendanceCodeRepository attendanceCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private NoShowScheduledTasks noShowScheduledTasks;

    private User organizer;
    private User volunteer;
    private Event testEvent;
    private Application testApplication;
    private AttendanceCode testAttendanceCode;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
                .id(1L)
                .email("organizer@example.com")
                .fullName("Test Organizer")
                .role(Role.ORGANIZER)
                .build();

        volunteer = User.builder()
                .id(2L)
                .email("volunteer@example.com")
                .fullName("Test Volunteer")
                .role(Role.VOLUNTEER)
                .noShowCount(0)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .location("Test Location")
                .date(LocalDate.now().minusDays(2))
                .time(LocalTime.of(10, 0))
                .requiredVolunteers(5)
                .paymentPerVolunteer(new BigDecimal("50.00"))
                .status(EventStatus.DEPOSIT_PAID)
                .organizer(organizer)
                .build();

        testApplication = Application.builder()
                .id(1L)
                .event(testEvent)
                .volunteer(volunteer)
                .status(ApplicationStatus.CONFIRMED)
                .build();

        testAttendanceCode = AttendanceCode.builder()
                .id(1L)
                .event(testEvent)
                .volunteer(volunteer)
                .code("ABC123XYZ")
                .build();
    }

    @Test
    void testProcessNoShows_ShouldProcessEventsAndIdentifyNoShows() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty()); // No attendance code = no-show
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        verify(paymentService).processRefund(1L, 2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testProcessNoShows_WithAttendanceMarked_ShouldNotProcessAsNoShow() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        testAttendanceCode.markAttendance(organizer);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.of(testAttendanceCode)); // Attendance marked = not no-show

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        verify(paymentService, never()).processRefund(anyLong(), anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testProcessNoShows_FirstNoShow_ShouldIncrementCount() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        assertEquals(1, volunteer.getNoShowCount());
        assertEquals(AccountStatus.ACTIVE, volunteer.getAccountStatus());
        assertNull(volunteer.getSuspendedUntil());
    }

    @Test
    void testProcessNoShows_ThirdNoShow_ShouldSuspendAccount() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        volunteer.setNoShowCount(2); // Already has 2 no-shows
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        assertEquals(3, volunteer.getNoShowCount());
        assertEquals(AccountStatus.SUSPENDED, volunteer.getAccountStatus());
        assertNotNull(volunteer.getSuspendedUntil());
        assertTrue(volunteer.getSuspendedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void testProcessNoShows_FifthNoShow_ShouldBanAccount() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        volunteer.setNoShowCount(4); // Already has 4 no-shows
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        assertEquals(5, volunteer.getNoShowCount());
        assertEquals(AccountStatus.BANNED, volunteer.getAccountStatus());
        assertNull(volunteer.getSuspendedUntil()); // Permanent ban doesn't need expiry
    }

    @Test
    void testProcessNoShows_AlreadySuspended_ShouldNotChangeSuspension() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        volunteer.setNoShowCount(3);
        volunteer.setAccountStatus(AccountStatus.SUSPENDED);
        LocalDateTime existingSuspension = LocalDateTime.now().plusDays(15);
        volunteer.setSuspendedUntil(existingSuspension);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        assertEquals(4, volunteer.getNoShowCount());
        assertEquals(AccountStatus.SUSPENDED, volunteer.getAccountStatus());
        // Suspension time will be updated to a new 30-day period
        assertNotNull(volunteer.getSuspendedUntil());
        assertTrue(volunteer.getSuspendedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void testProcessNoShows_AlreadyBanned_ShouldNotProcessFurther() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        volunteer.setNoShowCount(5);
        volunteer.setAccountStatus(AccountStatus.BANNED);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        assertEquals(6, volunteer.getNoShowCount()); // Still increments count
        assertEquals(AccountStatus.BANNED, volunteer.getAccountStatus()); // Stays banned
    }

    @Test
    void testProcessNoShows_MultipleVolunteers_ShouldProcessAll() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        
        User volunteer2 = User.builder()
                .id(3L)
                .email("volunteer2@example.com")
                .fullName("Test Volunteer 2")
                .role(Role.VOLUNTEER)
                .noShowCount(0)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        
        Application application2 = Application.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(volunteer2)
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        List<Application> confirmedApplications = List.of(testApplication, application2);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty()); // No-show
        AttendanceCode markedCode = AttendanceCode.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(User.builder().id(3L).build())
                .code("DEF456UVW")
                .build();
        markedCode.markAttendance(organizer);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 3L))
                .thenReturn(Optional.of(markedCode)); // Attended
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doNothing().when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        verify(paymentService, times(1)).processRefund(1L, 2L); // Only for volunteer 1
        verify(userRepository, times(1)).save(any(User.class)); // Only for volunteer 1
    }

    @Test
    void testProcessNoShows_PaymentServiceException_ShouldContinueProcessing() {
        // Arrange
        List<Event> eventsToProcess = List.of(testEvent);
        List<Application> confirmedApplications = List.of(testApplication);
        
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(eventsToProcess);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(volunteer);
        doThrow(new RuntimeException("Payment service error")).when(paymentService).processRefund(1L, 2L);

        // Act
        noShowScheduledTasks.processNoShows(); // Should not throw exception

        // Assert
        verify(paymentService).processRefund(1L, 2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testProcessNoShows_NoEventsToProcess_ShouldDoNothing() {
        // Arrange
        when(eventRepository.findEventsForNoShowProcessing(any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.DEPOSIT_PAID)))
                .thenReturn(new ArrayList<>());

        // Act
        noShowScheduledTasks.processNoShows();

        // Assert
        verify(applicationRepository, never()).findByEventIdAndStatus(anyLong(), any());
        verify(paymentService, never()).processRefund(anyLong(), anyLong());
        verify(userRepository, never()).save(any(User.class));
    }
}