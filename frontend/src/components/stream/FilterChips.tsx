import { cn } from '../../lib/utils';
import type { LogLevel } from '../../types';

const LEVELS: LogLevel[] = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL'];

interface FilterChipsProps {
  selectedLevels: LogLevel[];
  onToggleLevel: (level: LogLevel) => void;
  onClear: () => void;
}

export function FilterChips({ selectedLevels, onToggleLevel, onClear }: FilterChipsProps) {
  return (
    <div className="flex items-center gap-2 flex-wrap">
      <span className="text-xs text-gray-500">Filter:</span>
      {LEVELS.map(level => (
        <button
          key={level}
          onClick={() => onToggleLevel(level)}
          className={cn(
            'px-2 py-0.5 rounded text-xs font-mono font-semibold transition-opacity',
            selectedLevels.includes(level) ? 'opacity-100' : 'opacity-30',
            level === 'ERROR' || level === 'FATAL' ? 'bg-red-100 text-red-700' :
            level === 'WARN'  ? 'bg-amber-100 text-amber-700' :
            level === 'INFO'  ? 'bg-blue-100 text-blue-700' :
                                'bg-gray-100 text-gray-600'
          )}
        >
          {level}
        </button>
      ))}
      <button onClick={onClear} className="text-xs text-gray-400 hover:text-gray-600 ml-2">
        clear stream
      </button>
    </div>
  );
}
