# LogLens Architecture

## System Overview

LogLens is a polyglot microservices system for semantic log search and observability. It ingests logs at high throughput, indexes them for keyword and vector search, translates natural language queries into hybrid searches, detects anomalies, and fans out alerts.

```
Clients (SDK / REST)
        │
        ▼
ingestion-service (Spring WebFlux, :8082)
        │ Kafka: raw-logs
        ▼
parser-service (Spring, :8083) ──► Elasticsearch (logs-{tenant}-{date})
        │ Kafka: parsed-logs
        ├──────────────────────────────────────┐
        ▼                                      ▼
embedding-service (Node.js)        alerting-service (Spring, :8085)
   OpenAI embeddings                  z-score anomaly detection
   Qdrant upsert                      Kafka: alert-events
                                              │
                                              ▼
                                    notification-service (Node.js)
                                      Slack / email / webhook

query-service (Spring, :8084)
   NL → ES DSL (LLM few-shot)
   Parallel ES + Qdrant search
   RRF merge → ranked results

auth-service (Spring, :8081)
   JWT RS256, JWKS endpoint
   API key management
```

---

## Data Flow: Log Ingestion

1. Client sends `POST /ingest/logs` with API key or JWT
2. `ingestion-service` validates, assigns UUID, publishes `RawLogMessage` to `raw-logs` topic
   - Partition key: `{tenant_id}:{service_name}` — preserves per-service ordering
3. `parser-service` consumes `raw-logs`:
   - Detects format (JSON/logfmt/plaintext) and extracts structured fields
   - Indexes to Elasticsearch: `logs-{tenant_id}-{yyyy.MM.dd}`
   - Publishes `ParsedLogMessage` to `parsed-logs`
4. `embedding-service` consumes `parsed-logs`:
   - Batches up to 100 messages or 500ms window
   - Calls OpenAI `text-embedding-3-small`
   - Upserts to Qdrant collection `logs-{tenant_id}`
   - **Best-effort**: if OpenAI is unavailable, keyword search still works

Logs are searchable in Elasticsearch within ~1 second. Semantic search catches up within ~1 minute.

---

## Data Flow: Query Path

1. Client sends `POST /query/search` with natural language query + time range
2. `query-service` translates query using LLM few-shot prompt → `{keywords, level, service_name}`
3. Parallel execution:
   - ES search: `multi_match` on `message` + `normalized_message`, filtered by level/service/time
   - Qdrant search: embed query → cosine similarity search with `tenant_id` payload filter
4. Results merged via **Reciprocal Rank Fusion**: `score = Σ 1/(60 + rank_i)`
5. Paginated response returned; result cached in Redis for 60s

If the LLM translation fails, falls back to keyword-only search. If Qdrant is unavailable, returns keyword results with `query_mode: KEYWORD`.

---

## Data Flow: Alerting

1. `alerting-service` consumes `parsed-logs` to count log volume per `tenant:service` per window
2. `@Scheduled` evaluation every 60 seconds:
   - **Z-score rules**: computes mean + stddev over last 20 windows; fires if `z > 3.0`
   - **Threshold rules**: fires if current window count exceeds configured threshold
   - Guard: no alert if `N < 3` windows (insufficient history)
3. On firing: persists `AlertFiring`, publishes `AlertEvent` to `alert-events` topic
4. `notification-service` consumes `alert-events`, routes to:
   - `slack:{channelId}` → Slack Block Kit message
   - `email:{address}` → nodemailer SMTP
   - `webhook:{url}` → HTTP POST with 3x retry, 10s timeout

---

## ADR-001: Kafka Partitioning Strategy

**Decision:** Partition key = `{tenant_id}:{service_name}`

**Why:** Log order within a single service needs to be preserved (log #3 must not be indexed before log #1). Cross-service ordering doesn't matter. This partition key gives us per-service ordering while allowing horizontal consumer scaling across services and tenants.

**Alternative considered:** Partition by `tenant_id` only — would serialize all services for a tenant onto one partition, limiting parallelism. Partition by `log_id` — no ordering guarantees at all.

---

## ADR-002: Hybrid Search over Pure Vector

**Decision:** ES keyword + Qdrant vector in parallel, merged with Reciprocal Rank Fusion (k=60).

**Why:** Pure vector search is slow (~200-400ms) and performs poorly on exact-match queries (trace IDs, user IDs, specific error codes). Pure keyword search misses semantically similar errors with different wording. Hybrid gets the best of both: exact matches stay fast, semantic matches get smart.

**Alternative considered:** Re-rank with a cross-encoder after pure vector retrieval — adds 300-600ms latency per query, not acceptable for interactive search.

---

## ADR-003: Async Embeddings — Intentional Lossy Pipeline

**Decision:** Embedding generation never blocks ingestion. If the embedding service lags or fails, keyword search still works.

**Why:** Availability of the ingestion path is critical. A downed OpenAI API or overloaded embedding service must not drop logs. The tradeoff is that semantic search may be seconds to a minute behind — acceptable for this use case.

**Alternative considered:** Synchronous embedding in ingestion-service — creates tight coupling, makes ingestion latency dependent on OpenAI API latency (~50-200ms per batch). Rejected.

---

## ADR-004: Per-Tenant Elasticsearch Indices

**Decision:** Each tenant gets daily indices: `logs-{tenant_id}-{yyyy.MM.dd}`

**Why:** Shared indices cause noisy-neighbor query performance problems. A tenant running an expensive aggregation query can slow down all other tenants. Per-tenant indices also make data retention trivially easy (delete old indices) and allow different shard counts for large vs small tenants.

**Alternative considered:** Shared index with `tenant_id` filter on every query — simpler operationally but creates noisy-neighbor risk and complicates query authorization enforcement.

---

## ADR-005: Z-Score Anomaly Detection Before Semantic Clustering

**Decision:** v1 uses statistical z-score on log volume per service window. Semantic cluster-drift detection is v2.

**Why:** Z-score is cheap (O(N) per window, runs in-memory), fires in milliseconds, and catches the most common alert case: "something is generating way more errors than usual." Semantic cluster-drift (detecting when a *new type* of error never seen before appears) requires embedding comparison across cluster centroids — higher value but higher cost and complexity. Shipping z-score first gets 80% of the value immediately.
