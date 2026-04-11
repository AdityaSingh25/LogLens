package com.loglens.parser.kafka;

import com.loglens.parser.domain.ParsedLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParsedLogProducer {

    static final String TOPIC = "parsed-logs";

    private final KafkaTemplate<String, ParsedLogMessage> kafkaTemplate;

    public void publish(ParsedLogMessage message) {
        String partitionKey = message.getTenantId() + ":" + message.getServiceName();
        kafkaTemplate.send(TOPIC, partitionKey, message);
        log.debug("Published parsed log {} to {}", message.getLogId(), TOPIC);
    }
}
