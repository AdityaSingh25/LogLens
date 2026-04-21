import { sendSlackMessage } from '../clients/slackClient';
import { sendEmail }        from '../clients/emailClient';
import { sendWebhook }      from '../clients/webhookClient';
import type { AlertEvent }  from '../schemas/alertEventSchema';

/**
 * Routes an alert event to all configured notification channels.
 * Channel format:
 *   "slack:C1234567"         → Slack channel ID
 *   "email:user@example.com" → email address
 *   "webhook:https://..."    → HTTP POST endpoint
 */
export async function dispatch(event: AlertEvent): Promise<void> {
  const tasks = event.notification_channels.map(async channel => {
    try {
      if (channel.startsWith('slack:')) {
        await sendSlackMessage(channel.slice(6), event);
      } else if (channel.startsWith('email:')) {
        await sendEmail(channel.slice(6), event);
      } else if (channel.startsWith('webhook:')) {
        await sendWebhook(channel.slice(8), event);
      } else {
        console.warn(`[dispatcher] Unknown channel format: ${channel}`);
      }
    } catch (err) {
      // Individual channel failure must not block others
      console.error(`[dispatcher] Delivery failed for channel ${channel}:`, err);
    }
  });

  await Promise.allSettled(tasks);
}
