package com.loglens.auth.service;

import com.loglens.auth.domain.ApiKey;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    /** Creates an API key and returns the plain-text key (returned once only). */
    String create(UUID tenantId, UUID userId, String name, String expiresAt);
    List<ApiKey> listActive(UUID tenantId);
    void revoke(UUID keyId, UUID tenantId);
    /** Returns the ApiKey entity if valid, throws otherwise. */
    ApiKey validate(String plainKey);
}
