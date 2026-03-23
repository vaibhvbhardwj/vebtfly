package com.example.vently.attendance;

import java.time.LocalDateTime;

import com.example.vently.event.Event;
import com.example.vently.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    name = "attendance_code",
    uniqueConstraints = @UniqueConstraint(columnNames = {"code"})
)
public class AttendanceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @ManyToOne
    @JoinColumn(name = "marked_by")
    private User markedBy;

    // PRESENT, LATE, or null (not marked)
    @Column(name = "attendance_status", length = 10)
    private String attendanceStatus;

    // Helper method to check if attendance has been marked
    public boolean isMarked() {
        return markedAt != null;
    }

    // Helper method to mark attendance as PRESENT
    public void markAttendance(User organizer) {
        this.markedAt = LocalDateTime.now();
        this.markedBy = organizer;
        this.attendanceStatus = "PRESENT";
    }

    // Helper method to mark attendance as LATE
    public void markLate(User organizer) {
        this.markedAt = LocalDateTime.now();
        this.markedBy = organizer;
        this.attendanceStatus = "LATE";
    }

    // Helper method to check if code is unmarked
    public boolean isUnmarked() {
        return markedAt == null;
    }
}
