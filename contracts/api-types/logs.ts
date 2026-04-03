export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL';

export interface IngestLogRequest {
  service_name: string;
  level: LogLevel;
  message: string;
  timestamp?: string;
  metadata?: Record<string, string>;
  trace_id?: string;
  span_id?: string;
}

export interface IngestLogResponse {
  log_id: string;
  accepted_at: string;
}

export interface IngestBatchRequest {
  logs: IngestLogRequest[];
}

export interface IngestBatchResponse {
  accepted: number;
  rejected: number;
  log_ids: string[];
}

export interface LogEntry {
  log_id: string;
  tenant_id: string;
  service_name: string;
  timestamp: string;
  level: LogLevel;
  message: string;
  parsed_fields: Record<string, string>;
  trace_id?: string;
}
