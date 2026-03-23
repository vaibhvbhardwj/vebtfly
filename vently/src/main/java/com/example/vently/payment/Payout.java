package com.example.vently.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.vently.application.Application;
import com.example.vently.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    // Razorpay transfer ID for tracking
    private String razorpayTransferId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Number of retry attempts for failed payouts
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    private String failureReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PayoutStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to mark payout as completed
    public void markCompleted() {
        this.status = PayoutStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // Helper method to mark payout as failed
    public void markFailed(String reason) {
        this.status = PayoutStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
    }

    // Helper method to check if max retries reached
    public boolean hasReachedMaxRetries() {
        return retryCount >= 3;
    }

    // Helper method to calculate net amount (amount - platform fee)
    public BigDecimal getNetAmount() {
        return amount.subtract(platformFee);
    }
}
