package com.loglens.query.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loglens.query.api.dto.SearchRequest;
import com.loglens.query.api.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${query.cache.ttl-seconds:60}")
    private long ttlSeconds;

    public String buildKey(String tenantId, SearchRequest request) {
        try {
            String raw = tenantId + ":" + request.getQuery() + ":" +
                         request.getTimeRange().getFrom() + ":" + request.getTimeRange().getTo();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return "query:" + tenantId + ":" + HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "query:" + tenantId + ":" + request.getQuery().hashCode();
        }
    }

    public SearchResponse get(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, SearchResponse.class);
        } catch (Exception e) {
            log.debug("Cache miss or deserialization error for key {}", key);
            return null;
        }
    }

    public void put(String key, SearchResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.debug("Failed to cache response for key {}", key);
        }
    }
}
