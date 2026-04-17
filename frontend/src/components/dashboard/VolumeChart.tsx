import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend,
} from 'recharts';
import { format } from 'date-fns';
import type { VolumeDataPoint } from '../../api/ingestion';

interface VolumeChartProps {
  data: VolumeDataPoint[];
}

export function VolumeChart({ data }: VolumeChartProps) {
  const formatted = data.map(d => ({
    ...d,
    time: format(new Date(d.timestamp), 'HH:mm'),
  }));

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4">
      <h3 className="text-sm font-semibold text-gray-700 mb-4">Log Volume (24h)</h3>
      <ResponsiveContainer width="100%" height={200}>
        <AreaChart data={formatted}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="time" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 11 }} />
          <Tooltip />
          <Legend />
          <Area type="monotone" dataKey="count"      name="Total"  stroke="#3b82f6" fill="#eff6ff" strokeWidth={2} />
          <Area type="monotone" dataKey="errorCount" name="Errors" stroke="#ef4444" fill="#fef2f2" strokeWidth={2} />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
