import { embedTexts } from '../clients/openaiClient';
import { upsertPoints } from '../clients/qdrantClient';
import { ParsedLog } from '../schemas/parsedLogSchema';

export async function processBatch(logs: ParsedLog[]): Promise<void> {
  if (logs.length === 0) return;

  const texts = logs.map(log =>
    [log.normalizedMessage ?? log.message, log.serviceName, log.level].join(' ')
  );

  const vectors = await embedTexts(texts);

  // Best-effort: if embedding fails, skip silently — keyword search still works
  if (!vectors) {
    console.warn(`[embeddingService] Skipping batch of ${logs.length} — embedding returned null`);
    return;
  }

  const points = logs.map((log, i) => ({
    logId: log.logId,
    tenantId: log.tenantId,
    serviceName: log.serviceName,
    timestamp: log.timestamp,
    level: log.level,
    vector: vectors[i],
  }));

  await upsertPoints(points);
  console.log(`[embeddingService] Upserted ${points.length} points to Qdrant`);
}
