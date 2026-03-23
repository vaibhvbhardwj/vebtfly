package com.example.vently.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vently.admin.dto.AverageRatingsDTO;
import com.example.vently.admin.dto.DisputeMetricsDTO;
import com.example.vently.admin.dto.NoShowStatisticsDTO;
import com.example.vently.admin.dto.PlatformAnalyticsDTO;
import com.example.vently.admin.dto.RevenueMetricsDTO;
import com.example.vently.admin.dto.UserGrowthTrendDTO;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.dispute.DisputeRepository;
import com.example.vently.dispute.Dispute;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.payment.PaymentRepository;
import com.example.vently.payment.PaymentStatus;
import com.example.vently.rating.RatingRepository;
import com.example.vently.user.Role;
import com.example.vently.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now();
    }

    @Test
    @DisplayName("Should calculate platform analytics correctly")
    void testGetPlatformAnalytics() {
        // Arrange
        when(userRepository.countByRole(Role.VOLUNTEER)).thenReturn(100L);
        when(userRepository.countByRole(Role.ORGANIZER)).thenReturn(50L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(5L);
        when(eventRepository.count()).thenReturn(200L);
        when(eventRepository.countByStatus(EventStatus.COMPLETED)).thenReturn(150L);
        when(eventRepository.countByStatus(EventStatus.CANCELLED)).thenReturn(20L);

        List<Object[]> paymentStats = new ArrayList<>();
        paymentStats.add(new Object[]{100L, BigDecimal.valueOf(10000), BigDecimal.valueOf(1000)});
        when(paymentRepository.getPaymentStats(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(paymentStats);

        // Act
        PlatformAnalyticsDTO result = analyticsService.getPlatformAnalytics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(155L, result.getTotalUsers());
        assertEquals(100L, result.getTotalVolunteers());
        assertEquals(50L, result.getTotalOrganizers());
        assertEquals(5L, result.getTotalAdmins());
        assertEquals(200L, result.getTotalEvents());
        assertEquals(150L, result.getCompletedEvents());
        assertEquals(20L, result.getCancelledEvents());
        assertEquals(100L, result.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(10000), result.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(1000), result.getPlatformFeesCollected());

        verify(userRepository).countByRole(Role.VOLUNTEER);
        verify(userRepository).countByRole(Role.ORGANIZER);
        verify(userRepository).countByRole(Role.ADMIN);
        verify(eventRepository).count();
        verify(eventRepository).countByStatus(EventStatus.COMPLETED);
        verify(eventRepository).countByStatus(EventStatus.CANCELLED);
        verify(paymentRepository).getPaymentStats(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should calculate user growth trends correctly")
    void testGetUserGrowthTrends() {
        // Arrange
        LocalDate testDate = LocalDate.now().minusDays(5);
        when(userRepository.countByRoleAndCreatedAtBetween(eq(Role.VOLUNTEER), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);
        when(userRepository.countByRoleAndCreatedAtBetween(eq(Role.ORGANIZER), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(userRepository.countByRoleAndCreatedAtBefore(eq(Role.VOLUNTEER), any(LocalDateTime.class)))
                .thenReturn(100L);
        when(userRepository.countByRoleAndCreatedAtBefore(eq(Role.ORGANIZER), any(LocalDateTime.class)))
                .thenReturn(50L);

        // Act
        List<UserGrowthTrendDTO> trends = analyticsService.getUserGrowthTrends(testDate, testDate.plusDays(2));

        // Assert
        assertNotNull(trends);
        assertEquals(3, trends.size());
        for (UserGrowthTrendDTO trend : trends) {
            assertEquals(10L, trend.getNewVolunteers());
            assertEquals(5L, trend.getNewOrganizers());
            assertEquals(100L, trend.getTotalVolunteers());
            assertEquals(50L, trend.getTotalOrganizers());
        }
    }

    @Test
    @DisplayName("Should calculate revenue metrics correctly")
    void testGetRevenueMetrics() {
        // Arrange
        List<Object[]> paymentStats = new ArrayList<>();
        paymentStats.add(new Object[]{50L, BigDecimal.valueOf(5000), BigDecimal.valueOf(500)});
        when(paymentRepository.getPaymentStats(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(paymentStats);
        when(paymentRepository.countByStatusAndCreatedAtBetween(eq(PaymentStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(45L);
        when(paymentRepository.countByStatusAndCreatedAtBetween(eq(PaymentStatus.FAILED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(paymentRepository.countByStatusAndCreatedAtBetween(eq(PaymentStatus.REFUNDED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2L);

        // Act
        RevenueMetricsDTO result = analyticsService.getRevenueMetrics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(5000), result.getTotalTransactionVolume());
        assertEquals(BigDecimal.valueOf(500), result.getPlatformFeesCollected());
        assertEquals(0, result.getAverageTransactionAmount().compareTo(BigDecimal.valueOf(100)));
        assertEquals(45L, result.getCompletedPayments());
        assertEquals(3L, result.getFailedPayments());
        assertEquals(2L, result.getRefundedPayments());

        verify(paymentRepository).getPaymentStats(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(paymentRepository).countByStatusAndCreatedAtBetween(eq(PaymentStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(paymentRepository).countByStatusAndCreatedAtBetween(eq(PaymentStatus.FAILED), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(paymentRepository).countByStatusAndCreatedAtBetween(eq(PaymentStatus.REFUNDED), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should calculate dispute metrics correctly")
    void testGetDisputeMetrics() {
        // Arrange
        when(disputeRepository.countByStatus(any())).thenReturn(10L);
        
        // Create mock disputes with resolution times
        Dispute dispute1 = new Dispute();
        dispute1.setCreatedAt(LocalDateTime.now().minusHours(24));
        dispute1.setResolvedAt(LocalDateTime.now());
        
        List<Dispute> disputes = new ArrayList<>();
        disputes.add(dispute1);
        
        when(disputeRepository.getDisputesForResolutionTimeCalculation(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(disputes);

        // Act
        DisputeMetricsDTO result = analyticsService.getDisputeMetrics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getOpenDisputes());
        assertEquals(10L, result.getResolvedDisputes());
        assertTrue(result.getAverageResolutionTimeHours() > 0);

        verify(disputeRepository, times(4)).countByStatus(any());
        verify(disputeRepository).getDisputesForResolutionTimeCalculation(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should calculate average ratings correctly")
    void testGetAverageRatings() {
        // Arrange
        when(ratingRepository.getAverageRatingForRole(Role.VOLUNTEER)).thenReturn(4.5);
        when(ratingRepository.countRatingsForRole(Role.VOLUNTEER)).thenReturn(100L);
        when(ratingRepository.getAverageRatingForRole(Role.ORGANIZER)).thenReturn(4.2);
        when(ratingRepository.countRatingsForRole(Role.ORGANIZER)).thenReturn(80L);

        // Act
        AverageRatingsDTO result = analyticsService.getAverageRatings();

        // Assert
        assertNotNull(result);
        assertEquals(4.5, result.getAverageVolunteerRating());
        assertEquals(100L, result.getVolunteerRatingCount());
        assertEquals(4.2, result.getAverageOrganizerRating());
        assertEquals(80L, result.getOrganizerRatingCount());

        verify(ratingRepository, times(2)).getAverageRatingForRole(any(Role.class));
        verify(ratingRepository, times(2)).countRatingsForRole(any(Role.class));
    }

    @Test
    @DisplayName("Should calculate no-show statistics correctly")
    void testGetNoShowStatistics() {
        // Arrange
        when(userRepository.countVolunteersWithNoShows(Role.VOLUNTEER)).thenReturn(20L);
        when(userRepository.sumNoShowCounts(Role.VOLUNTEER)).thenReturn(50L);
        when(userRepository.countByRole(Role.VOLUNTEER)).thenReturn(100L);
        when(userRepository.countSuspendedDueToNoShows(Role.VOLUNTEER)).thenReturn(5L);
        when(userRepository.countBannedDueToNoShows(Role.VOLUNTEER)).thenReturn(2L);

        // Act
        NoShowStatisticsDTO result = analyticsService.getNoShowStatistics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.getTotalNoShows());
        assertEquals(20L, result.getVolunteersWithNoShows());
        assertEquals(0.2, result.getNoShowRate());
        assertEquals(5L, result.getSuspendedDueToNoShows());
        assertEquals(2L, result.getBannedDueToNoShows());

        verify(userRepository).countVolunteersWithNoShows(Role.VOLUNTEER);
        verify(userRepository).sumNoShowCounts(Role.VOLUNTEER);
        verify(userRepository).countByRole(Role.VOLUNTEER);
        verify(userRepository).countSuspendedDueToNoShows(Role.VOLUNTEER);
        verify(userRepository).countBannedDueToNoShows(Role.VOLUNTEER);
    }

    @Test
    @DisplayName("Should handle empty payment stats")
    void testGetPlatformAnalyticsWithEmptyPaymentStats() {
        // Arrange
        when(userRepository.countByRole(Role.VOLUNTEER)).thenReturn(100L);
        when(userRepository.countByRole(Role.ORGANIZER)).thenReturn(50L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(5L);
        when(eventRepository.count()).thenReturn(200L);
        when(eventRepository.countByStatus(EventStatus.COMPLETED)).thenReturn(150L);
        when(eventRepository.countByStatus(EventStatus.CANCELLED)).thenReturn(20L);
        when(paymentRepository.getPaymentStats(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        PlatformAnalyticsDTO result = analyticsService.getPlatformAnalytics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalTransactions());
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, result.getPlatformFeesCollected());
    }

    @Test
    @DisplayName("Should handle null average ratings")
    void testGetAverageRatingsWithNullValues() {
        // Arrange
        when(ratingRepository.getAverageRatingForRole(Role.VOLUNTEER)).thenReturn(null);
        when(ratingRepository.countRatingsForRole(Role.VOLUNTEER)).thenReturn(null);
        when(ratingRepository.getAverageRatingForRole(Role.ORGANIZER)).thenReturn(null);
        when(ratingRepository.countRatingsForRole(Role.ORGANIZER)).thenReturn(null);

        // Act
        AverageRatingsDTO result = analyticsService.getAverageRatings();

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getAverageVolunteerRating());
        assertEquals(0L, result.getVolunteerRatingCount());
        assertEquals(0.0, result.getAverageOrganizerRating());
        assertEquals(0L, result.getOrganizerRatingCount());
    }

    @Test
    @DisplayName("Should calculate date range filtering correctly")
    void testDateRangeFiltering() {
        // Arrange
        LocalDate customStart = LocalDate.now().minusDays(60);
        LocalDate customEnd = LocalDate.now().minusDays(30);

        when(userRepository.countByRole(Role.VOLUNTEER)).thenReturn(100L);
        when(userRepository.countByRole(Role.ORGANIZER)).thenReturn(50L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(5L);
        when(eventRepository.count()).thenReturn(200L);
        when(eventRepository.countByStatus(EventStatus.COMPLETED)).thenReturn(150L);
        when(eventRepository.countByStatus(EventStatus.CANCELLED)).thenReturn(20L);
        when(paymentRepository.getPaymentStats(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        PlatformAnalyticsDTO result = analyticsService.getPlatformAnalytics(customStart, customEnd);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).getPaymentStats(
                argThat(arg -> arg.toLocalDate().equals(customStart)),
                argThat(arg -> arg.toLocalDate().equals(customEnd))
        );
    }
}
