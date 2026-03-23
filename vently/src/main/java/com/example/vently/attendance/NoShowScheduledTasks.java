package com.example.vently.attendance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.payment.PaymentService;
import com.example.vently.user.AccountStatus;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoShowScheduledTasks {

    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final AttendanceCodeRepository attendanceCodeRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    /**
     * Process no-shows for events that ended 24 hours ago
     * Runs every hour to check for events that need no-show processing
     * Requirements: 11.1, 11.2, 11.6, 13.1
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour at the top of the hour
    @Transactional
    public void processNoShows() {
        log.info("Starting no-show processing task");

        // Find events that ended 24 hours ago (assuming 8-hour event duration)
        // Event ends at eventDateTime + 8 hours, then wait 24 hours for no-show processing
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(32); // 8 hours event + 24 hours wait
        LocalDateTime startTime = LocalDateTime.now().minusHours(33); // Process events in 1-hour window

        List<Event> eventsToProcess = eventRepository.findEventsForNoShowProcessing(
                cutoffTime, startTime, EventStatus.DEPOSIT_PAID);

        log.info("Found {} events to process for no-shows", eventsToProcess.size());

        for (Event event : eventsToProcess) {
            try {
                processEventNoShows(event);
            } catch (Exception e) {
                log.error("Failed to process no-shows for event {}: {}", event.getId(), e.getMessage(), e);
            }
        }

        log.info("Completed no-show processing task");
    }

    /**
     * Process no-shows for a specific event
     * 
     * @param event Event to process
     */
    private void processEventNoShows(Event event) {
        log.info("Processing no-shows for event: {} - {}", event.getId(), event.getTitle());

        // Get all confirmed applications for this event
        List<Application> confirmedApplications = applicationRepository
                .findByEventIdAndStatus(event.getId(), ApplicationStatus.CONFIRMED);

        int noShowCount = 0;

        for (Application application : confirmedApplications) {
            // Check if attendance was marked for this volunteer
            AttendanceCode attendanceCode = attendanceCodeRepository
                    .findByEventIdAndVolunteerId(event.getId(), application.getVolunteer().getId())
                    .orElse(null);

            if (attendanceCode == null || attendanceCode.isUnmarked()) {
                // This is a no-show
                processNoShow(event, application);
                noShowCount++;
            }
        }

        log.info("Processed {} no-shows for event: {}", noShowCount, event.getId());
    }

    /**
     * Process a single no-show
     * - Increment user's no-show count
     * - Apply penalties (suspension/ban)
     * - Process refund to organizer
     * - Send notification to volunteer
     * 
     * @param event       Event
     * @param application Application
     */
    private void processNoShow(Event event, Application application) {
        User volunteer = application.getVolunteer();
        log.info("Processing no-show for volunteer: {} on event: {}", volunteer.getId(), event.getId());

        // Increment no-show count
        volunteer.setNoShowCount(volunteer.getNoShowCount() + 1);
        int currentNoShowCount = volunteer.getNoShowCount();

        // Apply penalties based on no-show count
        if (currentNoShowCount >= 5) {
            // Permanent ban
            volunteer.setAccountStatus(AccountStatus.BANNED);
            volunteer.setSuspendedUntil(null); // Permanent ban doesn't need expiry
            log.warn("User {} permanently banned after {} no-shows", volunteer.getId(), currentNoShowCount);
        } else if (currentNoShowCount >= 3) {
            // 30-day suspension
            volunteer.setAccountStatus(AccountStatus.SUSPENDED);
            volunteer.setSuspendedUntil(LocalDateTime.now().plusDays(30));
            log.warn("User {} suspended for 30 days after {} no-shows", volunteer.getId(), currentNoShowCount);
        }

        userRepository.save(volunteer);

        // Process refund to organizer
        try {
            paymentService.processRefund(event.getId(), volunteer.getId());
            log.info("Refund processed for no-show: event {} volunteer {}", event.getId(), volunteer.getId());
        } catch (Exception e) {
            log.error("Failed to process refund for no-show: {}", e.getMessage(), e);
        }

        // TODO: Send notification to volunteer about no-show penalty
        log.info("No-show processed for volunteer: {} - new count: {}", volunteer.getId(), currentNoShowCount);
    }
}
