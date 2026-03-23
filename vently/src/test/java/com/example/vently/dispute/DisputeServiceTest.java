package com.example.vently.dispute;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;
import com.example.vently.user.Role;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.vently.application.ApplicationRepository applicationRepository;

    @Mock
    private com.example.vently.service.S3Service s3Service;

    @Mock
    private com.example.vently.notification.NotificationService notificationService;

    @InjectMocks
    private DisputeService disputeService;

    private User volunteer;
    private User organizer;
    private User admin;
    private Event testEvent;
    private Dispute testDispute;

    @BeforeEach
    void setUp() {
        volunteer = User.builder()
                .id(1L)
                .email("volunteer@example.com")
                .fullName("Test Volunteer")
                .role(Role.VOLUNTEER)
                .build();

        organizer = User.builder()
                .id(2L)
                .email("organizer@example.com")
                .fullName("Test Organizer")
                .role(Role.ORGANIZER)
                .build();

        admin = User.builder()
                .id(3L)
                .email("admin@example.com")
                .fullName("Test Admin")
                .role(Role.ADMIN)
                .build();

        testEvent = Event.builder()
                .id(100L)
                .title("Test Event")
                .organizer(organizer)
                .status(EventStatus.COMPLETED)
                .build();

        testDispute = Dispute.builder()
                .id(1L)
                .event(testEvent)
                .raisedBy(volunteer)
                .againstUser(organizer)
                .description("Test dispute")
                .status(DisputeStatus.OPEN)
                .build();
    }

    @Test
    void testCreateDispute_Success() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(organizer));
        when(disputeRepository.hasOpenDisputeForEvent(100L, 1L)).thenReturn(false);
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(
                eq(100L), eq(1L), any())).thenReturn(true);
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        Dispute result = disputeService.createDispute(100L, 1L, 2L, "Test dispute");

        // Assert
        assertNotNull(result);
        assertEquals("Test dispute", result.getDescription());
        verify(disputeRepository).save(any(Dispute.class));
    }

    @Test
    void testResolveDispute_Success() {
        // Arrange
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(testDispute));
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        Dispute result = disputeService.resolveDispute(1L, 3L, "Resolved", null, null);

        // Assert
        assertNotNull(result);
        verify(disputeRepository).save(any(Dispute.class));
        verify(notificationService, times(2)).createNotification(any(), any(), any(), any());
    }

    @Test
    void testGetOpenDisputes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Dispute> disputePage = new PageImpl<>(List.of(testDispute));
        when(disputeRepository.findOpenDisputes(pageable)).thenReturn(disputePage);

        // Act
        Page<Dispute> result = disputeService.getOpenDisputes(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(disputeRepository).findOpenDisputes(pageable);
    }
}
