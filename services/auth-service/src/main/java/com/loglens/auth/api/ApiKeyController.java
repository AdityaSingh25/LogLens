package com.loglens.auth.api;

import com.loglens.auth.api.dto.ApiKeyCreateRequest;
import com.loglens.auth.domain.ApiKey;
import com.loglens.auth.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<Map<String, String>> create(
            @Valid @RequestBody ApiKeyCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("X-Tenant-Id") UUID tenantId
    ) {
        String plainKey = apiKeyService.create(
                tenantId,
                UUID.fromString(userDetails.getUsername()),
                request.getName(),
                request.getExpiresAt()
        );
        return ResponseEntity.ok(Map.of(
                "api_key", plainKey,
                "message", "Store this key securely — it will not be shown again"
        ));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> list(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        List<ApiKey> keys = apiKeyService.listActive(tenantId);
        List<Map<String, String>> response = keys.stream()
                .map(k -> Map.of(
                        "key_id", k.getId().toString(),
                        "name", k.getName(),
                        "created_at", k.getCreatedAt().toString()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @RequestHeader("X-Tenant-Id") UUID tenantId) {
        apiKeyService.revoke(id, tenantId);
        return ResponseEntity.noContent().build();
    }
}
