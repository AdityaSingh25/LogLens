CREATE TABLE alert_firings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id     UUID NOT NULL REFERENCES alert_rules(id),
    tenant_id   UUID NOT NULL,
    fired_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ,
    context     JSONB
);

CREATE INDEX idx_alert_firings_rule_id   ON alert_firings(rule_id);
CREATE INDEX idx_alert_firings_tenant_id ON alert_firings(tenant_id);
