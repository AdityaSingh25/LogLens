package com.loglens.auth.service;

import com.loglens.auth.domain.User;

import java.util.Map;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    Map<String, Object> validateToken(String token);
    String getPublicKeyAsJwks();
}
