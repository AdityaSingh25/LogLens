package com.loglens.query.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SearchRequest {

    @NotBlank
    private String query;

    @NotNull @Valid
    private TimeRangeDto timeRange;

    private SearchFiltersDto filters;

    private int page = 0;
    private int pageSize = 20;

    @Data
    public static class TimeRangeDto {
        @NotBlank private String from;
        @NotBlank private String to;
    }

    @Data
    public static class SearchFiltersDto {
        private List<String> services;
        private List<String> levels;
    }
}
