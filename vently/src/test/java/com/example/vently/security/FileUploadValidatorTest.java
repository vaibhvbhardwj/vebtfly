package com.example.vently.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tests for file upload validation.
 * Validates that malicious files are rejected.
 */
public class FileUploadValidatorTest {

    @Test
    public void testValidImageUpload() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testValidPngImageUpload() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.png",
            "image/png",
            "fake image content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testValidGifImageUpload() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.gif",
            "image/gif",
            "fake image content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testValidWebpImageUpload() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.webp",
            "image/webp",
            "fake image content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testInvalidImageMimeType() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "file.txt",
            "text/plain",
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testEmptyImageFile() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg",
            "image/jpeg",
            new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testNullImageFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(null));
    }

    @Test
    public void testImageFileTooLarge() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg",
            "image/jpeg",
            largeContent
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testValidEvidenceUploadPdf() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "evidence.pdf",
            "application/pdf",
            "fake pdf content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateEvidenceUpload(file));
    }

    @Test
    public void testValidEvidenceUploadWord() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "evidence.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "fake word content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateEvidenceUpload(file));
    }

    @Test
    public void testValidEvidenceUploadImage() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "evidence.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateEvidenceUpload(file));
    }

    @Test
    public void testInvalidEvidenceUploadExecutable() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "malware.exe",
            "application/octet-stream",
            "fake executable".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateEvidenceUpload(file));
    }

    @Test
    public void testInvalidEvidenceUploadScript() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "script.js",
            "application/javascript",
            "fake script".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateEvidenceUpload(file));
    }

    @Test
    public void testFileNameWithPathTraversal() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "../../etc/passwd",
            "text/plain",
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testFileNameWithSpecialCharacters() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image<script>.jpg",
            "image/jpeg",
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testFileNameWithNullBytes() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg\0.exe",
            "image/jpeg",
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testMaximumAllowedFileSize() {
        byte[] maxContent = new byte[10 * 1024 * 1024]; // Exactly 10MB
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg",
            "image/jpeg",
            maxContent
        );

        assertDoesNotThrow(() -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testJustOverMaximumFileSize() {
        byte[] overMaxContent = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg",
            "image/jpeg",
            overMaxContent
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testFileNameWithDoubleExtension() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "image.jpg.exe",
            "image/jpeg",
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }

    @Test
    public void testFileNameWithUppercaseExecutableExtension() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "malware.EXE",
            "application/octet-stream",
            "fake content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.validateImageUpload(file));
    }
}
