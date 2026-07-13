package com.activity_hub.notification_feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ProfileUpdateRequestDto {
    @NotBlank(message = "Name cannot be empty")
    private String name;
    private String aliasName;
    private List<String> bioLines;
    private String category;
    private String location;
    private String avatarImageKey;
    private String coverImageKey;
}