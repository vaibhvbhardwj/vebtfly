package com.example.vently.validation;

import java.util.regex.Pattern;

/**
 * Utility class for common validation patterns.
 */
public class ValidationUtils {
    
    // Email regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Phone number pattern (basic international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );
    
    // Date format validation (YYYY-MM-DD)
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}$"
    );

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.replaceAll("[\\s-]", "")).matches();
    }

    /**
     * Validate date format (YYYY-MM-DD).
     */
    public static boolean isValidDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        return DATE_PATTERN.matcher(date).matches();
    }

    /**
     * Validate string length.
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return minLength == 0;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validate that a string is not empty or null.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate file extension.
     */
    public static boolean isValidFileExtension(String filename, String... allowedExtensions) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowed : allowedExtensions) {
            if (extension.equals(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get file extension from filename.
     */
    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Validate file size.
     */
    public static boolean isValidFileSize(long fileSize, long maxSizeInBytes) {
        return fileSize > 0 && fileSize <= maxSizeInBytes;
    }

    /**
     * Validate numeric range.
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        return doubleValue >= min.doubleValue() && doubleValue <= max.doubleValue();
    }
}
