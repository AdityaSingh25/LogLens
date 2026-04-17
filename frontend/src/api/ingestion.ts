import { getToken } from './auth';

export interface VolumeDataPoint {
  timestamp: string;
  count: number;
  errorCount: number;
}

export interface ServiceHealth {
  serviceName: string;
  status: 'healthy' | 'degraded' | 'down';
  errorRate: number;
  logCount: number;
}

export async function getLogVolume(from: string, to: string): Promise<VolumeDataPoint[]> {
  const res = await fetch(`/query/stats/volume?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`, {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error('Failed to fetch volume data');
  return res.json();
}

export async function getServiceHealth(): Promise<ServiceHealth[]> {
  const res = await fetch('/query/stats/services', {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error('Failed to fetch service health');
  return res.json();
}
