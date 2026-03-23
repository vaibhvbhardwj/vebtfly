package com.example.vently.rating.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingStatisticsDto {
    
    private Long userId;
    private Double averageRating;
    private Long totalRatings;
    private Map<Integer, Long> ratingDistribution; // rating value -> count
    private Integer oneStarCount;
    private Integer twoStarCount;
    private Integer threeStarCount;
    private Integer fourStarCount;
    private Integer fiveStarCount;
    
    public void calculateDistribution(Map<Integer, Long> distribution) {
        this.ratingDistribution = distribution;
        this.oneStarCount = distribution.getOrDefault(1, 0L).intValue();
        this.twoStarCount = distribution.getOrDefault(2, 0L).intValue();
        this.threeStarCount = distribution.getOrDefault(3, 0L).intValue();
        this.fourStarCount = distribution.getOrDefault(4, 0L).intValue();
        this.fiveStarCount = distribution.getOrDefault(5, 0L).intValue();
    }
}