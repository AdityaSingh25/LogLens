CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    tenant_id     UUID NOT NULL REFERENCES tenants(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE INDEX idx_users_email     ON users(email);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
