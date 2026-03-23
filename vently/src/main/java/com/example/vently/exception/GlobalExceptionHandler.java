package com.example.vently.exception;

import com.example.vently.errorlog.ErrorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorLogService errorLogService;

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private String getIpAddress(WebRequest request) {
        try {
            if (request instanceof org.springframework.web.context.request.ServletWebRequest swr) {
                String xff = swr.getRequest().getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
                return swr.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.example.vently.user.User u) {
                return u.getId();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.warn("Resource not found [traceId: {}]: {}", traceId, ex.getMessage());
        errorLogService.saveError(traceId, "NOT_FOUND", ex.getMessage(), path, null, getCurrentUserId(), getIpAddress(request), 404);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(404).error("NOT_FOUND").message(ex.getMessage())
                .path(path).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.warn("Unauthorized [traceId: {}]: {}", traceId, ex.getMessage());
        errorLogService.saveError(traceId, "FORBIDDEN", ex.getMessage(), path, null, getCurrentUserId(), getIpAddress(request), 403);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(403).error("FORBIDDEN").message(ex.getMessage())
                .path(path).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.warn("Validation failed [traceId: {}]: {}", traceId, ex.getMessage());
        errorLogService.saveError(traceId, "VALIDATION_ERROR", ex.getMessage(), path, null, getCurrentUserId(), getIpAddress(request), 400);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400).error("VALIDATION_ERROR").message(ex.getMessage())
                .path(path).fieldErrors(ex.getFieldErrors()).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.error("Payment failed [traceId: {}]: {}", traceId, ex.getMessage());
        errorLogService.saveError(traceId, "PAYMENT_ERROR", ex.getMessage(), path, ex, getCurrentUserId(), getIpAddress(request), 400);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400).error("PAYMENT_ERROR").message(ex.getMessage())
                .path(path).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            fieldErrors.put(fieldName, error.getDefaultMessage());
        });
        log.warn("Validation failed [traceId: {}]: {} field(s)", traceId, fieldErrors.size());
        errorLogService.saveError(traceId, "VALIDATION_ERROR", "Request validation failed", path, null, getCurrentUserId(), getIpAddress(request), 400);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400).error("VALIDATION_ERROR").message("Request validation failed")
                .path(path).fieldErrors(fieldErrors).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.warn("Invalid argument [traceId: {}]: {}", traceId, ex.getMessage());
        errorLogService.saveError(traceId, "INVALID_ARGUMENT", ex.getMessage(), path, null, getCurrentUserId(), getIpAddress(request), 400);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400).error("INVALID_ARGUMENT").message(ex.getMessage())
                .path(path).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.warn("Invalid state [traceId: {}]: {}", traceId, ex.getMessage());
        errorLogService.saveError(traceId, "INVALID_STATE", ex.getMessage(), path, null, getCurrentUserId(), getIpAddress(request), 400);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400).error("INVALID_STATE").message(ex.getMessage())
                .path(path).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        String path = getPath(request);
        log.error("Unexpected error [traceId: {}]", traceId, ex);
        errorLogService.saveError(traceId, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", path, ex, getCurrentUserId(), getIpAddress(request), 500);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(500).error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Contact support with trace ID: " + traceId)
                .path(path).timestamp(java.time.LocalDateTime.now()).traceId(traceId).build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
