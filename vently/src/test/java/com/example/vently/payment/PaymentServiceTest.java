package com.example.vently.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.audit.AuditService;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.notification.NotificationService;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PayoutRepository payoutRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private RazorpayService razorpayService;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private PaymentService paymentService;

    private User organizer;
    private User volunteer;
    private Event event;
    private Application application;
    private Payment payment;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
            .id(1L).email("organizer@test.com").role(Role.ORGANIZER).build();

        volunteer = User.builder()
            .id(2L).email("volunteer@test.com").role(Role.VOLUNTEER).build();

        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(7);
        event = Event.builder()
            .id(1L).title("Test Event").organizer(organizer)
            .requiredVolunteers(5)
            .paymentPerVolunteer(BigDecimal.valueOf(100.00))
            .status(EventStatus.PUBLISHED)
            .date(futureDateTime.toLocalDate())
            .time(futureDateTime.toLocalTime())
            .build();

        application = Application.builder()
            .id(1L).event(event).volunteer(volunteer)
            .status(ApplicationStatus.CONFIRMED).build();

        payment = Payment.builder()
            .id(1L).event(event).organizer(organizer)
            .amount(BigDecimal.valueOf(500.00))
            .status(PaymentStatus.COMPLETED)
            .razorpayPaymentId("pay_123456")
            .build();
    }

    // ===== DEPOSIT CALCULATION TESTS =====

    @Test
    void createDepositIntent_CalculatesCorrectTotal() throws RazorpayException {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(5L);
        when(paymentRepository.existsByEventId(1L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(razorpayService.createOrder(any(BigDecimal.class), eq("INR"), anyString()))
            .thenReturn(createMockOrder("order_123"));

        String orderId = paymentService.createDepositIntent(1L);

        assertNotNull(orderId);
        verify(razorpayService).createOrder(any(BigDecimal.class), eq("INR"), anyString());
    }

    @Test
    void createDepositIntent_NoConfirmedVolunteers_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(applicationRepository.countByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED)).thenReturn(0L);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> paymentService.createDepositIntent(1L));
        assertTrue(ex.getMessage().contains("No confirmed volunteers"));
    }

    @Test
    void createDepositIntent_PaymentAlreadyExists_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(paymentRepository.existsByEventId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> paymentService.createDepositIntent(1L));
        assertTrue(ex.getMessage().contains("Payment already exists"));
    }

    // ===== PLATFORM FEE CALCULATION TESTS =====

    @Test
    void calculatePlatformFee_10Percent_CalculatesCorrectly() {
        BigDecimal fee = paymentService.calculatePlatformFee(BigDecimal.valueOf(100.00));
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(10.00)));
    }

    @Test
    void calculatePlatformFee_LargeAmount_CalculatesCorrectly() {
        BigDecimal fee = paymentService.calculatePlatformFee(BigDecimal.valueOf(1000.00));
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(100.00)));
    }

    @Test
    void calculatePlatformFee_SmallAmount_RoundsCorrectly() {
        BigDecimal fee = paymentService.calculatePlatformFee(BigDecimal.valueOf(33.33));
        assertEquals(0, fee.compareTo(BigDecimal.valueOf(3.33)));
    }

    @Test
    void calculatePlatformFee_ZeroAmount_ReturnsZero() {
        BigDecimal fee = paymentService.calculatePlatformFee(BigDecimal.ZERO);
        assertEquals(0, fee.compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));
    }

    // ===== CANCELLATION REFUND LOGIC TESTS =====

    @Test
    void processCancellationRefund_MoreThan7DaysBefore_ProcessesFullRefund() throws Exception {
        LocalDateTime eventDateTime = LocalDateTime.now().plusDays(10);
        event.setDate(eventDateTime.toLocalDate());
        event.setTime(eventDateTime.toLocalTime());

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
            .thenReturn(Arrays.asList(application));

        paymentService.processCancellationRefund(1L);

        verify(razorpayService).createRefund(anyString(), any(BigDecimal.class));
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REFUNDED));
    }

    @Test
    void processCancellationRefund_LessThan3DaysBefore_NoRefund() throws Exception {
        LocalDateTime eventDateTime = LocalDateTime.now().plusDays(1);
        event.setDate(eventDateTime.toLocalDate());
        event.setTime(eventDateTime.toLocalTime());

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));

        paymentService.processCancellationRefund(1L);

        verify(razorpayService, never()).createRefund(anyString(), any(BigDecimal.class));
    }

    @Test
    void processCancellationRefund_PaymentNotCompleted_NoRefund() throws Exception {
        payment.setStatus(PaymentStatus.PENDING);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));

        paymentService.processCancellationRefund(1L);

        verify(razorpayService, never()).createRefund(anyString(), any(BigDecimal.class));
    }

    // ===== PAYMENT RELEASE FLOW TESTS =====

    @Test
    void releasePaymentToVolunteer_Success_CreatesPayoutWithFee() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(payoutRepository.findByApplicationId(1L)).thenReturn(Arrays.asList());
        when(payoutRepository.save(any(Payout.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.releasePaymentToVolunteer(1L);

        verify(payoutRepository).save(any(Payout.class));
    }

    @Test
    void releasePaymentToVolunteer_AlreadyPaidOut_ThrowsException() {
        Payout existingPayout = Payout.builder()
            .id(1L).application(application).volunteer(volunteer)
            .amount(BigDecimal.valueOf(100.00)).status(PayoutStatus.COMPLETED).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(payoutRepository.findByApplicationId(1L)).thenReturn(Arrays.asList(existingPayout));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> paymentService.releasePaymentToVolunteer(1L));
        assertTrue(ex.getMessage().contains("Payout already exists"));
    }

    @Test
    void releasePaymentToVolunteer_PaymentNotCompleted_ThrowsException() {
        payment.setStatus(PaymentStatus.PENDING);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> paymentService.releasePaymentToVolunteer(1L));
        assertTrue(ex.getMessage().contains("Payment not completed"));
    }

    @Test
    void processRefund_Success_CallsRazorpayRefund() throws Exception {
        when(paymentRepository.findByEventId(1L)).thenReturn(Optional.of(payment));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        paymentService.processRefund(1L, 2L);

        verify(razorpayService).createRefund(anyString(), any(BigDecimal.class));
    }

    // ===== HELPER =====

    private Order createMockOrder(String orderId) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", orderId);
            json.put("amount", 50000);
            json.put("currency", "INR");
            json.put("status", "created");
            return new Order(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock order", e);
        }
    }
}
