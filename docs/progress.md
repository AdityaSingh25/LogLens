# LogLens — Build Progress

Track what's done as we go. Check off each item when complete.

---

## Phase 1 — Foundation (Week 1)

### Milestone 1.1 — Docker Compose + Makefile ✅
- [x] `infra/docker-compose.yml` — all 9 containers with health checks
- [x] `infra/otel-collector-config.yml`
- [x] `infra/kafka-init/create-topics.sh`
- [x] `infra/.env.example`
- [x] `Makefile` — `infra-up`, `infra-down`, `infra-logs`, `kafka-topics`, `dev`, `build`, `test`, `lint`, `clean`, `help`
- [ ] **Smoke test:** `make infra-up` → all containers healthy, ES green, Qdrant responding

### Milestone 1.2 — Contracts ✅
- [x] `contracts/kafka-schemas/KafkaTopics.java`
- [x] `contracts/kafka-schemas/topics.ts`
- [x] `contracts/kafka-schemas/raw-log.schema.json`
- [x] `contracts/kafka-schemas/parsed-log.schema.json`
- [x] `contracts/kafka-schemas/embedding-result.schema.json`
- [x] `contracts/kafka-schemas/alert-event.schema.json`
- [x] `contracts/api-types/auth.ts`
- [x] `contracts/api-types/logs.ts`
- [x] `contracts/api-types/query.ts`
- [x] `contracts/api-types/alerts.ts`
- [x] `contracts/api-types/index.ts`
- [x] `contracts/api-types/package.json`

### Milestone 1.3 — auth-service (port 8081) ✅
- [x] `pom.xml`
- [x] `AuthServiceApplication.java`
- [x] `api/AuthController.java` — POST /auth/login, POST /auth/refresh, GET /auth/me
- [x] `api/ApiKeyController.java`
- [x] `api/TenantController.java`
- [x] `api/dto/` — LoginRequest, LoginResponse, ApiKeyCreateRequest, ApiKeyResponse, TenantCreateRequest
- [x] `domain/` — User, ApiKey, Tenant, Role
- [x] `repository/` — UserRepository, ApiKeyRepository, TenantRepository
- [x] `service/JwtServiceImpl.java` — RS256 sign/verify with Nimbus
- [x] `service/AuthServiceImpl.java`
- [x] `service/ApiKeyServiceImpl.java` — BCrypt hash, create-return-once
- [x] `config/SecurityConfig.java`
- [x] `config/JwksController.java` — GET /auth/.well-known/jwks.json
- [x] `security/GlobalExceptionHandler.java`
- [x] `application.yml`
- [x] `db/migration/V1__create_tenants.sql`
- [x] `db/migration/V2__create_users.sql`
- [x] `db/migration/V3__create_api_keys.sql`
- [x] Tests: JwtServiceTest, AuthControllerIntegrationTest
- [ ] **Smoke test:** create tenant → login → get JWKS

### Milestone 1.4 — ingestion-service (port 8082) ✅
- [x] `pom.xml`
- [x] `IngestionServiceApplication.java`
- [x] `api/LogIngestionController.java` — POST /ingest/logs, POST /ingest/logs/batch
- [x] `api/dto/` — IngestLogRequest, IngestLogResponse, IngestBatchRequest, IngestBatchResponse
- [x] `domain/RawLogMessage.java`
- [x] `kafka/RawLogProducer.java` — partitionKey = tenantId:serviceName
- [x] `service/IngestionServiceImpl.java`
- [x] `config/KafkaProducerConfig.java`
- [x] `config/SecurityConfig.java` — JWKS URI validation
- [x] `security/TenantContextHolder.java`
- [x] `security/GlobalExceptionHandler.java`
- [x] `application.yml`
- [x] Tests: IngestionServiceTest
- [ ] **Smoke test:** authenticated POST → message appears in Kafka UI

### Milestone 1.5 — parser-service (port 8083) ✅
- [x] `pom.xml`
- [x] `kafka/RawLogConsumer.java` — DLQ with 3 retries + exponential backoff
- [x] `kafka/ParsedLogProducer.java`
- [x] `service/LogParserServiceImpl.java`
- [x] `service/JsonLogParser.java`
- [x] `service/LogfmtParser.java`
- [x] `service/PlainTextParser.java`
- [x] `elasticsearch/ElasticsearchIndexer.java`
- [x] `elasticsearch/IndexNameResolver.java` — logs-{tenantId}-{yyyy.MM.dd}
- [x] `config/KafkaConfig.java` — consumer + producer + DLQ config
- [x] `config/ElasticsearchConfig.java`
- [x] `application.yml`
- [x] `src/main/resources/es-index-template.json`
- [x] Tests: JsonLogParserTest, LogfmtParserTest, PlainTextParserTest
- [ ] **Phase 1 E2E smoke test:** ingest log → wait 3s → search in ES → 1 hit ✓

---

## Phase 2 — Intelligence Layer (Week 2)

### Milestone 2.1 — embedding-service (Node.js) ✅
- [x] `package.json`, `tsconfig.json`
- [x] `src/config/config.ts` — Zod env validation
- [x] `src/kafka/consumer.ts`
- [x] `src/services/batchBuffer.ts` — 100 msgs OR 500ms trigger
- [x] `src/services/embeddingService.ts`
- [x] `src/clients/openaiClient.ts` — text-embedding-3-small
- [x] `src/clients/qdrantClient.ts` — auto-create collection, upsert points
- [x] `src/schemas/parsedLogSchema.ts`
- [x] `src/index.ts`
- [x] Tests: embeddingService.test.ts, batchBuffer.test.ts
- [ ] **Smoke test:** ingest log → Qdrant collection gets a point

