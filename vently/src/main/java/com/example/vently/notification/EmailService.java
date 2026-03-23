package com.example.vently.notification;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.vently.application.Application;
import com.example.vently.event.Event;
import com.example.vently.user.User;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private SesClient sesClient;

    @Value("${aws.ses.from-email}")
    private String fromEmail;

    @Value("${aws.ses.from-name}")
    private String fromName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Send welcome email on user registration
     */
    public void sendWelcomeEmail(User user) {
        if (!shouldSendEmail(user, true)) {
            return;
        }

        String subject = "Welcome to Vently!";
        String htmlBody = buildWelcomeEmailTemplate(user);

        sendEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Send verification email with verification link
     */
    public void sendVerificationEmail(User user, String verificationLink) {
        // Always send verification emails regardless of preferences
        String subject = "Verify Your Email - Vently";
        String htmlBody = buildVerificationEmailTemplate(user, verificationLink);

        sendEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Send application status change email
     */
    public void sendApplicationStatusEmail(User user, Application application, String status) {
        if (!shouldSendEmail(user, user.getNotifyOnApplicationStatus())) {
            return;
        }

        String subject = "Application Status Update - Vently";
        String htmlBody = buildApplicationStatusEmailTemplate(user, application, status);

        sendEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Send payment confirmation email
     */
    public void sendPaymentConfirmationEmail(User user, Event event, Double amount, String paymentType) {
        if (!shouldSendEmail(user, user.getNotifyOnPayment())) {
            return;
        }

        String subject = "Payment Confirmation - Vently";
        String htmlBody = buildPaymentConfirmationEmailTemplate(user, event, amount, paymentType);

        sendEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Send event cancellation email
     */
    public void sendEventCancellationEmail(User user, Event event, String reason) {
        if (!shouldSendEmail(user, user.getNotifyOnEventCancellation())) {
            return;
        }

        String subject = "Event Cancelled - Vently";
        String htmlBody = buildEventCancellationEmailTemplate(user, event, reason);

        sendEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Send dispute resolution email
     */
    public void sendDisputeResolutionEmail(User user, Long disputeId, String resolution) {
        if (!shouldSendEmail(user, user.getNotifyOnDisputeResolution())) {
            return;
        }

        String subject = "Dispute Resolved - Vently";
        String htmlBody = buildDisputeResolutionEmailTemplate(user, disputeId, resolution);

        sendEmail(user.getEmail(), subject, htmlBody);
    }

    /**
     * Check if email should be sent based on user preferences
     */
    private boolean shouldSendEmail(User user, Boolean specificPreference) {
        if (user.getEmailNotificationsEnabled() == null || !user.getEmailNotificationsEnabled()) {
            logger.info("Email notifications disabled for user: {}", user.getEmail());
            return false;
        }
        if (specificPreference == null || !specificPreference) {
            logger.info("Specific email notification disabled for user: {}", user.getEmail());
            return false;
        }
        return true;
    }

    /**
     * Core method to send email via AWS SES
     */
    private void sendEmail(String toEmail, String subject, String htmlBody) {
        // In development mode or when SES is not configured, just log the email
        if (sesClient == null || "dev".equals(activeProfile)) {
            logger.info("=== EMAIL (Development Mode) ===");
            logger.info("To: {}", toEmail);
            logger.info("Subject: {}", subject);
            logger.info("Body: {}", htmlBody);
            logger.info("================================");
            return;
        }

        try {
            Destination destination = Destination.builder()
                    .toAddresses(toEmail)
                    .build();

            Content subjectContent = Content.builder()
                    .data(subject)
                    .build();

            Content htmlContent = Content.builder()
                    .data(htmlBody)
                    .build();

            Body body = Body.builder()
                    .html(htmlContent)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(body)
                    .build();

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(fromName + " <" + fromEmail + ">")
                    .build();

            sesClient.sendEmail(emailRequest);
            logger.info("Email sent successfully to: {}", toEmail);

        } catch (SesException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // ==================== Email Template Builders ====================

    private String buildWelcomeEmailTemplate(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .button { background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Vently!</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Thank you for joining Vently, the platform connecting event organizers with volunteers!</p>
                        <p>Your account has been successfully created as a <strong>%s</strong>.</p>
                        <p>Get started by exploring events and connecting with the community.</p>
                        <p>If you have any questions, feel free to reach out to our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(user.getFullName(), user.getRole().name());
    }

    private String buildVerificationEmailTemplate(User user, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .button { background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Verify Your Email</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Please verify your email address to activate your Vently account.</p>
                        <p>Click the button below to verify your email:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Verify Email</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #4F46E5;">%s</p>
                        <p>This link will expire in 24 hours.</p>
                        <p>If you didn't create an account with Vently, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(user.getFullName(), verificationLink, verificationLink);
    }

    private String buildApplicationStatusEmailTemplate(User user, Application application, String status) {
        Event event = application.getEvent();
        String statusMessage = switch (status) {
            case "ACCEPTED" -> "Your application has been accepted! Please confirm your participation within 48 hours.";
            case "REJECTED" -> "Unfortunately, your application was not selected for this event.";
            case "CONFIRMED" -> "Your participation has been confirmed. We look forward to seeing you at the event!";
            default -> "Your application status has been updated to: " + status;
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .event-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4F46E5; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Application Status Update</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>%s</p>
                        <div class="event-details">
                            <h3>%s</h3>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                            <p><strong>Location:</strong> %s</p>
                            <p><strong>Payment:</strong> ₹%.2f</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFullName(),
                statusMessage,
                event.getTitle(),
                event.getDate().format(DATE_FORMATTER),
                event.getTime().format(TIME_FORMATTER),
                event.getLocation(),
                event.getPaymentPerVolunteer()
            );
    }

    private String buildPaymentConfirmationEmailTemplate(User user, Event event, Double amount, String paymentType) {
        String message = paymentType.equals("DEPOSIT") 
            ? "Your deposit has been received and held in escrow."
            : "Your payment has been processed successfully.";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #10B981; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .payment-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #10B981; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Payment Confirmation</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>%s</p>
                        <div class="payment-details">
                            <h3>Payment Details</h3>
                            <p><strong>Event:</strong> %s</p>
                            <p><strong>Amount:</strong> ₹%.2f</p>
                            <p><strong>Type:</strong> %s</p>
                        </div>
                        <p>You can view your transaction history in your account dashboard.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFullName(),
                message,
                event.getTitle(),
                amount,
                paymentType
            );
    }

    private String buildEventCancellationEmailTemplate(User user, Event event, String reason) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #EF4444; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .event-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #EF4444; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Event Cancelled</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>We regret to inform you that the following event has been cancelled:</p>
                        <div class="event-details">
                            <h3>%s</h3>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Location:</strong> %s</p>
                            <p><strong>Reason:</strong> %s</p>
                        </div>
                        <p>Any payments will be refunded according to our cancellation policy.</p>
                        <p>We apologize for any inconvenience this may cause.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFullName(),
                event.getTitle(),
                event.getDate().format(DATE_FORMATTER),
                event.getLocation(),
                reason != null ? reason : "Not specified"
            );
    }

    private String buildDisputeResolutionEmailTemplate(User user, Long disputeId, String resolution) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .resolution-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4F46E5; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Dispute Resolved</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Your dispute (ID: #%d) has been resolved by our admin team.</p>
                        <div class="resolution-details">
                            <h3>Resolution Details</h3>
                            <p>%s</p>
                        </div>
                        <p>If you have any questions about this resolution, please contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Vently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFullName(),
                disputeId,
                resolution
            );
    }
}
