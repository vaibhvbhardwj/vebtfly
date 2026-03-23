package com.example.vently.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {
    
    // Common statistics
    private Double averageRating;
    private Long totalRatings;
    private Integer noShowCount;
    
    // Volunteer-specific statistics
    private Long totalApplications;
    private Long confirmedApplications;
    
    // Organizer-specific statistics
    private Long totalEvents;
    private Long completedEvents;
}
