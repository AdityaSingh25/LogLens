package com.loglens.auth.service;

import com.loglens.auth.domain.Role;
import com.loglens.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtServiceImpl jwtService;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl(3600L, 86400L);
        testUser = new User();
        testUser.setId(UUID.randomUUID()); // set via reflection workaround
        testUser.setEmail("test@example.com");
        testUser.setTenantId(UUID.randomUUID());
        testUser.setRoles(List.of(Role.ADMIN));
    }

    @Test
    void generateAndValidateAccessToken() {
        // Use reflection to set id since it's normally set by JPA
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testUser, UUID.randomUUID());
        } catch (Exception ignored) {}

        String token = jwtService.generateAccessToken(testUser);
        assertThat(token).isNotBlank();

        Map<String, Object> claims = jwtService.validateToken(token);
        assertThat(claims.get("tenant_id")).isEqualTo(testUser.getTenantId().toString());
    }

    @Test
    void invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtService.validateToken("not.a.valid.token"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void jwksEndpoint_returnsPublicKey() {
        String jwks = jwtService.getPublicKeyAsJwks();
        assertThat(jwks).contains("\"kty\":\"RSA\"");
        assertThat(jwks).contains("\"use\":\"sig\"");
        assertThat(jwks).doesNotContain("\"d\":"); // private key must not be exposed
    }
}
