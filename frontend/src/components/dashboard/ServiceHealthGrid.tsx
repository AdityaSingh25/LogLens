import { cn } from '../../lib/utils';
import type { ServiceHealth } from '../../api/ingestion';

interface ServiceHealthGridProps {
  services: ServiceHealth[];
}

const statusColor: Record<ServiceHealth['status'], string> = {
  healthy:  'bg-green-100 text-green-700 border-green-200',
  degraded: 'bg-amber-100 text-amber-700 border-amber-200',
  down:     'bg-red-100 text-red-700 border-red-200',
};

const statusDot: Record<ServiceHealth['status'], string> = {
  healthy:  'bg-green-500',
  degraded: 'bg-amber-500',
  down:     'bg-red-500',
};

export function ServiceHealthGrid({ services }: ServiceHealthGridProps) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4">
      <h3 className="text-sm font-semibold text-gray-700 mb-4">Service Health</h3>
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-4">
        {services.map(svc => (
          <div
            key={svc.serviceName}
            className={cn('rounded-lg border p-3', statusColor[svc.status])}
          >
            <div className="flex items-center gap-1.5 mb-1">
              <div className={cn('w-2 h-2 rounded-full', statusDot[svc.status])} />
              <span className="text-xs font-semibold truncate">{svc.serviceName}</span>
            </div>
            <p className="text-xs opacity-75">{(svc.errorRate * 100).toFixed(1)}% errors</p>
          </div>
        ))}
      </div>
    </div>
  );
}
