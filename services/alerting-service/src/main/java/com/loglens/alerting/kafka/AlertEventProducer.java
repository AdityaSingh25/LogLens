package com.loglens.alerting.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEventProducer {

    static final String TOPIC = "alert-events";

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void publish(
            String alertId,
            String ruleId,
            String tenantId,
            String severity,
            String title,
            String description,
            List<String> notificationChannels,
            Map<String, Object> context
    ) {
        Map<String, Object> event = new HashMap<>();
        event.put("alert_id", alertId);
        event.put("rule_id", ruleId);
        event.put("tenant_id", tenantId);
        event.put("severity", severity);
        event.put("title", title);
        event.put("description", description);
        event.put("triggered_at", Instant.now().toString());
        event.put("notification_channels", notificationChannels);
        event.put("context", context);

        kafkaTemplate.send(TOPIC, tenantId, event);
        log.info("Published alert event: alertId={} rule={}", alertId, ruleId);
    }
}
