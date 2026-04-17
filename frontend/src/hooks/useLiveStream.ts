import { useEffect, useRef, useState, useCallback } from 'react';
import { getToken } from '../api/auth';
import type { LogEntry } from '../types';

const MAX_LOGS = 1000;
const FLUSH_INTERVAL_MS = 100;

export function useLiveStream() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [connected, setConnected] = useState(false);
  const buffer = useRef<LogEntry[]>([]);
  const wsRef = useRef<WebSocket | null>(null);
  const retryTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);
  const retryCount = useRef(0);

  const connect = useCallback(() => {
    const token = getToken();
    const ws = new WebSocket(`ws://localhost:8082/ws/logs?token=${token}`);
    wsRef.current = ws;

    ws.onopen = () => {
      setConnected(true);
      retryCount.current = 0;
    };

    ws.onmessage = (event) => {
      try {
        const log: LogEntry = JSON.parse(event.data);
        buffer.current.push(log);
      } catch {
        // ignore malformed messages
      }
    };

    ws.onclose = () => {
      setConnected(false);
      // Exponential backoff: 1s, 2s, 4s, 8s, max 30s
      const delay = Math.min(1000 * Math.pow(2, retryCount.current), 30_000);
      retryCount.current++;
      retryTimeout.current = setTimeout(connect, delay);
    };

    ws.onerror = () => ws.close();
  }, []);

  // Flush buffer to state every 100ms
  useEffect(() => {
    const interval = setInterval(() => {
      if (buffer.current.length === 0) return;
      const incoming = buffer.current.splice(0);
      setLogs(prev => [...prev, ...incoming].slice(-MAX_LOGS));
    }, FLUSH_INTERVAL_MS);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    connect();
    return () => {
      wsRef.current?.close();
      if (retryTimeout.current) clearTimeout(retryTimeout.current);
    };
  }, [connect]);

  const clear = useCallback(() => setLogs([]), []);

  return { logs, connected, clear };
}
