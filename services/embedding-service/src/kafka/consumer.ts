import { Kafka, EachMessagePayload } from 'kafkajs';
import { config } from '../config/config';
import { ParsedLogSchema } from '../schemas/parsedLogSchema';
import { BatchBuffer } from '../services/batchBuffer';
import { processBatch } from '../services/embeddingService';

const kafka = new Kafka({
  clientId: 'embedding-service',
  brokers: config.KAFKA_BROKERS,
});

const consumer = kafka.consumer({ groupId: config.KAFKA_GROUP_ID });

const buffer = new BatchBuffer(processBatch);

export async function startConsumer(): Promise<void> {
  await consumer.connect();
  await consumer.subscribe({ topic: 'parsed-logs', fromBeginning: false });

  console.log('[consumer] Connected. Listening on parsed-logs...');

  await consumer.run({
    eachMessage: async ({ message }: EachMessagePayload) => {
      const value = message.value?.toString();
      if (!value) return;

      try {
        const raw = JSON.parse(value);
        const log = ParsedLogSchema.parse(raw);
        buffer.add(log);
      } catch (err) {
        // Parse/validation errors go to error log — never retry
        console.error('[consumer] Failed to parse message, skipping:', err);
      }
    },
  });
}

export async function stopConsumer(): Promise<void> {
  buffer.stop();
  await consumer.disconnect();
}
