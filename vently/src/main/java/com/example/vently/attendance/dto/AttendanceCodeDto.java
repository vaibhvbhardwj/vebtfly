package com.example.vently.attendance.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCodeDto {

    private Long id;
    private Long eventId;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerEmail;
    private String code;
    private LocalDateTime markedAt;
    private String markedBy;
    private boolean isMarked;
    private String attendanceStatus; // PRESENT, LATE, or null
}
