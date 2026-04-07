package com.loglens.auth.service;

import com.loglens.auth.domain.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final RSAKey rsaKey;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpirySeconds;

    public JwtServiceImpl(
            @Value("${jwt.access-token-expiry-seconds:3600}") long accessTokenExpirySeconds,
            @Value("${jwt.refresh-token-expiry-seconds:86400}") long refreshTokenExpirySeconds
    ) throws Exception {
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
        // Generate RSA key pair on startup (dev mode)
        // In production, load from env via jwt.private-key / jwt.public-key
        this.rsaKey = new RSAKeyGenerator(2048)
                .keyID("loglens-key-1")
                .keyUse(KeyUse.SIGNATURE)
                .generate();
        log.info("RSA key pair generated for JWT signing (dev mode)");
    }

    @Override
    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpirySeconds, "access");
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpirySeconds, "refresh");
    }

    private String buildToken(User user, long expirySeconds, String tokenType) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issuer("loglens-auth")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(expirySeconds)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("tenant_id", user.getTenantId().toString())
                    .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                    .claim("token_type", tokenType)
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                    claims
            );
            jwt.sign(new RSASSASigner(rsaKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new RSASSAVerifier(rsaKey.toPublicJWK()))) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                throw new IllegalArgumentException("JWT expired");
            }
            return claims.toJSONObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage());
        }
    }

    @Override
    public String getPublicKeyAsJwks() {
        try {
            JWKSet jwkSet = new JWKSet(rsaKey.toPublicJWK());
            return jwkSet.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export JWKS", e);
        }
    }
}
