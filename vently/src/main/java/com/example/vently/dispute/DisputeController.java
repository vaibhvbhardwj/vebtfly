package com.example.vently.dispute;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.dispute.dto.DisputeRequestDto;
import com.example.vently.dispute.dto.DisputeResponseDto;
import com.example.vently.dispute.dto.DisputeStatistics;
import com.example.vently.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<DisputeResponseDto> createDispute(
            @Valid @RequestBody DisputeRequestDto disputeRequest,
            @AuthenticationPrincipal User raisedBy) {
        try {
            Dispute dispute = disputeService.createDispute(
                    disputeRequest.getEventId(),
                    raisedBy.getId(),
                    disputeRequest.getAgainstUserId(),
                    disputeRequest.getDescription());
            
            DisputeResponseDto response = convertToResponseDto(dispute);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{disputeId}/evidence")
    public ResponseEntity<DisputeResponseDto> uploadEvidence(
            @PathVariable Long disputeId,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal User user) {
        try {
            Dispute dispute = disputeService.uploadEvidence(disputeId, user.getId(), files);
            DisputeResponseDto response = convertToResponseDto(dispute);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-disputes")
    public ResponseEntity<List<DisputeResponseDto>> getMyDisputes(@AuthenticationPrincipal User user) {
        List<Dispute> disputes = disputeService.getUserDisputes(user.getId());
        List<DisputeResponseDto> response = disputes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{disputeId}")
    public ResponseEntity<DisputeResponseDto> getDisputeDetails(
            @PathVariable Long disputeId,
            @AuthenticationPrincipal User user) {
        try {
            Dispute dispute = disputeService.getDisputeDetails(disputeId);
            
            // Check if user has access to this dispute
            if (!dispute.getRaisedBy().getId().equals(user.getId()) && 
                (dispute.getAgainstUser() == null || !dispute.getAgainstUser().getId().equals(user.getId())) &&
                !user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            DisputeResponseDto response = convertToResponseDto(dispute);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<DisputeResponseDto>> getEventDisputes(@PathVariable Long eventId) {
        List<Dispute> disputes = disputeService.getEventDisputes(eventId);
        List<DisputeResponseDto> response = disputes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DisputeResponseDto>> getOpenDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Dispute> disputes = disputeService.getOpenDisputes(pageable);
        
        Page<DisputeResponseDto> response = disputes.map(this::convertToResponseDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/{disputeId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeResponseDto> resolveDispute(
            @PathVariable Long disputeId,
            @RequestParam String resolution,
            @RequestParam(required = false) Double paymentAdjustment,
            @RequestParam(required = false) Integer noShowAdjustment,
            @AuthenticationPrincipal User admin) {
        try {
            Dispute dispute = disputeService.resolveDispute(
                    disputeId, admin.getId(), resolution, paymentAdjustment, noShowAdjustment);
            
            DisputeResponseDto response = convertToResponseDto(dispute);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/{disputeId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeResponseDto> closeDispute(
            @PathVariable Long disputeId,
            @AuthenticationPrincipal User admin) {
        try {
            Dispute dispute = disputeService.closeDispute(disputeId, admin.getId());
            DisputeResponseDto response = convertToResponseDto(dispute);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeStatistics> getDisputeStatistics() {
        DisputeStatistics statistics = disputeService.getDisputeStatistics();
        return ResponseEntity.ok(statistics);
    }

    private DisputeResponseDto convertToResponseDto(Dispute dispute) {
        return DisputeResponseDto.builder()
                .id(dispute.getId())
                .eventId(dispute.getEvent().getId())
                .eventTitle(dispute.getEvent().getTitle())
                .raisedById(dispute.getRaisedBy().getId())
                .raisedByName(dispute.getRaisedBy().getFullName())
                .againstUserId(dispute.getAgainstUser() != null ? dispute.getAgainstUser().getId() : null)
                .againstUserName(dispute.getAgainstUser() != null ? dispute.getAgainstUser().getFullName() : null)
                .status(dispute.getStatus())
                .description(dispute.getDescription())
                .evidenceUrls(dispute.getEvidenceUrls())
                .resolution(dispute.getResolution())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .resolvedById(dispute.getResolvedBy() != null ? dispute.getResolvedBy().getId() : null)
                .resolvedByName(dispute.getResolvedBy() != null ? dispute.getResolvedBy().getFullName() : null)
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }
}