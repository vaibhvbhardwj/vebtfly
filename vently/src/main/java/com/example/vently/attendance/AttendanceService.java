package com.example.vently.attendance;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.notification.NotificationService;
import com.example.vently.payment.PaymentService;
import com.example.vently.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceCodeRepository attendanceCodeRepository;
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final PaymentService paymentService;
    private final ExcelParser excelParser;
    private final NotificationService notificationService;

    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate unique attendance codes for all confirmed volunteers
     * Called when event status transitions to IN_PROGRESS
     * Requirements: 9.1, 9.2
     */
    @Transactional
    public void generateAttendanceCodes(Long eventId) {
        log.info("Generating attendance codes for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Verify event is in IN_PROGRESS status
        if (event.getStatus() != EventStatus.IN_PROGRESS) {
            throw new IllegalStateException("Attendance codes can only be generated for events in IN_PROGRESS status");
        }

        // Check if codes already exist for this event
        if (attendanceCodeRepository.countByEventId(eventId) > 0) {
            throw new IllegalStateException("Attendance codes already generated for this event");
        }

        // Get all confirmed volunteers
        List<Application> confirmedApplications = applicationRepository
                .findByEventIdAndStatus(eventId, ApplicationStatus.CONFIRMED);

        if (confirmedApplications.isEmpty()) {
            throw new IllegalStateException("No confirmed volunteers found for this event");
        }

        // Generate unique codes for each confirmed volunteer
        List<AttendanceCode> attendanceCodes = new ArrayList<>();
        for (Application application : confirmedApplications) {
            String uniqueCode = generateUniqueCode();
            
            AttendanceCode attendanceCode = AttendanceCode.builder()
                    .event(event)
                    .volunteer(application.getVolunteer())
                    .code(uniqueCode)
                    .build();
            
            attendanceCodes.add(attendanceCode);
        }

        // Save all attendance codes
        attendanceCodeRepository.saveAll(attendanceCodes);

        // Notify each volunteer with their attendance code
        for (AttendanceCode ac : attendanceCodes) {
            notificationService.createNotification(
                ac.getVolunteer(),
                "ATTENDANCE_CODE",
                "Your Attendance Code for " + event.getTitle(),
                "Your attendance code is: " + ac.getCode() + ". Show this to the organizer at the event."
            );
        }

        log.info("Generated {} attendance codes for event: {}", attendanceCodes.size(), eventId);
    }

    /**
     * Generate a unique attendance code
     * Ensures code doesn't already exist in database
     */
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = generateRandomCode();
            attempts++;
            
            if (attempts > maxAttempts) {
                throw new RuntimeException("Failed to generate unique attendance code after " + maxAttempts + " attempts");
            }
        } while (attendanceCodeRepository.existsByCode(code));

        return code;
    }

    /**
     * Generate a random alphanumeric code
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CODE_CHARACTERS.length());
            code.append(CODE_CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    /**
     * Mark attendance by code
     * Validates code, marks volunteer as PRESENT, triggers payment release
     * Requirements: 9.3, 9.4, 9.5, 9.6, 9.7
     */
    @Transactional
    public void markAttendanceByCode(Long eventId, String code, User organizer) {
        log.info("Marking attendance by code for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("Only the event organizer can mark attendance");
        }

        // Validate event timing
        validateAttendanceTimingWindow(event);

        // Find attendance code
        AttendanceCode attendanceCode = attendanceCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid attendance code"));

        // Verify code belongs to this event
        if (!attendanceCode.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Attendance code does not belong to this event");
        }

        // Check if already marked
        if (attendanceCode.isMarked()) {
            throw new IllegalStateException("Attendance already marked for this code");
        }

        // Mark attendance
        attendanceCode.markAttendance(organizer);
        attendanceCodeRepository.save(attendanceCode);

        // Find the application to trigger payment release
        Application application = applicationRepository
                .findByEventIdAndVolunteerId(eventId, attendanceCode.getVolunteer().getId())
                .orElseThrow(() -> new IllegalStateException("Application not found for volunteer"));

        // Trigger payment release to volunteer
        try {
            paymentService.releasePaymentToVolunteer(application.getId());
            log.info("Payment released for volunteer: {} at event: {}", 
                    attendanceCode.getVolunteer().getId(), eventId);
        } catch (Exception e) {
            log.error("Failed to release payment for volunteer: {}", attendanceCode.getVolunteer().getId(), e);
            // Don't rollback attendance marking if payment fails
            // Payment can be retried later
        }

        log.info("Attendance marked for code: {} at event: {}", code, eventId);
    }

    /**
     * Mark attendance as LATE by code
     */
    @Transactional
    public void markLateByCode(Long eventId, String code, User organizer) {
        log.info("Marking LATE attendance by code for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("Only the event organizer can mark attendance");
        }

        validateAttendanceTimingWindow(event);

        AttendanceCode attendanceCode = attendanceCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid attendance code"));

        if (!attendanceCode.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Attendance code does not belong to this event");
        }

        if (attendanceCode.isMarked()) {
            throw new IllegalStateException("Attendance already marked for this code");
        }

        attendanceCode.markLate(organizer);
        attendanceCodeRepository.save(attendanceCode);

        Application application = applicationRepository
                .findByEventIdAndVolunteerId(eventId, attendanceCode.getVolunteer().getId())
                .orElseThrow(() -> new IllegalStateException("Application not found for volunteer"));

        try {
            paymentService.releasePaymentToVolunteer(application.getId());
        } catch (Exception e) {
            log.error("Failed to release payment for late volunteer: {}", attendanceCode.getVolunteer().getId(), e);
        }

        log.info("Late attendance marked for code: {} at event: {}", code, eventId);
    }

    /**
     * Validate that attendance can be marked based on event timing
     * Requirements: 9.6, 9.7
     * 
     * Attendance can only be marked:
     * - Not before event start time
     * - Within 24 hours after event end time
     * 
     * Note: Assuming events last 8 hours by default
     */
    private void validateAttendanceTimingWindow(Event event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = event.getEventDateTime();
        
        // Assume event duration is 8 hours (can be made configurable)
        LocalDateTime eventEnd = eventStart.plusHours(8);

        // Cannot mark before event start
        if (now.isBefore(eventStart)) {
            throw new IllegalStateException("Attendance cannot be marked before event start time");
        }

        // Cannot mark more than 24 hours after event end
        LocalDateTime attendanceDeadline = eventEnd.plusHours(24);
        if (now.isAfter(attendanceDeadline)) {
            throw new IllegalStateException("Attendance marking window has closed (24 hours after event end)");
        }
    }

    /**
     * Mark attendance by Excel upload
     * Parses file, validates codes, marks multiple volunteers as PRESENT
     * Requirements: 10.3, 10.4
     */
    @Transactional
    public void markAttendanceByExcel(Long eventId, MultipartFile file, User organizer) {
        log.info("Marking attendance by Excel for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("Only the event organizer can mark attendance");
        }

        // Validate event timing
        validateAttendanceTimingWindow(event);

        // Parse Excel file
        List<String> codes = excelParser.parseAttendanceFile(file);

        // Mark attendance for each code
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            int rowNumber = i + 2; // Excel rows start at 1, header is row 1

            try {
                // Find attendance code
                AttendanceCode attendanceCode = attendanceCodeRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid code"));

                // Verify code belongs to this event
                if (!attendanceCode.getEvent().getId().equals(eventId)) {
                    throw new IllegalArgumentException("Code does not belong to this event");
                }

                // Skip if already marked
                if (attendanceCode.isMarked()) {
                    continue;
                }

                // Mark attendance
                attendanceCode.markAttendance(organizer);
                attendanceCodeRepository.save(attendanceCode);

                // Trigger payment release
                Application application = applicationRepository
                        .findByEventIdAndVolunteerId(eventId, attendanceCode.getVolunteer().getId())
                        .orElseThrow(() -> new IllegalStateException("Application not found"));

                try {
                    paymentService.releasePaymentToVolunteer(application.getId());
                } catch (Exception e) {
                    log.error("Failed to release payment for volunteer: {}", 
                            attendanceCode.getVolunteer().getId(), e);
                }

                successCount++;

            } catch (Exception e) {
                errors.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Errors in Excel file: " + String.join("; ", errors));
        }

        log.info("Marked attendance for {} volunteers at event: {}", successCount, eventId);
    }

    /**
     * Download attendance template with volunteer names and codes
     * Requirements: 10.2, 10.5, 10.6, 10.7
     */
    public byte[] downloadAttendanceTemplate(Long eventId, User organizer) {
        log.info("Generating attendance template for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("Only the event organizer can download attendance template");
        }

        // Get all attendance codes for this event
        List<AttendanceCode> attendanceCodes = attendanceCodeRepository.findByEventId(eventId);

        if (attendanceCodes.isEmpty()) {
            throw new IllegalStateException("No attendance codes found for this event. Generate codes first.");
        }

        // Generate Excel template
        return excelParser.generateAttendanceTemplate(attendanceCodes);
    }

    /**
     * Get attendance list for an event
     * Requirements: 24.6
     */
    public List<AttendanceCode> getEventAttendance(Long eventId, User organizer) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Verify organizer owns this event
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new IllegalStateException("Only the event organizer can view attendance");
        }

        return attendanceCodeRepository.findByEventId(eventId);
    }
}
