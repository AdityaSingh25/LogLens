import type { LogEntry, LogLevel } from './logs';

export interface TimeRange {
  from: string;
  to: string;
}

export interface SearchFilters {
  services?: string[];
  levels?: LogLevel[];
}

export interface SearchRequest {
  query: string;
  time_range: TimeRange;
  filters?: SearchFilters;
  page?: number;
  page_size?: number;
}

export interface SearchResult {
  log: LogEntry;
  score: number;
  keyword_score?: number;
  vector_score?: number;
  highlight?: string;
}

export interface SearchResponse {
  results: SearchResult[];
  total: number;
  took_ms: number;
  query_mode: 'HYBRID' | 'KEYWORD' | 'VECTOR';
}

export interface SavedQuery {
  query_id: string;
  name: string;
  query: string;
  filters?: SearchFilters;
  created_at: string;
}
