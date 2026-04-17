import { useState } from 'react';
import { useLiveStream } from '../../hooks/useLiveStream';
import { FilterChips } from './FilterChips';
import { getLevelColor, getLevelDot } from '../../lib/logLevel';
import { cn } from '../../lib/utils';
import type { LogLevel } from '../../types';

export function LiveLogStream() {
  const { logs, connected, clear } = useLiveStream();
  const [selectedLevels, setSelectedLevels] = useState<LogLevel[]>(
    ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL']
  );

  const toggleLevel = (level: LogLevel) => {
    setSelectedLevels(prev =>
      prev.includes(level) ? prev.filter(l => l !== level) : [...prev, level]
    );
  };

  const filtered = logs.filter(log => selectedLevels.includes(log.level as LogLevel));

  return (
    <div className="flex flex-col gap-3 h-full">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className={cn('w-2 h-2 rounded-full', connected ? 'bg-green-500 animate-pulse' : 'bg-gray-400')} />
          <span className="text-sm text-gray-600">
            {connected ? 'Live' : 'Disconnected'} · {filtered.length} logs
          </span>
        </div>
      </div>

      <FilterChips selectedLevels={selectedLevels} onToggleLevel={toggleLevel} onClear={clear} />

      <div className="flex-1 overflow-y-auto font-mono text-xs space-y-0.5 bg-gray-950 rounded-xl p-3">
        {filtered.length === 0 ? (
          <p className="text-gray-500 text-center py-8">Waiting for logs...</p>
        ) : (
          filtered.map(log => (
            <div key={log.logId} className="flex gap-2 text-gray-300 hover:bg-gray-800 px-1 rounded">
              <span className="text-gray-500 flex-shrink-0">
                {new Date(log.timestamp).toLocaleTimeString()}
              </span>
              <span className={cn('flex-shrink-0 w-10 text-center rounded text-xs', getLevelColor(log.level))}>
                {log.level.substring(0, 4)}
              </span>
              <span className="text-blue-400 flex-shrink-0 w-28 truncate">{log.serviceName}</span>
              <span className="text-gray-200 truncate">{log.message}</span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
