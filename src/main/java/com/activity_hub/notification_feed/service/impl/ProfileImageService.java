package com.activity_hub.notification_feed.service.impl;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class ProfileImageService {

    private final S3Presigner s3Presigner;
    private final String bucketName = "voxa-profile-bucket";

    public ProfileImageService(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public String generatePresignedUploadUrl(String userId, String imageType, String contentType) {

        String objectKey = String.format("profiles/%s/%s_%s", userId, imageType, UUID.randomUUID());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }
}