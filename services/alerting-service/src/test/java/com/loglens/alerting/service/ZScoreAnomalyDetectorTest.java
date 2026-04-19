package com.loglens.alerting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZScoreAnomalyDetectorTest {

    private ZScoreAnomalyDetector detector;
    private WindowAggregatorService windowAggregator;

    @BeforeEach
    void setUp() throws Exception {
        windowAggregator = new WindowAggregatorService();
        detector = new ZScoreAnomalyDetector(windowAggregator);

        // Set threshold via reflection (normally injected by Spring)
        var thresholdField = ZScoreAnomalyDetector.class.getDeclaredField("zscoreThreshold");
        thresholdField.setAccessible(true);
        thresholdField.set(detector, 3.0);

        var historyField = ZScoreAnomalyDetector.class.getDeclaredField("historyCount");
        historyField.setAccessible(true);
        historyField.set(detector, 20);
    }

    @Test
    void noAnomaly_whenVolumeIsStable() {
        // Simulate 10 windows of steady traffic (~100 logs each)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100; j++) windowAggregator.increment("t1", "payment-service");
            windowAggregator.flushBucket(20);
        }
        assertThat(detector.isAnomaly("t1", "payment-service")).isFalse();
    }

    @Test
    void anomalyFires_whenVolumeSpikes() {
        // 15 normal windows
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 10; j++) windowAggregator.increment("t2", "order-service");
            windowAggregator.flushBucket(20);
        }
        // Massive spike
        for (int j = 0; j < 1000; j++) windowAggregator.increment("t2", "order-service");
        windowAggregator.flushBucket(20);

        assertThat(detector.isAnomaly("t2", "order-service")).isTrue();
    }

    @Test
    void noAnomaly_whenInsufficientHistory() {
        // Only 2 windows — not enough
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 100; j++) windowAggregator.increment("t3", "auth-service");
            windowAggregator.flushBucket(20);
        }
        assertThat(detector.isAnomaly("t3", "auth-service")).isFalse();
    }

    @Test
    void noAnomaly_whenNoDataForService() {
        assertThat(detector.isAnomaly("t4", "unknown-service")).isFalse();
    }
}
