package com.example.vently.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageRatingsDTO {
    private Double averageVolunteerRating;
    private Long volunteerRatingCount;
    private Double averageOrganizerRating;
    private Long organizerRatingCount;
}
