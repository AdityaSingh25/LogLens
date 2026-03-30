# LogLens API Reference

All authenticated endpoints require `Authorization: Bearer <JWT>` header.  
Get a JWT by logging in via `POST /auth/login`.

---

## Auth Service — port 8081

### POST /auth/tenants
Create a new tenant (no auth required).
```json
Request:  { "name": "acme-corp" }
Response: { "tenant_id": "uuid", "name": "acme-corp" }
```

### POST /auth/register
Register a new user (no auth required).
```json
Request:  { "email": "admin@acme.com", "password": "...", "tenantId": "uuid" }
Response: { "userId": "uuid", "email": "...", "tenantId": "uuid", "roles": ["ADMIN"] }
```

### POST /auth/login
```json
Request:  { "email": "admin@acme.com", "password": "..." }
Response: { "accessToken": "eyJ...", "refreshToken": "eyJ...", "expiresIn": 3600, "tokenType": "Bearer" }
```

### POST /auth/refresh
```
Header:   X-Refresh-Token: <refresh_token>
Response: { "accessToken": "...", "refreshToken": "...", ... }
```

### GET /auth/me
Returns the current user's profile.

### GET /auth/.well-known/jwks.json
Returns the RSA public key as a JWKS document. Used by all other services for JWT validation.

### POST /auth/api-keys
Create an API key (returned once only).
```json
Request:  { "name": "production-ingest", "expiresAt": "2025-12-31T00:00:00Z" }
Response: { "api_key": "ll_...", "message": "Store this key securely — it will not be shown again" }
```

### DELETE /auth/api-keys/{id}
Revoke an API key.

---

## Ingestion Service — port 8082

### POST /ingest/logs
Ingest a single log entry.
```json
Request:
{
  "serviceName": "payment-service",
  "level": "ERROR",
  "message": "Payment gateway timeout after 30s",
  "timestamp": "2024-01-15T10:30:00Z",  // optional, defaults to now
  "metadata": { "order_id": "ord_123", "gateway": "stripe" },
  "traceId": "abc123"  // optional
}

Response: { "logId": "uuid", "acceptedAt": "2024-01-15T10:30:00.123Z" }
```

### POST /ingest/logs/batch
Ingest up to 1000 logs in a single request.
```json
Request:  { "logs": [ ...IngestLogRequest ] }
Response: { "accepted": 100, "rejected": 0, "logIds": ["uuid", ...] }
```

### WebSocket /ws/logs?token=JWT
Connect to receive a real-time stream of all logs for your tenant.  
Messages are JSON-encoded `LogEntry` objects pushed as logs are ingested.

---

## Query Service — port 8084

### POST /query/search
Search logs using natural language or keyword queries.
```json
Request:
{
  "query": "payment gateway errors in the last hour",
  "timeRange": { "from": "2024-01-15T09:00:00Z", "to": "2024-01-15T10:00:00Z" },
  "filters": { "services": ["payment-service"], "levels": ["ERROR", "FATAL"] },
  "page": 0,
  "pageSize": 20
}

Response:
{
  "results": [
    {
      "logId": "uuid",
      "serviceName": "payment-service",
      "timestamp": "2024-01-15T09:45:12Z",
      "level": "ERROR",
      "message": "Payment gateway timeout after 30s",
      "score": 0.0312,
      "keywordScore": 1.82,
      "vectorScore": 0.94
    }
  ],
  "total": 47,
  "tookMs": 83,
  "queryMode": "HYBRID"
}
```

`queryMode` values: `HYBRID` (ES + Qdrant merged), `KEYWORD` (ES only, Qdrant unavailable), `VECTOR` (Qdrant only, ES returned no results).

### GET /query/saved-queries
List saved queries for the current tenant.

### POST /query/saved-queries
Save a query for later reuse.
```json
Request: { "name": "Payment errors", "query": "payment gateway errors" }
```

### DELETE /query/saved-queries/{id}

---

## Alerting Service — port 8085

### GET /alerting/rules
List all alert rules for the current tenant.

### POST /alerting/rules
Create an alert rule.
```json
Request:
{
  "name": "Payment service anomaly",
  "condition": {
    "type": "ZSCORE",
    "metric": "LOG_VOLUME",
    "windowMinutes": 5,
    "serviceName": "payment-service"
  },
  "severity": "HIGH",
  "notificationChannels": ["slack:C1234567", "email:oncall@acme.com"]
}
```

`condition.type` values:
- `ZSCORE` — fires when log volume z-score > 3.0 (anomaly detection)
- `THRESHOLD` — fires when log volume exceeds `condition.threshold` in the window

### PATCH /alerting/rules/{id}
Toggle a rule on or off.
```json
Request: { "enabled": false }
```

### DELETE /alerting/rules/{id}

---

## Notification Channels

Channels are strings with a prefix that determines the delivery method:

| Prefix | Example | Delivery |
|---|---|---|
| `slack:` | `slack:C1234567` | Slack Block Kit message to channel ID |
| `email:` | `email:user@example.com` | SMTP email |
| `webhook:` | `webhook:https://hooks.example.com/...` | HTTP POST, JSON body, 3x retry |
