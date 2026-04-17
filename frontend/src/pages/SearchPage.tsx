import { useState, useCallback } from 'react';
import { SearchBar } from '../components/search/SearchBar';
import { SearchResults } from '../components/search/SearchResults';
import { useSearch } from '../hooks/useSearch';
import type { SearchRequest } from '../types';

const DEFAULT_TIME_RANGE = {
  from: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
  to: new Date().toISOString(),
};

export function SearchPage() {
  const [searchRequest, setSearchRequest] = useState<SearchRequest | null>(null);

  const handleSearch = useCallback((query: string) => {
    setSearchRequest({
      query,
      timeRange: DEFAULT_TIME_RANGE,
      pageSize: 20,
    });
  }, []);

  const { data, isLoading, isError } = useSearch(searchRequest);

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Search Logs</h1>
        <p className="text-sm text-gray-500">Ask in plain English — semantic + keyword hybrid search</p>
      </div>

      <SearchBar onSearch={handleSearch} isLoading={isLoading} />

      {isError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">
          Search failed. Make sure all services are running.
        </div>
      )}

      {isLoading && (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="h-16 bg-gray-200 rounded-lg animate-pulse" />
          ))}
        </div>
      )}

      {data && !isLoading && <SearchResults response={data} />}

      {!searchRequest && !isLoading && (
        <div className="text-center py-24 text-gray-400">
          <p className="text-lg font-medium">Start searching your logs</p>
          <p className="text-sm mt-1">Try: "payment errors in the last hour" or "database timeouts"</p>
        </div>
      )}
    </div>
  );
}
