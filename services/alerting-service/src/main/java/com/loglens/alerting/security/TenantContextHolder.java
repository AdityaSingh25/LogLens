package com.loglens.alerting.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class TenantContextHolder {

    private TenantContextHolder() {}

    public static String getTenantId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("tenant_id");
        }
        throw new IllegalStateException("No JWT in security context");
    }
}
