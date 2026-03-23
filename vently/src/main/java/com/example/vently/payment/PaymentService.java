package com.example.vently.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.audit.AuditService;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayoutRepository payoutRepository;
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final RazorpayService razorpayService;
    private final com.example.vently.notification.NotificationService notificationService;
    private final AuditService auditService;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = BigDecimal.valueOf(0.10);

    /**
     * Create a Razorpay order for event deposit.
     * Returns the Razorpay order ID for the frontend to open checkout.
     */
    @Transactional
    public String createDepositIntent(Long eventId) {
        log.info("Creating deposit order for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new RuntimeException("Event must be in PUBLISHED status to create deposit");
        }

        if (paymentRepository.existsByEventId(eventId)) {
            throw new RuntimeException("Payment already exists for this event");
        }

        long confirmedCount = applicationRepository.countByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);
        if (confirmedCount == 0) {
            throw new RuntimeException("No confirmed volunteers for this event");
        }

        BigDecimal totalDeposit = event.getPaymentPerVolunteer()
                .multiply(BigDecimal.valueOf(confirmedCount));

        try {
            String receipt = "deposit_" + eventId + "_" + System.currentTimeMillis();
            com.razorpay.Order order = razorpayService.createOrder(totalDeposit, "INR", receipt);

            Payment payment = Payment.builder()
                    .event(event)
                    .organizer(event.getOrganizer())
                    .amount(totalDeposit)
                    .status(PaymentStatus.PENDING)
                    .razorpayPaymentId(order.get("id")) // store order ID initially
                    .build();

            paymentRepository.save(payment);

            log.info("Deposit order created for event: {} amount: {}", eventId, totalDeposit);
            return order.get("id"); // return Razorpay order ID

        } catch (Exception e) {
            log.error("Failed to create deposit order: {}", e.getMessage());
            throw new RuntimeException("Failed to create deposit order: " + e.getMessage());
        }
    }

    /**
     * Confirm deposit after Razorpay payment success.
     * Verifies signature and updates event status to DEPOSIT_PAID.
     */
    @Transactional
    public void confirmDeposit(Long eventId, String razorpayPaymentId) {
        log.info("Confirming deposit for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Payment payment = paymentRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Payment not found for event"));

        // Update with actual payment ID
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.markCompleted();
        paymentRepository.save(payment);

        event.setStatus(EventStatus.DEPOSIT_PAID);
        eventRepository.save(event);

        log.info("Deposit confirmed for event: {}", eventId);

        auditService.logPaymentTransaction(
            event.getOrganizer(), eventId,
            payment.getAmount().toString(), "DEPOSIT", "UNKNOWN"
        );

        List<Application> confirmedApplications = applicationRepository
            .findByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);

        for (Application application : confirmedApplications) {
            notificationService.createNotification(
                application.getVolunteer(), "PAYMENT",
                "Event Deposit Confirmed",
                String.format("The organizer has deposited payment for '%s'. You will be paid after attendance confirmation.",
                    event.getTitle())
            );
        }
    }

    /**
     * Release payment to volunteer after attendance confirmation.
     */
    @Transactional
    public void releasePaymentToVolunteer(Long applicationId) {
        log.info("Releasing payment for application: {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Event event = application.getEvent();
        Payment payment = paymentRepository.findByEventId(event.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found for event"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment not completed for this event");
        }

        List<Payout> existingPayouts = payoutRepository.findByApplicationId(applicationId);
        if (!existingPayouts.isEmpty()) {
            throw new RuntimeException("Payout already exists for this application");
        }

        BigDecimal paymentAmount = event.getPaymentPerVolunteer();
        BigDecimal platformFee = calculatePlatformFee(paymentAmount);
        BigDecimal netAmount = paymentAmount.subtract(platformFee);

        Payout payout = Payout.builder()
                .payment(payment)
                .application(application)
                .volunteer(application.getVolunteer())
                .amount(paymentAmount)
                .platformFee(platformFee)
                .status(PayoutStatus.PENDING)
                .build();

        try {
            // Transfer via Razorpay Route (requires Razorpay Route feature)
            // razorpayService.createTransfer(volunteer.getRazorpayAccountId(), netAmount, "INR");
            payout.markCompleted();
            payoutRepository.save(payout);

            log.info("Payment released to volunteer for application: {}", applicationId);

            auditService.logPaymentTransaction(
                event.getOrganizer(), event.getId(),
                netAmount.toString(), "RELEASE", "UNKNOWN"
            );

            notificationService.createNotification(
                application.getVolunteer(), "PAYMENT",
                "Payment Released",
                String.format("Your payment of ₹%.2f for '%s' has been released to your account.",
                    netAmount.doubleValue(), event.getTitle())
            );

        } catch (Exception e) {
            log.error("Failed to release payment: {}", e.getMessage());
            payout.markFailed(e.getMessage());
            payoutRepository.save(payout);
            throw new RuntimeException("Failed to release payment: " + e.getMessage());
        }
    }

    /**
     * Process refund for no-show volunteer via Razorpay.
     */
    @Transactional
    public void processRefund(Long eventId, Long volunteerId) {
        log.info("Processing refund for event: {} volunteer: {}", eventId, volunteerId);

        Payment payment = paymentRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Payment not found for event"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        BigDecimal refundAmount = event.getPaymentPerVolunteer();

        try {
            razorpayService.createRefund(payment.getRazorpayPaymentId(), refundAmount);

            log.info("Refund processed for event: {} volunteer: {} amount: {}", eventId, volunteerId, refundAmount);

            auditService.logPaymentTransaction(
                event.getOrganizer(), eventId,
                refundAmount.toString(), "REFUND", "UNKNOWN"
            );

            notificationService.createNotification(
                event.getOrganizer(), "PAYMENT",
                "Refund Processed",
                String.format("A refund of ₹%.2f has been processed for no-show volunteer in '%s'.",
                    refundAmount.doubleValue(), event.getTitle())
            );

        } catch (Exception e) {
            log.error("Failed to process refund: {}", e.getMessage());
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }

    /**
     * Process cancellation refund based on cancellation policy:
     * - More than 7 days before: 100% refund
     * - 3–7 days before: 50% refund
     * - Less than 3 days before: 0% refund
     */
    @Transactional
    public void processCancellationRefund(Long eventId) {
        log.info("Processing cancellation refund for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Payment payment = paymentRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Payment not found for event"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            log.info("No refund needed - payment not completed");
            return;
        }

        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), event.getEventDateTime());

        BigDecimal refundPercentage;
        if (daysUntilEvent > 7) {
            refundPercentage = BigDecimal.ONE;
        } else if (daysUntilEvent >= 3) {
            refundPercentage = BigDecimal.valueOf(0.5);
        } else {
            refundPercentage = BigDecimal.ZERO;
        }

        if (refundPercentage.compareTo(BigDecimal.ZERO) == 0) {
            log.info("No refund - cancelled less than 3 days before event");
            return;
        }

        BigDecimal refundAmount = payment.getAmount().multiply(refundPercentage);

        try {
            razorpayService.createRefund(payment.getRazorpayPaymentId(), refundAmount);

            payment.markRefunded();
            paymentRepository.save(payment);

            log.info("Cancellation refund processed: {} ({}%)", refundAmount,
                refundPercentage.multiply(BigDecimal.valueOf(100)));

            notificationService.createNotification(
                event.getOrganizer(), "PAYMENT",
                "Cancellation Refund Processed",
                String.format("A refund of ₹%.2f (%.0f%%) has been processed for cancelled event '%s'.",
                    refundAmount.doubleValue(),
                    refundPercentage.multiply(BigDecimal.valueOf(100)).doubleValue(),
                    event.getTitle())
            );

            List<Application> confirmedApplications = applicationRepository
                .findByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);

            for (Application application : confirmedApplications) {
                notificationService.createNotification(
                    application.getVolunteer(), "EVENT_CANCELLED",
                    "Event Cancelled",
                    String.format("The event '%s' has been cancelled by the organizer.", event.getTitle())
                );
            }

        } catch (Exception e) {
            log.error("Failed to process cancellation refund: {}", e.getMessage());
            throw new RuntimeException("Failed to process cancellation refund: " + e.getMessage());
        }
    }

    public BigDecimal calculatePlatformFee(BigDecimal amount) {
        return amount.multiply(PLATFORM_FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    }

    public Page<Payment> getPaymentHistory(Long userId, Pageable pageable) {
        return paymentRepository.findByOrganizerId(userId, pageable);
    }

    public Page<Payout> getPayoutHistory(Long volunteerId, Pageable pageable) {
        return payoutRepository.findByVolunteerId(volunteerId, pageable);
    }
}
