package com.loglens.auth.service;

import com.loglens.auth.api.dto.LoginRequest;
import com.loglens.auth.api.dto.LoginResponse;
import com.loglens.auth.api.dto.UserInfoResponse;
import com.loglens.auth.domain.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(String refreshToken);
    User register(String email, String password, String tenantId);
    UserInfoResponse getMe(String userId);
}
