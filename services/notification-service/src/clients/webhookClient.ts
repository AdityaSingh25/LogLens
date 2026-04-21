import axios from 'axios';
import type { AlertEvent } from '../schemas/alertEventSchema';

const MAX_RETRIES = 3;
const TIMEOUT_MS  = 10_000;

export async function sendWebhook(url: string, event: AlertEvent): Promise<void> {
  for (let attempt = 0; attempt < MAX_RETRIES; attempt++) {
    try {
      const res = await axios.post(url, event, { timeout: TIMEOUT_MS });

      // 4xx errors are not retried — bad request won't be fixed by retrying
      if (res.status >= 400 && res.status < 500) {
        console.error(`[webhookClient] 4xx response (${res.status}) for ${url}, not retrying`);
        return;
      }
      return; // success
    } catch (err) {
      const isLastAttempt = attempt === MAX_RETRIES - 1;
      if (isLastAttempt) {
        console.error(`[webhookClient] All ${MAX_RETRIES} attempts failed for ${url}:`, err);
        return;
      }
      // Exponential backoff: 1s, 2s, 4s
      const delay = Math.pow(2, attempt) * 1000;
      console.warn(`[webhookClient] Attempt ${attempt + 1} failed, retrying in ${delay}ms`);
      await new Promise(r => setTimeout(r, delay));
    }
  }
}
