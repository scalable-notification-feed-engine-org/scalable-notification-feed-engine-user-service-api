package com.activity_hub.notification_feed.dto.request;

import lombok.Data;

@Data
public class PresignedUrlRequestDto {
    private String imageType;
    private String contentType;
}