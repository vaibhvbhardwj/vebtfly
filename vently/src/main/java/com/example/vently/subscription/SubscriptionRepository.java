package com.example.vently.subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find subscription by user ID
     */
    Optional<Subscription> findByUserId(Long userId);

    /**
     * Find active subscription by user ID
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.active = true")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    /**
     * Find all subscriptions by tier
     */
    Page<Subscription> findByTier(SubscriptionTier tier, Pageable pageable);

    /**
     * Find all active subscriptions by tier
     */
    Page<Subscription> findByTierAndActiveTrue(SubscriptionTier tier, Pageable pageable);

    /**
     * Find expired subscriptions for cleanup
     * (subscriptions where endDate is in the past and still marked as active)
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :currentDate AND s.active = true")
    List<Subscription> findExpiredSubscriptions(@Param("currentDate") LocalDate currentDate);

    /**
     * Check if user has an active paid subscription (GOLD or PLATINUM)
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.tier IN (com.example.vently.subscription.SubscriptionTier.GOLD, com.example.vently.subscription.SubscriptionTier.PLATINUM) AND s.active = true")
    boolean hasActivePremiumSubscription(@Param("userId") Long userId);

    /**
     * Check if user has any subscription (active or inactive)
     */
    boolean existsByUserId(Long userId);

    /**
     * Find subscription by Razorpay payment ID
     */
    Optional<Subscription> findByRazorpayPaymentId(String razorpayPaymentId);

    /**
     * Count active subscriptions by tier
     */
    Long countByTierAndActiveTrue(SubscriptionTier tier);

    /**
     * Count total active subscriptions
     */
    Long countByActiveTrue();

    /**
     * Find all subscriptions that will expire within a certain number of days
     * (useful for sending renewal reminders)
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :startDate AND :endDate AND s.active = true")
    List<Subscription> findSubscriptionsExpiringBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find all active paid subscriptions (GOLD or PLATINUM) for analytics
     */
    @Query("SELECT s FROM Subscription s WHERE s.tier IN (com.example.vently.subscription.SubscriptionTier.GOLD, com.example.vently.subscription.SubscriptionTier.PLATINUM) AND s.active = true")
    List<Subscription> findAllActivePremiumSubscriptions();

    /**
     * Count subscriptions created within a date range (for analytics)
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.startDate BETWEEN :startDate AND :endDate")
    Long countSubscriptionsCreatedBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find subscriptions by user role and tier (for analytics)
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.role = :role AND s.tier = :tier AND s.active = true")
    List<Subscription> findByUserRoleAndTier(
        @Param("role") String role,
        @Param("tier") SubscriptionTier tier
    );
}
