package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.dto.request.LoginRequestDto;
import com.activity_hub.notification_feed.dto.request.PasswordRequestDto;
import com.activity_hub.notification_feed.dto.request.UserRequestDto;
import com.activity_hub.notification_feed.dto.request.UserUpdateRequestDto;
import com.activity_hub.notification_feed.dto.response.LoginResponseDto;
import com.activity_hub.notification_feed.dto.response.UserResponseDto;
import com.activity_hub.notification_feed.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public void createUser(UserRequestDto dto) {

    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        return null;
    }

    @Override
    public void resend(String email, String type) {

    }

    @Override
    public void forgotPasswordSendVerificationCode(String email) {

    }

    @Override
    public boolean verifyReset(String otp, String email) {
        return false;
    }

    @Override
    public boolean passwordReset(PasswordRequestDto dto) {
        return false;
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        return false;
    }

    @Override
    public void updateUserDetails(String email, UserUpdateRequestDto data) {

    }

    @Override
    public UserResponseDto getUserDetails(String email) {
        return null;
    }
}
