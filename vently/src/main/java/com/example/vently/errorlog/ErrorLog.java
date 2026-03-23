package com.example.vently.errorlog;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", nullable = false, unique = true, length = 36)
    private String traceId;

    @Column(name = "error_type", length = 100)
    private String errorType;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
