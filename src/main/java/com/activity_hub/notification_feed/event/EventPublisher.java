package com.activity_hub.notification_feed.event;

import com.activity_hub.notification_feed.dto.event.*;
import com.activity_hub.notification_feed.enums.FollowType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserSendOtp(UserSendOtpEvent user) throws ExecutionException, InterruptedException {
        KafkaEvent<UserRegisteredData> event = new KafkaEvent<>(
                EventTypes.USER_OTP,
                LocalDateTime.now(),
                new UserRegisteredData(
                        user.getUser_id(),
                        user.getEmail(),
                        user.getFirst_name(),
                        user.getLast_name(),
                        String.valueOf(user.getOtp())
                )
        );

        sendEvent(user.getUser_id(),event,EventTypes.USER_OTP);
        log.info("User event published successfully");

    }

    public void publishFollowUser(FollowEvent follow, FollowType followType) throws ExecutionException, InterruptedException {
        KafkaEvent<FollowEvent> event = new KafkaEvent<>(
                followType.toString(),
                LocalDateTime.now(),
                new FollowEvent(
                        follow.getFollowerId(),
                        follow.getFolloweeId(),
                        followType
                )
        );

        sendEvent((follow.getFollowerId().toString()+follow.getFolloweeId().toString()),event,
                follow.getFollowType().equals(FollowType.FOLLOW)?EventTypes.USER_FOLLOW:EventTypes.USER_UNFOLLOW);
        log.info("Follow event published successfully");
    }



    private void sendEvent(
            String key,
            Object event,
            String topic
    ) throws ExecutionException, InterruptedException {

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(topic, key, event);



        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "Event sent | topic={} | partition={} | offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("Failed to send event to topic={}", topic, ex);
            }
        }).get();
    }

}
