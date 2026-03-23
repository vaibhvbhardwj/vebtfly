package com.example.vently.dispute;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.dispute.dto.DisputeStatistics;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.service.S3Service;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final com.example.vently.notification.NotificationService notificationService;
    
    @Autowired(required = false)
    private S3Service s3Service;
    
    public DisputeService(DisputeRepository disputeRepository, EventRepository eventRepository,
                         UserRepository userRepository, ApplicationRepository applicationRepository,
                         com.example.vently.notification.NotificationService notificationService) {
        this.disputeRepository = disputeRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Create a new dispute
     * Requirements: 16.1, 16.2, 16.3
     */
    @Transactional
    public Dispute createDispute(Long eventId, Long raisedById, Long againstUserId, String description) {
        log.info("Creating dispute for event {} raised by {} against {}", eventId, raisedById, againstUserId);

        // Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Validate raised by user exists
        User raisedBy = userRepository.findById(raisedById)
                .orElseThrow(() -> new IllegalArgumentException("Raised by user not found"));

        // Validate against user exists (if provided)
        User againstUser = null;
        if (againstUserId != null) {
            againstUser = userRepository.findById(againstUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Against user not found"));
        }

        // Check if user has already raised an open dispute for this event
        if (disputeRepository.hasOpenDisputeForEvent(eventId, raisedById)) {
            throw new IllegalStateException("You already have an open dispute for this event");
        }

        // Check if user participated in the event (either as volunteer or organizer)
        boolean userParticipated = false;
        if (raisedBy.getRole().name().equals("VOLUNTEER")) {
            // Check if volunteer was confirmed for this event
            // We'll use the application repository to check
            userParticipated = applicationRepository.existsByEventIdAndVolunteerIdAndStatus(
                    eventId, raisedById, ApplicationStatus.CONFIRMED);
        } else if (raisedBy.getRole().name().equals("ORGANIZER")) {
            // Check if organizer owns the event
            userParticipated = event.getOrganizer().getId().equals(raisedById);
        }

        if (!userParticipated) {
            throw new IllegalStateException("You must have participated in the event to raise a dispute");
        }

        // Create dispute
        Dispute dispute = Dispute.builder()
                .event(event)
                .raisedBy(raisedBy)
                .againstUser(againstUser)
                .description(description)
                .status(DisputeStatus.OPEN)
                .build();

        Dispute savedDispute = disputeRepository.save(dispute);
        log.info("Dispute created successfully: {}", savedDispute.getId());
        
        return savedDispute;
    }

    /**
     * Upload evidence files for a dispute
     * Requirements: 16.1, 16.2, 16.4, 26.7
     */
    @Transactional
    public Dispute uploadEvidence(Long disputeId, Long userId, MultipartFile[] files) {
        log.info("Uploading evidence for dispute {} by user {}", disputeId, userId);

        // Validate dispute exists and user has access
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        if (!dispute.getRaisedBy().getId().equals(userId)) {
            throw new IllegalStateException("You can only upload evidence to disputes you raised");
        }

        if (!dispute.isOpen()) {
            throw new IllegalStateException("Cannot upload evidence to closed or resolved disputes");
        }

        // Validate file types and sizes
        validateFiles(files);

        // Upload files to S3 and get URLs
        String[] evidenceUrls = uploadFilesToS3(files);

        // Update dispute with evidence URLs
        String[] existingUrls = dispute.getEvidenceUrls() != null ? dispute.getEvidenceUrls() : new String[0];
        String[] newUrls = combineArrays(existingUrls, evidenceUrls);
        
        dispute.setEvidenceUrls(newUrls);
        dispute.markUnderReview(); // Move to UNDER_REVIEW when evidence is uploaded

        Dispute updatedDispute = disputeRepository.save(dispute);
        log.info("Evidence uploaded successfully for dispute: {}", disputeId);
        
        return updatedDispute;
    }

    /**
     * Resolve a dispute (admin only)
     * Requirements: 16.1, 16.3, 17.1, 17.2, 17.3, 17.5, 17.6, 17.7
     */
    @Transactional
    public Dispute resolveDispute(Long disputeId, Long adminId, String resolution, 
                                  Double paymentAdjustment, Integer noShowAdjustment) {
        log.info("Resolving dispute {} by admin {}", disputeId, adminId);

        // Validate dispute exists
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        // Validate admin user exists and is admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        if (!admin.getRole().name().equals("ADMIN")) {
            throw new IllegalStateException("Only admins can resolve disputes");
        }

        if (!dispute.isOpen()) {
            throw new IllegalStateException("Cannot resolve a closed or already resolved dispute");
        }

        // Apply resolution
        dispute.resolve(admin, resolution);

        // Apply payment adjustment if specified
        if (paymentAdjustment != null) {
            applyPaymentAdjustment(dispute, paymentAdjustment);
        }

        // Apply no-show adjustment if specified
        if (noShowAdjustment != null) {
            applyNoShowAdjustment(dispute, noShowAdjustment);
        }

        Dispute resolvedDispute = disputeRepository.save(dispute);
        log.info("Dispute {} resolved successfully by admin {}", disputeId, adminId);
        
        // Send notifications to both parties
        notificationService.createNotification(
            dispute.getRaisedBy(),
            "DISPUTE_RESOLVED",
            "Dispute Resolved",
            String.format("Your dispute for event '%s' has been resolved.", 
                dispute.getEvent().getTitle())
        );
        
        if (dispute.getAgainstUser() != null) {
            notificationService.createNotification(
                dispute.getAgainstUser(),
                "DISPUTE_RESOLVED",
                "Dispute Resolved",
                String.format("A dispute against you for event '%s' has been resolved.", 
                    dispute.getEvent().getTitle())
            );
        }
        
        // TODO: Log audit trail
        
        return resolvedDispute;
    }

    /**
     * Close a dispute without resolution (admin only)
     * Requirements: 16.1, 16.3
     */
    @Transactional
    public Dispute closeDispute(Long disputeId, Long adminId) {
        log.info("Closing dispute {} by admin {}", disputeId, adminId);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        if (!admin.getRole().name().equals("ADMIN")) {
            throw new IllegalStateException("Only admins can close disputes");
        }

        dispute.close(admin);
        
        Dispute closedDispute = disputeRepository.save(dispute);
        log.info("Dispute {} closed by admin {}", disputeId, adminId);
        
        return closedDispute;
    }

    /**
     * Get all open disputes for admin dashboard
     * Requirements: 16.1, 17.1
     */
    public Page<Dispute> getOpenDisputes(Pageable pageable) {
        return disputeRepository.findOpenDisputes(pageable);
    }

    /**
     * Get dispute details by ID
     * Requirements: 16.1
     */
    public Dispute getDisputeDetails(Long disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
    }

    /**
     * Get disputes for a specific user
     * Requirements: 16.1
     */
    public List<Dispute> getUserDisputes(Long userId) {
        return disputeRepository.findByUserId(userId);
    }

    /**
     * Get disputes for a specific event
     * Requirements: 16.1
     */
    public List<Dispute> getEventDisputes(Long eventId) {
        return disputeRepository.findByEventId(eventId);
    }

    /**
     * Validate file types and sizes
     * Requirements: 16.4, 26.7
     */
    private void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files provided");
        }

        if (files.length > 10) {
            throw new IllegalArgumentException("Maximum 10 files allowed");
        }

        long totalSize = 0;
        for (MultipartFile file : files) {
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.startsWith("image/") && 
                 !contentType.equals("application/pdf") &&
                 !contentType.equals("application/msword") &&
                 !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                throw new IllegalArgumentException("Invalid file type: " + contentType + 
                        ". Only images, PDF, and Word documents are allowed");
            }

            // Check file size (max 10MB per file)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File too large: " + file.getOriginalFilename() + 
                        ". Maximum size is 10MB");
            }

            totalSize += file.getSize();
        }

        // Check total size (max 50MB)
        if (totalSize > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Total file size exceeds 50MB limit");
        }
    }

    /**
     * Upload files to S3 and return URLs
     * Requirements: 16.4, 26.7
     */
    private String[] uploadFilesToS3(MultipartFile[] files) {
        String[] urls = new String[files.length];
        
        // If S3 is not configured, return empty URLs
        if (s3Service == null) {
            log.warn("S3 service not configured, skipping file upload");
            for (int i = 0; i < files.length; i++) {
                urls[i] = "";
            }
            return urls;
        }
        
        for (int i = 0; i < files.length; i++) {
            try {
                // Generate unique filename
                String filename = "dispute-evidence/" + LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) + 
                                 "_" + i + "_" + files[i].getOriginalFilename();
                
                // Upload to S3
                String url = s3Service.uploadFile(files[i], "dispute-evidence");
                urls[i] = url;
                
                log.debug("File uploaded to S3: {}", url);
            } catch (Exception e) {
                log.error("Failed to upload file to S3: {}", e.getMessage());
                throw new RuntimeException("Failed to upload file: " + files[i].getOriginalFilename(), e);
            }
        }
        
        return urls;
    }

    /**
     * Apply payment adjustment to the dispute parties
     * Requirements: 17.2, 17.3
     */
    private void applyPaymentAdjustment(Dispute dispute, Double adjustmentAmount) {
        // TODO: Implement payment adjustment logic
        // This would involve creating a payment/refund transaction
        // and updating user balances
        log.info("Applying payment adjustment of {} for dispute {}", adjustmentAmount, dispute.getId());
    }

    /**
     * Apply no-show adjustment to user's record
     * Requirements: 17.2, 17.3
     */
    private void applyNoShowAdjustment(Dispute dispute, Integer adjustmentCount) {
        // TODO: Implement no-show adjustment logic
        // This would involve updating the against user's noShowCount
        log.info("Applying no-show adjustment of {} for dispute {}", adjustmentCount, dispute.getId());
    }

    /**
     * Combine two string arrays
     */
    private String[] combineArrays(String[] array1, String[] array2) {
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * Get dispute statistics for analytics
     * Requirements: 17.5
     */
    public DisputeStatistics getDisputeStatistics() {
        Long totalDisputes = disputeRepository.count();
        Long openDisputes = disputeRepository.countOpenDisputes();
        Long resolvedDisputes = disputeRepository.countByStatus(DisputeStatus.RESOLVED);
        Long closedDisputes = disputeRepository.countByStatus(DisputeStatus.CLOSED);
        
        // Calculate average resolution time
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<Dispute> disputes = disputeRepository.getDisputesForResolutionTimeCalculation(startDate, endDate);
        
        Double avgResolutionTime = 0.0;
        if (!disputes.isEmpty()) {
            double totalHours = 0;
            for (Dispute dispute : disputes) {
                if (dispute.getResolvedAt() != null && dispute.getCreatedAt() != null) {
                    long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(dispute.getCreatedAt(), dispute.getResolvedAt());
                    totalHours += durationMinutes / 60.0;
                }
            }
            // Convert hours to seconds
            avgResolutionTime = (totalHours / disputes.size()) * 3600;
        }
        
        return DisputeStatistics.builder()
                .totalDisputes(totalDisputes)
                .openDisputes(openDisputes)
                .resolvedDisputes(resolvedDisputes)
                .closedDisputes(closedDisputes)
                .averageResolutionTimeSeconds(avgResolutionTime)
                .build();
    }
}