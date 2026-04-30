package com.example.fullstack.service;

import com.example.fullstack.dto.CaptchaResponse;
import com.example.fullstack.dto.LoginRequest;
import com.example.fullstack.dto.LoginResponse;
import com.example.fullstack.dto.RegisterRequest;
import com.example.fullstack.dto.UserProfileResponse;

public interface AuthService {

    CaptchaResponse generateCaptcha();

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse currentUser();
}
