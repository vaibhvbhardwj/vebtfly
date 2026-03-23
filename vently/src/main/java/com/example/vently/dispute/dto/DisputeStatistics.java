package com.example.vently.dispute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeStatistics {
    
    private Long totalDisputes;
    
    private Long openDisputes;
    
    private Long resolvedDisputes;
    
    private Long closedDisputes;
    
    private Double averageResolutionTimeSeconds;
    
    private Double averageResolutionTimeHours() {
        return averageResolutionTimeSeconds != null ? averageResolutionTimeSeconds / 3600.0 : null;
    }
    
    private Double averageResolutionTimeDays() {
        return averageResolutionTimeSeconds != null ? averageResolutionTimeSeconds / 86400.0 : null;
    }
    
    private Double resolutionRate() {
        if (totalDisputes == null || totalDisputes == 0) {
            return 0.0;
        }
        long resolvedAndClosed = (resolvedDisputes != null ? resolvedDisputes : 0) + 
                                (closedDisputes != null ? closedDisputes : 0);
        return (double) resolvedAndClosed / totalDisputes * 100.0;
    }
}