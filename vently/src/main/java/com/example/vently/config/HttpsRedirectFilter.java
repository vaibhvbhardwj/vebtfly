package com.example.vently.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to redirect HTTP requests to HTTPS.
 * Ensures all communication is encrypted.
 */
@Component
public class HttpsRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String serverName = request.getServerName();

        // Skip redirect for localhost
        if ("localhost".equals(serverName) || "127.0.0.1".equals(serverName)) {
            filterChain.doFilter(request, response);
            return;
        }

        // When behind nginx, check X-Forwarded-Proto instead of request.isSecure()
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        boolean isHttps = "https".equalsIgnoreCase(forwardedProto) || request.isSecure();

        if (!isHttps) {
            String httpsUrl = "https://" + serverName + request.getRequestURI();
            if (request.getQueryString() != null) {
                httpsUrl += "?" + request.getQueryString();
            }
            response.sendRedirect(httpsUrl);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

