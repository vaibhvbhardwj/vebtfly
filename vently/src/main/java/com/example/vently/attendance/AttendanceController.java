package com.example.vently.attendance;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.attendance.dto.AttendanceCodeDto;
import com.example.vently.attendance.dto.MarkAttendanceRequest;
import com.example.vently.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for attendance management
 * Requirements: 9.2, 9.3, 10.2, 10.3, 24.6
 */
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Generate attendance codes for an event (Organizer only)
     * Requirements: 9.2
     */
    @PostMapping("/{eventId}/generate-codes")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> generateAttendanceCodes(
            Authentication authentication,
            @PathVariable Long eventId) {
        
        attendanceService.generateAttendanceCodes(eventId);
        return ResponseEntity.ok("Attendance codes generated successfully");
    }

    /**
     * Mark attendance by code (Organizer only)
     * Requirements: 9.3
     */
    @PostMapping("/{eventId}/mark-by-code")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> markAttendanceByCode(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody MarkAttendanceRequest request) {
        
        User currentUser = (User) authentication.getPrincipal();
        attendanceService.markAttendanceByCode(eventId, request.getCode(), currentUser);
        return ResponseEntity.ok("Attendance marked successfully");
    }

    /**
     * Mark attendance as LATE by code (Organizer only)
     */
    @PostMapping("/{eventId}/mark-late-by-code")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> markLateByCode(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody MarkAttendanceRequest request) {

        User currentUser = (User) authentication.getPrincipal();
        attendanceService.markLateByCode(eventId, request.getCode(), currentUser);
        return ResponseEntity.ok("Attendance marked as late successfully");
    }

    /**
     * Requirements: 10.3
     */
    @PostMapping("/{eventId}/mark-by-excel")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> markAttendanceByExcel(
            Authentication authentication,
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file) {
        
        User currentUser = (User) authentication.getPrincipal();
        attendanceService.markAttendanceByExcel(eventId, file, currentUser);
        return ResponseEntity.ok("Attendance marked successfully from Excel file");
    }

    /**
     * Download attendance template (Organizer only)
     * Requirements: 10.2
     */
    @GetMapping("/{eventId}/template")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<byte[]> downloadAttendanceTemplate(
            Authentication authentication,
            @PathVariable Long eventId) {
        
        User currentUser = (User) authentication.getPrincipal();
        byte[] excelFile = attendanceService.downloadAttendanceTemplate(eventId, currentUser);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "attendance_template_event_" + eventId + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    /**
     * Get attendance list for an event (Organizer only)
     * Requirements: 24.6
     */
    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<AttendanceCodeDto>> getEventAttendance(
            Authentication authentication,
            @PathVariable Long eventId) {
        
        User currentUser = (User) authentication.getPrincipal();
        List<AttendanceCode> attendanceCodes = attendanceService.getEventAttendance(eventId, currentUser);
        
        List<AttendanceCodeDto> responseDtos = attendanceCodes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Convert AttendanceCode entity to AttendanceCodeDto
     */
    private AttendanceCodeDto toDto(AttendanceCode attendanceCode) {
        return AttendanceCodeDto.builder()
                .id(attendanceCode.getId())
                .eventId(attendanceCode.getEvent().getId())
                .volunteerId(attendanceCode.getVolunteer().getId())
                .volunteerName(attendanceCode.getVolunteer().getFullName())
                .volunteerEmail(attendanceCode.getVolunteer().getEmail())
                .code(attendanceCode.getCode())
                .markedAt(attendanceCode.getMarkedAt())
                .markedBy(attendanceCode.getMarkedBy() != null ? 
                        attendanceCode.getMarkedBy().getFullName() : null)
                .isMarked(attendanceCode.isMarked())
                .attendanceStatus(attendanceCode.getAttendanceStatus())
                .build();
    }
}
