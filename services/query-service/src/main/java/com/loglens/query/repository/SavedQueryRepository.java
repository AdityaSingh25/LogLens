package com.loglens.query.repository;

import com.loglens.query.domain.SavedQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavedQueryRepository extends JpaRepository<SavedQuery, UUID> {
    List<SavedQuery> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
