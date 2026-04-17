import { useState } from 'react';
import { useCreateAlertRule } from '../../hooks/useAlerts';
import type { AlertCondition, AlertSeverity } from '../../types';

export function AlertRuleBuilder() {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [severity, setSeverity] = useState<AlertSeverity>('MEDIUM');
  const [conditionType, setConditionType] = useState<'ZSCORE' | 'THRESHOLD'>('ZSCORE');
  const [threshold, setThreshold] = useState(100);
  const [windowMinutes, setWindowMinutes] = useState(5);
  const [serviceName, setServiceName] = useState('');
  const [channels, setChannels] = useState('');

  const createRule = useCreateAlertRule();

  const handleSubmit = () => {
    const condition: AlertCondition = {
      type: conditionType,
      metric: 'LOG_VOLUME',
      windowMinutes,
      ...(conditionType === 'THRESHOLD' ? { threshold } : { zscoreThreshold: 3.0 }),
      ...(serviceName ? { serviceName } : {}),
    };
    createRule.mutate({
      name,
      condition,
      severity,
      notificationChannels: channels.split(',').map(c => c.trim()).filter(Boolean),
      enabled: true,
    }, { onSuccess: () => { setOpen(false); setName(''); setChannels(''); } });
  };

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 transition-colors"
      >
        + New Rule
      </button>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
      <h3 className="font-semibold text-gray-900">New Alert Rule</h3>

      <div className="space-y-3">
        <input
          placeholder="Rule name"
          value={name}
          onChange={e => setName(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        <div className="flex gap-2">
          <select value={conditionType} onChange={e => setConditionType(e.target.value as 'ZSCORE' | 'THRESHOLD')}
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option value="ZSCORE">Z-Score Anomaly</option>
            <option value="THRESHOLD">Fixed Threshold</option>
          </select>
          <select value={severity} onChange={e => setSeverity(e.target.value as AlertSeverity)}
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm">
            <option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>CRITICAL</option>
          </select>
        </div>

        <div className="flex gap-2">
          <input type="number" value={windowMinutes} onChange={e => setWindowMinutes(+e.target.value)}
            placeholder="Window (min)" className="w-32 px-3 py-2 border border-gray-300 rounded-lg text-sm" />
          {conditionType === 'THRESHOLD' && (
            <input type="number" value={threshold} onChange={e => setThreshold(+e.target.value)}
              placeholder="Threshold" className="w-32 px-3 py-2 border border-gray-300 rounded-lg text-sm" />
          )}
          <input value={serviceName} onChange={e => setServiceName(e.target.value)}
            placeholder="Service (optional)" className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm" />
        </div>

        <input value={channels} onChange={e => setChannels(e.target.value)}
          placeholder="Channels: slack:C123, email:you@co.com, webhook:https://..."
          className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
      </div>

      <div className="flex gap-2">
        <button onClick={handleSubmit} disabled={!name}
          className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50">
          Create Rule
        </button>
        <button onClick={() => setOpen(false)}
          className="px-4 py-2 text-gray-600 text-sm rounded-lg hover:bg-gray-100">
          Cancel
        </button>
      </div>
    </div>
  );
}
