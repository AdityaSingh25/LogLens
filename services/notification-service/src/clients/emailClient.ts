import nodemailer from 'nodemailer';
import { config } from '../config/config';
import type { AlertEvent } from '../schemas/alertEventSchema';

const transporter = config.SMTP_HOST
  ? nodemailer.createTransport({
      host: config.SMTP_HOST,
      port: config.SMTP_PORT,
      auth: config.SMTP_USER ? { user: config.SMTP_USER, pass: config.SMTP_PASS } : undefined,
    })
  : null;

export async function sendEmail(to: string, event: AlertEvent): Promise<void> {
  if (!transporter) {
    console.warn('[emailClient] SMTP not configured, skipping');
    return;
  }
  try {
    await transporter.sendMail({
      from: config.SMTP_FROM ?? 'loglens@noreply.com',
      to,
      subject: `[${event.severity}] ${event.title}`,
      text: `${event.description}\n\nTriggered at: ${event.triggered_at}`,
      html: `<h2>${event.title}</h2><p><strong>Severity:</strong> ${event.severity}</p><p>${event.description}</p><p><small>Triggered at: ${event.triggered_at}</small></p>`,
    });
  } catch (err) {
    console.error('[emailClient] Failed to send email:', err);
  }
}
