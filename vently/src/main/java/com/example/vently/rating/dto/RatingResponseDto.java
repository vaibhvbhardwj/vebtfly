package com.example.vently.rating.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto {
    
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long raterId;
    private String raterName;
    private Long ratedUserId;
    private String ratedUserName;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
    
    // Helper method to check if rating is within 7-day window
    public boolean isWithinSubmissionWindow(LocalDateTime eventCompletionDate) {
        LocalDateTime deadline = eventCompletionDate.plusDays(7);
        return createdAt.isBefore(deadline) || createdAt.isEqual(deadline);
    }
}