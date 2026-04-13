import { z } from 'zod';

export const ParsedLogSchema = z.object({
  logId: z.string(),
  tenantId: z.string(),
  serviceName: z.string(),
  timestamp: z.string().optional(),
  level: z.enum(['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL']),
  message: z.string(),
  normalizedMessage: z.string().optional(),
  parsedFields: z.record(z.unknown()).optional(),
  traceId: z.string().optional(),
});

export type ParsedLog = z.infer<typeof ParsedLogSchema>;
