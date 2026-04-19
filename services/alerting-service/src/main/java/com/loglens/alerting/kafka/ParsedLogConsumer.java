package com.loglens.alerting.kafka;

import com.loglens.alerting.service.WindowAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParsedLogConsumer {

    private final WindowAggregatorService windowAggregator;

    @KafkaListener(
            topics = "parsed-logs",
            groupId = "${spring.kafka.consumer.group-id:alerting-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> message) {
        try {
            String tenantId   = (String) message.get("tenantId");
            String serviceName = (String) message.get("serviceName");
            if (tenantId != null && serviceName != null) {
                windowAggregator.increment(tenantId, serviceName);
            }
        } catch (Exception e) {
            // Per-message error handling — never crash the consumer
            log.error("Error processing message in alerting consumer: {}", e.getMessage());
        }
    }
}
