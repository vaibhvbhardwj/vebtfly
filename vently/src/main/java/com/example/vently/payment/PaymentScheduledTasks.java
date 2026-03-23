package com.example.vently.payment;

import java.time.LocalDate;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduledTasks {

    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Check for events past deposit deadline without payment
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void checkDepositDeadlines() {
        log.info("Checking deposit deadlines...");

        // Find all PUBLISHED events where application deadline has passed
        List<Event> publishedEvents = eventRepository.findByStatus(EventStatus.PUBLISHED);

        LocalDate now = LocalDate.now();

        for (Event event : publishedEvents) {
            // Check if application deadline has passed
            if (event.getApplicationDeadline() != null && 
                event.getApplicationDeadline().isBefore(now)) {

                // Check if payment exists
                boolean paymentExists = paymentRepository.existsByEventId(event.getId());

                if (!paymentExists) {
                    log.warn("Event {} past deposit deadline without payment - cancelling", event.getId());
                    cancelEventDueToNoDeposit(event);
                }
            }
        }

        log.info("Deposit deadline check completed");
    }

    /**
     * Cancel event due to missing deposit
     * Notify all confirmed volunteers
     */
    private void cancelEventDueToNoDeposit(Event event) {
        // Update event status to CANCELLED
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        // Cancel all confirmed applications
        List<Application> confirmedApplications = applicationRepository
                .findByEventIdAndStatus(event.getId(), ApplicationStatus.CONFIRMED);

        for (Application application : confirmedApplications) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationRepository.save(application);

            // TODO: Send notification to volunteer about event cancellation
            log.info("Cancelled application {} due to no deposit", application.getId());
        }

        // TODO: Send notification to organizer about event cancellation
        log.info("Event {} cancelled due to missing deposit", event.getId());
    }
}
