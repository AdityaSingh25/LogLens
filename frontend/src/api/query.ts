import { getToken } from './auth';
import type { SearchRequest, SearchResponse } from '../types';

export async function searchLogs(req: SearchRequest): Promise<SearchResponse> {
  const res = await fetch('/query/search', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getToken()}`,
    },
    body: JSON.stringify(req),
  });
  if (!res.ok) throw new Error(`Search failed: ${res.status}`);
  return res.json();
}

export async function getSavedQueries() {
  const res = await fetch('/query/saved-queries', {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error('Failed to fetch saved queries');
  return res.json();
}
