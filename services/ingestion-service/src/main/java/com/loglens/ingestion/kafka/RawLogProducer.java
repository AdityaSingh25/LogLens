package com.loglens.ingestion.kafka;

import com.loglens.ingestion.domain.RawLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RawLogProducer {

    static final String TOPIC = "raw-logs";

    private final KafkaTemplate<String, RawLogMessage> kafkaTemplate;

    public Mono<Void> publish(RawLogMessage message) {
        String partitionKey = message.getTenantId() + ":" + message.getServiceName();
        return Mono.fromFuture(
                kafkaTemplate.send(TOPIC, partitionKey, message).toCompletableFuture()
        ).doOnSuccess(r -> log.debug("Published log {} to {}", message.getLogId(), TOPIC))
         .doOnError(e -> log.error("Failed to publish log {}", message.getLogId(), e))
         .then();
    }
}
