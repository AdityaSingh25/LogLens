package com.loglens.parser.kafka;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import com.loglens.parser.elasticsearch.ElasticsearchIndexer;
import com.loglens.parser.service.LogParserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RawLogConsumer {

    private final LogParserServiceImpl parserService;
    private final ElasticsearchIndexer esIndexer;
    private final ParsedLogProducer parsedLogProducer;

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
