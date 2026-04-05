package com.loglens.auth.repository;

import com.loglens.auth.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByName(String name);
    boolean existsByName(String name);
}
