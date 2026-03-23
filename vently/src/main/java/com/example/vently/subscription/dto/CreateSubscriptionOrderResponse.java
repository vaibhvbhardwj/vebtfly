package com.example.vently.subscription.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSubscriptionOrderResponse {
    private String orderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String tier; // GOLD or PLATINUM
}
