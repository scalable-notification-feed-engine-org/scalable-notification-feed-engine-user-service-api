package com.activity_hub.notification_feed.api.internal;

import com.activity_hub.notification_feed.dto.response.UserMetadataResponseDTO;
import com.activity_hub.notification_feed.service.internal.InternalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final InternalUserService internalUserService;

    @PostMapping("/metadata")
    public Map<UUID, UserMetadataResponseDTO> getUsersMetadata(@RequestBody List<UUID> ids) {
        return internalUserService.getUsersMetadata(ids);
    }

}
