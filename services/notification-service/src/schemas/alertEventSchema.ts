import { z } from 'zod';

export const AlertEventSchema = z.object({
  alert_id:   z.string(),
  rule_id:    z.string(),
  tenant_id:  z.string(),
  severity:   z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']),
  title:      z.string(),
  description: z.string(),
  triggered_at: z.string(),
  notification_channels: z.array(z.string()),
  context:    z.record(z.unknown()).optional(),
});

export type AlertEvent = z.infer<typeof AlertEventSchema>;
