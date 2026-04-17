import { useQuery } from '@tanstack/react-query';
import { getLogVolume, getServiceHealth } from '../api/ingestion';

const DEFAULT_FROM = () => new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
const DEFAULT_TO   = () => new Date().toISOString();

export function useLogVolume() {
  return useQuery({
    queryKey: ['dashboard', 'volume'],
    queryFn: () => getLogVolume(DEFAULT_FROM(), DEFAULT_TO()),
    refetchInterval: 30_000,
    retry: 1,
  });
}

export function useServiceHealth() {
  return useQuery({
    queryKey: ['dashboard', 'services'],
    queryFn: getServiceHealth,
    refetchInterval: 30_000,
    retry: 1,
  });
}
