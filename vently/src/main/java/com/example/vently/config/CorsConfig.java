package com.example.vently.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

/**
 * CORS Configuration for the Vently platform.
 * Defines allowed origins, methods, headers, and credentials for cross-origin requests.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOriginsProperty;

    /**
     * Get the list of allowed origins for CORS requests.
     * Can be configured via application.properties or environment variables.
     * 
     * @return List of allowed origins
     */
    public List<String> getAllowedOrigins() {
        return Arrays.asList(allowedOriginsProperty.split(","));
    }

    /**
     * Create a CORS configuration with security best practices.
     * 
     * @return Configured CorsConfiguration object
     */
    public CorsConfiguration createCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Set allowed origins from configuration
        configuration.setAllowedOrigins(getAllowedOrigins());
        
        // Allow only necessary HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Allow necessary headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "X-CSRF-Token"
        ));
        
        // Expose Authorization header for JWT tokens
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Set max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);
        
        return configuration;
    }
}
