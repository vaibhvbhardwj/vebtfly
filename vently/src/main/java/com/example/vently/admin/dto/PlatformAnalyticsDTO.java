package com.example.vently.admin.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAnalyticsDTO {
    private Long totalUsers;
    private Long totalVolunteers;
    private Long totalOrganizers;
    private Long totalAdmins;
    private Long totalEvents;
    private Long completedEvents;
    private Long cancelledEvents;
    private Long totalTransactions;
    private BigDecimal totalRevenue;
    private BigDecimal platformFeesCollected;
}
