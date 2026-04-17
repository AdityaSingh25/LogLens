import { AlertRuleList } from '../components/alerts/AlertRuleList';
import { AlertRuleBuilder } from '../components/alerts/AlertRuleBuilder';

export function AlertsPage() {
  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Alerts</h1>
          <p className="text-sm text-gray-500">Z-score anomaly detection + threshold rules</p>
        </div>
        <AlertRuleBuilder />
      </div>
      <AlertRuleList />
    </div>
  );
}
