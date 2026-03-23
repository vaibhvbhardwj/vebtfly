package com.example.vently.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for authentication endpoints.
 * Limits requests to 5 per minute per IP address to prevent brute force attacks.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long ONE_MINUTE_IN_MILLIS = 60_000;

    // Map to track IP addresses and their request data
    private final Map<String, RequestData> requestTracker = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        long currentTime = System.currentTimeMillis();

        RequestData requestData = requestTracker.compute(clientIp, (ip, data) -> {
            if (data == null || currentTime - data.windowStart > ONE_MINUTE_IN_MILLIS) {
                // Start a new time window
                return new RequestData(currentTime, 1);
            } else {
                // Increment request count in current window
                data.requestCount++;
                return data;
            }
        });

        if (requestData.requestCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Too many requests\", \"message\": \"Rate limit exceeded. Maximum " + 
                MAX_REQUESTS_PER_MINUTE + " requests per minute allowed.\"}"
            );
            return;
        }

        // Cleanup old entries periodically (simple approach)
        if (requestTracker.size() > 10000) {
            cleanupOldEntries(currentTime);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only apply rate limiting to auth endpoints
        return !path.startsWith("/api/v1/auth/");
    }

    /**
     * Extract client IP address from request, considering proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Remove entries older than 2 minutes to prevent memory leaks.
     */
    private void cleanupOldEntries(long currentTime) {
        requestTracker.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().windowStart > (2 * ONE_MINUTE_IN_MILLIS)
        );
        log.debug("Cleaned up old rate limit entries. Current size: {}", requestTracker.size());
    }

    /**
     * Inner class to track request data for each IP.
     */
    private static class RequestData {
        long windowStart;
        int requestCount;

        RequestData(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
