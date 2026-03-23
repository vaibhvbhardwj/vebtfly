package com.example.vently.payment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    // Find all payouts for a payment
    List<Payout> findByPaymentId(Long paymentId);

    // Find payout by application
    List<Payout> findByApplicationId(Long applicationId);

    // Get all payouts for a volunteer
    Page<Payout> findByVolunteerId(Long volunteerId, Pageable pageable);

    // Get payouts for a volunteer with specific status
    Page<Payout> findByVolunteerIdAndStatus(Long volunteerId, PayoutStatus status, Pageable pageable);

    // Find all pending payouts
    List<Payout> findByStatus(PayoutStatus status);

    // Find failed payouts that haven't reached max retries
    @Query("SELECT p FROM Payout p WHERE p.status = com.example.vently.payment.PayoutStatus.FAILED AND p.retryCount < 3")
    List<Payout> findFailedPayoutsForRetry();

    // Get total earnings for a volunteer
    @Query("SELECT COALESCE(SUM(p.amount - p.platformFee), 0) FROM Payout p WHERE p.volunteer.id = :volunteerId AND p.status = com.example.vently.payment.PayoutStatus.COMPLETED")
    Double getTotalEarningsByVolunteerId(@Param("volunteerId") Long volunteerId);

    // Get total platform fees collected
    @Query("SELECT COALESCE(SUM(p.platformFee), 0) FROM Payout p WHERE p.status = com.example.vently.payment.PayoutStatus.COMPLETED")
    Double getTotalPlatformFees();

    // Count completed payouts for a volunteer
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.volunteer.id = :volunteerId AND p.status = com.example.vently.payment.PayoutStatus.COMPLETED")
    long countCompletedPayoutsByVolunteerId(@Param("volunteerId") Long volunteerId);
}
