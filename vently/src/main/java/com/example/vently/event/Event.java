package com.example.vently.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private Integer requiredVolunteers;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer requiredMaleVolunteers = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer requiredFemaleVolunteers = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentPerVolunteer;

    @Column(precision = 10, scale = 2)
    private BigDecimal paymentPerMaleVolunteer;

    @Column(precision = 10, scale = 2)
    private BigDecimal paymentPerFemaleVolunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    private String category;

    private LocalDate applicationDeadline;

    @Column(length = 10000000)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private String cancellationReason;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = EventStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to get event date and time as LocalDateTime
    public LocalDateTime getEventDateTime() {
        return LocalDateTime.of(date, time);
    }
}
