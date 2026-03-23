package com.example.vently.dispute;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "raised_by", nullable = false)
    private User raisedBy;

    @ManyToOne
    @JoinColumn(name = "against_user_id")
    private User againstUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Store evidence URLs as JSON array
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String[] evidenceUrls;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DisputeStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to mark dispute as under review
    public void markUnderReview() {
        this.status = DisputeStatus.UNDER_REVIEW;
    }

    // Helper method to resolve dispute
    public void resolve(User admin, String resolutionText) {
        this.status = DisputeStatus.RESOLVED;
        this.resolvedBy = admin;
        this.resolution = resolutionText;
        this.resolvedAt = LocalDateTime.now();
    }

    // Helper method to close dispute
    public void close(User admin) {
        this.status = DisputeStatus.CLOSED;
        this.resolvedBy = admin;
        this.resolvedAt = LocalDateTime.now();
    }

    // Helper method to check if dispute is open
    public boolean isOpen() {
        return status == DisputeStatus.OPEN || status == DisputeStatus.UNDER_REVIEW;
    }
}