### Milestone 2.2 — query-service (port 8084) ✅
- [x] `pom.xml`
- [x] `api/SearchController.java` — POST /query/search
- [x] `domain/SavedQuery.java` + migration `V1__create_saved_queries.sql`
- [x] `service/QueryOrchestrationService.java` — parallel ES + Qdrant → RRF
- [x] `service/NlQueryTranslatorService.java` — LLM few-shot prompt
- [x] `service/RrfMergeService.java` — score = Σ 1/(k+rank); k=60
- [x] `service/QueryCacheService.java` — Redis TTL 60s
- [x] `elasticsearch/EsSearchClient.java`
- [x] `qdrant/QdrantSearchClient.java`
- [x] `qdrant/EmbeddingClient.java`
- [x] `config/SecurityConfig.java`, `ElasticsearchConfig.java`
- [x] `security/TenantContextHolder.java`, `GlobalExceptionHandler.java`
- [x] `application.yml`
- [x] Tests: RrfMergeServiceTest
- [ ] **Smoke test:** POST /query/search → HYBRID results returned

### Milestone 2.3 — Frontend Skeleton ✅
- [x] Vite + React + TS scaffold
- [x] Tailwind CSS + postcss setup
- [x] `vite.config.ts` — proxy for all backend services + vitest config
- [x] `src/api/` — auth.ts, query.ts, alerts.ts
- [x] `src/components/search/` — SearchBar (debounced), SearchResults, LogCard
- [x] `src/components/stream/` — LiveLogStream (WebSocket + 100ms buffer), FilterChips
- [x] `src/components/layout/` — AppShell, Navbar
- [x] `src/hooks/useSearch.ts`, `useLiveStream.ts`
- [x] `src/pages/` — SearchPage, StreamPage, AlertsPage (placeholder)
- [x] `src/lib/` — utils.ts, logLevel.ts
- [x] `src/types/index.ts`
- [x] Tests: SearchBar.test.tsx
- [ ] **Smoke test:** search bar → results; /stream → live logs

---

## Phase 3 — Alerting + Polish (Week 3)

### Milestone 3.1 — alerting-service (port 8085) ✅
- [x] `pom.xml`
- [x] `api/AlertRuleController.java` — CRUD /alerting/rules
- [x] `domain/AlertRule.java` + `AlertFiring.java`
- [x] `kafka/ParsedLogConsumer.java` + `AlertEventProducer.java`
- [x] `service/AlertEvaluationService.java` — @Scheduled every 60s
- [x] `service/ZScoreAnomalyDetector.java` — sliding window, z>3.0 fires
- [x] `service/WindowAggregatorService.java`
- [x] `config/KafkaConfig.java`, `SecurityConfig.java`
- [x] `security/TenantContextHolder.java`, `GlobalExceptionHandler.java`
- [x] DB migrations: V1__create_alert_rules.sql, V2__create_alert_firings.sql
- [x] Tests: ZScoreAnomalyDetectorTest
- [ ] **Smoke test:** burst logs → z-score fires → alert-events topic gets message

### Milestone 3.2 — notification-service (Node.js) ✅
- [x] `package.json`, `tsconfig.json`
- [x] `src/config/config.ts`
- [x] `src/kafka/consumer.ts` — subscribes to ALERT_EVENTS
- [x] `src/services/notificationDispatcher.ts` — routes by channel prefix
- [x] `src/clients/slackClient.ts` — Block Kit message
- [x] `src/clients/emailClient.ts` — nodemailer
- [x] `src/clients/webhookClient.ts` — 3x retry, 10s timeout, no retry on 4xx
- [x] Tests: webhookClient.test.ts
- [ ] **Smoke test:** alert fires → Slack/webhook receives message

### Milestone 3.3 — Frontend Dashboards ✅
- [x] `components/dashboard/` — VolumeChart, ServiceHealthGrid
- [x] `components/alerts/` — AlertRuleBuilder, AlertRuleList
- [x] `pages/DashboardPage.tsx` + `AlertsPage.tsx` (full implementation)
- [x] `hooks/useDashboard.ts` (30s auto-refresh), `useAlerts.ts`
- [x] Dashboard added to navbar and router
- [ ] **Smoke test:** dashboard charts render; create alert rule → appears in list

### Milestone 3.4 — Kubernetes Manifests ✅
- [x] `infra/k8s/namespace.yaml`
- [x] deployment + service YAML for all 7 services
- [x] HPA for ingestion-service (3→10) and query-service (2→8)
- [x] All deployments: resource limits, envFrom.secretRef, readiness/liveness probes

### Milestone 3.5 — GitHub Actions CI ✅
- [x] 8 workflow files (one per service + frontend)
- [x] Path filters per service + contracts
- [x] Spring: build → unit tests → integration tests
- [x] Node/Frontend: npm ci → lint → build → test

### Milestone 3.6 — Demo Prep ✅
- [x] `scripts/seed-demo-data.sh` — 200 logs across 5 services, 2 alert rules
- [x] `docs/architecture.md` — 5 ADRs written out
- [x] `docs/api.md` — full REST + WebSocket reference
- [ ] **Final smoke test:** `./scripts/seed-demo-data.sh` → open localhost:5173 → full demo works end-to-end
