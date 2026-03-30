# LogLens Spring Boot Service Development

Use this skill when building or modifying any of the Spring Boot services in LogLens: `ingestion-service`, `parser-service`, `query-service`, `alerting-service`, or `auth-service`.

---

## Stack

- **Spring Boot 3** with Java 21
- **Spring WebFlux** (ingestion-service) — reactive, non-blocking; use `Mono`/`Flux` throughout
- **Spring MVC** (parser, query, alerting, auth) — standard servlet stack; fine for Kafka-driven services
- **Spring Security** — JWT validation on every request; `auth-service` issues tokens
- **Spring Kafka** — `@KafkaListener` for consumers, `KafkaTemplate` for producers
- **Spring Data JPA + PostgreSQL** — entities for users, orgs, alert rules, saved queries

---

## Project Conventions

### Package Structure (per service)
```
com.loglens.<service>/
├── api/          # Controllers, request/response DTOs
├── domain/       # Entities, value objects
├── kafka/        # Producers, consumers, message models
├── service/      # Business logic (no framework annotations — plain classes)
├── repository/   # Spring Data repos
├── config/       # Spring @Configuration classes
└── security/     # JWT filter, tenant context
```

### Naming
- Controllers: `*Controller` with `@RestController`
- Services: `*Service` (interface) + `*ServiceImpl`
- Kafka consumers: `*Consumer` with `@Component`
- Kafka producers: `*Producer` with `@Component`

### Tenant Context
Every request carries a tenant ID from the JWT claim `tenant_id`. Extract it in a `TenantContextHolder` (thread-local) populated by the JWT security filter. All repository queries must scope to the current tenant. **Never query without a tenant filter.**

### Error Handling
Use a `@RestControllerAdvice` global exception handler. Map domain exceptions (`LogNotFoundException`, `TenantNotFoundException`, etc.) to HTTP status codes. Never leak stack traces to clients.

### Kafka Topics
Defined as constants in `contracts/kafka-schemas/`. Import from there — don't hardcode topic names in service code.

```java
// Good
kafkaTemplate.send(KafkaTopics.PARSED_LOGS, message);

// Bad
kafkaTemplate.send("parsed-logs", message);
```

### Configuration
All config via `application.yml`. Secrets via environment variables with `${ENV_VAR}` references. Never hardcode credentials.

---

## Kafka Consumer Pattern

```java
@Component
@RequiredArgsConstructor
public class ParsedLogConsumer {

    private final EmbeddingService embeddingService;

    @KafkaListener(
        topics = KafkaTopics.PARSED_LOGS,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ParsedLogMessage message) {
        embeddingService.process(message);
    }
}
```

### Dead Letter Queue
All consumer beans must configure a DLQ for poison pills. Failed messages go to `{topic}.DLT` after 3 retries with exponential backoff.

---

## WebFlux (ingestion-service only)

- Return `Mono<ResponseEntity<T>>` from controllers
- Use `WebClient` for outbound calls, never `RestTemplate`
- Publish to Kafka via reactive wrapper: `Mono.fromFuture(kafkaTemplate.send(...))`
- Never block in reactive chains (no `.block()` inside a `Flux`/`Mono` pipeline)

---

## Testing

- Unit tests: `@ExtendWith(MockitoExtension.class)`, mock repositories and external clients
- Integration tests: `@SpringBootTest` + `@EmbeddedKafka` + Testcontainers for Postgres/ES
- **Do not mock Kafka for integration tests** — use `@EmbeddedKafka`
- **Do not mock the database for integration tests** — use Testcontainers

---

## Elasticsearch Access

Only `query-service` and `parser-service` write to/read from Elasticsearch. Use the official `ElasticsearchClient` (not the legacy `RestHighLevelClient`). Index names follow the pattern `logs-{tenantId}-{yyyy.MM.dd}` — always build index names from the tenant context, never hardcode.

---

## Auth Service JWT

JWTs are RS256-signed. Public key exposed at `auth-service/.well-known/jwks.json`. All other services validate using that JWKS endpoint — configure `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`.

Claims to use:
- `sub` — user ID
- `tenant_id` — tenant ID
- `roles` — list of roles (`ADMIN`, `MEMBER`, `VIEWER`)
- `exp`, `iat` — standard expiry

---

## What to Do Now

Read the specific task. Identify which service is being modified. Read the relevant source files before making changes. If the change crosses service boundaries, update `contracts/` first.
