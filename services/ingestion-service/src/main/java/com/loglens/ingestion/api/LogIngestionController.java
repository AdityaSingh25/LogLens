package com.loglens.ingestion.api;

import com.loglens.ingestion.api.dto.*;
import com.loglens.ingestion.security.TenantContextHolder;
import com.loglens.ingestion.service.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class LogIngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/logs")
    public Mono<ResponseEntity<IngestLogResponse>> ingest(@Valid @RequestBody IngestLogRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> ingestionService.ingest(request, tenantId))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/logs/batch")
    public Mono<ResponseEntity<IngestBatchResponse>> ingestBatch(@Valid @RequestBody IngestBatchRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> ingestionService.ingestBatch(request, tenantId))
                .map(ResponseEntity::ok);
    }
}
