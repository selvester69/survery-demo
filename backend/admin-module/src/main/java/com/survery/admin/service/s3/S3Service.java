package com.survery.admin.service.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.net.URL;

@Service
public class S3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(String key, byte[] fileBytes) {
        // TODO: This is a temporary workaround for the S3Client build issue.
        if (s3Client == null) {
            LOGGER.warn("S3Client is not configured. Returning a dummy URL.");
            return "http://s3.dummy.url/" + bucketName + "/" + key;
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

            URL url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key));

            LOGGER.info("Successfully uploaded file {} to S3. ETag: {}", key, response.eTag());
            return url.toString();

        } catch (Exception e) {
            LOGGER.error("Error uploading file {} to S3", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
}
