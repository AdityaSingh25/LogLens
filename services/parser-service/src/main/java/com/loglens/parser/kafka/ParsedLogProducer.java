package com.loglens.parser.kafka;

import com.loglens.parser.domain.ParsedLogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ParsedLogProducer {

    private static final Logger log = LoggerFactory.getLogger(ParsedLogProducer.class);
    static final String TOPIC = "parsed-logs";

    private final KafkaTemplate<String, ParsedLogMessage> kafkaTemplate;

    public ParsedLogProducer(KafkaTemplate<String, ParsedLogMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ParsedLogMessage message) {
        String partitionKey = message.getTenantId() + ":" + message.getServiceName();
        kafkaTemplate.send(TOPIC, partitionKey, message);
        log.debug("Published parsed log {} to {}", message.getLogId(), TOPIC);
    }
}
