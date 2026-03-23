package com.example.vently.rating;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final com.example.vently.notification.NotificationService notificationService;

    /**
     * Submit a rating for a user after an event
     * Requirements: 15.1, 15.2, 15.3, 15.5, 15.6
     */
    @Transactional
    public Rating submitRating(RatingRequestDto ratingRequest, User rater) {
        log.info("Submitting rating by user {} for event {}", rater.getId(), ratingRequest.getEventId());

        // Validate eligibility to rate
        validateRatingEligibility(ratingRequest, rater);

        // Check for duplicate rating
        if (ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(
                ratingRequest.getEventId(), rater.getId(), ratingRequest.getRatedUserId())) {
            throw new IllegalStateException("You have already rated this user for this event");
        }

        // Check 7-day window
        Event event = eventRepository.findById(ratingRequest.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        
        if (!isWithin7DayWindow(event)) {
            throw new IllegalStateException("Rating window has closed (7 days after event completion)");
        }

        // Get rated user
        User ratedUser = userRepository.findById(ratingRequest.getRatedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Rated user not found"));

        // Create and save rating
        Rating rating = Rating.builder()
                .event(event)
                .rater(rater)
                .ratedUser(ratedUser)
                .rating(ratingRequest.getRating())
                .review(ratingRequest.getReview())
                .build();

        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating submitted successfully: {}", savedRating.getId());
        
        // Send notification to rated user
        notificationService.createNotification(
            ratedUser,
            "RATING_RECEIVED",
            "New Rating Received",
            String.format("You received a %d-star rating from %s for event '%s'.", 
                rating.getRating(),
                rater.getFullName(),
                event.getTitle())
        );
        
        return savedRating;
    }

    /**
     * Validate if user is eligible to rate
     * Requirements: 15.3, 15.5, 15.6
     */
    private void validateRatingEligibility(RatingRequestDto ratingRequest, User rater) {
        Event event = eventRepository.findById(ratingRequest.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if event is completed
        if (event.getStatus() != EventStatus.COMPLETED) {
            throw new IllegalStateException("Cannot rate for events that are not completed");
        }

        // Check if rater is trying to rate themselves
        if (rater.getId().equals(ratingRequest.getRatedUserId())) {
            throw new IllegalArgumentException("Cannot rate yourself");
        }

        // Check if rated user participated in the event
        boolean ratedUserParticipated = applicationRepository.existsByEventIdAndVolunteerIdAndStatus(
                ratingRequest.getEventId(), ratingRequest.getRatedUserId(), ApplicationStatus.CONFIRMED);
        
        if (!ratedUserParticipated) {
            throw new IllegalArgumentException("Rated user did not participate in this event");
        }

        // Check if rater participated in or organized the event
        boolean raterParticipated = false;
        
        if (rater.getRole().name().equals("VOLUNTEER")) {
            // Check if volunteer attended the event
            raterParticipated = applicationRepository.existsByEventIdAndVolunteerIdAndStatus(
                    ratingRequest.getEventId(), rater.getId(), ApplicationStatus.CONFIRMED);
        } else if (rater.getRole().name().equals("ORGANIZER")) {
            // Check if organizer owns the event
            raterParticipated = event.getOrganizer().getId().equals(rater.getId());
        }

        if (!raterParticipated) {
            throw new IllegalStateException("You must have participated in or organized the event to rate others");
        }
    }

    /**
     * Check if rating is within 7-day window after event completion
     * Requirements: 15.6
     */
    private boolean isWithin7DayWindow(Event event) {
        // Assuming event completion time is event date + 8 hours (standard event duration)
        LocalDateTime eventCompletionTime = event.getEventDateTime().plusHours(8);
        LocalDateTime ratingDeadline = eventCompletionTime.plusDays(7);
        
        return LocalDateTime.now().isBefore(ratingDeadline) || LocalDateTime.now().isEqual(ratingDeadline);
    }

    /**
     * Check if user can rate for a specific event and user
     * Requirements: 15.3, 15.5, 15.6
     */
    public boolean canRate(Long eventId, Long raterId, Long ratedUserId) {
        try {
            RatingRequestDto ratingRequest = RatingRequestDto.builder()
                    .eventId(eventId)
                    .ratedUserId(ratedUserId)
                    .rating(5) // Default rating for validation
                    .build();
            
            User rater = userRepository.findById(raterId)
                    .orElseThrow(() -> new IllegalArgumentException("Rater not found"));
            
            validateRatingEligibility(ratingRequest, rater);
            
            // Check for duplicate
            if (ratingRepository.existsByEventIdAndRaterIdAndRatedUserId(eventId, raterId, ratedUserId)) {
                return false;
            }
            
            // Check 7-day window
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found"));
            
            return isWithin7DayWindow(event);
            
        } catch (Exception e) {
            log.debug("User cannot rate: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate average rating for a user
     * Requirements: 15.4
     */
    public Double calculateAverageRating(Long userId) {
        Double average = ratingRepository.calculateAverageRating(userId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0; // Round to 1 decimal place
    }

    /**
     * Get paginated ratings for a user
     * Requirements: 15.4
     */
    public Page<RatingResponseDto> getUserRatings(Long userId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByRatedUserId(userId, pageable);
        
        return ratings.map(this::convertToResponseDto);
    }

    /**
     * Get rating statistics for a user
     * Requirements: 15.4
     */
    public RatingStatisticsDto getUserRatingStatistics(Long userId) {
        Double averageRating = calculateAverageRating(userId);
        Long totalRatings = ratingRepository.countByRatedUserId(userId);
        
        // Get rating distribution
        Object[][] distributionData = ratingRepository.getRatingDistribution(userId);
        Map<Integer, Long> distribution = new HashMap<>();
        
        for (Object[] row : distributionData) {
            Integer ratingValue = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(ratingValue, count);
        }
        
        RatingStatisticsDto statistics = RatingStatisticsDto.builder()
                .userId(userId)
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .build();
        
        statistics.calculateDistribution(distribution);
        return statistics;
    }

    /**
     * Get ratings given by a user
     */
    public Page<RatingResponseDto> getRatingsGivenByUser(Long userId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByRaterId(userId, pageable);
        return ratings.map(this::convertToResponseDto);
    }

    /**
     * Convert Rating entity to RatingResponseDto
     */
    private RatingResponseDto convertToResponseDto(Rating rating) {
        return RatingResponseDto.builder()
                .id(rating.getId())
                .eventId(rating.getEvent().getId())
                .eventTitle(rating.getEvent().getTitle())
                .raterId(rating.getRater().getId())
                .raterName(rating.getRater().getFullName())
                .ratedUserId(rating.getRatedUser().getId())
                .ratedUserName(rating.getRatedUser().getFullName())
                .rating(rating.getRating())
                .review(rating.getReview())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    /**
     * Get average rating for volunteers
     */
    public Double getAverageVolunteerRating() {
        Double average = ratingRepository.getAverageVolunteerRating();
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    /**
     * Get average rating for organizers
     */
    public Double getAverageOrganizerRating() {
        Double average = ratingRepository.getAverageOrganizerRating();
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    /**
     * Get ratings for a specific event
     */
    public Page<RatingResponseDto> getEventRatings(Long eventId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByEventId(eventId, pageable);
        return ratings.map(this::convertToResponseDto);
    }
}