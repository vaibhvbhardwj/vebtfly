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
public class RevenueMetricsDTO {
    private Long totalTransactions;
    private BigDecimal totalTransactionVolume;
    private BigDecimal platformFeesCollected;
    private BigDecimal averageTransactionAmount;
    private Long completedPayments;
    private Long failedPayments;
    private Long refundedPayments;
}
