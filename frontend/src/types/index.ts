// Shared types — mirrors contracts/api-types

export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL';

export interface LogEntry {
  logId: string;
  tenantId: string;
  serviceName: string;
  timestamp: string;
  level: LogLevel;
  message: string;
  parsedFields: Record<string, unknown>;
  traceId?: string;
}

export interface SearchRequest {
  query: string;
  timeRange: { from: string; to: string };
  filters?: { services?: string[]; levels?: string[] };
  page?: number;
  pageSize?: number;
}

export interface SearchResult {
  logId: string;
  tenantId: string;
  serviceName: string;
  timestamp: string;
  level: LogLevel;
  message: string;
  parsedFields: Record<string, unknown>;
  traceId?: string;
  score: number;
  keywordScore?: number;
  vectorScore?: number;
  highlight?: string;
}

export interface SearchResponse {
  results: SearchResult[];
  total: number;
  tookMs: number;
  queryMode: 'HYBRID' | 'KEYWORD' | 'VECTOR';
}

export interface AlertRule {
  ruleId: string;
  tenantId: string;
  name: string;
  condition: AlertCondition;
  severity: AlertSeverity;
  notificationChannels: string[];
  enabled: boolean;
  createdAt: string;
}

export interface AlertCondition {
  type: 'THRESHOLD' | 'ZSCORE';
  metric: 'LOG_VOLUME' | 'ERROR_RATE';
  windowMinutes: number;
  threshold?: number;
  zscoreThreshold?: number;
  serviceName?: string;
}

export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
