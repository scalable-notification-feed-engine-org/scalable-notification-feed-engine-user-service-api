package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.dto.event.FollowEvent;
import com.activity_hub.notification_feed.entity.Follow;
import com.activity_hub.notification_feed.entity.FollowId;
import com.activity_hub.notification_feed.entity.OutboxEvent;
import com.activity_hub.notification_feed.entity.User;
import com.activity_hub.notification_feed.enums.FollowType;
import com.activity_hub.notification_feed.exception.BadRequestException;
import com.activity_hub.notification_feed.exception.NotFoundException;
import com.activity_hub.notification_feed.repository.FollowRepository;
import com.activity_hub.notification_feed.repository.OutboxRepository;
import com.activity_hub.notification_feed.repository.UserRepository;
import com.activity_hub.notification_feed.service.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void followUser(UUID followerId, UUID followeeId) {

        validateUsers(followerId, followeeId);

        Optional<Follow> selectedFollow = followRepository
                .findByIdFollowerIdAndIdFolloweeId(followerId, followeeId);

        Follow followRecord;

        if(selectedFollow.isPresent()) {

            followRecord = selectedFollow.get();

            if(followRecord.getFollowType() == FollowType.FOLLOW) {
                throw new BadRequestException("Follow already exists");
            }

            followRecord.setFollowType(FollowType.FOLLOW);
        }else {
            followRecord = Follow.builder()
                    .id(FollowId.builder()
                            .followerId(followerId)
                            .followeeId(followeeId)
                            .build())
                    .followType(FollowType.FOLLOW)
                    .build();

        }
        Follow savedFollow = followRepository.save(followRecord);

        saveToOutbox(savedFollow,FollowType.FOLLOW);

    }

    @Override
    @Transactional
    public void unfollowUser(UUID followerId, UUID followeeId) {

        validateUsers(followerId, followeeId);

        Follow selectedFollow = followRepository
                .findByIdFollowerIdAndIdFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new NotFoundException("Follow not found"));


        if(selectedFollow.getFollowType() == FollowType.UNFOLLOW) {
            throw new BadRequestException("Already unfollowed this user");
        }

        System.out.println("Unfollowing user " + selectedFollow.getFollowType());

        selectedFollow.setFollowType(FollowType.UNFOLLOW);
        Follow follow = followRepository.saveAndFlush(selectedFollow);
        System.out.println("Status after update: " + follow.getFollowType());

        saveToOutbox(follow,FollowType.UNFOLLOW);

    }

    private void validateUsers(UUID followerId, UUID followeeId){
        if(followerId.equals(followeeId)) {
            throw new BadRequestException("User cannot follow/unfollow themselves");
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
    }

    private void saveToOutbox(Follow follow, FollowType eventType){
        try {
            FollowEvent followEvent = FollowEvent.builder()
                    .followerId(follow.getId().getFollowerId())
                    .followeeId(follow.getId().getFolloweeId())
                    .followType(eventType)
                    .build();

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(eventType.toString())
                    .payload(objectMapper.writeValueAsString(followEvent))
                    .followType(eventType)
                    .status("PENDING")
                    .build();

            outboxRepository.save(outboxEvent);

        }catch (Exception e){
            log.error("Failed to publish follow created event", e);
            throw new RuntimeException("Event publishing failed. Rolling back transaction.");
        }
    }
}
