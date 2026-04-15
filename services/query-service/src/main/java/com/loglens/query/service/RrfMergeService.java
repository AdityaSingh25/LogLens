package com.loglens.query.service;

import com.loglens.query.api.dto.SearchResponse.SearchResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RrfMergeService {

    @Value("${query.rrf.k:60}")
    private int k;

    /**
     * Merges two ranked lists using Reciprocal Rank Fusion.
     * score_rrf(d) = Σ 1 / (k + rank_i(d))
     */
    public List<SearchResultDto> merge(
            List<SearchResultDto> keywordResults,
            List<SearchResultDto> vectorResults
    ) {
        Map<String, SearchResultDto> byId = new LinkedHashMap<>();
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Double> kScores = new HashMap<>();
        Map<String, Double> vScores = new HashMap<>();

        // Score keyword results
        for (int i = 0; i < keywordResults.size(); i++) {
            SearchResultDto r = keywordResults.get(i);
            byId.put(r.getLogId(), r);
            rrfScores.merge(r.getLogId(), 1.0 / (k + i + 1), Double::sum);
            kScores.put(r.getLogId(), r.getScore());
        }

        // Score vector results
        for (int i = 0; i < vectorResults.size(); i++) {
            SearchResultDto r = vectorResults.get(i);
            byId.putIfAbsent(r.getLogId(), r);
            rrfScores.merge(r.getLogId(), 1.0 / (k + i + 1), Double::sum);
            vScores.put(r.getLogId(), r.getScore());
        }

        // Build final list sorted by RRF score descending
        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> {
                    SearchResultDto base = byId.get(e.getKey());
                    return SearchResultDto.builder()
                            .logId(base.getLogId())
                            .tenantId(base.getTenantId())
                            .serviceName(base.getServiceName())
                            .timestamp(base.getTimestamp())
                            .level(base.getLevel())
                            .message(base.getMessage())
                            .parsedFields(base.getParsedFields())
                            .traceId(base.getTraceId())
                            .score(e.getValue())
                            .keywordScore(kScores.get(e.getKey()))
                            .vectorScore(vScores.get(e.getKey()))
                            .build();
                })
                .toList();
    }
}
