package com.activity_hub.notification_feed.repository;

import com.activity_hub.notification_feed.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(String status);
}