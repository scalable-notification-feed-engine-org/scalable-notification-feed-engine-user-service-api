package com.activity_hub.notification_feed.api;

import com.activity_hub.notification_feed.dto.request.PresignedUrlRequestDto;
import com.activity_hub.notification_feed.dto.response.PresignedUrlResponseDto;
import com.activity_hub.notification_feed.service.impl.ProfileImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user-service/api/v1/profiles/media")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    public ProfileImageController(ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponseDto> getUploadUrl(
            @RequestHeader("X-User-Id") String loggedInUserId,
            @RequestBody PresignedUrlRequestDto request) {

        String objectKey = String.format("profiles/%s/%s_%s", 
                loggedInUserId, request.getImageType(), UUID.randomUUID());

        String uploadUrl = profileImageService.generatePresignedUploadUrl(
                loggedInUserId, 
                request.getImageType(), 
                request.getContentType()
        );

        return ResponseEntity.ok(new PresignedUrlResponseDto(uploadUrl, objectKey));
    }
}