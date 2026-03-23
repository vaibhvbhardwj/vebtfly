package com.example.vently.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to sanitize all user inputs and prevent XSS attacks.
 * Wraps the request to automatically sanitize parameters.
 */
@Component
public class InputSanitizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Wrap the request with sanitizing wrapper
        SanitizingRequestWrapper sanitizedRequest = new SanitizingRequestWrapper(request);
        
        filterChain.doFilter(sanitizedRequest, response);
    }
}
