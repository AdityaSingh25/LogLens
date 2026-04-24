package com.loglens.parser.kafka;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import com.loglens.parser.elasticsearch.ElasticsearchIndexer;
import com.loglens.parser.service.LogParserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class RawLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(RawLogConsumer.class);

    private final LogParserServiceImpl parserService;
    private final ElasticsearchIndexer esIndexer;
    private final ParsedLogProducer parsedLogProducer;

    public RawLogConsumer(LogParserServiceImpl parserService, ElasticsearchIndexer esIndexer, ParsedLogProducer parsedLogProducer) {
        this.parserService = parserService;
        this.esIndexer = esIndexer;
        this.parsedLogProducer = parsedLogProducer;
    }

    @KafkaListener(
            topics = "raw-logs",
            groupId = "${spring.kafka.consumer.group-id:parser-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(RawLogMessage message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.debug("Received raw log {} from topic {}", message.getLogId(), topic);
            ParsedLogMessage parsed = parserService.parse(message);
            esIndexer.index(parsed);
            parsedLogProducer.publish(parsed);
        } catch (Exception e) {
            log.error("Failed to process log {}: {}", message.getLogId(), e.getMessage(), e);
            // Re-throw to trigger DLQ after retries
            throw new RuntimeException("Processing failed for log " + message.getLogId(), e);
        }
    }
}
