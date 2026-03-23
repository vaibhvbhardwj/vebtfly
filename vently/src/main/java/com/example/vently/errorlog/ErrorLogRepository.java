package com.example.vently.errorlog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    Optional<ErrorLog> findByTraceId(String traceId);
}
