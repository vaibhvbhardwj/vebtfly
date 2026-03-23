package com.example.vently.security;

import org.springframework.web.multipart.MultipartFile;

/**
 * Validator for file uploads to prevent malicious file uploads.
 * Validates file type, size, and content.
 */
public class FileUploadValidator {

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed MIME types for profile pictures
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    };

    // Allowed MIME types for evidence uploads
    private static final String[] ALLOWED_EVIDENCE_TYPES = {
        "image/jpeg",
        "image/png",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    /**
     * Validate image file upload (for profile pictures).
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateImageUpload(MultipartFile file) {
        validateFileUpload(file, ALLOWED_IMAGE_TYPES, "Image");
    }

    /**
     * Validate evidence file upload (for disputes).
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateEvidenceUpload(MultipartFile file) {
        validateFileUpload(file, ALLOWED_EVIDENCE_TYPES, "Evidence");
    }

    /**
     * Validate a file upload with specified allowed types.
     * 
     * @param file The file to validate
     * @param allowedTypes Array of allowed MIME types
     * @param fileType Description of file type (for error messages)
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateFileUpload(MultipartFile file, String[] allowedTypes, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileType + " file cannot be empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                fileType + " file size exceeds maximum allowed size of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB"
            );
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedMimeType(contentType, allowedTypes)) {
            throw new IllegalArgumentException(
                "File type '" + contentType + "' is not allowed for " + fileType + " uploads"
            );
        }

        // Sanitize file name
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        InputSanitizer.validateFileUpload(fileName, file.getSize(), MAX_FILE_SIZE);
    }

    /**
     * Check if MIME type is in the allowed list.
     * 
     * @param contentType The MIME type to check
     * @param allowedTypes Array of allowed MIME types
     * @return true if MIME type is allowed, false otherwise
     */
    private static boolean isAllowedMimeType(String contentType, String[] allowedTypes) {
        for (String allowedType : allowedTypes) {
            if (contentType.equals(allowedType) || contentType.startsWith(allowedType.split("/")[0] + "/")) {
                return true;
            }
        }
        return false;
    }
}
