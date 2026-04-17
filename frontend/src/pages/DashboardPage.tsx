import { VolumeChart } from '../components/dashboard/VolumeChart';
import { ServiceHealthGrid } from '../components/dashboard/ServiceHealthGrid';
import { useLogVolume, useServiceHealth } from '../hooks/useDashboard';

export function DashboardPage() {
  const { data: volumeData } = useLogVolume();
  const { data: services }   = useServiceHealth();

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Dashboard</h1>
        <p className="text-sm text-gray-500">Auto-refreshes every 30 seconds</p>
      </div>

      {volumeData ? (
        <VolumeChart data={volumeData} />
      ) : (
        <div className="h-52 bg-gray-100 rounded-xl animate-pulse" />
      )}

      {services ? (
        <ServiceHealthGrid services={services} />
      ) : (
        <div className="h-36 bg-gray-100 rounded-xl animate-pulse" />
      )}
    </div>
  );
}
