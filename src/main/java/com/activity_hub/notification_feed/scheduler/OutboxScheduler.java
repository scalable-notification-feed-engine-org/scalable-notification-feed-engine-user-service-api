package com.activity_hub.notification_feed.scheduler;

import com.activity_hub.notification_feed.dto.event.FollowEvent;
import com.activity_hub.notification_feed.entity.OutboxEvent;
import com.activity_hub.notification_feed.event.EventPublisher;
import com.activity_hub.notification_feed.repository.OutboxRepository;
import com.activity_hub.notification_feed.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;


    @Scheduled(fixedRate = 2000)
    public void processOutboxEvents() {

        List<OutboxEvent> events = outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            processSingleEvent(event);
        }
    }

    private void processSingleEvent(OutboxEvent event) {
        try {

            FollowEvent followEvent = objectMapper.readValue(event.getPayload(), FollowEvent.class);

            eventPublisher.publishFollowUser(followEvent, event.getFollowType());

            outboxService.updateStatus(event, "PROCESSED");

            log.info("Successfully published event ID: {}", event.getId());

        } catch (Exception e) {
            log.error("Failed to process outbox event ID: {}", event.getId(), e);

            outboxService.updateStatus(event, "FAILED");
        }
    }

}
