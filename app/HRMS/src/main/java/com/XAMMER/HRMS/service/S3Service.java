package com.XAMMER.HRMS.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import com.XAMMER.HRMS.dto.S3UploadResult;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final S3Client s3Client;
    private final S3Presigner s3Presigner; // Add S3Presigner

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // Spring will inject the S3Client created by the AwsConfig @Bean method
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        // Build S3Presigner using the same client configuration (region, credentials)
        this.s3Presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build();
    }

    // This method is for uploading the file and returning its S3 key and original name.
    // The URL generation is separated into getPresignedUrl().
    public S3UploadResult uploadFileAndGetDetails(MultipartFile file, String directory) throws IOException {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String storageKey = (directory != null && !directory.isEmpty() ? directory + "/" : "") + UUID.randomUUID() + fileExtension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        logger.info("Uploaded file {} to S3 bucket {} with key {}", file.getOriginalFilename(), bucketName, storageKey);

        // When a file is uploaded, we store its key and original name. The URL is generated on-demand later.
        return new S3UploadResult(storageKey, file.getOriginalFilename(), null);
    }

    // This method is for generating a temporary, secure URL to access a file.
    public String getPresignedUrl(String fileKey, Duration expirationDuration) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expirationDuration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            logger.debug("Generated presigned URL for key {}: {}", fileKey, presignedRequest.url().toString());
            return presignedRequest.url().toString();
        } catch (Exception e) {
            logger.error("Error generating presigned URL for key {}: {}", fileKey, e.getMessage());
            return null;
        }
    }

    // This method is for deleting a file from S3.
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Successfully deleted file with key {} from S3 bucket {}", fileKey, bucketName);
        } catch (Exception e) {
            logger.error("Could not delete file {} from S3 bucket {}.", fileKey, bucketName, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex) : "";
    }
}