package com.example.vently.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.vently.payment.Payment;
import com.example.vently.payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long organizerId;
    private String organizerName;
    private BigDecimal amount;
    private PaymentStatus status;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static PaymentResponseDto fromEntity(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .eventId(payment.getEvent().getId())
                .eventTitle(payment.getEvent().getTitle())
                .organizerId(payment.getOrganizer().getId())
                .organizerName(payment.getOrganizer().getFullName())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
