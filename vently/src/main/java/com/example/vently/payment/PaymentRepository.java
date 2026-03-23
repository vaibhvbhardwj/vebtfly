package com.example.vently.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by event
    Optional<Payment> findByEventId(Long eventId);

    // Find payment by Razorpay payment ID
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    // Get all payments for an organizer
    Page<Payment> findByOrganizerId(Long organizerId, Pageable pageable);

    // Get payment history for an organizer with specific status
    Page<Payment> findByOrganizerIdAndStatus(Long organizerId, PaymentStatus status, Pageable pageable);

    // Check if payment exists for an event
    boolean existsByEventId(Long eventId);

    // Get all completed payments
    List<Payment> findByStatus(PaymentStatus status);

    // Get total payment amount for an organizer
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.organizer.id = :organizerId AND p.status = com.example.vently.payment.PaymentStatus.COMPLETED")
    Double getTotalPaymentsByOrganizerId(@Param("organizerId") Long organizerId);

    // Get total platform revenue (sum of all completed payments)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.example.vently.payment.PaymentStatus.COMPLETED")
    Double getTotalPlatformRevenue();
    
    // Analytics queries — avoid COALESCE with integer literals to prevent type mismatch
    @Query("SELECT COUNT(p), SUM(p.amount), SUM(p.amount) FROM Payment p WHERE p.status = com.example.vently.payment.PaymentStatus.COMPLETED AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Object[]> getPaymentStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    Long countByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
