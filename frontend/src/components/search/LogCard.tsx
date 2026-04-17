import { getLevelColor } from '../../lib/logLevel';
import { cn } from '../../lib/utils';
import type { SearchResult } from '../../types';

interface LogCardProps {
  result: SearchResult;
}

export function LogCard({ result }: LogCardProps) {
  const time = new Date(result.timestamp).toLocaleTimeString();

  return (
    <div className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-2 flex-shrink-0">
          <span className={cn('text-xs font-mono font-semibold px-2 py-0.5 rounded', getLevelColor(result.level))}>
            {result.level}
          </span>
          <span className="text-xs text-gray-500 font-mono">{result.serviceName}</span>
        </div>
        <div className="flex items-center gap-3 flex-shrink-0">
          <span className="text-xs text-gray-400 font-mono">{time}</span>
          <span className="text-xs text-gray-300 font-mono">
            {result.score.toFixed(4)}
          </span>
        </div>
      </div>
      <p className="mt-2 text-sm text-gray-800 font-mono leading-relaxed">
        {result.highlight ?? result.message}
      </p>
      {result.traceId && (
        <p className="mt-1 text-xs text-gray-400 font-mono">
          trace: {result.traceId}
        </p>
      )}
    </div>
  );
}
