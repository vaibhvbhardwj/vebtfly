package com.example.vently.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Service for handling S3 file uploads
 * Requirements: 2.5, 26.7
 */
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public S3Service(@Autowired(required = false) S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public boolean isConfigured() {
        return s3Client != null;
    }

    /**
     * Upload a file to S3 and return the public URL
     * 
     * @param file   The file to upload
     * @param folder The folder path in S3 (e.g., "profile-pictures")
     * @return The public URL of the uploaded file
     * @throws IOException if file upload fails
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (s3Client == null) {
            throw new IOException("S3 client is not configured");
        }
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String key = folder + "/" + uniqueFilename;

        try {
            // Upload file to S3 (no ACL — relies on bucket policy for public access)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Return public URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
        } catch (Exception e) {
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }
}
