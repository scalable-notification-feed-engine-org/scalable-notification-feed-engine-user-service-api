package com.activity_hub.notification_feed.dto.event;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSendOtpEvent {
    private String user_id;
    private String otp;
    private String email;
    private String first_name;
    private String last_name;
}