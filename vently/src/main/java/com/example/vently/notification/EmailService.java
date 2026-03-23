package com.example.vently.notification;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.vently.application.Application;
import com.example.vently.event.Event;
import com.example.vently.user.User;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${mail.from.name:Ventfly}")
    private String fromName;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /** Send a raw HTML email — used for OTP and other transactional emails */
    public void sendRawEmail(String toEmail, String subject, String htmlBody) {
        sendEmail(toEmail, subject, htmlBody);
    }

    public void sendWelcomeEmail(User user) {
        if (!shouldSendEmail(user, true)) return;
        sendEmail(user.getEmail(), "Welcome to Ventfly!", buildWelcomeEmailTemplate(user));
    }

    public void sendApplicationStatusEmail(User user, Application application, String status) {
        if (!shouldSendEmail(user, user.getNotifyOnApplicationStatus())) return;
        sendEmail(user.getEmail(), "Application Status Update - Ventfly", buildApplicationStatusEmailTemplate(user, application, status));
    }

    public void sendPaymentConfirmationEmail(User user, Event event, Double amount, String paymentType) {
        if (!shouldSendEmail(user, user.getNotifyOnPayment())) return;
        sendEmail(user.getEmail(), "Payment Confirmation - Ventfly", buildPaymentConfirmationEmailTemplate(user, event, amount, paymentType));
    }

    public void sendEventCancellationEmail(User user, Event event, String reason) {
        if (!shouldSendEmail(user, user.getNotifyOnEventCancellation())) return;
        sendEmail(user.getEmail(), "Event Cancelled - Ventfly", buildEventCancellationEmailTemplate(user, event, reason));
    }

    public void sendDisputeResolutionEmail(User user, Long disputeId, String resolution) {
        if (!shouldSendEmail(user, user.getNotifyOnDisputeResolution())) return;
        sendEmail(user.getEmail(), "Dispute Resolved - Ventfly", buildDisputeResolutionEmailTemplate(user, disputeId, resolution));
    }

    private boolean shouldSendEmail(User user, Boolean specificPreference) {
        if (user.getEmailNotificationsEnabled() == null || !user.getEmailNotificationsEnabled()) return false;
        if (specificPreference == null || !specificPreference) return false;
        return true;
    }

    private void sendEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.info("Email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ==================== Templates ====================

    private String buildWelcomeEmailTemplate(User user) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <h2 style="color:#807aeb">Welcome to Ventfly!</h2>
              <p>Hi %s,</p>
              <p>Thanks for joining Ventfly — the platform connecting event organizers with volunteers.</p>
              <p>Your account is ready. Start exploring events today!</p>
              <p style="color:#6B7280;font-size:13px">— The Ventfly Team</p>
            </div>
            """.formatted(user.getFullName());
    }

    private String buildApplicationStatusEmailTemplate(User user, Application application, String status) {
        Event event = application.getEvent();
        String statusMessage = switch (status) {
            case "ACCEPTED" -> "Your application has been accepted! Please confirm your participation within 48 hours.";
            case "REJECTED" -> "Unfortunately, your application was not selected for this event.";
            case "CONFIRMED" -> "Your participation has been confirmed. See you at the event!";
            default -> "Your application status has been updated to: " + status;
        };
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <h2 style="color:#807aeb">Application Update</h2>
              <p>Hi %s,</p>
              <p>%s</p>
              <div style="background:#ebf2fa;padding:16px;border-radius:12px;margin:16px 0">
                <strong>%s</strong><br/>
                Date: %s &nbsp;|&nbsp; Time: %s<br/>
                Location: %s<br/>
                Payment: ₹%.2f
              </div>
              <p style="color:#6B7280;font-size:13px">— The Ventfly Team</p>
            </div>
            """.formatted(user.getFullName(), statusMessage, event.getTitle(),
                event.getDate().format(DATE_FORMATTER), event.getTime().format(TIME_FORMATTER),
                event.getLocation(), event.getPaymentPerVolunteer());
    }

    private String buildPaymentConfirmationEmailTemplate(User user, Event event, Double amount, String paymentType) {
        String msg = paymentType.equals("DEPOSIT") ? "Your deposit has been received and held in escrow." : "Your payment has been processed successfully.";
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <h2 style="color:#10B981">Payment Confirmation</h2>
              <p>Hi %s,</p>
              <p>%s</p>
              <div style="background:#ebf2fa;padding:16px;border-radius:12px;margin:16px 0">
                Event: <strong>%s</strong><br/>
                Amount: <strong>₹%.2f</strong><br/>
                Type: %s
              </div>
              <p style="color:#6B7280;font-size:13px">— The Ventfly Team</p>
            </div>
            """.formatted(user.getFullName(), msg, event.getTitle(), amount, paymentType);
    }

    private String buildEventCancellationEmailTemplate(User user, Event event, String reason) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <h2 style="color:#EF4444">Event Cancelled</h2>
              <p>Hi %s,</p>
              <p>The following event has been cancelled:</p>
              <div style="background:#ebf2fa;padding:16px;border-radius:12px;margin:16px 0">
                <strong>%s</strong><br/>
                Date: %s &nbsp;|&nbsp; Location: %s<br/>
                Reason: %s
              </div>
              <p>Any payments will be refunded per our cancellation policy.</p>
              <p style="color:#6B7280;font-size:13px">— The Ventfly Team</p>
            </div>
            """.formatted(user.getFullName(), event.getTitle(), event.getDate().format(DATE_FORMATTER),
                event.getLocation(), reason != null ? reason : "Not specified");
    }

    private String buildDisputeResolutionEmailTemplate(User user, Long disputeId, String resolution) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <h2 style="color:#807aeb">Dispute Resolved</h2>
              <p>Hi %s,</p>
              <p>Your dispute (ID: #%d) has been resolved.</p>
              <div style="background:#ebf2fa;padding:16px;border-radius:12px;margin:16px 0">%s</div>
              <p style="color:#6B7280;font-size:13px">— The Ventfly Team</p>
            </div>
            """.formatted(user.getFullName(), disputeId, resolution);
    }
}
