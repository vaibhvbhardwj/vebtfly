package com.example.vently.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final com.example.vently.subscription.SubscriptionService subscriptionService;
    private final com.example.vently.notification.NotificationService notificationService;

    /**
     * Submit a volunteer application to an event
     * Validates:
     * - Volunteer eligibility (role, account status)
     * - No duplicate applications
     * - Event capacity not exceeded
     * - Tier-based application limits
     * 
     * @param volunteerId ID of the volunteer applying
     * @param eventId ID of the event to apply to
     * @return Created application
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Application submitApplication(Long volunteerId, Long eventId) {
        // Validate volunteer exists and has VOLUNTEER role
        User volunteer = userRepository.findById(volunteerId)
            .orElseThrow(() -> new IllegalArgumentException("Volunteer not found with id: " + volunteerId));
        
        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new IllegalArgumentException("User must have VOLUNTEER role to apply to events");
        }

        // Validate event exists and is published
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Cannot apply to event with status: " + event.getStatus());
        }

        // Check if event date has passed
        if (event.getEventDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot apply to past events");
        }

        // Check for duplicate application
        if (applicationRepository.existsByEventIdAndVolunteerId(eventId, volunteerId)) {
            throw new IllegalArgumentException("You have already applied to this event");
        }

        // Check event capacity
        long confirmedCount = applicationRepository.countByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);
        if (confirmedCount >= event.getRequiredVolunteers()) {
            throw new IllegalArgumentException("Event has reached maximum capacity");
        }

        // Check tier-based application limits
        validateApplicationLimit(volunteerId);

        // Create and save application
        Application application = Application.builder()
            .event(event)
            .volunteer(volunteer)
            .status(ApplicationStatus.PENDING)
            .build();

        return applicationRepository.save(application);
    }

    /**
     * Withdraw a pending application
     * Only PENDING applications can be withdrawn
     * 
     * @param applicationId ID of the application to withdraw
     * @throws IllegalArgumentException if application not found or not in PENDING status
     */
    @Transactional
    public void withdrawApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException(
                "Only PENDING applications can be withdrawn. Current status: " + application.getStatus()
            );
        }

        applicationRepository.delete(application);
    }

    /**
     * Validate that volunteer hasn't exceeded their tier-based application limit
     * Free tier: max 5 active applications (PENDING, ACCEPTED, CONFIRMED)
     * Premium tier: unlimited applications
     * 
     * @param volunteerId ID of the volunteer
     * @throws IllegalArgumentException if limit exceeded
     */
    private void validateApplicationLimit(Long volunteerId) {
        // Check tier-based application limits using SubscriptionService
        if (!subscriptionService.canApplyToEvent(volunteerId)) {
            throw new IllegalArgumentException(
                "Application limit reached for your subscription tier. " +
                "Upgrade to Premium for unlimited applications."
            );
        }
    }

    /**
     * Get all applications for a specific event
     * 
     * @param eventId ID of the event
     * @return List of applications
     */
    @Transactional(readOnly = true)
    public List<Application> getEventApplications(Long eventId) {
        return applicationRepository.findByEventId(eventId);
    }

    /**
     * Get all applications for a specific volunteer (paginated)
     * 
     * @param volunteerId ID of the volunteer
     * @param pageable Pagination parameters
     * @return Page of applications
     */
    @Transactional(readOnly = true)
    public Page<Application> getVolunteerApplications(Long volunteerId, Pageable pageable) {
        return applicationRepository.findByVolunteerId(volunteerId, pageable);
    }

    /**
     * Get a specific application by ID
     * 
     * @param applicationId ID of the application
     * @return Application
     * @throws IllegalArgumentException if not found
     */
    @Transactional(readOnly = true)
    public Application getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));
    }

    /**
     * Check if a volunteer can apply to an event
     * Returns true if all validation checks pass
     * 
     * @param volunteerId ID of the volunteer
     * @param eventId ID of the event
     * @return true if volunteer can apply, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canApplyToEvent(Long volunteerId, Long eventId) {
        try {
            // Check volunteer exists and has correct role
            User volunteer = userRepository.findById(volunteerId).orElse(null);
            if (volunteer == null || volunteer.getRole() != Role.VOLUNTEER) {
                return false;
            }

            // Check event exists and is published
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null || event.getStatus() != EventStatus.PUBLISHED) {
                return false;
            }

            // Check event date
            if (event.getEventDateTime().isBefore(LocalDateTime.now())) {
                return false;
            }

            // Check for duplicate
            if (applicationRepository.existsByEventIdAndVolunteerId(eventId, volunteerId)) {
                return false;
            }

            // Check capacity
            long confirmedCount = applicationRepository.countByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);
            if (confirmedCount >= event.getRequiredVolunteers()) {
                return false;
            }

            // Check tier limit using SubscriptionService
            if (!subscriptionService.canApplyToEvent(volunteerId)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Accept an application (Organizer action)
     * Changes status to ACCEPTED and starts 48-hour confirmation timer
     * Only organizers can accept applications for their own events
     *
     * @param applicationId ID of the application to accept
     * @param organizerId ID of the organizer accepting the application
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public void acceptApplication(Long applicationId, Long organizerId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        // Verify organizer owns the event
        if (!application.getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new IllegalArgumentException("Only the event organizer can accept applications");
        }

        // Only PENDING applications can be accepted
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException(
                "Only PENDING applications can be accepted. Current status: " + application.getStatus()
            );
        }

        // Check if event has reached capacity
        long confirmedCount = applicationRepository.countByEventIdAndStatus(
            application.getEvent().getId(),
            ApplicationStatus.CONFIRMED
        );
        if (confirmedCount >= application.getEvent().getRequiredVolunteers()) {
            throw new IllegalArgumentException("Event has reached maximum capacity");
        }

        // Use helper method to set accepted status with 48-hour deadline
        application.setAccepted();
        applicationRepository.save(application);

        // Send notification to volunteer
        notificationService.createNotification(
            application.getVolunteer(),
            "APPLICATION_STATUS",
            "Application Accepted",
            String.format("Your application for '%s' has been accepted! Please confirm within 48 hours.", 
                application.getEvent().getTitle())
        );
    }

    /**
     * Reject an application (Organizer action)
     * Changes status to REJECTED
     * Only organizers can reject applications for their own events
     *
     * @param applicationId ID of the application to reject
     * @param organizerId ID of the organizer rejecting the application
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public void rejectApplication(Long applicationId, Long organizerId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        // Verify organizer owns the event
        if (!application.getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new IllegalArgumentException("Only the event organizer can reject applications");
        }

        // Only PENDING applications can be rejected
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException(
                "Only PENDING applications can be rejected. Current status: " + application.getStatus()
            );
        }

        // Set status to REJECTED
        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);

        // Send notification to volunteer
        notificationService.createNotification(
            application.getVolunteer(),
            "APPLICATION_STATUS",
            "Application Rejected",
            String.format("Your application for '%s' has been rejected.", 
                application.getEvent().getTitle())
        );
    }

    /**
     * Automatically reject remaining pending applications when event capacity is reached
     * This is called after an application is confirmed to check if capacity is full
     *
     * @param eventId ID of the event
     */
    @Transactional
    public void autoRejectExcessApplications(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Count confirmed volunteers
        long confirmedCount = applicationRepository.countByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);

        // If capacity is reached, reject all pending applications
        if (confirmedCount >= event.getRequiredVolunteers()) {
            List<Application> pendingApplications = applicationRepository.findPendingApplicationsByEventId(eventId);

            for (Application application : pendingApplications) {
                application.setStatus(ApplicationStatus.REJECTED);
                applicationRepository.save(application);

                // Send notification to volunteer
                notificationService.createNotification(
                    application.getVolunteer(),
                    "APPLICATION_STATUS",
                    "Application Rejected",
                    String.format("Your application for '%s' has been rejected as the event has reached capacity.", 
                        application.getEvent().getTitle())
                );
            }
        }
    }

    /**
     * Confirm an application (Volunteer action)
     * Changes status from ACCEPTED to CONFIRMED within 48-hour window
     * Only volunteers can confirm their own applications
     *
     * @param applicationId ID of the application to confirm
     * @param volunteerId ID of the volunteer confirming the application
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public void confirmApplication(Long applicationId, Long volunteerId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        // Verify volunteer owns the application
        if (!application.getVolunteer().getId().equals(volunteerId)) {
            throw new IllegalArgumentException("Only the applicant can confirm their application");
        }

        // Only ACCEPTED applications can be confirmed
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                "Only ACCEPTED applications can be confirmed. Current status: " + application.getStatus()
            );
        }

        // Check if confirmation deadline has passed
        if (application.isConfirmationExpired()) {
            throw new IllegalArgumentException(
                "Confirmation deadline has expired. The application will be automatically declined."
            );
        }

        // Use helper method to set confirmed status
        application.setConfirmed();
        applicationRepository.save(application);

        // Check if event capacity is reached and auto-reject excess applications
        autoRejectExcessApplications(application.getEvent().getId());

        // Send notification to organizer
        notificationService.createNotification(
            application.getEvent().getOrganizer(),
            "APPLICATION_STATUS",
            "Volunteer Confirmed",
            String.format("%s has confirmed their participation in '%s'.", 
                application.getVolunteer().getFullName(),
                application.getEvent().getTitle())
        );
    }

    /**
     * Decline an application (Volunteer action)
     * Changes status from ACCEPTED to DECLINED
     * Only volunteers can decline their own applications
     * This allows organizer to accept another pending application
     *
     * @param applicationId ID of the application to decline
     * @param volunteerId ID of the volunteer declining the application
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public void declineApplication(Long applicationId, Long volunteerId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        // Verify volunteer owns the application
        if (!application.getVolunteer().getId().equals(volunteerId)) {
            throw new IllegalArgumentException("Only the applicant can decline their application");
        }

        // Only ACCEPTED applications can be declined
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                "Only ACCEPTED applications can be declined. Current status: " + application.getStatus()
            );
        }

        // Use helper method to set declined status
        application.setDeclined();
        applicationRepository.save(application);

        // Send notification to organizer
        notificationService.createNotification(
            application.getEvent().getOrganizer(),
            "APPLICATION_STATUS",
            "Volunteer Declined",
            String.format("%s has declined their acceptance for '%s'.", 
                application.getVolunteer().getFullName(),
                application.getEvent().getTitle())
        );
    }

    /**
     * Automatically decline expired confirmations (Scheduled task)
     * Finds all ACCEPTED applications past their 48-hour confirmation deadline
     * Changes status to DECLINED and notifies organizers
     * This runs periodically (every hour) to process expired confirmations
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour at the top of the hour
    @Transactional
    public void autoDeclineExpiredApplications() {
        List<Application> expiredApplications = applicationRepository.findExpiredConfirmations(LocalDateTime.now());

        for (Application application : expiredApplications) {
            // Use helper method to set declined status
            application.setDeclined();
            applicationRepository.save(application);

            // Send notification to organizer about expired confirmation
            notificationService.createNotification(
                application.getEvent().getOrganizer(),
                "APPLICATION_STATUS",
                "Confirmation Expired",
                String.format("%s's confirmation for '%s' has expired.", 
                    application.getVolunteer().getFullName(),
                    application.getEvent().getTitle())
            );
        }
    }

}
