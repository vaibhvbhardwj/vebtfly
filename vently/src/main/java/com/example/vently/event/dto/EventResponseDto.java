package com.example.vently.event.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.vently.event.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for event response
 * Requirements: 4.4, 4.5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {
    
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDate date;
    private LocalTime time;
    private Integer requiredVolunteers;
    private Integer requiredMaleVolunteers;
    private Integer requiredFemaleVolunteers;
    private BigDecimal paymentPerVolunteer;
    private BigDecimal paymentPerMaleVolunteer;
    private BigDecimal paymentPerFemaleVolunteer;
    private EventStatus status;
    private String category;
    private LocalDate applicationDeadline;
    private String imageUrl;
    
    // Organizer information
    private OrganizerInfo organizer;
    
    // Application information
    private Integer applicationCount;
    private Integer confirmedCount;
    private Integer remainingSlots;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Inner class for organizer information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizerInfo {
        private Long id;
        private String name;
        private String organization;
        private Double averageRating;
        private Integer ratingCount;
        private Boolean verificationBadge;
    }
}
