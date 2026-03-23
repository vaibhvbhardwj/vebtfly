package com.example.vently.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDepositDto {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;
}
