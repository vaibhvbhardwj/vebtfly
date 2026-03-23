package com.example.vently.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifySubscriptionPaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Signature is required")
    private String signature;

    /** GOLD or PLATINUM — which tier was purchased */
    private String tier;
}
