import { z } from 'zod';

const ConfigSchema = z.object({
  KAFKA_BROKERS: z.string().transform(s => s.split(',')),
  KAFKA_GROUP_ID: z.string().default('embedding-service'),
  QDRANT_URL: z.string().url().default('http://localhost:6333'),
  OPENAI_API_KEY: z.string().min(1),
  EMBEDDING_MODEL: z.string().default('text-embedding-3-small'),
  EMBEDDING_DIMENSIONS: z.coerce.number().default(1536),
  BATCH_MAX_SIZE: z.coerce.number().default(100),
  BATCH_FLUSH_MS: z.coerce.number().default(500),
});

function loadConfig() {
  const result = ConfigSchema.safeParse(process.env);
  if (!result.success) {
    console.error('Invalid configuration:', result.error.flatten().fieldErrors);
    process.exit(1);
  }
  return result.data;
}

export const config = loadConfig();
export type Config = typeof config;
