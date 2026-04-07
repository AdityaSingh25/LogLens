package com.loglens.auth.api;

import com.loglens.auth.api.dto.TenantCreateRequest;
import com.loglens.auth.domain.Tenant;
import com.loglens.auth.repository.TenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRepository tenantRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody TenantCreateRequest request) {
        if (tenantRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant name already taken"));
        }
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        Tenant saved = tenantRepository.save(tenant);
        return ResponseEntity.ok(Map.of(
                "tenant_id", saved.getId().toString(),
                "name", saved.getName()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> get(@PathVariable UUID id) {
        return tenantRepository.findById(id)
                .map(t -> ResponseEntity.ok(Map.of(
                        "tenant_id", t.getId().toString(),
                        "name", t.getName(),
                        "created_at", t.getCreatedAt().toString()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
