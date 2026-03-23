package com.example.vently.errorlog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    public void saveError(String traceId, String errorType, String message, String path,
                          Throwable ex, Long userId, String ipAddress, int httpStatus) {
        try {
            String stackTrace = null;
            if (ex != null) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                stackTrace = sw.toString();
                // Truncate to avoid huge DB entries
                if (stackTrace.length() > 8000) {
                    stackTrace = stackTrace.substring(0, 8000) + "\n... (truncated)";
                }
            }

            ErrorLog errorLog = ErrorLog.builder()
                    .traceId(traceId)
                    .errorType(errorType)
                    .message(message)
                    .path(path)
                    .stackTrace(stackTrace)
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .httpStatus(httpStatus)
                    .timestamp(LocalDateTime.now())
                    .build();

            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            log.error("Failed to persist error log for traceId: {}", traceId, e);
        }
    }

    public Optional<ErrorLog> findByTraceId(String traceId) {
        return errorLogRepository.findByTraceId(traceId);
    }
}
