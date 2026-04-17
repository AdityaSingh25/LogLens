import { useAlertRules, useToggleAlertRule } from '../../hooks/useAlerts';
import { cn } from '../../lib/utils';

const severityColor: Record<string, string> = {
  LOW:      'bg-gray-100 text-gray-700',
  MEDIUM:   'bg-blue-100 text-blue-700',
  HIGH:     'bg-amber-100 text-amber-700',
  CRITICAL: 'bg-red-100 text-red-700',
};

export function AlertRuleList() {
  const { data: rules, isLoading } = useAlertRules();
  const toggle = useToggleAlertRule();

  if (isLoading) {
    return <div className="space-y-2">{Array.from({length: 3}).map((_,i) => (
      <div key={i} className="h-14 bg-gray-100 rounded-lg animate-pulse" />
    ))}</div>;
  }

  if (!rules?.length) {
    return <p className="text-gray-400 text-sm text-center py-8">No alert rules yet.</p>;
  }

  return (
    <div className="space-y-2">
      {rules.map(rule => (
        <div key={rule.ruleId} className="flex items-center justify-between p-4 bg-white rounded-xl border border-gray-200">
          <div className="flex items-center gap-3">
            <span className={cn('text-xs font-semibold px-2 py-0.5 rounded', severityColor[rule.severity])}>
              {rule.severity}
            </span>
            <div>
              <p className="text-sm font-medium text-gray-900">{rule.name}</p>
              <p className="text-xs text-gray-500">
                {rule.condition.type} · {rule.condition.windowMinutes}min window
              </p>
            </div>
          </div>
          <button
            onClick={() => toggle.mutate({ ruleId: rule.ruleId, enabled: !rule.enabled })}
            className={cn(
              'relative w-10 h-5 rounded-full transition-colors',
              rule.enabled ? 'bg-blue-500' : 'bg-gray-300'
            )}
          >
            <span className={cn(
              'absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform',
              rule.enabled ? 'translate-x-5' : 'translate-x-0.5'
            )} />
          </button>
        </div>
      ))}
    </div>
  );
}
