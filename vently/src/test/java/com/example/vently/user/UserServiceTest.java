package com.example.vently.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.rating.RatingRepository;
import com.example.vently.service.S3Service;
import com.example.vently.user.dto.EmailPreferencesRequest;
import com.example.vently.user.dto.EmailPreferencesResponse;
import com.example.vently.user.dto.UserProfileDto;
import com.example.vently.user.dto.UserStatisticsDto;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserService userService;

    private User testVolunteer;
    private User testOrganizer;

    @BeforeEach
    void setUp() {
        testVolunteer = User.builder()
                .id(1L)
                .email("volunteer@example.com")
                .fullName("Test Volunteer")
                .role(Role.VOLUNTEER)
                .bio("I am a volunteer")
                .phone("1234567890")
                .skills("[\"Java\", \"Python\"]")
                .availability("[\"Monday\", \"Tuesday\"]")
                .experience("2 years")
                .noShowCount(0)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        testOrganizer = User.builder()
                .id(2L)
                .email("organizer@example.com")
                .fullName("Test Organizer")
                .role(Role.ORGANIZER)
                .bio("I organize events")
                .phone("0987654321")
                .organizationName("Test Org")
                .organizationDetails("Details")
                .noShowCount(0)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    // ==================== Profile Update Validation Tests ====================

    @Test
    void testUpdateProfile_WithValidData_ShouldSucceed() {
        // Arrange
        UserProfileDto updateDto = UserProfileDto.builder()
                .fullName("Updated Name")
                .bio("Updated bio")
                .phone("9999999999")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));
        when(userRepository.save(any(User.class))).thenReturn(testVolunteer);
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(4.5);
        when(ratingRepository.countByRatedUserId(1L)).thenReturn(10L);

        // Act
        UserProfileDto result = userService.updateProfile(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", testVolunteer.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_VolunteerSpecificFields_ShouldUpdate() {
        // Arrange
        UserProfileDto updateDto = UserProfileDto.builder()
                .skills("[\"Java\", \"Go\"]")
                .availability("[\"Wednesday\"]")
                .experience("3 years")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));
        when(userRepository.save(any(User.class))).thenReturn(testVolunteer);
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(null);
        when(ratingRepository.countByRatedUserId(1L)).thenReturn(0L);

        // Act
        UserProfileDto result = userService.updateProfile(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("[\"Java\", \"Go\"]", testVolunteer.getSkills());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_OrganizerSpecificFields_ShouldUpdate() {
        // Arrange
        UserProfileDto updateDto = UserProfileDto.builder()
                .organizationName("New Org")
                .organizationDetails("New Details")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(testOrganizer));
        when(userRepository.save(any(User.class))).thenReturn(testOrganizer);
        when(ratingRepository.calculateAverageRating(2L)).thenReturn(4.8);
        when(ratingRepository.countByRatedUserId(2L)).thenReturn(15L);

        // Act
        UserProfileDto result = userService.updateProfile(2L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Org", testOrganizer.getOrganizationName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_UserNotFound_ShouldThrowException() {
        // Arrange
        UserProfileDto updateDto = UserProfileDto.builder()
                .fullName("Updated Name")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.updateProfile(999L, updateDto);
        });
    }

    // ==================== Statistics Calculation Tests ====================

    @Test
    void testGetUserStatistics_ForVolunteer_ShouldCalculateCorrectly() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(4.5);
        when(ratingRepository.countByRatedUserId(1L)).thenReturn(10L);
        when(applicationRepository.countByVolunteerId(1L)).thenReturn(5L);
        when(applicationRepository.countByVolunteerIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(3L);

        // Act
        UserStatisticsDto result = userService.getUserStatistics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10L, result.getTotalRatings());
        assertEquals(5L, result.getTotalApplications());
        assertEquals(3L, result.getConfirmedApplications());
    }

    @Test
    void testGetUserStatistics_ForOrganizer_ShouldCalculateCorrectly() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(testOrganizer));
        when(ratingRepository.calculateAverageRating(2L)).thenReturn(4.8);
        when(ratingRepository.countByRatedUserId(2L)).thenReturn(15L);
        when(eventRepository.findByOrganizerId(2L)).thenReturn(java.util.List.of());

        // Act
        UserStatisticsDto result = userService.getUserStatistics(2L);

        // Assert
        assertNotNull(result);
        assertEquals(4.8, result.getAverageRating());
        assertEquals(15L, result.getTotalRatings());
        assertEquals(0L, result.getTotalEvents());
    }

    @Test
    void testGetUserStatistics_WithNoRatings_ShouldReturnZero() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(null);
        when(ratingRepository.countByRatedUserId(1L)).thenReturn(0L);
        when(applicationRepository.countByVolunteerId(1L)).thenReturn(0L);
        when(applicationRepository.countByVolunteerIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(0L);

        // Act
        UserStatisticsDto result = userService.getUserStatistics(1L);

        // Assert
        assertNotNull(result);
        assertNull(result.getAverageRating());
        assertEquals(0L, result.getTotalRatings());
    }

    // ==================== Profile Picture Upload Tests ====================

    // ==================== Suspension Status Tests ====================

    @Test
    void testCheckAndUpdateSuspensionStatus_SuspensionExpired_ShouldReactivate() {
        // Arrange
        testVolunteer.setAccountStatus(AccountStatus.SUSPENDED);
        testVolunteer.setSuspendedUntil(LocalDateTime.now().minusHours(1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));
        when(userRepository.save(any(User.class))).thenReturn(testVolunteer);

        // Act
        userService.checkAndUpdateSuspensionStatus(1L);

        // Assert
        assertEquals(AccountStatus.ACTIVE, testVolunteer.getAccountStatus());
        assertNull(testVolunteer.getSuspendedUntil());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCheckAndUpdateSuspensionStatus_SuspensionNotExpired_ShouldNotChange() {
        // Arrange
        testVolunteer.setAccountStatus(AccountStatus.SUSPENDED);
        testVolunteer.setSuspendedUntil(LocalDateTime.now().plusHours(1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));

        // Act
        userService.checkAndUpdateSuspensionStatus(1L);

        // Assert
        assertEquals(AccountStatus.SUSPENDED, testVolunteer.getAccountStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Email Preferences Tests ====================

    @Test
    void testUpdateEmailPreferences_ShouldUpdateAllPreferences() {
        // Arrange
        EmailPreferencesRequest request = EmailPreferencesRequest.builder()
                .emailNotificationsEnabled(true)
                .notifyOnApplicationStatus(true)
                .notifyOnEventCancellation(false)
                .notifyOnPayment(true)
                .notifyOnDisputeResolution(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));
        when(userRepository.save(any(User.class))).thenReturn(testVolunteer);

        // Act
        EmailPreferencesResponse result = userService.updateEmailPreferences(1L, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetEmailPreferences_ShouldReturnCurrentPreferences() {
        // Arrange
        testVolunteer.setEmailNotificationsEnabled(true);
        testVolunteer.setNotifyOnApplicationStatus(true);
        testVolunteer.setNotifyOnEventCancellation(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testVolunteer));

        // Act
        EmailPreferencesResponse result = userService.getEmailPreferences(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getEmailNotificationsEnabled());
        assertTrue(result.getNotifyOnApplicationStatus());
        assertFalse(result.getNotifyOnEventCancellation());
    }
}
