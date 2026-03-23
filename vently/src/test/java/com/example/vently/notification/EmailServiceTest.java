package com.example.vently.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.Event;
import com.example.vently.event.EventStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;

import jakarta.mail.internet.MimeMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private Event testEvent;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.VOLUNTEER)
                .emailNotificationsEnabled(true)
                .notifyOnApplicationStatus(true)
                .notifyOnEventCancellation(true)
                .notifyOnPayment(true)
                .notifyOnDisputeResolution(true)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .date(LocalDate.of(2024, 12, 25))
                .time(LocalTime.of(10, 0))
                .location("Test Location")
                .paymentPerVolunteer(new java.math.BigDecimal("500.0"))
                .status(EventStatus.PUBLISHED)
                .build();

        testApplication = Application.builder()
                .id(1L)
                .event(testEvent)
                .volunteer(testUser)
                .status(ApplicationStatus.PENDING)
                .build();

        ReflectionTestUtils.setField(emailService, "fromEmail", "vaibhvbhardwj@gmail.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Ventfly");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendWelcomeEmail_Success() {
        emailService.sendWelcomeEmail(testUser);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWelcomeEmail_WhenEmailNotificationsDisabled_DoesNotSend() {
        testUser.setEmailNotificationsEnabled(false);
        emailService.sendWelcomeEmail(testUser);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendApplicationStatusEmail_Success() {
        emailService.sendApplicationStatusEmail(testUser, testApplication, "ACCEPTED");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendApplicationStatusEmail_WhenPreferenceDisabled_DoesNotSend() {
        testUser.setNotifyOnApplicationStatus(false);
        emailService.sendApplicationStatusEmail(testUser, testApplication, "ACCEPTED");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendPaymentConfirmationEmail_Success() {
        emailService.sendPaymentConfirmationEmail(testUser, testEvent, 500.0, "PAYOUT");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendPaymentConfirmationEmail_WhenPreferenceDisabled_DoesNotSend() {
        testUser.setNotifyOnPayment(false);
        emailService.sendPaymentConfirmationEmail(testUser, testEvent, 500.0, "PAYOUT");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEventCancellationEmail_Success() {
        emailService.sendEventCancellationEmail(testUser, testEvent, "Weather conditions");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEventCancellationEmail_WhenPreferenceDisabled_DoesNotSend() {
        testUser.setNotifyOnEventCancellation(false);
        emailService.sendEventCancellationEmail(testUser, testEvent, "Weather conditions");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendDisputeResolutionEmail_Success() {
        emailService.sendDisputeResolutionEmail(testUser, 1L, "Resolved in your favor");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendDisputeResolutionEmail_WhenPreferenceDisabled_DoesNotSend() {
        testUser.setNotifyOnDisputeResolution(false);
        emailService.sendDisputeResolutionEmail(testUser, 1L, "Resolved in your favor");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testGlobalDisable_OverridesSpecificPreferences() {
        testUser.setEmailNotificationsEnabled(false);
        testUser.setNotifyOnApplicationStatus(true);
        emailService.sendApplicationStatusEmail(testUser, testApplication, "ACCEPTED");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
