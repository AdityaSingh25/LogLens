package com.loglens.auth.service;

import com.loglens.auth.api.dto.LoginRequest;
import com.loglens.auth.api.dto.LoginResponse;
import com.loglens.auth.api.dto.UserInfoResponse;
import com.loglens.auth.domain.Role;
import com.loglens.auth.domain.User;
import com.loglens.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .expiresIn(3600L)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        var claims = jwtService.validateToken(refreshToken);
        String userId = (String) claims.get("sub");
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .expiresIn(3600L)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public User register(String email, String password, String tenantId) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setTenantId(UUID.fromString(tenantId));
        user.setRoles(List.of(Role.ADMIN));
        return userRepository.save(user);
    }

    @Override
    public UserInfoResponse getMe(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserInfoResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .tenantId(user.getTenantId().toString())
                .roles(user.getRoles().stream().map(Enum::name).toList())
                .build();
    }
}
