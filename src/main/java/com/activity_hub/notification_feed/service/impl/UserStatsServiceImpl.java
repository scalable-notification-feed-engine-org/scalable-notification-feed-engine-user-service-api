package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.entity.UserStats;
import com.activity_hub.notification_feed.enums.FollowType;
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

    @Transactional
    public void updateStats(UUID followerId, UUID followeeId, FollowType followType){
        if(followType.equals(FollowType.FOLLOW)){
            ensureUserStatsExists(followerId);
            ensureUserStatsExists(followeeId);
            userStatsRepository.incrementFollowingCount(followerId);
            userStatsRepository.incrementFollowerCount(followeeId);

        }else if(followType.equals(FollowType.UNFOLLOW)){
            userStatsRepository.decrementFollowingCount(followerId);
            userStatsRepository.decrementFollowerCount(followeeId);
        }
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