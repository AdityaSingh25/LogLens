# LogLens Infrastructure & DevOps

Use this skill when working on Docker Compose, Kubernetes manifests, CI/CD, or the local development environment.

---

## Local Data Plane

All infrastructure dependencies run via Docker Compose:

```bash
docker compose -f infra/docker-compose.yml up -d
```

Services started:
| Container | Port | Purpose |
|---|---|---|
| Kafka (KRaft mode) | 9092 | Event backbone |
| Kafka UI | 8080 | Topic browser |
| PostgreSQL 16 | 5432 | Relational data |
| Elasticsearch 8 | 9200 | Log index |
| Kibana | 5601 | ES browser (dev only) |
| Qdrant | 6333 | Vector DB |
| Redis 7 | 6379 | Cache + rate limiting |

### Health Check Before Developing

```bash
# All containers running?
docker compose -f infra/docker-compose.yml ps

# Kafka ready?
docker compose -f infra/docker-compose.yml exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# ES ready?
curl -s http://localhost:9200/_cluster/health | jq .status
```

---

## Makefile Targets

The root `Makefile` is the canonical way to run development tasks:

```bash
make dev          # Start all services in development mode
make build        # Build all services
make test         # Run all service tests
make lint         # Run linters across all services
make clean        # Stop and remove containers, clear build artifacts
```

Each service also has its own targets (check `services/<name>/Makefile` or the root Makefile's `help` target).

---

## Kafka Topics

Defined as constants in `contracts/kafka-schemas/`. Topic naming convention:

| Topic | Producer | Consumer(s) |
|---|---|---|
| `raw-logs` | ingestion-service | parser-service |
| `parsed-logs` | parser-service | embedding-service, alerting-service |
| `embedding-results` | embedding-service | (future: cluster analysis) |
| `alert-events` | alerting-service | notification-service |

**Partitioning:** All topics partitioned by `{tenant_id}:{service_name}`.  
**Retention:** 7 days default. Adjust per topic in `infra/docker-compose.yml` for local dev.

### Creating Topics Manually (local dev)
```bash
docker compose -f infra/docker-compose.yml exec kafka \
  kafka-topics.sh --create \
  --topic raw-logs \
  --partitions 12 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092
```

---

## PostgreSQL

Default connection: `postgresql://loglens:loglens@localhost:5432/loglens`

Schema migrations run via Flyway (Spring Boot services apply them on startup). Migration files live in `services/<service>/src/main/resources/db/migration/`. Follow `V{n}__{description}.sql` naming.

**Never modify existing migration files** — add a new migration instead.

---

## Elasticsearch

Default: `http://localhost:9200` (no auth in local dev).

Index pattern: `logs-{tenant_id}-{YYYY.MM.dd}`  
ILM policy: hot → warm → delete at 30 days (configure in `infra/` for production).

Check index health:
```bash
curl -s http://localhost:9200/_cat/indices/logs-*?v
```

---

## Qdrant

Default: `http://localhost:6333`

Collections follow `logs-{tenant_id}`. Vector size depends on embedding model (1536 for `text-embedding-3-small`).

Check collections:
```bash
curl -s http://localhost:6333/collections | jq .
```

---

## OpenTelemetry

All services export traces to a local OTel collector (included in docker-compose). Trace context is propagated via W3C `traceparent` headers across service boundaries.

Environment vars for each service:
```
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
OTEL_SERVICE_NAME=<service-name>
OTEL_TRACES_EXPORTER=otlp
```

---

## Kubernetes (Production)

Manifests live in `infra/k8s/`. Structure:
```
infra/k8s/
├── namespace.yaml
├── services/
│   └── <service-name>/
│       ├── deployment.yaml
│       ├── service.yaml
│       └── hpa.yaml
└── data-plane/
    └── (Kafka, ES, Qdrant operators or Helm values)
```

**Guidelines:**
- All services run with 2+ replicas minimum
- CPU/memory limits set on every container — no unbounded resource usage
- Secrets via Kubernetes Secrets (not ConfigMaps) — never commit secret values
- HPA configured for ingestion-service and query-service (latency-sensitive)

---

## CI/CD (GitHub Actions)

Workflows in `.github/workflows/`. Per-service jobs triggered by path filter (`services/<name>/**`). Shared steps:
1. Lint
2. Unit tests
3. Build Docker image
4. Integration tests (Testcontainers or docker-compose in CI)
5. Push to registry (on `main` merge)

---

## What to Do Now

Read the specific task. For local environment issues, start with `docker compose ps` and service logs. For new infra resources, add to `docker-compose.yml` and update the relevant service's `application.yml` or `config.ts`.
