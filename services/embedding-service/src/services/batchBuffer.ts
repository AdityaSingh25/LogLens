import { config } from '../config/config';
import { ParsedLog } from '../schemas/parsedLogSchema';

type FlushCallback = (batch: ParsedLog[]) => Promise<void>;

export class BatchBuffer {
  private buffer: ParsedLog[] = [];
  private timer: NodeJS.Timeout | null = null;
  private readonly onFlush: FlushCallback;

  constructor(onFlush: FlushCallback) {
    this.onFlush = onFlush;
  }

  add(log: ParsedLog): void {
    this.buffer.push(log);

    if (!this.timer) {
      this.timer = setTimeout(() => this.flush(), config.BATCH_FLUSH_MS);
    }

    if (this.buffer.length >= config.BATCH_MAX_SIZE) {
      this.flush();
    }
  }

  flush(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.buffer.length === 0) return;

    const batch = this.buffer.splice(0);
    this.onFlush(batch).catch(err =>
      console.error('[batchBuffer] Flush error:', err)
    );
  }

  stop(): void {
    this.flush();
  }
}
