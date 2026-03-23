package com.example.vently.subscription.dto;

import java.time.LocalDate;

import com.example.vently.subscription.SubscriptionTier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;
    private Long userId;
    private SubscriptionTier tier;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private String razorpayPaymentId;
    
    // Tier limits for display
    private Integer eventLimit; // -1 means unlimited
    private Integer applicationLimit; // -1 means unlimited
}
