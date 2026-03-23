package com.example.vently.subscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User volunteer;
    private User organizer;
    private Subscription freeSubscription;
    private Subscription platinumSubscription;

    @BeforeEach
    void setUp() {
        volunteer = User.builder().id(1L).email("volunteer@test.com").role(Role.VOLUNTEER).build();
        organizer = User.builder().id(2L).email("organizer@test.com").role(Role.ORGANIZER).build();

        freeSubscription = Subscription.builder()
            .id(1L).user(volunteer).tier(SubscriptionTier.FREE)
            .startDate(LocalDate.now()).active(true).build();

        platinumSubscription = Subscription.builder()
            .id(2L).user(volunteer).tier(SubscriptionTier.PLATINUM)
            .startDate(LocalDate.now()).active(true).build();
    }

    // ── Volunteer application limits ──────────────────────────────────────────

    @Test
    void canApplyToEvent_FreeVolunteer_BelowLimit_ReturnsTrue() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.of(freeSubscription));
        when(applicationRepository.countByVolunteerIdAndStatusIn(1L,
            ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED, ApplicationStatus.CONFIRMED))
            .thenReturn(3L);
        assertTrue(subscriptionService.canApplyToEvent(1L));
    }

    @Test
    void canApplyToEvent_FreeVolunteer_AtLimit_ReturnsFalse() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.of(freeSubscription));
        when(applicationRepository.countByVolunteerIdAndStatusIn(1L,
            ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED, ApplicationStatus.CONFIRMED))
            .thenReturn(5L);
        assertFalse(subscriptionService.canApplyToEvent(1L));
    }

    @Test
    void canApplyToEvent_GoldVolunteer_BelowGoldLimit_ReturnsTrue() {
        Subscription goldSub = Subscription.builder().id(3L).user(volunteer)
            .tier(SubscriptionTier.GOLD).startDate(LocalDate.now()).active(true).build();
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.of(goldSub));
        when(applicationRepository.countByVolunteerIdAndStatusIn(1L,
            ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED, ApplicationStatus.CONFIRMED))
            .thenReturn(10L); // below gold limit of 12
        assertTrue(subscriptionService.canApplyToEvent(1L));
    }

    @Test
    void canApplyToEvent_GoldVolunteer_AtGoldLimit_ReturnsFalse() {
        Subscription goldSub = Subscription.builder().id(3L).user(volunteer)
            .tier(SubscriptionTier.GOLD).startDate(LocalDate.now()).active(true).build();
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.of(goldSub));
        when(applicationRepository.countByVolunteerIdAndStatusIn(1L,
            ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED, ApplicationStatus.CONFIRMED))
            .thenReturn(12L);
        assertFalse(subscriptionService.canApplyToEvent(1L));
    }

    @Test
    void canApplyToEvent_PlatinumVolunteer_UnlimitedApplications_ReturnsTrue() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.of(platinumSubscription));
        assertTrue(subscriptionService.canApplyToEvent(1L));
        verify(applicationRepository, never()).countByVolunteerIdAndStatusIn(anyLong(), any(), any(), any());
    }

    // ── Organizer event limits ────────────────────────────────────────────────

    @Test
    void canCreateEvent_FreeOrganizer_BelowLimit_ReturnsTrue() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(2L))
            .thenReturn(Optional.of(Subscription.builder().id(3L).user(organizer)
                .tier(SubscriptionTier.FREE).startDate(LocalDate.now()).active(true).build()));
        when(eventRepository.countByOrganizerIdAndStatusIn(2L,
            EventStatus.DRAFT, EventStatus.PUBLISHED, EventStatus.DEPOSIT_PAID, EventStatus.IN_PROGRESS))
            .thenReturn(2L);
        assertTrue(subscriptionService.canCreateEvent(2L));
    }

    @Test
    void canCreateEvent_FreeOrganizer_AtLimit_ReturnsFalse() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(2L))
            .thenReturn(Optional.of(Subscription.builder().id(3L).user(organizer)
                .tier(SubscriptionTier.FREE).startDate(LocalDate.now()).active(true).build()));
        when(eventRepository.countByOrganizerIdAndStatusIn(2L,
            EventStatus.DRAFT, EventStatus.PUBLISHED, EventStatus.DEPOSIT_PAID, EventStatus.IN_PROGRESS))
            .thenReturn(3L);
        assertFalse(subscriptionService.canCreateEvent(2L));
    }

    @Test
    void canCreateEvent_GoldOrganizer_BelowGoldLimit_ReturnsTrue() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(2L))
            .thenReturn(Optional.of(Subscription.builder().id(3L).user(organizer)
                .tier(SubscriptionTier.GOLD).startDate(LocalDate.now()).active(true).build()));
        when(eventRepository.countByOrganizerIdAndStatusIn(2L,
            EventStatus.DRAFT, EventStatus.PUBLISHED, EventStatus.DEPOSIT_PAID, EventStatus.IN_PROGRESS))
            .thenReturn(6L); // below gold limit of 8
        assertTrue(subscriptionService.canCreateEvent(2L));
    }

    @Test
    void canCreateEvent_PlatinumOrganizer_UnlimitedEvents_ReturnsTrue() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(2L))
            .thenReturn(Optional.of(Subscription.builder().id(3L).user(organizer)
                .tier(SubscriptionTier.PLATINUM).startDate(LocalDate.now()).active(true).build()));
        assertTrue(subscriptionService.canCreateEvent(2L));
        verify(eventRepository, never()).countByOrganizerIdAndStatusIn(anyLong(), any(), any(), any(), any());
    }

    // ── Upgrade / downgrade ───────────────────────────────────────────────────

    @Test
    void upgradeSubscription_ToGold_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(freeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription result = subscriptionService.upgradeSubscription(1L, SubscriptionTier.GOLD, "pay_gold_123");

        assertEquals(SubscriptionTier.GOLD, result.getTier());
        assertTrue(result.getActive());
        assertEquals("pay_gold_123", result.getRazorpayPaymentId());
    }

    @Test
    void upgradeSubscription_ToPlatinum_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(freeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription result = subscriptionService.upgradeSubscription(1L, SubscriptionTier.PLATINUM, "pay_plat_456");

        assertEquals(SubscriptionTier.PLATINUM, result.getTier());
        assertEquals("pay_plat_456", result.getRazorpayPaymentId());
    }

    @Test
    void upgradeSubscription_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> subscriptionService.upgradeSubscription(999L, SubscriptionTier.GOLD, "pay_123"));
    }

    @Test
    void upgradeSubscription_CreatesDefaultIfNoSubscription() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription result = subscriptionService.upgradeSubscription(1L, SubscriptionTier.PLATINUM, "pay_123");

        assertEquals(SubscriptionTier.PLATINUM, result.getTier());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
    }

    @Test
    void downgradeSubscription_Success_DowngradesToFree() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(platinumSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription result = subscriptionService.downgradeSubscription(1L);

        assertEquals(SubscriptionTier.FREE, result.getTier());
    }

    @Test
    void downgradeSubscription_NotFound_ThrowsException() {
        when(subscriptionRepository.findByUserId(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> subscriptionService.downgradeSubscription(999L));
    }

    @Test
    void deactivateSubscription_Success() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(platinumSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
        subscriptionService.deactivateSubscription(1L);
        verify(subscriptionRepository).save(argThat(s -> !s.getActive()));
    }

    @Test
    void reactivateSubscription_Success() {
        Subscription inactive = Subscription.builder().id(1L).user(volunteer)
            .tier(SubscriptionTier.PLATINUM).startDate(LocalDate.now()).active(false).build();
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(inactive));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
        subscriptionService.reactivateSubscription(1L);
        verify(subscriptionRepository).save(argThat(Subscription::getActive));
    }

    // ── Limit helpers ─────────────────────────────────────────────────────────

    @Test
    void getOrganizerEventLimit_Free_Returns3() {
        assertEquals(3, subscriptionService.getOrganizerEventLimit(SubscriptionTier.FREE));
    }

    @Test
    void getOrganizerEventLimit_Gold_Returns8() {
        assertEquals(8, subscriptionService.getOrganizerEventLimit(SubscriptionTier.GOLD));
    }

    @Test
    void getOrganizerEventLimit_Platinum_ReturnsUnlimited() {
        assertEquals(-1, subscriptionService.getOrganizerEventLimit(SubscriptionTier.PLATINUM));
    }

    @Test
    void getVolunteerApplicationLimit_Free_Returns5() {
        assertEquals(5, subscriptionService.getVolunteerApplicationLimit(SubscriptionTier.FREE));
    }

    @Test
    void getVolunteerApplicationLimit_Gold_Returns12() {
        assertEquals(12, subscriptionService.getVolunteerApplicationLimit(SubscriptionTier.GOLD));
    }

    @Test
    void getVolunteerApplicationLimit_Platinum_ReturnsUnlimited() {
        assertEquals(-1, subscriptionService.getVolunteerApplicationLimit(SubscriptionTier.PLATINUM));
    }

    // ── getCurrentSubscription ────────────────────────────────────────────────

    @Test
    void getCurrentSubscription_ActiveExists_ReturnsIt() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.of(freeSubscription));
        Subscription result = subscriptionService.getCurrentSubscription(1L);
        assertEquals(SubscriptionTier.FREE, result.getTier());
    }

    @Test
    void getCurrentSubscription_NoneExists_CreatesDefault() {
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
        Subscription result = subscriptionService.getCurrentSubscription(1L);
        assertEquals(SubscriptionTier.FREE, result.getTier());
        assertTrue(result.getActive());
    }
}
