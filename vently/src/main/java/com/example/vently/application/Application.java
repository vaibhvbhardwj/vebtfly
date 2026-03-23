package com.example.vently.application;

import java.time.LocalDateTime;

import com.example.vently.event.Event;
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
    name = "application",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "volunteer_id"})
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime declinedAt;

    // Confirmation deadline (48 hours after acceptance)
    private LocalDateTime confirmationDeadline;

    // Audit fields
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to check if confirmation has expired
    public boolean isConfirmationExpired() {
        return confirmationDeadline != null && 
               LocalDateTime.now().isAfter(confirmationDeadline) &&
               status == ApplicationStatus.ACCEPTED;
    }

    // Helper method to set acceptance with 48-hour deadline
    public void setAccepted() {
        this.status = ApplicationStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.confirmationDeadline = LocalDateTime.now().plusHours(48);
    }

    // Helper method to confirm application
    public void setConfirmed() {
        this.status = ApplicationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // Helper method to decline application
    public void setDeclined() {
        this.status = ApplicationStatus.DECLINED;
        this.declinedAt = LocalDateTime.now();
    }
}
