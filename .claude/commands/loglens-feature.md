# LogLens Feature Context

You are working on **LogLens** — a semantic log search and observability platform. Before building any feature, absorb this full context so you understand conventions, patterns, and architecture.

---

## Project Context

This is a **portfolio project** for Aditya, targeting roles at Razorpay, PhonePe, Atlassian, Deutsche Bank. The goal is demonstrating senior-level backend thinking: not just what was built but *why* decisions were made. The "Scale & Design Decisions" section in the README is intentional — always preserve and extend that reasoning style.

---

## What LogLens Does

LogLens ingests structured/unstructured logs at high throughput, indexes them in Elasticsearch for keyword search, generates vector embeddings for semantic search, translates natural language queries into hybrid searches, detects anomalies, and fans out alerts.

The core value prop: search logs in plain English, get alerted on novel error patterns — not just volume spikes.

---

## Monorepo Layout

```
loglens/
├── services/
│   ├── ingestion-service/    # Spring Boot WebFlux — REST/gRPC log intake → Kafka
│   ├── parser-service/       # Spring Boot — Kafka consumer, parses + indexes to ES
│   ├── embedding-service/    # Node.js + TS — Kafka consumer, generates embeddings → Qdrant
│   ├── query-service/        # Spring Boot — NL → hybrid query (ES DSL + vector RRF)
│   ├── alerting-service/     # Spring Boot — alert rules, anomaly detection
│   ├── notification-service/ # Node.js + TS — Slack/email/webhook fan-out
│   └── auth-service/         # Spring Boot — multi-tenant JWT + API key auth
├── contracts/
│   ├── kafka-schemas/        # Shared Avro/JSON schemas for all Kafka topics
│   └── api-types/            # Shared TypeScript types for REST APIs
├── frontend/                 # React 18 + TS + TanStack Query + Tailwind + shadcn/ui
├── infra/
│   ├── docker-compose.yml    # Local data plane: Kafka, Postgres, ES, Qdrant, Redis
│   └── k8s/                  # Kubernetes manifests
└── docs/                     # Architecture, API docs, ADRs
```

---

## Service Responsibilities & Tech

| Service | Stack | Key Responsibility |
|---|---|---|
| `ingestion-service` | Spring Boot 3 + WebFlux | Accept logs via REST/gRPC, validate schema, publish to Kafka |
| `parser-service` | Spring Boot 3 | Consume raw-logs topic, parse JSON/logfmt/plain text, index to ES |
| `embedding-service` | Node.js 20 + TS | Consume parsed-logs topic, call embedding model, write to Qdrant |
| `query-service` | Spring Boot 3 | Translate NL query → ES DSL + vector query, merge via RRF |
| `alerting-service` | Spring Boot 3 | Evaluate alert rules on windows, z-score anomaly detection |
| `notification-service` | Node.js 20 + TS | Subscribe to alert-events, fan out to Slack/email/webhooks |
| `auth-service` | Spring Boot 3 + Spring Security | Multi-tenant auth, JWT issuance, API key management |

---

## Data Stores

- **Kafka** — event backbone; topics: `raw-logs`, `parsed-logs`, `embedding-results`, `alert-events`
- **PostgreSQL 16** — users, orgs, alert rules, saved queries; shared schema, tenant-isolated rows
- **Elasticsearch 8** — per-tenant daily indices: `logs-{tenant_id}-{YYYY.MM.DD}`
- **Qdrant** — per-tenant collections for vector search
- **Redis 7** — query result cache (TTL-based), rate limiting per API key

---

## Key Architecture Decisions

**Kafka partitioning:** `{tenant_id}:{service_name}` — preserves per-service log ordering, enables horizontal consumer scaling.

**Hybrid search:** ES keyword + Qdrant vector in parallel, merged with Reciprocal Rank Fusion (RRF). Never pure-vector.

**Async embeddings:** Ingestion never blocks on embedding. Logs searchable in ES within seconds; semantic search catches up within ~1 min. Graceful degradation if embedding-service lags.

**Anomaly detection layers:** Cheap (z-score on log volume per service per window) runs every window. Expensive (cluster drift detection) runs on a slower cadence.

**Multi-tenancy:** Tenant isolation at the index level (ES) and collection level (Qdrant). Enforced by query-service via JWT claims. Never query across tenant boundaries.

**Auth flow:** Every service validates JWTs issued by auth-service. API keys are hashed and stored in PostgreSQL; auth-service validates and mints short-lived JWTs.

**Distributed tracing:** OpenTelemetry propagated across all service boundaries. `trace_id` extracted and indexed in ES for correlation.

---

## Frontend Stack

React 18, TypeScript, TanStack Query v5, Tailwind CSS, shadcn/ui, Recharts.

Key views:
- **Search bar** — natural language input, sends to query-service `/search`
- **Live log stream** — WebSocket from ingestion-service, filter chips
- **Semantic cluster view** — groups similar errors visually
- **Alert rule builder** — condition preview with live evaluation
- **Dashboards** — log volume, error rates, service health time-series

---

## Contracts (Shared Schemas)

All Kafka message schemas live in `contracts/kafka-schemas/`. All REST API TypeScript types in `contracts/api-types/`. Spring services use the JSON schemas via jackson; Node services import the TS types directly.

**Never duplicate schemas across services** — define once in `contracts/`, import everywhere.

---

## Running Locally

```bash
# Start data plane
docker compose -f infra/docker-compose.yml up -d

# Start all services (check Makefile for individual targets)
make dev

# Frontend
cd frontend && npm install && npm run dev
# UI at http://localhost:5173
```

---

## v1 Scope Decisions (Already Made — Don't Revisit)

- **Embeddings:** Use OpenAI `text-embedding-3-small` via managed API — no self-hosted model
- **NL→query:** LLM prompt (Claude/GPT few-shot) — no fine-tuning
- **Trace correlation:** v2 — skip in v1, mention in roadmap
- **Cluster-drift anomaly detection:** v2 — v1 uses z-score on log volume per service window
- **Infra:** Docker Compose for local dev; K8s manifests in `infra/k8s/` for production story

---

## What to Do Now

Read the task the user gave you, identify which service(s) are involved, and implement the feature following the patterns above. Cross-service changes must update `contracts/` first, then the affected services.
