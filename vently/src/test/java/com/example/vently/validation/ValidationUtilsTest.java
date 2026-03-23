package com.example.vently.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ValidationUtils utility class.
 */
class ValidationUtilsTest {

    @Test
    void testValidEmail_ValidFormats() {
        // Test valid email formats
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@example.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("user+tag@example.com"));
        assertTrue(ValidationUtils.isValidEmail("123@example.com"));
    }

    @Test
    void testValidEmail_InvalidFormats() {
        // Test invalid email formats
        assertFalse(ValidationUtils.isValidEmail("notanemail"));
        assertFalse(ValidationUtils.isValidEmail("missing@domain"));
        assertFalse(ValidationUtils.isValidEmail("@nodomain.com"));
        assertFalse(ValidationUtils.isValidEmail("spaces in@email.com"));
        assertFalse(ValidationUtils.isValidEmail("double@@domain.com"));
    }

    @Test
    void testValidEmail_NullAndEmpty() {
        // Test null and empty email
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("   "));
    }

    @Test
    void testValidPhoneNumber_ValidFormats() {
        // Test valid phone number formats
        assertTrue(ValidationUtils.isValidPhoneNumber("1234567890"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+1234567890"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+1 234 567 8900"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+1-234-567-8900"));
    }

    @Test
    void testValidPhoneNumber_InvalidFormats() {
        // Test invalid phone number formats
        // Phone regex requires: +?[1-9]\d{1,14}
        // So minimum is 2 digits (first digit 1-9, then at least 1 more digit)
        assertFalse(ValidationUtils.isValidPhoneNumber("1"));  // Only 1 digit
        assertFalse(ValidationUtils.isValidPhoneNumber(""));   // Empty
    }

    @Test
    void testValidPhoneNumber_NullAndEmpty() {
        // Test null and empty phone number
        assertFalse(ValidationUtils.isValidPhoneNumber(null));
        assertFalse(ValidationUtils.isValidPhoneNumber(""));
        assertFalse(ValidationUtils.isValidPhoneNumber("   "));
    }

    @Test
    void testValidDateFormat_ValidFormats() {
        // Test valid date formats
        assertTrue(ValidationUtils.isValidDateFormat("2024-01-15"));
        assertTrue(ValidationUtils.isValidDateFormat("2024-12-31"));
        assertTrue(ValidationUtils.isValidDateFormat("2000-01-01"));
    }

    @Test
    void testValidDateFormat_InvalidFormats() {
        // Test invalid date formats
        assertFalse(ValidationUtils.isValidDateFormat("01-15-2024"));
        assertFalse(ValidationUtils.isValidDateFormat("2024/01/15"));
        assertFalse(ValidationUtils.isValidDateFormat("15-01-2024"));
        assertFalse(ValidationUtils.isValidDateFormat("2024-1-15"));
    }

    @Test
    void testValidDateFormat_NullAndEmpty() {
        // Test null and empty date
        assertFalse(ValidationUtils.isValidDateFormat(null));
        assertFalse(ValidationUtils.isValidDateFormat(""));
        assertFalse(ValidationUtils.isValidDateFormat("   "));
    }

    @Test
    void testValidLength_WithinRange() {
        // Test strings within valid length range
        assertTrue(ValidationUtils.isValidLength("hello", 1, 10));
        assertTrue(ValidationUtils.isValidLength("test", 4, 4));
        assertTrue(ValidationUtils.isValidLength("a", 1, 5));
    }

    @Test
    void testValidLength_OutsideRange() {
        // Test strings outside valid length range
        assertFalse(ValidationUtils.isValidLength("hi", 3, 10));
        assertFalse(ValidationUtils.isValidLength("verylongstring", 1, 5));
        assertFalse(ValidationUtils.isValidLength("", 1, 10));
    }

    @Test
    void testValidLength_NullValue() {
        // Test null value
        assertTrue(ValidationUtils.isValidLength(null, 0, 10));
        assertFalse(ValidationUtils.isValidLength(null, 1, 10));
    }

    @Test
    void testValidLength_WithWhitespace() {
        // Test that whitespace is trimmed
        assertTrue(ValidationUtils.isValidLength("  hello  ", 5, 10));
        assertTrue(ValidationUtils.isValidLength("   ", 0, 10));
    }

    @Test
    void testIsNotEmpty_ValidStrings() {
        // Test non-empty strings
        assertTrue(ValidationUtils.isNotEmpty("hello"));
        assertTrue(ValidationUtils.isNotEmpty("a"));
        assertTrue(ValidationUtils.isNotEmpty("  text  "));
    }

    @Test
    void testIsNotEmpty_EmptyStrings() {
        // Test empty strings
        assertFalse(ValidationUtils.isNotEmpty(""));
        assertFalse(ValidationUtils.isNotEmpty("   "));
        assertFalse(ValidationUtils.isNotEmpty(null));
    }

    @Test
    void testValidFileExtension_ValidExtensions() {
        // Test valid file extensions
        assertTrue(ValidationUtils.isValidFileExtension("image.jpg", "jpg", "png", "gif"));
        assertTrue(ValidationUtils.isValidFileExtension("document.pdf", "pdf", "doc", "docx"));
        assertTrue(ValidationUtils.isValidFileExtension("file.XLSX", "xlsx", "xls"));
    }

    @Test
    void testValidFileExtension_InvalidExtensions() {
        // Test invalid file extensions
        assertFalse(ValidationUtils.isValidFileExtension("image.jpg", "png", "gif"));
        assertFalse(ValidationUtils.isValidFileExtension("script.exe", "jpg", "png"));
    }

    @Test
    void testValidFileExtension_NullAndEmpty() {
        // Test null and empty filenames
        assertFalse(ValidationUtils.isValidFileExtension(null, "jpg"));
        assertFalse(ValidationUtils.isValidFileExtension("", "jpg"));
        assertFalse(ValidationUtils.isValidFileExtension("noextension", "jpg"));
    }

    @Test
    void testGetFileExtension_ValidFilenames() {
        // Test getting file extension
        assertEquals("jpg", ValidationUtils.getFileExtension("image.jpg"));
        assertEquals("pdf", ValidationUtils.getFileExtension("document.pdf"));
        assertEquals("xlsx", ValidationUtils.getFileExtension("spreadsheet.xlsx"));
    }

    @Test
    void testGetFileExtension_NoExtension() {
        // Test filenames without extension
        assertEquals("", ValidationUtils.getFileExtension("noextension"));
        assertEquals("", ValidationUtils.getFileExtension(""));
        assertEquals("", ValidationUtils.getFileExtension(null));
    }

    @Test
    void testGetFileExtension_MultipleExtensions() {
        // Test filenames with multiple dots
        assertEquals("gz", ValidationUtils.getFileExtension("archive.tar.gz"));
        assertEquals("old", ValidationUtils.getFileExtension("file.backup.old"));
    }

    @Test
    void testValidFileSize_ValidSizes() {
        // Test valid file sizes
        assertTrue(ValidationUtils.isValidFileSize(1000, 10000));
        assertTrue(ValidationUtils.isValidFileSize(5000, 5000));
        assertTrue(ValidationUtils.isValidFileSize(1, 1000000));
    }

    @Test
    void testValidFileSize_InvalidSizes() {
        // Test invalid file sizes
        assertFalse(ValidationUtils.isValidFileSize(0, 10000));
        assertFalse(ValidationUtils.isValidFileSize(10001, 10000));
        assertFalse(ValidationUtils.isValidFileSize(-1, 10000));
    }

    @Test
    void testIsInRange_ValidNumbers() {
        // Test numbers within range
        assertTrue(ValidationUtils.isInRange(5, 1, 10));
        assertTrue(ValidationUtils.isInRange(1, 1, 10));
        assertTrue(ValidationUtils.isInRange(10, 1, 10));
        assertTrue(ValidationUtils.isInRange(5.5, 1.0, 10.0));
    }

    @Test
    void testIsInRange_InvalidNumbers() {
        // Test numbers outside range
        assertFalse(ValidationUtils.isInRange(0, 1, 10));
        assertFalse(ValidationUtils.isInRange(11, 1, 10));
        assertFalse(ValidationUtils.isInRange(5.5, 6.0, 10.0));
    }

    @Test
    void testIsInRange_NullValue() {
        // Test null value
        assertFalse(ValidationUtils.isInRange(null, 1, 10));
    }

    @Test
    void testValidFileExtension_CaseInsensitive() {
        // Test that extension matching is case-insensitive
        assertTrue(ValidationUtils.isValidFileExtension("image.JPG", "jpg"));
        assertTrue(ValidationUtils.isValidFileExtension("image.Jpg", "jpg"));
        assertTrue(ValidationUtils.isValidFileExtension("image.jpg", "JPG"));
    }

    @Test
    void testValidEmail_EdgeCases() {
        // Test edge cases for email validation
        assertTrue(ValidationUtils.isValidEmail("a@b.co"));
        assertTrue(ValidationUtils.isValidEmail("test.email+alex@leetcode.com"));
        assertFalse(ValidationUtils.isValidEmail("test@.com"));
        assertFalse(ValidationUtils.isValidEmail("test.@com"));
    }

    @Test
    void testValidPhoneNumber_EdgeCases() {
        // Test edge cases for phone validation
        assertTrue(ValidationUtils.isValidPhoneNumber("+12345678901234"));
        assertFalse(ValidationUtils.isValidPhoneNumber("+0123456789"));
        assertFalse(ValidationUtils.isValidPhoneNumber(""));
    }
}
