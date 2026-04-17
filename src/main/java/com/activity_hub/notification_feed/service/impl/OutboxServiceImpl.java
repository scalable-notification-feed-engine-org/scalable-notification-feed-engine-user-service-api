package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.entity.OutboxEvent;
import com.activity_hub.notification_feed.repository.OutboxRepository;
import com.activity_hub.notification_feed.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {
    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(OutboxEvent event, String status) {
        event.setStatus(status);
        outboxRepository.save(event);
    }
}