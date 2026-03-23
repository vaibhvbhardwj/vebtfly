package com.example.vently.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper that sanitizes all request parameters to prevent XSS attacks.
 * Automatically HTML-encodes all parameter values.
 */
public class SanitizingRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> sanitizedParameters = new HashMap<>();

    public SanitizingRequestWrapper(HttpServletRequest request) {
        super(request);
        sanitizeParameters(request);
    }

    /**
     * Sanitize all request parameters.
     * 
     * @param request The original request
     */
    private void sanitizeParameters(HttpServletRequest request) {
        Map<String, String[]> originalParams = request.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : originalParams.entrySet()) {
            String[] values = entry.getValue();
            String[] sanitizedValues = new String[values.length];
            
            for (int i = 0; i < values.length; i++) {
                sanitizedValues[i] = HtmlUtils.htmlEscape(values[i]);
            }
            
            sanitizedParameters.put(entry.getKey(), sanitizedValues);
        }
    }

    @Override
    public String getParameter(String name) {
        String[] values = sanitizedParameters.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return sanitizedParameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return sanitizedParameters;
    }
}
