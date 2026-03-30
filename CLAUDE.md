# LogLens — Claude Code Context

This is a **portfolio project** built by Aditya to demonstrate senior-level backend engineering. The goal is to impress recruiters at Razorpay, PhonePe, Atlassian, and Deutsche Bank.

## Project Summary

LogLens is a semantic log search and observability platform. Search logs in plain English, get alerted on novel error patterns.

## Stack Decisions (Final — Do Not Revisit)

**Spring Boot services:** ingestion-service, parser-service, query-service, auth-service, alerting-service  
**Node.js + TypeScript services:** embedding-service, notification-service  
**Why polyglot:** JVM owns the hot path (throughput, Kafka Streams, ES client maturity). Node owns I/O-bound edges where LLM/webhook SDK ecosystem is stronger. This is intentional and defensible in interviews.

**Monorepo** — single repo, all services in `services/`. One GitHub URL for recruiters, atomic cross-service commits, shared `contracts/` folder.

## Key Architecture Decisions (Already Made)

- Kafka partitioning: `{tenant_id}:{service_name}` key — preserves per-service ordering
- Hybrid search only: ES keyword + Qdrant vector in parallel, merged with RRF — never pure-vector
- Embeddings are async and lossy: ingestion never blocks on embedding generation
- Multi-tenancy at index level: `logs-{tenant_id}-{YYYY.MM.dd}` in ES, `logs-{tenant_id}` in Qdrant
- JWT RS256 from auth-service, verified by all other services via JWKS endpoint
- OpenTelemetry across all services for distributed tracing

## Scope Decisions for v1 (Already Decided)

- Use managed embedding APIs (OpenAI `text-embedding-3-small`) — not self-hosted models
- Docker Compose for local dev; K8s manifests exist for the "production-ready" story
- NL→query via LLM prompt (Claude/GPT few-shot) — not fine-tuning
- Trace correlation is v2 (in roadmap, not v1)
- Cluster-drift anomaly detection is v2; v1 uses z-score on log volume

## The "Senior Engineer Signal"

The README's "Scale & Design Decisions" section is intentional — it explains *why* decisions were made, not just *what* was built. When adding features or writing documentation, always explain the reasoning, not just the mechanics.

## Current State

Monorepo scaffold is done. docker-compose.yml for the data plane is the next step.

## Skills Available

- `/loglens-feature` — Full architecture context before building any feature
- `/loglens-spring-service` — Spring Boot service patterns
- `/loglens-node-service` — Node.js service patterns  
- `/loglens-frontend` — React frontend patterns
- `/loglens-infra` — Docker, Kafka, infra tasks
