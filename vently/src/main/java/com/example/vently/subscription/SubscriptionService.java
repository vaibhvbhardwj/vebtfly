package com.example.vently.subscription;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;

    // ── Volunteer limits ──────────────────────────────────────────────────────
    public static final int VOLUNTEER_FREE_LIMIT      = 5;
    public static final int VOLUNTEER_GOLD_LIMIT      = 12;
    public static final int VOLUNTEER_PLATINUM_LIMIT  = -1; // unlimited

    // ── Organizer limits ──────────────────────────────────────────────────────
    public static final int ORGANIZER_FREE_LIMIT      = 3;
    public static final int ORGANIZER_GOLD_LIMIT      = 8;
    public static final int ORGANIZER_PLATINUM_LIMIT  = -1; // unlimited

    // ── Prices (paise) ────────────────────────────────────────────────────────
    public static final int VOLUNTEER_GOLD_PRICE_PAISE     =  9900;  // ₹99
    public static final int VOLUNTEER_PLATINUM_PRICE_PAISE = 19900;  // ₹199
    public static final int ORGANIZER_GOLD_PRICE_PAISE     = 29900;  // ₹299
    public static final int ORGANIZER_PLATINUM_PRICE_PAISE = 79900;  // ₹799

    /** Get user's current active subscription (creates FREE one if missing). */
    public Subscription getCurrentSubscription(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        // ADMIN has no subscription
        if (user.getRole() == com.example.vently.user.Role.ADMIN) {
            return Subscription.builder()
                .user(user)
                .tier(SubscriptionTier.FREE)
                .startDate(java.time.LocalDate.now())
                .active(true)
                .build();
        }
        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .orElseGet(() -> createDefaultFreeSubscription(userId));
    }

    /** Price in paise for a given role + tier combination. */
    public int getPriceInPaise(Role role, SubscriptionTier tier) {
        if (role == Role.VOLUNTEER) {
            return switch (tier) {
                case GOLD     -> VOLUNTEER_GOLD_PRICE_PAISE;
                case PLATINUM -> VOLUNTEER_PLATINUM_PRICE_PAISE;
                default       -> 0;
            };
        } else { // ORGANIZER
            return switch (tier) {
                case GOLD     -> ORGANIZER_GOLD_PRICE_PAISE;
                case PLATINUM -> ORGANIZER_PLATINUM_PRICE_PAISE;
                default       -> 0;
            };
        }
    }

    /** Check if organizer can create a new event based on tier limits. */
    public boolean canCreateEvent(Long organizerId) {
        Subscription sub = getCurrentSubscription(organizerId);
        if (sub.getTier() == SubscriptionTier.PLATINUM) return true;

        int limit = sub.getTier() == SubscriptionTier.GOLD
            ? ORGANIZER_GOLD_LIMIT : ORGANIZER_FREE_LIMIT;

        long active = eventRepository.countByOrganizerIdAndStatusIn(
            organizerId,
            EventStatus.DRAFT, EventStatus.PUBLISHED,
            EventStatus.DEPOSIT_PAID, EventStatus.IN_PROGRESS
        );
        return active < limit;
    }

    /** Check if volunteer can apply to a new event based on tier limits. */
    public boolean canApplyToEvent(Long volunteerId) {
        Subscription sub = getCurrentSubscription(volunteerId);
        if (sub.getTier() == SubscriptionTier.PLATINUM) return true;

        int limit = sub.getTier() == SubscriptionTier.GOLD
            ? VOLUNTEER_GOLD_LIMIT : VOLUNTEER_FREE_LIMIT;

        long active = applicationRepository.countByVolunteerIdAndStatusIn(
            volunteerId,
            ApplicationStatus.PENDING,
            ApplicationStatus.ACCEPTED,
            ApplicationStatus.CONFIRMED
        );
        return active < limit;
    }

    /** Upgrade user subscription to the given tier. */
    @Transactional
    public Subscription upgradeSubscription(Long userId, SubscriptionTier tier, String razorpayPaymentId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Subscription sub = subscriptionRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultFreeSubscription(userId));

        sub.setTier(tier);
        sub.setRazorpayPaymentId(razorpayPaymentId);
        sub.setActive(true);
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusMonths(1));

        return subscriptionRepository.save(sub);
    }

    /** Downgrade user subscription to free tier. */
    @Transactional
    public Subscription downgradeSubscription(Long userId) {
        Subscription sub = subscriptionRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user: " + userId));
        sub.downgradeToFree();
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void deactivateSubscription(Long userId) {
        Subscription sub = subscriptionRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user: " + userId));
        sub.deactivate();
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void reactivateSubscription(Long userId) {
        Subscription sub = subscriptionRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user: " + userId));
        sub.reactivate();
        subscriptionRepository.save(sub);
    }

    /** Returns the event limit for a given tier (-1 = unlimited). */
    public int getOrganizerEventLimit(SubscriptionTier tier) {
        return switch (tier) {
            case GOLD     -> ORGANIZER_GOLD_LIMIT;
            case PLATINUM -> ORGANIZER_PLATINUM_LIMIT;
            default       -> ORGANIZER_FREE_LIMIT;
        };
    }

    /** Returns the application limit for a given tier (-1 = unlimited). */
    public int getVolunteerApplicationLimit(SubscriptionTier tier) {
        return switch (tier) {
            case GOLD     -> VOLUNTEER_GOLD_LIMIT;
            case PLATINUM -> VOLUNTEER_PLATINUM_LIMIT;
            default       -> VOLUNTEER_FREE_LIMIT;
        };
    }

    private Subscription createDefaultFreeSubscription(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Subscription sub = Subscription.builder()
            .user(user)
            .tier(SubscriptionTier.FREE)
            .startDate(LocalDate.now())
            .active(true)
            .build();

        return subscriptionRepository.save(sub);
    }
}
