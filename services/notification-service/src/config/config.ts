import { z } from 'zod';

const ConfigSchema = z.object({
  KAFKA_BROKERS:  z.string().transform(s => s.split(',')),
  KAFKA_GROUP_ID: z.string().default('notification-service'),
  SLACK_BOT_TOKEN: z.string().optional(),
  SMTP_HOST:  z.string().optional(),
  SMTP_PORT:  z.coerce.number().default(587),
  SMTP_USER:  z.string().optional(),
  SMTP_PASS:  z.string().optional(),
  SMTP_FROM:  z.string().optional(),
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
