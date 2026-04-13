package com.activity_hub.notification_feed.scheduler;

import com.activity_hub.notification_feed.dto.event.FollowEvent;
import com.activity_hub.notification_feed.entity.OutboxEvent;
import com.activity_hub.notification_feed.event.EventPublisher;
import com.activity_hub.notification_feed.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutboxEvents(){
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        if(pendingEvents.isEmpty()){
            return;
        }

        log.info("Processing {} outbox events...", pendingEvents.size());

        pendingEvents.forEach(outboxEvent -> {
            try {
                FollowEvent followEvent = objectMapper.readValue(outboxEvent.getPayload(), FollowEvent.class);
                eventPublisher.publishFollowUser(followEvent,outboxEvent.getFollowType());
                outboxEvent.setStatus("PROCESSED");
                outboxEvent.setFollowType(followEvent.getFollowType());
                outboxRepository.save(outboxEvent);
            } catch (Exception e) {
                log.error("Failed to process outbox event ID: {}. Error: {}", outboxEvent.getId(), e.getMessage());
            }
        });

    }
}
