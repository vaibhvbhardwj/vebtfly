package com.example.vently.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubscriptionOrderRequest {
    
    @NotNull(message = "Subscription tier is required")
    private String tier; // "PREMIUM"
    
    private String notes; // Optional notes for the order
}