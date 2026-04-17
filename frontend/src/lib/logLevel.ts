export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL';

export const logLevelColor: Record<LogLevel, string> = {
  TRACE: 'bg-gray-100 text-gray-600',
  DEBUG: 'bg-gray-200 text-gray-700',
  INFO:  'bg-blue-100 text-blue-700',
  WARN:  'bg-amber-100 text-amber-700',
  ERROR: 'bg-red-100 text-red-700',
  FATAL: 'bg-red-200 text-red-900',
};

export const logLevelDot: Record<LogLevel, string> = {
  TRACE: 'bg-gray-400',
  DEBUG: 'bg-gray-500',
  INFO:  'bg-blue-500',
  WARN:  'bg-amber-500',
  ERROR: 'bg-red-500',
  FATAL: 'bg-red-700',
};

export function getLevelColor(level: string): string {
  return logLevelColor[level as LogLevel] ?? 'bg-gray-100 text-gray-600';
}

export function getLevelDot(level: string): string {
  return logLevelDot[level as LogLevel] ?? 'bg-gray-400';
}
