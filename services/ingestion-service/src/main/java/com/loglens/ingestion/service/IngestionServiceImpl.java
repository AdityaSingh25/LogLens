package com.loglens.ingestion.service;

import com.loglens.ingestion.api.dto.*;
import com.loglens.ingestion.domain.RawLogMessage;
import com.loglens.ingestion.kafka.RawLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionServiceImpl implements IngestionService {

    private final RawLogProducer producer;

    @Override
    public Mono<IngestLogResponse> ingest(IngestLogRequest request, String tenantId) {
        RawLogMessage message = buildMessage(request, tenantId);
        return producer.publish(message)
                .thenReturn(IngestLogResponse.builder()
                        .logId(message.getLogId())
                        .acceptedAt(Instant.now().toString())
                        .build());
    }

    @Override
    public Mono<IngestBatchResponse> ingestBatch(IngestBatchRequest request, String tenantId) {
        return Flux.fromIterable(request.getLogs())
                .map(r -> buildMessage(r, tenantId))
                .flatMap(msg -> producer.publish(msg).thenReturn(msg.getLogId()))
                .collectList()
                .map(ids -> IngestBatchResponse.builder()
                        .accepted(ids.size())
                        .rejected(0)
                        .logIds(ids)
                        .build());
    }

    private RawLogMessage buildMessage(IngestLogRequest request, String tenantId) {
        String rawBody = request.getMessage(); // For now raw_body == message; parser will enrich
        Instant ts = request.getTimestamp() != null
                ? Instant.parse(request.getTimestamp())
                : Instant.now();

        return RawLogMessage.builder()
                .logId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .serviceName(request.getServiceName())
                .timestamp(ts)
                .level(request.getLevel())
                .message(request.getMessage())
                .rawBody(rawBody)
                .sourceFormat("PLAINTEXT")
                .metadata(request.getMetadata())
                .traceId(request.getTraceId())
                .spanId(request.getSpanId())
                .build();
    }
}
