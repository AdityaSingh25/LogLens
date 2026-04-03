export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface AlertCondition {
  type: 'THRESHOLD' | 'ZSCORE';
  metric: 'LOG_VOLUME' | 'ERROR_RATE';
  window_minutes: number;
  threshold?: number;
  zscore_threshold?: number;
  service_name?: string;
}

export interface AlertRule {
  rule_id: string;
  tenant_id: string;
  name: string;
  condition: AlertCondition;
  severity: AlertSeverity;
  notification_channels: string[];
  enabled: boolean;
  created_at: string;
}

export interface AlertRuleCreateRequest {
  name: string;
  condition: AlertCondition;
  severity: AlertSeverity;
  notification_channels: string[];
}

export interface AlertEvent {
  alert_id: string;
  rule_id: string;
  title: string;
  description: string;
  severity: AlertSeverity;
  triggered_at: string;
  context: Record<string, unknown>;
}
