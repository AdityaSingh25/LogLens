package com.loglens.alerting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class ZScoreAnomalyDetector {

    @Value("${alerting.zscore.threshold:3.0}")
    private double zscoreThreshold;

    @Value("${alerting.window.history-count:20}")
    private int historyCount;

    private final WindowAggregatorService windowAggregator;

    /**
     * Returns true if the latest window count is anomalously high.
     * Requires at least 3 historical windows to be meaningful.
     */
    public boolean isAnomaly(String tenantId, String serviceName) {
        Deque<Long> window = windowAggregator.getWindow(tenantId, serviceName);

        if (window.size() < 3) return false; // insufficient history

        long[] values = window.stream().mapToLong(Long::longValue).toArray();
        long current = values[values.length - 1];

        // Calculate mean and stddev over all but the current value
        double[] history = new double[values.length - 1];
        for (int i = 0; i < history.length; i++) history[i] = values[i];

        double mean = mean(history);
        double stddev = stddev(history, mean);

        if (stddev == 0) return false; // no variance — can't compute z-score

        double zScore = (current - mean) / stddev;
        return zScore > zscoreThreshold;
    }

    public double getZScore(String tenantId, String serviceName) {
        Deque<Long> window = windowAggregator.getWindow(tenantId, serviceName);
        if (window.size() < 3) return 0.0;

        long[] values = window.stream().mapToLong(Long::longValue).toArray();
        long current = values[values.length - 1];
        double[] history = new double[values.length - 1];
        for (int i = 0; i < history.length; i++) history[i] = values[i];

        double mean = mean(history);
        double stddev = stddev(history, mean);
        return stddev == 0 ? 0 : (current - mean) / stddev;
    }

    private double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private double stddev(double[] values, double mean) {
        double variance = 0;
        for (double v : values) variance += (v - mean) * (v - mean);
        return Math.sqrt(variance / values.length);
    }
}
