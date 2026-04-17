import { getToken } from './auth';
import type { AlertRule } from '../types';

export async function getAlertRules(): Promise<AlertRule[]> {
  const res = await fetch('/alerting/rules', {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error('Failed to fetch alert rules');
  return res.json();
}

export async function createAlertRule(rule: Omit<AlertRule, 'ruleId' | 'tenantId' | 'createdAt'>) {
  const res = await fetch('/alerting/rules', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getToken()}`,
    },
    body: JSON.stringify(rule),
  });
  if (!res.ok) throw new Error('Failed to create alert rule');
  return res.json();
}

export async function toggleAlertRule(ruleId: string, enabled: boolean) {
  const res = await fetch(`/alerting/rules/${ruleId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getToken()}`,
    },
    body: JSON.stringify({ enabled }),
  });
  if (!res.ok) throw new Error('Failed to toggle alert rule');
  return res.json();
}
