package com.loglens.query.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.loglens.query.api.dto.SearchRequest;
import com.loglens.query.api.dto.SearchResponse.SearchResultDto;
import com.loglens.query.service.NlQueryTranslatorService.TranslatedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EsSearchClient {

    private final ElasticsearchClient esClient;

    public List<SearchResultDto> search(
            String tenantId,
            TranslatedQuery translated,
            SearchRequest request,
            int size
    ) {
        try {
            String indexPattern = "logs-" + tenantId + "-*";

            List<Query> mustQueries = new ArrayList<>();

            // Full-text search on message
            if (translated.keywords() != null && !translated.keywords().isBlank()) {
                mustQueries.add(Query.of(q -> q
                        .multiMatch(m -> m
                                .fields("message", "normalized_message")
                                .query(translated.keywords())
                                .type(TextQueryType.BestFields)
                        )
                ));
            }

            // Time range filter
            mustQueries.add(Query.of(q -> q
                    .range(r -> r
                            .field("timestamp")
                            .gte(co.elastic.clients.json.JsonData.of(request.getTimeRange().getFrom()))
                            .lte(co.elastic.clients.json.JsonData.of(request.getTimeRange().getTo()))
                    )
            ));

            // Optional filters
            if (translated.level() != null) {
                mustQueries.add(Query.of(q -> q.term(t -> t.field("level").value(translated.level()))));
            }
            if (translated.serviceName() != null) {
                mustQueries.add(Query.of(q -> q.term(t -> t.field("service_name").value(translated.serviceName()))));
            }
            if (request.getFilters() != null) {
                if (request.getFilters().getServices() != null) {
                    request.getFilters().getServices().forEach(svc ->
                            mustQueries.add(Query.of(q -> q.term(t -> t.field("service_name").value(svc))))
                    );
                }
                if (request.getFilters().getLevels() != null) {
                    request.getFilters().getLevels().forEach(lvl ->
                            mustQueries.add(Query.of(q -> q.term(t -> t.field("level").value(lvl))))
                    );
                }
            }

            Query finalQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

            SearchResponse<Map> response = esClient.search(s -> s
                    .index(indexPattern)
                    .query(finalQuery)
                    .size(size)
                    .sort(so -> so.field(f -> f.field("_score").order(SortOrder.Desc)))
            , Map.class);

            return response.hits().hits().stream()
                    .map(this::toSearchResult)
                    .toList();

        } catch (Exception e) {
            log.error("ES search failed for tenant {}: {}", tenantId, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private SearchResultDto toSearchResult(Hit<Map> hit) {
        Map<String, Object> src = hit.source() != null ? hit.source() : Map.of();
        return SearchResultDto.builder()
                .logId((String) src.getOrDefault("log_id", hit.id()))
                .tenantId((String) src.get("tenant_id"))
                .serviceName((String) src.get("service_name"))
                .timestamp((String) src.get("timestamp"))
                .level((String) src.get("level"))
                .message((String) src.get("message"))
                .parsedFields((Map<String, Object>) src.getOrDefault("parsed_fields", Map.of()))
                .traceId((String) src.get("trace_id"))
                .score(hit.score() != null ? hit.score() : 0.0)
                .build();
    }
}
