package com.loglens.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
@Getter @Setter @NoArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID userId;

    private Instant expiresAt;

    private Instant revokedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public boolean isValid() {
        if (revokedAt != null) return false;
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) return false;
        return true;
    }
}
