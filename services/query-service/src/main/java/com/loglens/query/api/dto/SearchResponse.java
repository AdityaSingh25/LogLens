package com.loglens.query.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data @Builder
public class SearchResponse {
    private List<SearchResultDto> results;
    private long total;
    private long tookMs;
    private String queryMode;

    @Data @Builder
    public static class SearchResultDto {
        private String logId;
        private String tenantId;
        private String serviceName;
        private String timestamp;
        private String level;
        private String message;
        private Map<String, Object> parsedFields;
        private String traceId;
        private double score;
        private Double keywordScore;
        private Double vectorScore;
        private String highlight;
    }
}
