import { LiveLogStream } from '../components/stream/LiveLogStream';

export function StreamPage() {
  return (
    <div className="h-[calc(100vh-8rem)] flex flex-col gap-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Live Log Stream</h1>
        <p className="text-sm text-gray-500">Real-time log feed from all services</p>
      </div>
      <LiveLogStream />
    </div>
  );
}
