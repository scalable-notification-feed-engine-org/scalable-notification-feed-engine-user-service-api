package com.activity_hub.notification_feed.service;

import com.activity_hub.notification_feed.dto.request.*;
import com.activity_hub.notification_feed.dto.response.LoginResponseDto;
import com.activity_hub.notification_feed.dto.response.UserResponseDto;
import java.util.List;

public interface UserService {
   void createUser(UserRequestDto dto);
   LoginResponseDto login(LoginRequestDto dto);
   void resend(String email, String type);
   void forgotPasswordSendVerificationCode(String email);
   boolean verifyReset(String otp, String email);
   boolean passwordReset(PasswordRequestDto dto);
   boolean verifyEmail(String otp, String email);
   void updateUserDetails(String email, UserUpdateRequestDto data);
   UserResponseDto getUserDetails(String email);
   List<UserResponseDto> getUserDetailsByIds(List<String> ids);
   List<UserResponseDto> getAllUsers(String searchText);
}