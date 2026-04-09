package com.loglens.ingestion.service;

import com.loglens.ingestion.api.dto.IngestBatchRequest;
import com.loglens.ingestion.api.dto.IngestBatchResponse;
import com.loglens.ingestion.api.dto.IngestLogRequest;
import com.loglens.ingestion.api.dto.IngestLogResponse;
import reactor.core.publisher.Mono;

public interface IngestionService {
    Mono<IngestLogResponse> ingest(IngestLogRequest request, String tenantId);
    Mono<IngestBatchResponse> ingestBatch(IngestBatchRequest request, String tenantId);
}
