package com.loglens.query.service;

import com.loglens.query.api.dto.SearchRequest;
import com.loglens.query.api.dto.SearchResponse;
import com.loglens.query.api.dto.SearchResponse.SearchResultDto;
import com.loglens.query.elasticsearch.EsSearchClient;
import com.loglens.query.qdrant.EmbeddingClient;
import com.loglens.query.qdrant.QdrantSearchClient;
import com.loglens.query.service.NlQueryTranslatorService.TranslatedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryOrchestrationService {

    private final NlQueryTranslatorService nlTranslator;
    private final EsSearchClient esClient;
    private final QdrantSearchClient qdrantClient;
    private final EmbeddingClient embeddingClient;
    private final RrfMergeService rrfMergeService;
    private final QueryCacheService cacheService;

    public SearchResponse search(SearchRequest request, String tenantId) {
        long start = System.currentTimeMillis();

        // Check cache
        String cacheKey = cacheService.buildKey(tenantId, request);
        SearchResponse cached = cacheService.get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for query '{}'", request.getQuery());
            return cached;
        }

        // Translate NL query
        TranslatedQuery translated = nlTranslator.translate(request.getQuery());
        log.debug("NL translation: keywords='{}' level='{}' service='{}'",
                translated.keywords(), translated.level(), translated.serviceName());

        // Run ES and Qdrant searches in parallel
        CompletableFuture<List<SearchResultDto>> esFuture = CompletableFuture.supplyAsync(() ->
                esClient.search(tenantId, translated, request, 50)
        );

        CompletableFuture<List<SearchResultDto>> qdrantFuture = CompletableFuture.supplyAsync(() -> {
            float[] queryVector = embeddingClient.embed(translated.keywords());
            if (queryVector == null) return List.of();
            return qdrantClient.search(tenantId, queryVector, 50);
        });

        List<SearchResultDto> esResults = esFuture.join();
        List<SearchResultDto> qdrantResults = qdrantFuture.join();

        String queryMode;
        List<SearchResultDto> merged;

        if (esResults.isEmpty() && qdrantResults.isEmpty()) {
            merged = List.of();
            queryMode = "HYBRID";
        } else if (qdrantResults.isEmpty()) {
            merged = esResults;
            queryMode = "KEYWORD";
        } else if (esResults.isEmpty()) {
            merged = qdrantResults;
            queryMode = "VECTOR";
        } else {
            merged = rrfMergeService.merge(esResults, qdrantResults);
            queryMode = "HYBRID";
        }

        // Apply pagination
        int from = request.getPage() * request.getPageSize();
        List<SearchResultDto> page = merged.stream()
                .skip(from)
                .limit(request.getPageSize())
                .toList();

        SearchResponse response = SearchResponse.builder()
                .results(page)
                .total(merged.size())
                .tookMs(System.currentTimeMillis() - start)
                .queryMode(queryMode)
                .build();

        cacheService.put(cacheKey, response);
        return response;
    }
}
