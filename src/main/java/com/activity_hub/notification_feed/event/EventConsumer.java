package com.activity_hub.notification_feed.event;

import com.activity_hub.notification_feed.dto.event.EventTypes;
import com.activity_hub.notification_feed.dto.event.FollowEvent;
import com.activity_hub.notification_feed.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventConsumer {

    private final UserStatsService userStatsService;

    @KafkaListener(topics = EventTypes.USER_FOLLOW,groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFollowEvent(FollowEvent event) {
        log.info("Received follow event: {} follows {}", event.getFollowerId(), event.getFolloweeId());

        try {

            userStatsService.updateStats(event.getFollowerId(), event.getFolloweeId());

        }catch (Exception e) {

            log.error("Error updating stats for event: {}", event, e);

        }
    }
}
