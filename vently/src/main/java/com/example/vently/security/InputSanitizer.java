package com.example.vently.security;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user inputs to prevent XSS and injection attacks.
 * Provides HTML encoding and validation methods.
 */
public class InputSanitizer {

    // Pattern to detect potentially dangerous HTML/script content
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        "<script|javascript:|onerror=|onload=|onclick=|<iframe|<object|<embed|<img[^>]*on",
        Pattern.CASE_INSENSITIVE
    );

    // HTML special characters that need encoding
    private static final String[][] HTML_ESCAPE_TABLE = {
        {"&", "&amp;"},
        {"<", "&lt;"},
        {">", "&gt;"},
        {"\"", "&quot;"},
        {"'", "&#x27;"},
        {"/", "&#x2F;"}
    };

    /**
     * Sanitize user input by HTML encoding special characters.
     * This prevents XSS attacks by converting HTML special characters to entities.
     * 
     * @param input The user input to sanitize
     * @return HTML-encoded string safe for display
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }

        String result = input;
        for (String[] escape : HTML_ESCAPE_TABLE) {
            result = result.replace(escape[0], escape[1]);
        }
        return result;
    }

    /**
     * Check if input contains potentially dangerous content.
     * 
     * @param input The user input to check
     * @return true if dangerous content is detected, false otherwise
     */
    public static boolean containsDangerousContent(String input) {
        if (input == null) {
            return false;
        }
        return DANGEROUS_PATTERN.matcher(input).find();
    }

    /**
     * Validate and sanitize a string field.
     * Throws exception if dangerous content is detected.
     * 
     * @param input The user input to validate
     * @param fieldName The name of the field (for error messages)
     * @return Sanitized input
     * @throws IllegalArgumentException if dangerous content is detected
     */
    public static String validateAndSanitize(String input, String fieldName) {
        if (input == null) {
            return null;
        }

        if (containsDangerousContent(input)) {
            throw new IllegalArgumentException(
                "Field '" + fieldName + "' contains potentially dangerous content"
            );
        }

        return sanitizeHtml(input);
    }

    /**
     * Sanitize email address.
     * Removes potentially dangerous characters while preserving valid email format.
     * 
     * @param email The email to sanitize
     * @return Sanitized email
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }

        // Remove any HTML/script content
        String sanitized = email.replaceAll("[<>\"'%;()&+]", "");
        return sanitized.trim();
    }

    /**
     * Sanitize file name to prevent directory traversal attacks.
     * 
     * @param fileName The file name to sanitize
     * @return Sanitized file name
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }

        // Remove path traversal attempts
        String sanitized = fileName.replaceAll("\\.\\.[\\\\/]", "");
        sanitized = sanitized.replaceAll("[<>:\"|?*]", "");
        return sanitized.trim();
    }

    /**
     * Validate file upload for malicious content.
     * Checks file name and size.
     * 
     * @param fileName The name of the file
     * @param fileSize The size of the file in bytes
     * @param maxSizeBytes Maximum allowed file size
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateFileUpload(String fileName, long fileSize, long maxSizeBytes) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }

        if (fileSize > maxSizeBytes) {
            throw new IllegalArgumentException(
                "File size exceeds maximum allowed size of " + maxSizeBytes + " bytes"
            );
        }

        if (containsDangerousContent(fileName)) {
            throw new IllegalArgumentException("File name contains potentially dangerous content");
        }

        // Check for executable file extensions
        String lowerFileName = fileName.toLowerCase();
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js"};
        for (String ext : dangerousExtensions) {
            if (lowerFileName.endsWith(ext)) {
                throw new IllegalArgumentException("File type not allowed: " + ext);
            }
        }
    }
}
