package com.example.vently.subscription;

import java.time.LocalDate;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "subscription",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"})
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionTier tier = SubscriptionTier.FREE;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(length = 255)
    private String razorpayPaymentId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (tier == null) {
            tier = SubscriptionTier.FREE;
        }
        if (active == null) {
            active = true;
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    // Helper: true for any paid tier
    public boolean isPremium() {
        return (tier == SubscriptionTier.GOLD || tier == SubscriptionTier.PLATINUM) && active;
    }

    public boolean isFree() {
        return tier == SubscriptionTier.FREE;
    }

    public boolean isExpired() {
        if (endDate == null) return false;
        return LocalDate.now().isAfter(endDate);
    }

    public void upgradeToPremium(String razorpayPaymentId) {
        this.tier = SubscriptionTier.PLATINUM;
        this.razorpayPaymentId = razorpayPaymentId;
        this.active = true;
    }

    public void downgradeToFree() {
        this.tier = SubscriptionTier.FREE;
        this.razorpayPaymentId = null;
        this.endDate = LocalDate.now();
    }

    public void deactivate() {
        this.active = false;
        if (this.endDate == null) this.endDate = LocalDate.now();
    }

    public void reactivate() {
        this.active = true;
        this.endDate = null;
    }
}
