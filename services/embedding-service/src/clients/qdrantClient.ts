import { QdrantClient } from '@qdrant/js-client-rest';
import { config } from '../config/config';

const qdrant = new QdrantClient({ url: config.QDRANT_URL });

const ensuredCollections = new Set<string>();

async function ensureCollection(collectionName: string): Promise<void> {
  if (ensuredCollections.has(collectionName)) return;

  try {
    await qdrant.getCollection(collectionName);
    ensuredCollections.add(collectionName);
  } catch {
    // Collection doesn't exist — create it
    try {
      await qdrant.createCollection(collectionName, {
        vectors: {
          size: config.EMBEDDING_DIMENSIONS,
          distance: 'Cosine',
        },
      });
      console.log(`[qdrantClient] Created collection: ${collectionName}`);
      ensuredCollections.add(collectionName);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      // Handle race condition: another instance may have created it
      if (!msg.includes('already exists')) {
        throw err;
      }
      ensuredCollections.add(collectionName);
    }
  }
}

export interface LogPoint {
  logId: string;
  tenantId: string;
  serviceName: string;
  timestamp?: string;
  level: string;
  vector: number[];
}

export async function upsertPoints(points: LogPoint[]): Promise<void> {
  if (points.length === 0) return;

  // Group by tenant (each tenant has its own collection)
  const byTenant = new Map<string, LogPoint[]>();
  for (const point of points) {
    const existing = byTenant.get(point.tenantId) ?? [];
    existing.push(point);
    byTenant.set(point.tenantId, existing);
  }

  for (const [tenantId, tenantPoints] of byTenant) {
    const collectionName = `logs-${tenantId}`;
    await ensureCollection(collectionName);

    await qdrant.upsert(collectionName, {
      wait: false,
      points: tenantPoints.map(p => ({
        id: p.logId,
        vector: p.vector,
        payload: {
          log_id: p.logId,
          tenant_id: p.tenantId,
          service_name: p.serviceName,
          timestamp: p.timestamp,
          level: p.level,
        },
      })),
    });
  }
}
