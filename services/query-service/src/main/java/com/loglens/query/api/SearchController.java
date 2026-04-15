package com.loglens.query.api;

import com.loglens.query.api.dto.SearchRequest;
import com.loglens.query.api.dto.SearchResponse;
import com.loglens.query.security.TenantContextHolder;
import com.loglens.query.service.QueryOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
public class SearchController {

    private final QueryOrchestrationService queryService;

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        String tenantId = TenantContextHolder.getTenantId();
        return ResponseEntity.ok(queryService.search(request, tenantId));
    }
}
