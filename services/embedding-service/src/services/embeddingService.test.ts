import { describe, it, expect, vi, beforeEach } from 'vitest';
import { processBatch } from './embeddingService';
import * as openaiClient from '../clients/openaiClient';
import * as qdrantClient from '../clients/qdrantClient';
import { ParsedLog } from '../schemas/parsedLogSchema';

vi.mock('../clients/openaiClient');
vi.mock('../clients/qdrantClient');

const mockLog = (id: string): ParsedLog => ({
  logId: id,
  tenantId: 'tenant-1',
  serviceName: 'payment-service',
  level: 'ERROR',
  message: 'Payment failed',
  normalizedMessage: 'Payment failed',
  timestamp: new Date().toISOString(),
});

describe('processBatch', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls embedTexts and upserts to Qdrant on success', async () => {
    const fakeVectors = [[0.1, 0.2, 0.3]];
    vi.mocked(openaiClient.embedTexts).mockResolvedValue(fakeVectors);
    vi.mocked(qdrantClient.upsertPoints).mockResolvedValue(undefined);

    await processBatch([mockLog('log-1')]);

    expect(openaiClient.embedTexts).toHaveBeenCalledOnce();
    expect(qdrantClient.upsertPoints).toHaveBeenCalledOnce();
  });

  it('skips Qdrant upsert when embedTexts returns null', async () => {
    vi.mocked(openaiClient.embedTexts).mockResolvedValue(null);

    await processBatch([mockLog('log-2')]);

    expect(qdrantClient.upsertPoints).not.toHaveBeenCalled();
  });

  it('does nothing for empty batch', async () => {
    await processBatch([]);
    expect(openaiClient.embedTexts).not.toHaveBeenCalled();
  });
});
