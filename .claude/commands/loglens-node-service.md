# LogLens Node.js Service Development

Use this skill when building or modifying `embedding-service` or `notification-service`.

---

## Stack

- **Node.js 20** + **TypeScript** (strict mode)
- **KafkaJS** — Kafka consumer/producer
- **Qdrant JS client** (`@qdrant/js-client-rest`) — embedding-service only
- **Axios** / **node-fetch** — outbound HTTP (webhooks, Slack API)
- **nodemailer** — email notifications (notification-service)
- **Zod** — runtime schema validation for all incoming Kafka messages and env vars
- **Vitest** — unit and integration tests

---

## Project Conventions

### Directory Structure (per service)
```
src/
├── kafka/          # consumer.ts, producer.ts, topics.ts
├── services/       # business logic, no framework coupling
├── clients/        # external API wrappers (Qdrant, Slack, email)
├── schemas/        # Zod schemas for messages and config
├── config/         # env var validation and typed config object
└── index.ts        # bootstrap: connect Kafka, start consuming
```

### TypeScript Rules
- **Strict mode always on** (`"strict": true` in tsconfig)
- No `any` — use `unknown` and narrow, or define a proper type
- All async functions return `Promise<T>` with explicit type
- Import shared types from `../../contracts/api-types/` — never redefine them locally

### Environment Config Pattern
Validate all env vars at startup using Zod — fail fast before Kafka connects:

```typescript
import { z } from 'zod';

const ConfigSchema = z.object({
  KAFKA_BROKERS: z.string().transform(s => s.split(',')),
  QDRANT_URL: z.string().url(),
  EMBEDDING_MODEL: z.string().default('text-embedding-3-small'),
  KAFKA_GROUP_ID: z.string(),
});

export const config = ConfigSchema.parse(process.env);
```

---

## Kafka Consumer Pattern

```typescript
import { Kafka, EachMessagePayload } from 'kafkajs';
import { config } from './config';

const kafka = new Kafka({ brokers: config.KAFKA_BROKERS });
const consumer = kafka.consumer({ groupId: config.KAFKA_GROUP_ID });

export async function startConsumer() {
  await consumer.connect();
  await consumer.subscribe({ topic: 'parsed-logs', fromBeginning: false });

  await consumer.run({
    eachMessage: async ({ message }: EachMessagePayload) => {
      const value = message.value?.toString();
      if (!value) return;
      const parsed = ParsedLogSchema.parse(JSON.parse(value));
      await processLog(parsed);
    },
  });
}
```

### Error Handling in Consumers
- Catch and log processing errors **per message** — never let a single bad message crash the consumer
- On unrecoverable errors (e.g. Qdrant down), use exponential backoff and alert via stderr
- Parse/validation errors go to a dead-letter log (`console.error` with full message payload) — don't retry parse failures

---

## Embedding Service Specifics

- Uses OpenAI `text-embedding-3-small` (or configured model) via `openai` SDK
- Batch embeddings where possible — collect up to 100 messages or 500ms, whichever comes first
- Write to Qdrant with payload: `{ tenant_id, log_id, timestamp, service_name, level }`
- Qdrant collection per tenant: `logs-{tenantId}` — create if not exists on first write
- Embeddings are **best-effort** — if the model API is unavailable, log the error and skip (don't block Kafka)

```typescript
// Upsert to Qdrant
await qdrantClient.upsert(`logs-${tenantId}`, {
  wait: true,
  points: [{ id: logId, vector: embedding, payload: { ...metadata } }],
});
```

---

## Notification Service Specifics

- Consumes `alert-events` topic
- Fan-out destinations stored in PostgreSQL (read via REST call to auth-service or direct DB)
- **Slack:** use `@slack/web-api` with `chat.postMessage`
- **Email:** nodemailer with SMTP config from env
- **Webhook:** POST JSON to configured URL, retry 3x with exponential backoff, timeout 10s
- Each destination type is a separate module in `src/clients/`
- Failed deliveries are logged with full payload — do not throw, do not retry indefinitely

---

## Testing

- Vitest for all tests
- Mock Kafka with in-memory stubs for unit tests
- Mock Qdrant client and external APIs with `vi.mock`
- Integration tests use `@testcontainers/kafka` and a real Qdrant instance
- Test file naming: `*.test.ts` alongside the source file

---

## What to Do Now

Read the specific task. Identify which service is affected. Read the relevant source files. If adding a new Kafka topic or message schema, define it in `contracts/kafka-schemas/` first, then import in the service.
