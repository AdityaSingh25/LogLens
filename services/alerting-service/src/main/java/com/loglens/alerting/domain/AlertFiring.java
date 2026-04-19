package com.loglens.alerting.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "alert_firings")
@Getter @Setter @NoArgsConstructor
public class AlertFiring {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ruleId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false)
    private Instant firedAt = Instant.now();

    private Instant resolvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> context;
}
