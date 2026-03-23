package com.example.vently.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoShowStatisticsDTO {
    private Long totalNoShows;
    private Long volunteersWithNoShows;
    private Double noShowRate;
    private Long suspendedDueToNoShows;
    private Long bannedDueToNoShows;
}
