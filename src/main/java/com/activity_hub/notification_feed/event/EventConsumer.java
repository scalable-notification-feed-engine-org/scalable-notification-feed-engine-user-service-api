package com.activity_hub.notification_feed.event;

import com.activity_hub.notification_feed.dto.event.EventTypes;
import com.activity_hub.notification_feed.dto.event.FollowEvent;
import com.activity_hub.notification_feed.service.UserStatsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventConsumer {

    private final UserStatsService userStatsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = EventTypes.USER_FOLLOW,groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFollowEvent(String message) {
        log.info("Received message: {}", message);

        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            JsonNode dataNode = jsonNode.get("data");

            FollowEvent event = objectMapper.treeToValue(dataNode, FollowEvent.class);

            userStatsService.updateStats(event.getFollowerId(), event.getFolloweeId());

        }catch (Exception e) {

            log.error("Error updating stats for event: {}", message, e);

        }
    }
}
