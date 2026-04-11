package com.activity_hub.notification_feed.event;

import com.activity_hub.notification_feed.dto.event.KafkaEvent;
import com.activity_hub.notification_feed.dto.event.UserEventTypes;
import com.activity_hub.notification_feed.dto.event.UserRegisteredData;
import com.activity_hub.notification_feed.dto.event.UserSendOtpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserSendOtp(UserSendOtpEvent user) {
        KafkaEvent<UserRegisteredData> event = new KafkaEvent<>(
                UserEventTypes.USER_OTP,
                LocalDateTime.now(),
                new UserRegisteredData(
                        user.getUser_id(),
                        user.getEmail(),
                        user.getFirst_name(),
                        user.getLast_name(),
                        String.valueOf(user.getOtp())
                )
        );

        sendEvent(user.getUser_id(),  event);

    }

    private void sendEvent(
            String key,
            Object event
    ) {

        ProducerRecord<String, Object> record =
                new ProducerRecord<>("otp-send", key, event);



        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "Event sent | topic={} | partition={} | offset={}",
                        "otp-send",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("Failed to send event to topic={}", "otp-send", ex);
            }
        });
    }

}
