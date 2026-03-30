# LogLens — Agent Instructions

This file gives AI coding agents (OpenAI Codex, etc.) full context for working in this repository. Read it before making any changes.

---

## What Is LogLens

LogLens is a **semantic log search and observability platform**. It ingests logs at high throughput, indexes them for keyword and vector search, translates natural language queries into hybrid searches, detects anomalies, and fans out alerts.

Core value: engineers search logs in plain English ("show me payment failures where users retried more than twice") instead of writing regex or guessing exact error strings.

---

## Monorepo Structure

```
loglens/
├── services/
│   ├── ingestion-service/    # Spring Boot WebFlux — REST/gRPC → Kafka
│   ├── parser-service/       # Spring Boot — Kafka consumer → Elasticsearch
│   ├── embedding-service/    # Node.js 20 + TS — Kafka consumer → Qdrant
│   ├── query-service/        # Spring Boot — NL query → ES DSL + vector RRF
│   ├── alerting-service/     # Spring Boot — alert rules + anomaly detection
│   ├── notification-service/ # Node.js 20 + TS — Slack/email/webhook fan-out
│   └── auth-service/         # Spring Boot — JWT + API key auth
├── contracts/
│   ├── kafka-schemas/        # Shared Kafka message schemas (JSON Schema / Avro)
│   └── api-types/            # Shared TypeScript types for REST APIs
├── frontend/                 # React 18 + TS + TanStack Query + Tailwind + shadcn/ui
├── infra/
│   ├── docker-compose.yml    # Local data plane (Kafka, Postgres, ES, Qdrant, Redis)
│   └── k8s/                  # Kubernetes manifests
└── docs/                     # Architecture, API docs, ADRs
```

---

## Service Responsibilities

| Service | Stack | Responsibility |
|---|---|---|
| `ingestion-service` | Spring Boot 3 + WebFlux | Accept logs via REST/gRPC, validate, publish to `raw-logs` Kafka topic |
| `parser-service` | Spring Boot 3 | Consume `raw-logs`, parse JSON/logfmt/plain text, index to Elasticsearch |
| `embedding-service` | Node.js 20 + TS | Consume `parsed-logs`, generate embeddings, write to Qdrant |
| `query-service` | Spring Boot 3 | Translate NL → ES DSL + Qdrant vector query, merge with RRF |
| `alerting-service` | Spring Boot 3 | Evaluate alert rules, z-score anomaly detection, emit `alert-events` |
| `notification-service` | Node.js 20 + TS | Consume `alert-events`, fan out to Slack/email/webhooks |
| `auth-service` | Spring Boot 3 | Multi-tenant JWT issuance (RS256), API key management |

---

## Data Stores

| Store | Default URL | Usage |
|---|---|---|
| Kafka | `localhost:9092` | Event backbone between all services |
| PostgreSQL 16 | `localhost:5432` | Users, orgs, alert rules, saved queries |
| Elasticsearch 8 | `localhost:9200` | Per-tenant daily log indices: `logs-{tenant_id}-{YYYY.MM.dd}` |
| Qdrant | `localhost:6333` | Per-tenant vector collections: `logs-{tenant_id}` |
| Redis 7 | `localhost:6379` | Query cache (TTL) + rate limiting |

---

## Critical Architecture Rules

1. **Tenant isolation is mandatory.** Every ES query and Qdrant search must be scoped to the JWT's `tenant_id` claim. The query-service enforces this. Never query across tenants.

2. **Kafka topic names are constants.** They live in `contracts/kafka-schemas/`. Never hardcode topic strings in service code.

3. **Contracts first.** If a change adds a new Kafka message field or REST API type, update `contracts/` first, then update the affected services. Never duplicate schema definitions across services.

4. **Embeddings are async and lossy.** Ingestion never waits for embeddings. If the embedding service is down, keyword search still works. Don't add blocking calls to the embedding path.

5. **Hybrid search only.** The query-service runs ES keyword search and Qdrant vector search in parallel, merges via Reciprocal Rank Fusion. Never replace the hybrid approach with pure-vector search.

6. **Dead Letter Queues.** Every Kafka consumer must handle poison pill messages without crashing. Failed messages after 3 retries go to `{topic}.DLT`.

7. **No stack traces to clients.** Use a global exception handler that maps domain exceptions to HTTP status codes. Log full stack traces server-side only.

8. **Kafka partitioning:** `{tenant_id}:{service_name}` key on all messages. Do not change this — it preserves per-service log ordering.

---

## Spring Boot Services — Conventions

### Package Layout
```
com.loglens.<service>/
├── api/          # Controllers + DTOs
├── domain/       # Entities, value objects
├── kafka/        # Producers and consumers
├── service/      # Business logic (no Spring annotations)
├── repository/   # Spring Data repos
├── config/       # @Configuration classes
└── security/     # JWT filter, TenantContextHolder
```

### Key Patterns
- `TenantContextHolder` (thread-local) populated by JWT filter — use in every repository query
- `@RestControllerAdvice` for global error handling
- Flyway for DB migrations — files in `src/main/resources/db/migration/V{n}__{desc}.sql`
- JWTs validated via JWKS: `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`
- WebFlux (ingestion-service only): return `Mono`/`Flux`, use `WebClient`, never `.block()` in reactive chains

### Testing
- Unit: `@ExtendWith(MockitoExtension.class)`
- Integration: `@SpringBootTest` + `@EmbeddedKafka` + Testcontainers for Postgres/ES
- Do NOT mock Kafka or the database in integration tests

---

## Node.js Services — Conventions

### Directory Layout
```
src/
├── kafka/       # consumer.ts, producer.ts
├── services/    # Business logic
├── clients/     # External API wrappers (Qdrant, Slack, email)
├── schemas/     # Zod schemas for messages and env vars
├── config/      # Typed config from env vars (Zod-validated at startup)
└── index.ts     # Bootstrap
```

### Key Patterns
- TypeScript strict mode — no `any`
- Validate all env vars with Zod at startup — fail fast
- Import shared types from `../../contracts/api-types/` — never redefine
- KafkaJS for Kafka — catch per-message errors, never crash the consumer loop
- Vitest for tests — `@testcontainers/kafka` for integration tests

---

## Frontend — Conventions

- React 18, TypeScript strict, Vite
- **TanStack Query v5** for all server state — never fetch in `useEffect`
- **shadcn/ui** components — run `npx shadcn@latest add <component>` to add new ones; never edit `src/components/ui/` directly
- API client functions in `src/api/` — one file per backend service
- Import types from `contracts/api-types/` — don't redefine
- Recharts for charts — always use `ResponsiveContainer`
- Vitest + React Testing Library for tests — test user behavior, not internals

---

## Running Locally

```bash
# Start data plane
docker compose -f infra/docker-compose.yml up -d

# Start all services
make dev

# Frontend (separate terminal)
cd frontend && npm install && npm run dev
# UI at http://localhost:5173
```

---

## Making Changes

1. **Read before writing.** Always read the files you're about to change.
2. **Contracts first.** Shared schema changes → `contracts/` → services.
3. **One service at a time.** Scope changes narrowly. Cross-service changes need a clear migration plan.
4. **No speculative abstractions.** Build exactly what's needed. Don't add generalization for hypothetical future use cases.
5. **Migrations are append-only.** Never modify an existing Flyway migration file. Add a new one.
6. **Secrets via environment variables.** Never hardcode credentials, keys, or URLs in source files.
