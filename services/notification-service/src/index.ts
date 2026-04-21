import { startConsumer, stopConsumer } from './kafka/consumer';

async function main() {
  console.log('[notification-service] Starting...');
  await startConsumer();

  const shutdown = async (signal: string) => {
    console.log(`[notification-service] ${signal} received, shutting down...`);
    await stopConsumer();
    process.exit(0);
  };

  process.on('SIGTERM', () => shutdown('SIGTERM'));
  process.on('SIGINT',  () => shutdown('SIGINT'));
}

main().catch(err => {
  console.error('[notification-service] Fatal error:', err);
  process.exit(1);
});
