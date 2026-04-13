package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.entity.UserStats;
import com.activity_hub.notification_feed.repository.FollowRepository;
import com.activity_hub.notification_feed.repository.UserRepository;
import com.activity_hub.notification_feed.repository.UserStatusRepository;
import com.activity_hub.notification_feed.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserStatusRepository userStatsRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateStats(UUID followerId, UUID followeeId) {

        ensureUserStatsExists(followerId);

        ensureUserStatsExists(followeeId);

        userStatsRepository.incrementFollowingCount(followerId);

        userStatsRepository.incrementFollowerCount(followeeId);
    }

    private void ensureUserStatsExists(UUID userId) {
        if (!userStatsRepository.existsById(userId)) {

            UserStats newStats = UserStats.builder()
                    .userId(userId)
                    .followerCount(0)
                    .followingCount(0)
                    .build();
            userStatsRepository.saveAndFlush(newStats);
        }

    }

}