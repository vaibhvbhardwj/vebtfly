package com.example.vently.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeMetricsDTO {
    private Long openDisputes;
    private Long resolvedDisputes;
    private Long totalDisputes;
    private Double averageResolutionTimeHours;
    private Long disputesUnderReview;
}
