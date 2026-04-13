import { startConsumer, stopConsumer } from './kafka/consumer';

async function main() {
  console.log('[embedding-service] Starting...');

  await startConsumer();

  // Graceful shutdown
  const shutdown = async (signal: string) => {
    console.log(`[embedding-service] Received ${signal}, shutting down...`);
    await stopConsumer();
    process.exit(0);
  };

  process.on('SIGTERM', () => shutdown('SIGTERM'));
  process.on('SIGINT',  () => shutdown('SIGINT'));
}

main().catch(err => {
  console.error('[embedding-service] Fatal error:', err);
  process.exit(1);
});
