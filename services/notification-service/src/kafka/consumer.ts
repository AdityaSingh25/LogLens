import { Kafka, EachMessagePayload } from 'kafkajs';
import { config } from '../config/config';
import { AlertEventSchema } from '../schemas/alertEventSchema';
import { dispatch } from '../services/notificationDispatcher';

const kafka = new Kafka({
  clientId: 'notification-service',
  brokers: config.KAFKA_BROKERS,
});

const consumer = kafka.consumer({ groupId: config.KAFKA_GROUP_ID });

export async function startConsumer(): Promise<void> {
  await consumer.connect();
  await consumer.subscribe({ topic: 'alert-events', fromBeginning: false });

  console.log('[consumer] Connected. Listening on alert-events...');

  await consumer.run({
    eachMessage: async ({ message }: EachMessagePayload) => {
      const value = message.value?.toString();
      if (!value) return;

      try {
        const raw = JSON.parse(value);
        const event = AlertEventSchema.parse(raw);
        await dispatch(event);
      } catch (err) {
        // Parse/schema errors are not retried
        console.error('[consumer] Failed to process alert event, skipping:', err);
      }
    },
  });
}

export async function stopConsumer(): Promise<void> {
  await consumer.disconnect();
}
