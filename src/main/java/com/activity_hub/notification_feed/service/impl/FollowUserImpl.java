package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.dto.event.FollowEvent;
import com.activity_hub.notification_feed.entity.Follow;
import com.activity_hub.notification_feed.entity.FollowId;
import com.activity_hub.notification_feed.entity.User;
import com.activity_hub.notification_feed.event.EventPublisher;
import com.activity_hub.notification_feed.exception.BadRequestException;
import com.activity_hub.notification_feed.exception.NotFoundException;
import com.activity_hub.notification_feed.repository.FollowRepository;
import com.activity_hub.notification_feed.repository.UserRepository;
import com.activity_hub.notification_feed.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowUserImpl implements FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void followUser(UUID followerId, UUID followeeId) {

        if(followerId == followeeId) {
            throw new BadRequestException("Cannot follow both own user..");
        }

        List<UUID> ids = List.of(followerId, followeeId);

        List<User> selectUsers = userRepository.findAllById(ids);

        if(selectUsers.size() < 2) {
          throw new NotFoundException("users not found");
        }

        selectUsers.forEach(user -> {
            if (user.getId().equals(followerId)) {
                if(!user.isActive()){
                    throw new BadRequestException("Follower is not active");
                }
            }else {
                if(!user.isActive()){
                  throw new BadRequestException("Followee is not active");
                }
            }
        });

        Optional<Follow> selectedFollow = followRepository
                .findByIdFollowerIdAndIdFolloweeId(followerId, followeeId);

        if(selectedFollow.isPresent()) {
            throw new BadRequestException("Follow already exists");
        }

        Follow follow = Follow.builder()
                .id(FollowId.builder()
                        .followerId(followerId)
                        .followeeId(followeeId)
                        .build())
                .build();

        Follow save = followRepository.save(follow);

        try {
            FollowEvent followEvent = FollowEvent.builder()
                    .followerId(save.getId().getFollowerId())
                    .followeeId(save.getId().getFolloweeId())
                    .build();
            eventPublisher.publishFollowUser(followEvent);

        }catch (Exception e){

            log.error("Failed to publish follow created event", e);

        }

    }
}
