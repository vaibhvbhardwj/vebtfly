package com.example.vently.rating;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.vently.user.Role;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Check if rating already exists for this combination
    boolean existsByEventIdAndRaterIdAndRatedUserId(Long eventId, Long raterId, Long ratedUserId);

    // Find rating by event, rater, and rated user
    Optional<Rating> findByEventIdAndRaterIdAndRatedUserId(Long eventId, Long raterId, Long ratedUserId);

    // Get all ratings for a user (as the rated person)
    Page<Rating> findByRatedUserId(Long ratedUserId, Pageable pageable);

    // Get all ratings given by a user (as the rater)
    Page<Rating> findByRaterId(Long raterId, Pageable pageable);

    // Calculate average rating for a user
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser.id = :userId")
    Double calculateAverageRating(@Param("userId") Long userId);

    // Count total ratings received by a user
    long countByRatedUserId(Long ratedUserId);

    // Get ratings for a specific event
    Page<Rating> findByEventId(Long eventId, Pageable pageable);

    // Get average rating for volunteers (users with VOLUNTEER role)
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser.role = com.example.vently.user.Role.VOLUNTEER")
    Double getAverageVolunteerRating();

    // Get average rating for organizers (users with ORGANIZER role)
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser.role = com.example.vently.user.Role.ORGANIZER")
    Double getAverageOrganizerRating();

    // Count ratings by rating value for a user (for distribution analysis)
    @Query("SELECT r.rating, COUNT(r) FROM Rating r WHERE r.ratedUser.id = :userId GROUP BY r.rating")
    Object[][] getRatingDistribution(@Param("userId") Long userId);
    
    // Analytics queries
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser.role = :role")
    Double getAverageRatingForRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedUser.role = :role")
    Long countRatingsForRole(@Param("role") Role role);
}
