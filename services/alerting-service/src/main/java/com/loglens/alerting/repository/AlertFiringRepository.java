package com.loglens.alerting.repository;

import com.loglens.alerting.domain.AlertFiring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertFiringRepository extends JpaRepository<AlertFiring, UUID> {
    List<AlertFiring> findByTenantIdOrderByFiredAtDesc(UUID tenantId);
}
