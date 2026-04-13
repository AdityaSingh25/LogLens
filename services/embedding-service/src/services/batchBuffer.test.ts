import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { BatchBuffer } from './batchBuffer';
import { ParsedLog } from '../schemas/parsedLogSchema';

// Override config values for tests
vi.mock('../config/config', () => ({
  config: {
    BATCH_MAX_SIZE: 3,
    BATCH_FLUSH_MS: 100,
    KAFKA_BROKERS: ['localhost:9092'],
    KAFKA_GROUP_ID: 'test',
    QDRANT_URL: 'http://localhost:6333',
    OPENAI_API_KEY: 'test-key',
    EMBEDDING_MODEL: 'text-embedding-3-small',
    EMBEDDING_DIMENSIONS: 1536,
  },
}));

const mockLog = (id: string): ParsedLog => ({
  logId: id,
  tenantId: 'tenant-1',
  serviceName: 'svc',
  level: 'INFO',
  message: 'test',
});

describe('BatchBuffer', () => {
  beforeEach(() => { vi.useFakeTimers(); });
  afterEach(() => { vi.useRealTimers(); });

  it('flushes when batch size reaches max', async () => {
    const onFlush = vi.fn().mockResolvedValue(undefined);
    const buf = new BatchBuffer(onFlush);

    buf.add(mockLog('1'));
    buf.add(mockLog('2'));
    buf.add(mockLog('3')); // triggers flush at size 3

    await vi.runAllTimersAsync();
    expect(onFlush).toHaveBeenCalledWith(expect.arrayContaining([
      expect.objectContaining({ logId: '1' }),
      expect.objectContaining({ logId: '2' }),
      expect.objectContaining({ logId: '3' }),
    ]));
  });

  it('flushes after timeout even if batch not full', async () => {
    const onFlush = vi.fn().mockResolvedValue(undefined);
    const buf = new BatchBuffer(onFlush);

    buf.add(mockLog('1'));
    expect(onFlush).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(101);
    expect(onFlush).toHaveBeenCalledOnce();
  });
});
