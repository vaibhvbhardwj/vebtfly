package com.example.vently.admin.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthTrendDTO {
    private LocalDate date;
    private Long newVolunteers;
    private Long newOrganizers;
    private Long totalVolunteers;
    private Long totalOrganizers;
}
