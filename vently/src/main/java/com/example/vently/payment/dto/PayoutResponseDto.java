package com.example.vently.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.vently.payment.Payout;
import com.example.vently.payment.PayoutStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponseDto {

    private Long id;
    private Long applicationId;
    private Long eventId;
    private String eventTitle;
    private Long volunteerId;
    private String volunteerName;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal netAmount;
    private PayoutStatus status;
    private String razorpayTransferId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static PayoutResponseDto fromEntity(Payout payout) {
        return PayoutResponseDto.builder()
                .id(payout.getId())
                .applicationId(payout.getApplication().getId())
                .eventId(payout.getApplication().getEvent().getId())
                .eventTitle(payout.getApplication().getEvent().getTitle())
                .volunteerId(payout.getVolunteer().getId())
                .volunteerName(payout.getVolunteer().getFullName())
                .amount(payout.getAmount())
                .platformFee(payout.getPlatformFee())
                .netAmount(payout.getNetAmount())
                .status(payout.getStatus())
                .razorpayTransferId(payout.getRazorpayTransferId())
                .createdAt(payout.getCreatedAt())
                .completedAt(payout.getCompletedAt())
                .build();
    }
}
