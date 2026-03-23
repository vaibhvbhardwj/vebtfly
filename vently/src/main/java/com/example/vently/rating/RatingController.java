package com.example.vently.rating;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.rating.dto.RatingRequestDto;
import com.example.vently.rating.dto.RatingResponseDto;
import com.example.vently.rating.dto.RatingStatisticsDto;
import com.example.vently.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponseDto> submitRating(
            @Valid @RequestBody RatingRequestDto ratingRequest,
            @AuthenticationPrincipal User rater) {
        try {
            Rating rating = ratingService.submitRating(ratingRequest, rater);
            RatingResponseDto response = convertToResponseDto(rating);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/can-rate")
    public ResponseEntity<Boolean> canRate(
            @RequestParam Long eventId,
            @RequestParam Long ratedUserId,
            @AuthenticationPrincipal User rater) {
        boolean canRate = ratingService.canRate(eventId, rater.getId(), ratedUserId);
        return ResponseEntity.ok(canRate);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<RatingResponseDto>> getUserRatings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RatingResponseDto> ratings = ratingService.getUserRatings(userId, pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<RatingStatisticsDto> getUserRatingStatistics(@PathVariable Long userId) {
        RatingStatisticsDto statistics = ratingService.getUserRatingStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/given-by/{userId}")
    public ResponseEntity<Page<RatingResponseDto>> getRatingsGivenByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RatingResponseDto> ratings = ratingService.getRatingsGivenByUser(userId, pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/user/{userId}/average")
    public ResponseEntity<Double> getUserAverageRating(@PathVariable Long userId) {
        Double averageRating = ratingService.calculateAverageRating(userId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/volunteers/average")
    public ResponseEntity<Double> getAverageVolunteerRating() {
        Double average = ratingService.getAverageVolunteerRating();
        return ResponseEntity.ok(average);
    }

    @GetMapping("/organizers/average")
    public ResponseEntity<Double> getAverageOrganizerRating() {
        Double average = ratingService.getAverageOrganizerRating();
        return ResponseEntity.ok(average);
    }

    @GetMapping("/user/{userId}/distribution")
    public ResponseEntity<Map<Integer, Long>> getRatingDistribution(@PathVariable Long userId) {
        RatingStatisticsDto statistics = ratingService.getUserRatingStatistics(userId);
        return ResponseEntity.ok(statistics.getRatingDistribution());
    }

    @GetMapping("/can-rate-check")
    public ResponseEntity<Boolean> canUserRate(
            @RequestParam Long eventId,
            @RequestParam Long ratedUserId,
            @AuthenticationPrincipal User rater) {
        
        boolean canRate = ratingService.canRate(eventId, rater.getId(), ratedUserId);
        return ResponseEntity.ok(canRate);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Page<RatingResponseDto>> getEventRatings(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RatingResponseDto> ratings = ratingService.getEventRatings(eventId, pageable);
        return ResponseEntity.ok(ratings);
    }

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
}