package com.activity_hub.notification_feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignedUrlResponseDto {
    private String uploadUrl;
    private String objectKey;
}