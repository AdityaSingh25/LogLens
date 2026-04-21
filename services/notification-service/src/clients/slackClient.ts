import { WebClient } from '@slack/web-api';
import { config } from '../config/config';
import type { AlertEvent } from '../schemas/alertEventSchema';

const slack = config.SLACK_BOT_TOKEN ? new WebClient(config.SLACK_BOT_TOKEN) : null;

const SEVERITY_EMOJI: Record<string, string> = {
  LOW:      ':information_source:',
  MEDIUM:   ':warning:',
  HIGH:     ':rotating_light:',
  CRITICAL: ':fire:',
};

export async function sendSlackMessage(channel: string, event: AlertEvent): Promise<void> {
  if (!slack) {
    console.warn('[slackClient] SLACK_BOT_TOKEN not configured, skipping');
    return;
  }
  try {
    const emoji = SEVERITY_EMOJI[event.severity] ?? ':bell:';
    await slack.chat.postMessage({
      channel,
      text: `${emoji} *${event.title}*`,
      blocks: [
        {
          type: 'header',
          text: { type: 'plain_text', text: `${emoji} ${event.title}` },
        },
        {
          type: 'section',
          fields: [
            { type: 'mrkdwn', text: `*Severity:*\n${event.severity}` },
            { type: 'mrkdwn', text: `*Time:*\n${event.triggered_at}` },
          ],
        },
        {
          type: 'section',
          text: { type: 'mrkdwn', text: event.description },
        },
      ],
    });
  } catch (err) {
    // Log but do not throw — delivery failure must not crash the consumer
    console.error('[slackClient] Failed to send message:', err);
  }
}
