# LogLens Frontend Development

Use this skill when building or modifying the React frontend in `frontend/`.

---

## Stack

- **React 18** with TypeScript (strict)
- **TanStack Query v5** — all server state, no manual fetch/useEffect for data
- **Tailwind CSS** + **shadcn/ui** — all UI components
- **Recharts** — time-series charts (log volume, error rates, service health)
- **Vite** — dev server and build
- **WebSocket** — live log stream from ingestion-service

---

## Directory Structure

```
frontend/src/
├── components/
│   ├── ui/              # shadcn/ui generated components — DO NOT edit directly
│   ├── search/          # SearchBar, ResultList, LogCard
│   ├── stream/          # LiveLogStream, FilterChips
│   ├── clusters/        # SemanticClusterView
│   ├── alerts/          # AlertRuleBuilder, ConditionPreview
│   └── dashboard/       # VolumeChart, ErrorRateChart, ServiceHealthChart
├── pages/               # Route-level components (one per route)
├── hooks/               # Custom hooks (useSearch, useLiveStream, useAlerts)
├── api/                 # API client functions (one file per backend service)
├── types/               # Import from contracts/api-types — don't redefine
├── lib/                 # Utilities (date formatting, log level colors, etc.)
└── main.tsx
```

---

## Conventions

### Components
- Function components only, no class components
- Props interface defined directly above the component, named `<ComponentName>Props`
- No default exports for components — use named exports
- Keep components focused: presentational components receive data as props, don't fetch

### Data Fetching
Always use TanStack Query. Never fetch in `useEffect`.

```typescript
// Good
const { data: logs, isLoading } = useQuery({
  queryKey: ['search', query, timeRange],
  queryFn: () => searchLogs({ query, timeRange }),
  enabled: query.length > 0,
});

// Bad — never do this
useEffect(() => {
  fetch('/api/search').then(r => r.json()).then(setLogs);
}, [query]);
```

### Mutations
Use `useMutation` for writes. Invalidate relevant query keys on success.

```typescript
const createAlert = useMutation({
  mutationFn: (rule: AlertRule) => createAlertRule(rule),
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alerts'] }),
});
```

### Types
Import shared API types from `../../contracts/api-types/`. Define frontend-only types (UI state, form state) locally in `types/`. Never redefine types that exist in contracts.

### Tailwind & shadcn/ui
- Use shadcn/ui primitives (`Button`, `Input`, `Dialog`, `Card`, etc.) — don't build raw
- Add new shadcn components: `npx shadcn@latest add <component>` from `frontend/`
- Never edit files in `src/components/ui/` — they're generated and will be overwritten
- Compose shadcn primitives in `src/components/<feature>/` for feature-specific UI

### API Client Pattern
One file per backend service in `src/api/`. Functions are typed with shared contracts types.

```typescript
// src/api/query.ts
import type { SearchRequest, SearchResponse } from '../../../contracts/api-types';

export async function searchLogs(req: SearchRequest): Promise<SearchResponse> {
  const res = await fetch(`/api/query/search`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
    body: JSON.stringify(req),
  });
  if (!res.ok) throw new Error(`Search failed: ${res.status}`);
  return res.json();
}
```

---

## Live Log Stream (WebSocket)

The live stream hook connects to the ingestion-service WebSocket endpoint.

Key behaviors:
- Reconnect with exponential backoff on disconnect
- Buffer incoming messages, flush to state at 100ms intervals to avoid excessive re-renders
- Filter client-side using filter chip state (service name, log level, keyword)
- Cap displayed logs at 1000 entries, drop oldest when over limit

---

## Search Bar

The search bar is the product's wow moment. Handle these states clearly:
- **Empty** — show placeholder "Search logs in plain English..."
- **Typing** — debounce 300ms before firing query
- **Loading** — skeleton cards, not spinner
- **Results** — ranked list with relevance score, matched snippet highlighted
- **No results** — helpful empty state, suggest broadening time range

---

## Charts (Recharts)

- Use `ResponsiveContainer` always — never fixed pixel widths
- Time-series X axis: format with `date-fns`, use `{timeRange}` from dashboard state
- Consistent color palette: errors = `#ef4444`, warnings = `#f59e0b`, info = `#3b82f6`
- Tooltips show absolute timestamp + formatted value

---

## Testing

- **Vitest** + **React Testing Library**
- Test user behavior, not implementation: query by role/text, not by class name
- Mock `fetch` with `vi.stubGlobal` or `msw` for integration-style component tests
- Don't test TanStack Query internals — test the rendered output

---

## What to Do Now

Read the specific task. Identify which component/page/hook is involved. Read the relevant source files first. Shared type changes require updating `contracts/api-types/` first, then the frontend.
