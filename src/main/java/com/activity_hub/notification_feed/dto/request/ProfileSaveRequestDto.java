package com.activity_hub.notification_feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ProfileSaveRequestDto {
    
    @NotBlank(message = "User ID is required")
    private String id;
    @NotBlank(message = "Name is required")
    private String name;
    private String aliasName;
    private List<String> bioLines;
    private String category;
    private String location;
    private String avatarImageKey;
    private String coverImageKey;
}