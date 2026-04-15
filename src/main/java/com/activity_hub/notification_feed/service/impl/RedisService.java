package com.activity_hub.notification_feed.service.impl;
import com.activity_hub.notification_feed.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final OtpGenerator otpGenerator;

    public String saveOtp(String email) {
        String otp = otpGenerator.generateOtp(5);

        redisTemplate.opsForValue().setIfAbsent(email, otp, 15 ,TimeUnit.MINUTES);

        return otp;
    }

    public String getValue(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public boolean verifyAndDeleteOtp(String email, String userOtp) {

        String storedOtp = redisTemplate.opsForValue().get(email);

        if (storedOtp != null && storedOtp.equals(userOtp)) {
            redisTemplate.delete(email);
            return true;
        }

        return false;
    }

}
