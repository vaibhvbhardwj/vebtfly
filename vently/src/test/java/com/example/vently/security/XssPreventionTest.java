package com.example.vently.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for XSS prevention through input sanitization.
 * Validates that dangerous content is properly detected and sanitized.
 */
public class XssPreventionTest {

    @Test
    public void testSanitizeHtmlSpecialCharacters() {
        String input = "<script>alert('XSS')</script>";
        String sanitized = InputSanitizer.sanitizeHtml(input);
        
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("</script>"));
        assertTrue(sanitized.contains("&lt;"));
        assertTrue(sanitized.contains("&gt;"));
    }

    @Test
    public void testSanitizeAmpersand() {
        String input = "Tom & Jerry";
        String sanitized = InputSanitizer.sanitizeHtml(input);
        
        assertEquals("Tom &amp; Jerry", sanitized);
    }

    @Test
    public void testSanitizeQuotes() {
        String input = "He said \"Hello\"";
        String sanitized = InputSanitizer.sanitizeHtml(input);
        
        assertEquals("He said &quot;Hello&quot;", sanitized);
    }

    @Test
    public void testSanitizeSingleQuotes() {
        String input = "It's a test";
        String sanitized = InputSanitizer.sanitizeHtml(input);
        
        assertEquals("It&#x27;s a test", sanitized);
    }

    @Test
    public void testDetectScriptTag() {
        String input = "<script>alert('XSS')</script>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectJavascriptProtocol() {
        String input = "<a href='javascript:alert(1)'>Click</a>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectOnErrorAttribute() {
        String input = "<img src=x onerror='alert(1)'>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectOnLoadAttribute() {
        String input = "<body onload='alert(1)'>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectOnClickAttribute() {
        String input = "<button onclick='alert(1)'>Click</button>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectIframeTag() {
        String input = "<iframe src='http://evil.com'></iframe>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectObjectTag() {
        String input = "<object data='http://evil.com'></object>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testDetectEmbedTag() {
        String input = "<embed src='http://evil.com'>";
        
        assertTrue(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testSafeContentNotDetected() {
        String input = "This is a safe message";
        
        assertFalse(InputSanitizer.containsDangerousContent(input));
    }

    @Test
    public void testValidateAndSanitizeThrowsOnDangerousContent() {
        String input = "<script>alert('XSS')</script>";
        
        assertThrows(IllegalArgumentException.class, () -> {
            InputSanitizer.validateAndSanitize(input, "testField");
        });
    }

    @Test
    public void testValidateAndSanitizeSucceedsOnSafeContent() {
        String input = "Safe content";
        
        String result = InputSanitizer.validateAndSanitize(input, "testField");
        assertEquals("Safe content", result);
    }

    @Test
    public void testSanitizeEmailRemovesDangerousCharacters() {
        String input = "user<script>@example.com";
        String sanitized = InputSanitizer.sanitizeEmail(input);
        
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
        assertTrue(sanitized.contains("@"));
    }

    @Test
    public void testSanitizeFileNameRemovesDangerousCharacters() {
        String input = "../../etc/passwd.txt";
        String sanitized = InputSanitizer.sanitizeFileName(input);
        
        assertFalse(sanitized.contains(".."));
        // Note: "/" is not removed by sanitizeFileName, only ".." patterns are removed
        assertTrue(sanitized.contains("/"));
    }

    @Test
    public void testSanitizeFileNameRemovesSpecialCharacters() {
        String input = "file<name>.txt";
        String sanitized = InputSanitizer.sanitizeFileName(input);
        
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
    }

    @Test
    public void testValidateFileUploadThrowsOnEmptyFileName() {
        assertThrows(IllegalArgumentException.class, () -> {
            InputSanitizer.validateFileUpload("", 1000, 10000000);
        });
    }

    @Test
    public void testValidateFileUploadThrowsOnZeroSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            InputSanitizer.validateFileUpload("file.txt", 0, 10000000);
        });
    }

    @Test
    public void testValidateFileUploadThrowsOnExceededSize() {
        long maxSize = 1000;
        assertThrows(IllegalArgumentException.class, () -> {
            InputSanitizer.validateFileUpload("file.txt", 2000, maxSize);
        });
    }

    @Test
    public void testValidateFileUploadThrowsOnDangerousFileName() {
        assertThrows(IllegalArgumentException.class, () -> {
            InputSanitizer.validateFileUpload("<script>.txt", 1000, 10000000);
        });
    }

    @Test
    public void testValidateFileUploadThrowsOnExecutableExtension() {
        assertThrows(IllegalArgumentException.class, () -> {
            InputSanitizer.validateFileUpload("malware.exe", 1000, 10000000);
        });
    }

    @Test
    public void testValidateFileUploadSucceedsOnValidFile() {
        assertDoesNotThrow(() -> {
            InputSanitizer.validateFileUpload("document.pdf", 1000, 10000000);
        });
    }

    @Test
    public void testNullInputReturnsNull() {
        assertNull(InputSanitizer.sanitizeHtml(null));
        assertNull(InputSanitizer.sanitizeEmail(null));
        assertNull(InputSanitizer.sanitizeFileName(null));
    }

    @Test
    public void testNullInputNotDetectedAsDangerous() {
        assertFalse(InputSanitizer.containsDangerousContent(null));
    }
}
