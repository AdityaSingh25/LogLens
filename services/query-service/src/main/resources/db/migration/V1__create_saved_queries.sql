CREATE TABLE saved_queries (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL,
    user_id     UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    query_text  TEXT NOT NULL,
    filters     JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_saved_queries_tenant_id ON saved_queries(tenant_id);
