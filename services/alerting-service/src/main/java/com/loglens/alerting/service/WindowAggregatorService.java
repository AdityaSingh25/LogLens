package com.loglens.alerting.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains sliding window log counts per tenant:serviceName key.
 * Each entry in the deque represents the count for one 1-minute bucket.
 */
@Service
public class WindowAggregatorService {

    // key = "tenantId:serviceName", value = deque of per-minute counts
    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();
    private final Map<String, Long> currentBucket = new ConcurrentHashMap<>();

    public void increment(String tenantId, String serviceName) {
        String key = tenantId + ":" + serviceName;
        currentBucket.merge(key, 1L, Long::sum);
    }

    /**
     * Called every minute by the scheduler. Flushes current bucket into the window.
     * Keeps the last N=20 windows.
     */
    public void flushBucket(int historySize) {
        for (Map.Entry<String, Long> entry : currentBucket.entrySet()) {
            String key = entry.getKey();
            long count = entry.getValue();
            currentBucket.put(key, 0L);

            Deque<Long> window = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
            window.addLast(count);
            while (window.size() > historySize) {
                window.removeFirst();
            }
        }
    }

    public Deque<Long> getWindow(String tenantId, String serviceName) {
        return windows.getOrDefault(tenantId + ":" + serviceName, new ArrayDeque<>());
    }

    public Map<String, Deque<Long>> getAllWindows() {
        return windows;
    }
}
