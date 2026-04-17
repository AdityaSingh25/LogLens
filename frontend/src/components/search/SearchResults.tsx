import { LogCard } from './LogCard';
import type { SearchResponse } from '../../types';

interface SearchResultsProps {
  response: SearchResponse;
}

export function SearchResults({ response }: SearchResultsProps) {
  if (response.results.length === 0) {
    return (
      <div className="text-center py-16 text-gray-400">
        <p className="text-lg">No logs found</p>
        <p className="text-sm mt-1">Try broadening your time range or rephrasing the query</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-3 text-sm text-gray-500">
        <span>{response.total} results</span>
        <span className="font-mono text-xs">
          {response.queryMode} · {response.tookMs}ms
        </span>
      </div>
      <div className="space-y-2">
        {response.results.map(result => (
          <LogCard key={result.logId} result={result} />
        ))}
      </div>
    </div>
  );
}
