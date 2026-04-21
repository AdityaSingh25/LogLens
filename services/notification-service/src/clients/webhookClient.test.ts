import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { sendWebhook } from './webhookClient';
import type { AlertEvent } from '../schemas/alertEventSchema';

vi.mock('axios');

const mockEvent: AlertEvent = {
  alert_id: 'a1',
  rule_id:  'r1',
  tenant_id: 't1',
  severity: 'HIGH',
  title: 'Test Alert',
  description: 'Something went wrong',
  triggered_at: new Date().toISOString(),
  notification_channels: ['webhook:https://example.com/hook'],
};

describe('webhookClient', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('sends POST on first attempt success', async () => {
    vi.mocked(axios.post).mockResolvedValueOnce({ status: 200 });
    await sendWebhook('https://example.com/hook', mockEvent);
    expect(axios.post).toHaveBeenCalledOnce();
  });

  it('does not retry on 4xx', async () => {
    vi.mocked(axios.post).mockResolvedValueOnce({ status: 400 });
    await sendWebhook('https://example.com/hook', mockEvent);
    expect(axios.post).toHaveBeenCalledOnce();
  });

  it('retries on network error up to 3 times', async () => {
    vi.mocked(axios.post).mockRejectedValue(new Error('Network error'));
    // Mock setTimeout to avoid real delays
    vi.useFakeTimers();
    const promise = sendWebhook('https://example.com/hook', mockEvent);
    await vi.runAllTimersAsync();
    await promise;
    expect(axios.post).toHaveBeenCalledTimes(3);
    vi.useRealTimers();
  });
});
