# LogLens

**Search your logs in plain English. Get alerted when something weird shows up.**

LogLens is a semantic log search and observability platform. Traditional log tools make you write regex or remember exact keywords. LogLens lets you ask _"show me payment failures in the last hour where users retried more than twice"_ — and it just works.

Built on a Kafka-driven ingestion pipeline with vector embeddings over log content, so semantically similar errors cluster together even when the exact wording differs.

<p align="center">
  <img src="docs/architecture-diagram.png" alt="LogLens Architecture" width="800"/>
</p>

---

## Why LogLens

Every engineer has been here: production is broken, you're grepping logs at 2 AM, and you can't remember if the error message was "payment failed" or "transaction declined" or "checkout error." You miss the pattern because the wording drifted across services.

LogLens solves this by treating logs as _meaning_, not strings. Embeddings cluster similar errors. Natural language queries get translated into structured searches. Anomaly detection flags when a _new_ kind of error shows up — not just when volume spikes.

**What it does:**

- Ingests structured and unstructured logs at high throughput via Kafka
- Indexes logs in Elasticsearch for fast keyword/field search
- Generates embeddings for semantic search via a vector database
- Translates natural language queries into hybrid searches (keyword + vector)
- Detects anomalies (volume spikes, never-seen-before error patterns)
- Fans out alerts to Slack, email, and webhooks

---

## Architecture

LogLens is a polyglot microservices system. Spring Boot handles the throughput-critical, stateful services. Node.js handles I/O-bound integration services where the JS ecosystem for LLM and notification SDKs is stronger.

### Services

| Service                | Stack                 | Responsibility                                                                           |
| ---------------------- | --------------------- | ---------------------------------------------------------------------------------------- |
| `ingestion-service`    | Spring Boot (WebFlux) | Accepts logs via REST/gRPC, validates, publishes to Kafka                                |
| `parser-service`       | Spring Boot           | Kafka consumer; parses JSON/logfmt/plain text, extracts fields, indexes to Elasticsearch |
| `embedding-service`    | Node.js + TypeScript  | Consumes parsed logs, generates embeddings, writes to Qdrant                             |
| `query-service`        | Spring Boot           | Translates NL → hybrid query (ES DSL + vector similarity), returns ranked results        |
| `alerting-service`     | Spring Boot           | Evaluates alert rules, detects anomalies, emits alert events                             |
| `notification-service` | Node.js + TypeScript  | Fan-out to Slack, email, and webhook destinations                                        |
| `auth-service`         | Spring Boot           | Multi-tenant auth, JWT issuance, API key management                                      |

### Data Stores

- **Kafka** — event backbone between all services
- **PostgreSQL** — users, orgs, alert rules, saved queries
- **Elasticsearch** — structured log index, full-text and field search
- **Qdrant** — vector embeddings for semantic search
- **Redis** — query result cache, rate limiting

### Why Polyglot

JVM services own the hot path: ingestion throughput, stream processing, and query orchestration benefit from the JVM's mature Kafka and Elasticsearch client ecosystems. Node services own the edges: embedding generation and notification fan-out are I/O-bound, and the JS ecosystem for LLM SDKs and webhook integrations is typically ahead of Java.

All services share Kafka message schemas via a single `contracts/` module and propagate JWTs issued by `auth-service`. Distributed tracing across language boundaries is handled via OpenTelemetry.

---

## Frontend

React + TypeScript SPA. Highlights:

- **Natural language search bar** — the product's wow moment
- **Live log stream** via WebSocket with filter chips
- **Semantic clustering view** — similar errors grouped visually
- **Alert rule builder** with condition preview
- **Time-series dashboards** for log volume, error rates, and service health
- **Saved queries** and shareable links

Stack: React 18, TypeScript, TanStack Query, Tailwind, Recharts, shadcn/ui.

---

## Scale & Design Decisions

A few decisions worth calling out, since they drive the architecture:

**Kafka partitioning by tenant + service.** Log order within a service needs to be preserved, but cross-service ordering doesn't matter. Partitioning by `{tenant_id}:{service_name}` gives us parallelism without sacrificing per-service ordering. Consumer groups scale horizontally.

**Hybrid search, not pure vector.** Pure vector search is slow and often worse than keyword for exact-match queries (trace IDs, user IDs). The query-service runs both in parallel and merges results with reciprocal rank fusion. Fast queries stay fast, semantic queries get smart.

**Embeddings are async and lossy.** We don't block ingestion on embedding generation. Logs become searchable in Elasticsearch within seconds; semantic search catches up within a minute. If the embedding service falls behind, keyword search still works — graceful degradation.

**Anomaly detection is layered.** Cheap statistical checks (z-score on volume per service) run on every window. Expensive semantic checks (cluster drift detection) run on a slower cadence. Most alerts fire from the cheap layer.

**Multi-tenancy at the index level.** Each tenant gets a prefixed Elasticsearch index (`logs-{tenant_id}-{date}`) and a Qdrant collection. Enforced at the query-service layer via JWT claims. Avoids the noisy-neighbor query performance problem of shared indices.

---

## Getting Started

**Requirements:** Docker, Docker Compose, JDK 21, Node 20, and ~8 GB RAM free.

```bash
git clone https://github.com/<you>/loglens.git
cd loglens

# Boot the data plane (Kafka, Postgres, ES, Qdrant, Redis)
docker compose -f infra/docker-compose.yml up -d

# Build and run all services
make dev

# Frontend
cd frontend && npm install && npm run dev
```

The UI comes up at `http://localhost:5173`. A seed script loads sample logs so you can try the search immediately.

See [`docs/architecture.md`](docs/architecture.md) for a deeper walkthrough and [`docs/api.md`](docs/api.md) for the ingestion and query API reference.

---

## Project Structure

```
loglens/
├── services/              # All backend microservices
│   ├── ingestion-service/
│   ├── parser-service/
│   ├── embedding-service/
│   ├── query-service/
│   ├── alerting-service/
│   ├── notification-service/
│   └── auth-service/
├── contracts/             # Shared Kafka schemas + API types
├── frontend/              # React + TypeScript SPA
├── infra/                 # Docker Compose, Kubernetes manifests
└── docs/                  # Architecture, API docs, ADRs
```

---

## Roadmap

- [x] Ingestion pipeline end-to-end (Kafka → ES)
- [x] Natural language query translation
- [x] Semantic clustering view
- [x] Volume-based anomaly detection
- [ ] Distributed trace correlation (OTel trace_id join)
- [ ] Cluster-drift anomaly detection
- [ ] Log retention tiers (hot/warm/cold)
- [ ] Self-hosted embedding model option

---

## Tech Stack

**Backend:** Spring Boot 3, Spring WebFlux, Spring Security, Node.js 20, TypeScript, Kafka, Kafka Streams
**Data:** PostgreSQL 16, Elasticsearch 8, Qdrant, Redis 7
**Frontend:** React 18, TypeScript, TanStack Query, Tailwind, Recharts
**Infra:** Docker, Kubernetes, OpenTelemetry, GitHub Actions

---

## License

MIT

---

_Built by [Aditya](https://github.com/<you>) — feedback and PRs welcome._
