package com.activity_hub.notification_feed.service.internal;

import com.activity_hub.notification_feed.dto.response.UserMetadataResponseDTO;
import com.activity_hub.notification_feed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternalUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<UUID, UserMetadataResponseDTO> getUsersMetadata(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllMetadataByIds(ids).
                stream()
                .collect(Collectors.
                        toMap(UserMetadataResponseDTO::userId,dto -> dto));
    }

}
