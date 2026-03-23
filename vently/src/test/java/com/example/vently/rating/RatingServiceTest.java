package com.example.vently.rating;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.rating.dto.RatingRequestDto;
import com.example.vently.rating.dto.RatingResponseDto;
import com.example.vently.rating.dto.RatingStatisticsDto;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;
import com.example.vently.user.Role;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.vently.notification.NotificationService notificationService;

    @InjectMocks
    private RatingService ratingService;

    private User volunteer;
    private User organizer;
    private Event completedEvent;
    private RatingRequestDto ratingRequest;

    @BeforeEach
    void setUp() {
        volunteer = User.builder()
                .id(1L)
                .email("volunteer@example.com")
                .fullName("John Volunteer")
                .role(Role.VOLUNTEER)
                .build();

        organizer = User.builder()
                .id(2L)
                .email("organizer@example.com")
                .fullName("Jane Organizer")
                .role(Role.ORGANIZER)
                .build();

        completedEvent = Event.builder()
                .id(100L)
                .title("Test Event")
                .organizer(organizer)
                .status(EventStatus.COMPLETED)
                .date(LocalDateTime.now().minusDays(3).toLocalDate())
                .time(LocalDateTime.now().minusDays(3).toLocalTime())
                .build();

        ratingRequest = RatingRequestDto.builder()
                .eventId(100L)
                .ratedUserId(2L) // Organizer
                .rating(5)
                .review("Great organizer!")
                .build();
    }

    @Test
    void submitRating_ValidVolunteerRating_ShouldSucceed() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findById(2L)).thenReturn(Optional.of(organizer));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 2L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(100L, 1L, 2L))
                .thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(999L);
            return rating;
        });

        // Act
        Rating result = ratingService.submitRating(ratingRequest, volunteer);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertEquals(5, result.getRating());
        assertEquals("Great organizer!", result.getReview());
        assertEquals(volunteer, result.getRater());
        assertEquals(organizer, result.getRatedUser());
        assertEquals(completedEvent, result.getEvent());

        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void submitRating_ValidOrganizerRating_ShouldSucceed() {
        // Arrange
        ratingRequest = RatingRequestDto.builder()
                .eventId(100L)
                .ratedUserId(1L) // Volunteer
                .rating(4)
                .review("Good volunteer!")
                .build();

        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(100L, 2L, 1L))
                .thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(999L);
            return rating;
        });

        // Act
        Rating result = ratingService.submitRating(ratingRequest, organizer);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertEquals(4, result.getRating());
        assertEquals("Good volunteer!", result.getReview());
        assertEquals(organizer, result.getRater());
        assertEquals(volunteer, result.getRatedUser());

        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void submitRating_DuplicateRating_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 2L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(100L, 1L, 2L))
                .thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ratingService.submitRating(ratingRequest, volunteer));
        
        assertEquals("You have already rated this user for this event", exception.getMessage());
    }

    @Test
    void submitRating_EventNotCompleted_ShouldThrowException() {
        // Arrange
        Event draftEvent = Event.builder()
                .id(100L)
                .title("Draft Event")
                .organizer(organizer)
                .status(EventStatus.DRAFT)
                .date(LocalDateTime.now().minusDays(3).toLocalDate())
                .time(LocalDateTime.now().minusDays(3).toLocalTime())
                .build();

        when(eventRepository.findById(100L)).thenReturn(Optional.of(draftEvent));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ratingService.submitRating(ratingRequest, volunteer));
        
        assertEquals("Cannot rate for events that are not completed", exception.getMessage());
    }

    @Test
    void submitRating_RateSelf_ShouldThrowException() {
        // Arrange
        ratingRequest = RatingRequestDto.builder()
                .eventId(100L)
                .ratedUserId(1L) // Same as rater
                .rating(5)
                .review("Trying to rate myself")
                .build();

        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ratingService.submitRating(ratingRequest, volunteer));
        
        assertEquals("Cannot rate yourself", exception.getMessage());
    }

    @Test
    void submitRating_RatedUserNotParticipated_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 2L, ApplicationStatus.CONFIRMED))
                .thenReturn(false); // Rated user didn't participate
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 3L, ApplicationStatus.CONFIRMED))
                .thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ratingService.submitRating(ratingRequest, volunteer));
        
        assertEquals("Rated user did not participate in this event", exception.getMessage());
    }

    @Test
    void submitRating_RaterNotParticipated_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(false); // Rater didn't participate
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 2L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ratingService.submitRating(ratingRequest, volunteer));
        
        assertEquals("You must have participated in or organized the event to rate others", exception.getMessage());
    }

    @Test
    void canRate_ValidConditions_ShouldReturnTrue() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 2L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(100L, 1L, 2L))
                .thenReturn(false);

        // Act
        boolean result = ratingService.canRate(100L, 1L, 2L);

        // Assert
        assertTrue(result);
    }

    @Test
    void canRate_DuplicateRating_ShouldReturnFalse() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 1L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(applicationRepository.existsByEventIdAndVolunteerIdAndStatus(100L, 2L, ApplicationStatus.CONFIRMED))
                .thenReturn(true);
        when(ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(100L, 1L, 2L))
                .thenReturn(true); // Duplicate exists

        // Act
        boolean result = ratingService.canRate(100L, 1L, 2L);

        // Assert
        assertFalse(result);
    }

    @Test
    void canRate_EventNotFound_ShouldReturnFalse() {
        // Arrange
        when(eventRepository.findById(100L)).thenReturn(Optional.empty());

        // Act
        boolean result = ratingService.canRate(100L, 1L, 2L);

        // Assert
        assertFalse(result);
    }

    @Test
    void calculateAverageRating_WithRatings_ShouldReturnAverage() {
        // Arrange
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(4.5);

        // Act
        Double result = ratingService.calculateAverageRating(1L);

        // Assert
        assertEquals(4.5, result);
    }

    @Test
    void calculateAverageRating_NoRatings_ShouldReturnZero() {
        // Arrange
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(null);

        // Act
        Double result = ratingService.calculateAverageRating(1L);

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getUserRatings_ShouldReturnPaginatedRatings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Rating rating = Rating.builder()
                .id(999L)
                .event(completedEvent)
                .rater(volunteer)
                .ratedUser(organizer)
                .rating(5)
                .review("Great!")
                .build();
        
        Page<Rating> ratingPage = new PageImpl<>(java.util.List.of(rating), pageable, 1);
        when(ratingRepository.findByRatedUserId(1L, pageable)).thenReturn(ratingPage);

        // Act
        Page<RatingResponseDto> result = ratingService.getUserRatings(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        RatingResponseDto dto = result.getContent().get(0);
        assertEquals(999L, dto.getId());
        assertEquals(5, dto.getRating());
        assertEquals("Great!", dto.getReview());
    }

    @Test
    void getUserRatingStatistics_ShouldReturnStatistics() {
        // Arrange
        when(ratingRepository.calculateAverageRating(1L)).thenReturn(4.5);
        when(ratingRepository.countByRatedUserId(1L)).thenReturn(10L);
        when(ratingRepository.getRatingDistribution(1L)).thenReturn(new Object[][]{
                {5, 6L},
                {4, 3L},
                {3, 1L},
                {2, 0L},
                {1, 0L}
        });

        // Act
        RatingStatisticsDto result = ratingService.getUserRatingStatistics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10L, result.getTotalRatings());
        assertNotNull(result.getRatingDistribution());
        assertEquals(6L, result.getRatingDistribution().get(5));
        assertEquals(3L, result.getRatingDistribution().get(4));
        assertEquals(1L, result.getRatingDistribution().get(3));
    }

    @Test
    void getRatingsGivenByUser_ShouldReturnPaginatedRatings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Rating rating = Rating.builder()
                .id(999L)
                .event(completedEvent)
                .rater(volunteer)
                .ratedUser(organizer)
                .rating(5)
                .review("Given rating")
                .build();
        
        Page<Rating> ratingPage = new PageImpl<>(java.util.List.of(rating), pageable, 1);
        when(ratingRepository.findByRaterId(1L, pageable)).thenReturn(ratingPage);

        // Act
        Page<RatingResponseDto> result = ratingService.getRatingsGivenByUser(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        RatingResponseDto dto = result.getContent().get(0);
        assertEquals(999L, dto.getId());
        assertEquals(5, dto.getRating());
        assertEquals("Given rating", dto.getReview());
    }

    @Test
    void getEventRatings_ShouldReturnPaginatedRatings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Rating rating = Rating.builder()
                .id(999L)
                .event(completedEvent)
                .rater(volunteer)
                .ratedUser(organizer)
                .rating(5)
                .review("Event rating")
                .build();
        
        Page<Rating> ratingPage = new PageImpl<>(java.util.List.of(rating), pageable, 1);
        when(ratingRepository.findByEventId(100L, pageable)).thenReturn(ratingPage);

        // Act
        Page<RatingResponseDto> result = ratingService.getEventRatings(100L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        RatingResponseDto dto = result.getContent().get(0);
        assertEquals(999L, dto.getId());
        assertEquals(5, dto.getRating());
        assertEquals("Event rating", dto.getReview());
    }
}