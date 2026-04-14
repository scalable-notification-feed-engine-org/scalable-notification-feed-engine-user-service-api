package com.activity_hub.notification_feed.service.internal;

import com.activity_hub.notification_feed.dto.response.UserMetadataResponseDTO;
import com.activity_hub.notification_feed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InternalUserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private static final String CACHE_KEY_PREFIX = "user_metadata:";

    @Transactional(readOnly = true)
    public Map<UUID, UserMetadataResponseDTO> getUsersMetadata(List<UUID> ids) {

        Map<UUID, UserMetadataResponseDTO> resultMap = new HashMap<>();
        List<UUID> missingIds = new ArrayList<>();

        for (UUID id : ids) {
            UserMetadataResponseDTO cachedData = (UserMetadataResponseDTO) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + id);

            if (cachedData != null) {
                resultMap.put(id, cachedData);
            }else {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            List<UserMetadataResponseDTO> dbData = userRepository.findAllMetadataByIds(missingIds);
            for (UserMetadataResponseDTO dto : dbData) {
                resultMap.put(dto.userId(),dto);
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + dto.userId(),dto, Duration.ofHours(24));
            }
        }

        return resultMap;
    }

}
