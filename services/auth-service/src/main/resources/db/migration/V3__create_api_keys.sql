CREATE TABLE api_keys (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_hash   VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL,
    tenant_id  UUID NOT NULL REFERENCES tenants(id),
    user_id    UUID NOT NULL REFERENCES users(id),
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_api_keys_key_hash  ON api_keys(key_hash);
CREATE INDEX idx_api_keys_tenant_id ON api_keys(tenant_id);
