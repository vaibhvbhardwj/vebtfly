package com.example.vently.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vently.admin.dto.AverageRatingsDTO;
import com.example.vently.admin.dto.DisputeMetricsDTO;
import com.example.vently.admin.dto.NoShowStatisticsDTO;
import com.example.vently.admin.dto.PlatformAnalyticsDTO;
import com.example.vently.admin.dto.RevenueMetricsDTO;
import com.example.vently.admin.dto.UserGrowthTrendDTO;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.dispute.DisputeRepository;
import com.example.vently.dispute.DisputeStatus;
import com.example.vently.dispute.Dispute;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.payment.PaymentRepository;
import com.example.vently.payment.PaymentStatus;
import com.example.vently.rating.RatingRepository;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Get platform-wide analytics metrics
     * Requirements: 21.1, 21.2, 21.3
     */
    public PlatformAnalyticsDTO getPlatformAnalytics(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching platform analytics from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Count users by role
        Long totalVolunteers = userRepository.countByRole(Role.VOLUNTEER);
        Long totalOrganizers = userRepository.countByRole(Role.ORGANIZER);
        Long totalAdmins = userRepository.countByRole(Role.ADMIN);
        Long totalUsers = totalVolunteers + totalOrganizers + totalAdmins;

        // Count events by status
        Long totalEvents = eventRepository.count();
        Long completedEvents = eventRepository.countByStatus(EventStatus.COMPLETED);
        Long cancelledEvents = eventRepository.countByStatus(EventStatus.CANCELLED);

        // Calculate revenue metrics
        List<Object[]> paymentStats = paymentRepository.getPaymentStats(startDateTime, endDateTime);
        Long totalTransactions = 0L;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal platformFeesCollected = BigDecimal.ZERO;

        if (!paymentStats.isEmpty()) {
            Object[] stats = paymentStats.get(0);
            totalTransactions = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
            totalRevenue = stats[1] != null ? new BigDecimal(stats[1].toString()) : BigDecimal.ZERO;
            platformFeesCollected = totalRevenue.multiply(new BigDecimal("0.10"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return PlatformAnalyticsDTO.builder()
                .totalUsers(totalUsers)
                .totalVolunteers(totalVolunteers)
                .totalOrganizers(totalOrganizers)
                .totalAdmins(totalAdmins)
                .totalEvents(totalEvents)
                .completedEvents(completedEvents)
                .cancelledEvents(cancelledEvents)
                .totalTransactions(totalTransactions)
                .totalRevenue(totalRevenue)
                .platformFeesCollected(platformFeesCollected)
                .build();
    }

    /**
     * Get user growth trends over time
     * Requirements: 21.4
     */
    public List<UserGrowthTrendDTO> getUserGrowthTrends(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching user growth trends from {} to {}", startDate, endDate);

        List<UserGrowthTrendDTO> trends = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);

            // Count new volunteers and organizers for this day
            Long newVolunteers = userRepository.countByRoleAndCreatedAtBetween(
                    Role.VOLUNTEER, dayStart, dayEnd);
            Long newOrganizers = userRepository.countByRoleAndCreatedAtBetween(
                    Role.ORGANIZER, dayStart, dayEnd);

            // Count total volunteers and organizers up to this day
            Long totalVolunteers = userRepository.countByRoleAndCreatedAtBefore(
                    Role.VOLUNTEER, dayEnd);
            Long totalOrganizers = userRepository.countByRoleAndCreatedAtBefore(
                    Role.ORGANIZER, dayEnd);

            trends.add(UserGrowthTrendDTO.builder()
                    .date(currentDate)
                    .newVolunteers(newVolunteers)
                    .newOrganizers(newOrganizers)
                    .totalVolunteers(totalVolunteers)
                    .totalOrganizers(totalOrganizers)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return trends;
    }

    /**
     * Get revenue metrics
     * Requirements: 21.3
     */
    public RevenueMetricsDTO getRevenueMetrics(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching revenue metrics from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get payment statistics
        List<Object[]> paymentStats = paymentRepository.getPaymentStats(startDateTime, endDateTime);
        Long totalTransactions = 0L;
        BigDecimal totalTransactionVolume = BigDecimal.ZERO;
        BigDecimal platformFeesCollected = BigDecimal.ZERO;

        if (!paymentStats.isEmpty()) {
            Object[] stats = paymentStats.get(0);
            totalTransactions = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
            totalTransactionVolume = stats[1] != null ? new BigDecimal(stats[1].toString()) : BigDecimal.ZERO;
            platformFeesCollected = totalTransactionVolume.multiply(new BigDecimal("0.10"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        // Calculate average transaction amount
        BigDecimal averageTransactionAmount = BigDecimal.ZERO;
        if (totalTransactions > 0 && totalTransactionVolume.compareTo(BigDecimal.ZERO) > 0) {
            averageTransactionAmount = totalTransactionVolume.divide(
                    BigDecimal.valueOf(totalTransactions), 2, java.math.RoundingMode.HALF_UP);
        }

        // Count payments by status
        Long completedPayments = paymentRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.COMPLETED, startDateTime, endDateTime);
        Long failedPayments = paymentRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.FAILED, startDateTime, endDateTime);
        Long refundedPayments = paymentRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.REFUNDED, startDateTime, endDateTime);

        return RevenueMetricsDTO.builder()
                .totalTransactions(totalTransactions)
                .totalTransactionVolume(totalTransactionVolume)
                .platformFeesCollected(platformFeesCollected)
                .averageTransactionAmount(averageTransactionAmount)
                .completedPayments(completedPayments)
                .failedPayments(failedPayments)
                .refundedPayments(refundedPayments)
                .build();
    }

    /**
     * Get dispute metrics
     * Requirements: 21.8
     */
    public DisputeMetricsDTO getDisputeMetrics(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching dispute metrics from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Count disputes by status
        Long openDisputes = disputeRepository.countByStatus(DisputeStatus.OPEN);
        Long underReviewDisputes = disputeRepository.countByStatus(DisputeStatus.UNDER_REVIEW);
        Long resolvedDisputes = disputeRepository.countByStatus(DisputeStatus.RESOLVED);
        Long totalDisputes = openDisputes + underReviewDisputes + resolvedDisputes
                + disputeRepository.countByStatus(DisputeStatus.CLOSED);

        // Calculate average resolution time
        Double averageResolutionTimeHours = calculateAverageResolutionTime(startDateTime, endDateTime);

        return DisputeMetricsDTO.builder()
                .openDisputes(openDisputes)
                .resolvedDisputes(resolvedDisputes)
                .totalDisputes(totalDisputes)
                .averageResolutionTimeHours(averageResolutionTimeHours)
                .disputesUnderReview(underReviewDisputes)
                .build();
    }

    /**
     * Get average ratings for volunteers and organizers
     * Requirements: 21.5
     */
    public AverageRatingsDTO getAverageRatings() {
        log.info("Fetching average ratings");

        // Get average rating for volunteers (rated_id is a volunteer)
        Double averageVolunteerRating = ratingRepository.getAverageRatingForRole(Role.VOLUNTEER);
        Long volunteerRatingCount = ratingRepository.countRatingsForRole(Role.VOLUNTEER);

        // Get average rating for organizers (rated_id is an organizer)
        Double averageOrganizerRating = ratingRepository.getAverageRatingForRole(Role.ORGANIZER);
        Long organizerRatingCount = ratingRepository.countRatingsForRole(Role.ORGANIZER);

        return AverageRatingsDTO.builder()
                .averageVolunteerRating(averageVolunteerRating != null ? averageVolunteerRating : 0.0)
                .volunteerRatingCount(volunteerRatingCount != null ? volunteerRatingCount : 0L)
                .averageOrganizerRating(averageOrganizerRating != null ? averageOrganizerRating : 0.0)
                .organizerRatingCount(organizerRatingCount != null ? organizerRatingCount : 0L)
                .build();
    }

    /**
     * Get no-show statistics and trends
     * Requirements: 21.6
     */
    public NoShowStatisticsDTO getNoShowStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching no-show statistics from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Count total no-shows (users with noShowCount > 0)
        Long volunteersWithNoShows = userRepository.countVolunteersWithNoShows(Role.VOLUNTEER);
        Long totalNoShows = userRepository.sumNoShowCounts(Role.VOLUNTEER);
        if (totalNoShows == null) totalNoShows = 0L;

        // Calculate no-show rate
        Long totalVolunteers = userRepository.countByRole(Role.VOLUNTEER);
        Double noShowRate = totalVolunteers > 0 ? (double) volunteersWithNoShows / totalVolunteers : 0.0;

        // Count suspended and banned users due to no-shows
        Long suspendedDueToNoShows = userRepository.countSuspendedDueToNoShows(Role.VOLUNTEER);
        Long bannedDueToNoShows = userRepository.countBannedDueToNoShows(Role.VOLUNTEER);

        return NoShowStatisticsDTO.builder()
                .totalNoShows(totalNoShows)
                .volunteersWithNoShows(volunteersWithNoShows)
                .noShowRate(noShowRate)
                .suspendedDueToNoShows(suspendedDueToNoShows)
                .bannedDueToNoShows(bannedDueToNoShows)
                .build();
    }

    /**
     * Helper method to calculate average resolution time for disputes
     */
    private Double calculateAverageResolutionTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Dispute> disputes = disputeRepository.getDisputesForResolutionTimeCalculation(startDateTime, endDateTime);

        if (disputes.isEmpty()) {
            return 0.0;
        }

        double totalHours = 0;
        for (Dispute dispute : disputes) {
            if (dispute.getResolvedAt() != null && dispute.getCreatedAt() != null) {
                long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(dispute.getCreatedAt(), dispute.getResolvedAt());
                totalHours += durationMinutes / 60.0;
            }
        }

        return totalHours / disputes.size();
    }
}
