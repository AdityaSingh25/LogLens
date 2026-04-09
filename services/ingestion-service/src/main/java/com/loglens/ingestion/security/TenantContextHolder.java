package com.loglens.ingestion.security;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public class TenantContextHolder {

    private TenantContextHolder() {}

    public static Mono<String> getTenantId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> jwt.getClaimAsString("tenant_id"));
    }
}
