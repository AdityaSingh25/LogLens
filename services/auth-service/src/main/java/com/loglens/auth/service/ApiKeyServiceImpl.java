package com.loglens.auth.service;

import com.loglens.auth.domain.ApiKey;
import com.loglens.auth.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String create(UUID tenantId, UUID userId, String name, String expiresAt) {
        byte[] rawBytes = new byte[32];
        secureRandom.nextBytes(rawBytes);
        String plainKey = "ll_" + Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash(encoder.encode(plainKey));
        apiKey.setName(name);
        apiKey.setTenantId(tenantId);
        apiKey.setUserId(userId);
        if (expiresAt != null) {
            apiKey.setExpiresAt(Instant.parse(expiresAt));
        }
        apiKeyRepository.save(apiKey);
        return plainKey;
    }

    @Override
    public List<ApiKey> listActive(UUID tenantId) {
        return apiKeyRepository.findByTenantIdAndRevokedAtIsNull(tenantId);
    }

    @Override
    @Transactional
    public void revoke(UUID keyId, UUID tenantId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .filter(k -> k.getTenantId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));
        key.setRevokedAt(Instant.now());
    }

    @Override
    public ApiKey validate(String plainKey) {
        // Load all active keys for brute-force check — acceptable at this scale
        // In production: index by key prefix for fast lookup
        return apiKeyRepository.findAll().stream()
                .filter(k -> k.isValid() && encoder.matches(plainKey, k.getKeyHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired API key"));
    }
}
