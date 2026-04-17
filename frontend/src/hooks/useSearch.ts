import { useQuery } from '@tanstack/react-query';
import { searchLogs } from '../api/query';
import type { SearchRequest } from '../types';

export function useSearch(request: SearchRequest | null) {
  return useQuery({
    queryKey: ['search', request],
    queryFn: () => searchLogs(request!),
    enabled: request !== null && request.query.trim().length > 0,
    staleTime: 30_000,
  });
}
