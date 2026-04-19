package com.loglens.alerting.service;

import com.loglens.alerting.domain.AlertFiring;
import com.loglens.alerting.domain.AlertRule;
import com.loglens.alerting.kafka.AlertEventProducer;
import com.loglens.alerting.repository.AlertFiringRepository;
import com.loglens.alerting.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationService {

    private final AlertRuleRepository ruleRepository;
    private final AlertFiringRepository firingRepository;
    private final ZScoreAnomalyDetector zscoreDetector;
    private final WindowAggregatorService windowAggregator;
    private final AlertEventProducer alertEventProducer;

    @Value("${alerting.window.history-count:20}")
    private int historyCount;

    @Scheduled(fixedRateString = "${alerting.evaluation.interval-ms:60000}")
    @Transactional
    public void evaluate() {
        windowAggregator.flushBucket(historyCount);

        List<AlertRule> enabledRules = ruleRepository.findByEnabledTrue();
        if (enabledRules.isEmpty()) return;

        log.debug("Evaluating {} alert rules", enabledRules.size());

        for (AlertRule rule : enabledRules) {
            try {
                evaluateRule(rule);
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getId(), e.getMessage());
            }
        }
    }

    private void evaluateRule(AlertRule rule) {
        Map<String, Object> condition = rule.getCondition();
        String type = (String) condition.getOrDefault("type", "THRESHOLD");
        String serviceName = (String) condition.get("service_name");

        boolean shouldFire = switch (type) {
            case "ZSCORE" -> evaluateZScore(rule, serviceName);
            case "THRESHOLD" -> evaluateThreshold(rule, condition, serviceName);
            default -> false;
        };

        if (shouldFire) {
            fireAlert(rule, serviceName);
        }
    }

    private boolean evaluateZScore(AlertRule rule, String serviceName) {
        if (serviceName == null) {
            // Check all known services for this tenant
            return windowAggregator.getAllWindows().keySet().stream()
                    .filter(key -> key.startsWith(rule.getTenantId().toString() + ":"))
                    .map(key -> key.split(":")[1])
                    .anyMatch(svc -> zscoreDetector.isAnomaly(rule.getTenantId().toString(), svc));
        }
        return zscoreDetector.isAnomaly(rule.getTenantId().toString(), serviceName);
    }

    private boolean evaluateThreshold(AlertRule rule, Map<String, Object> condition, String serviceName) {
        Number thresholdNum = (Number) condition.get("threshold");
        if (thresholdNum == null) return false;
        long threshold = thresholdNum.longValue();

        var window = windowAggregator.getWindow(
                rule.getTenantId().toString(),
                serviceName != null ? serviceName : "all"
        );
        if (window.isEmpty()) return false;

        long latest = window.peekLast();
        return latest > threshold;
    }

    private void fireAlert(AlertRule rule, String serviceName) {
        double zScore = zscoreDetector.getZScore(rule.getTenantId().toString(),
                serviceName != null ? serviceName : "unknown");

        String title = String.format("[%s] %s", rule.getSeverity(), rule.getName());
        String description = serviceName != null
                ? String.format("Anomaly detected in %s (z-score: %.2f)", serviceName, zScore)
                : String.format("Alert rule '%s' triggered", rule.getName());

        // Persist firing
        AlertFiring firing = new AlertFiring();
        firing.setRuleId(rule.getId());
        firing.setTenantId(rule.getTenantId());
        firing.setContext(Map.of(
                "service_name", serviceName != null ? serviceName : "unknown",
                "z_score", zScore,
                "rule_name", rule.getName()
        ));
        firingRepository.save(firing);

        // Publish to Kafka
        alertEventProducer.publish(
                UUID.randomUUID().toString(),
                rule.getId().toString(),
                rule.getTenantId().toString(),
                rule.getSeverity(),
                title,
                description,
                rule.getNotificationChannels(),
                firing.getContext()
        );

        log.info("Alert fired: rule={} tenant={} service={}", rule.getId(), rule.getTenantId(), serviceName);
    }
}
