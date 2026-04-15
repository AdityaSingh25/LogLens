package com.loglens.query.service;

import com.loglens.query.api.dto.SearchResponse.SearchResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RrfMergeServiceTest {

    private RrfMergeService rrfMergeService;

    @BeforeEach
    void setUp() {
        rrfMergeService = new RrfMergeService();
    }

    private SearchResultDto result(String id, double score) {
        return SearchResultDto.builder()
                .logId(id)
                .tenantId("tenant-1")
                .serviceName("svc")
                .level("ERROR")
                .message("msg")
                .score(score)
                .build();
    }

    @Test
    void merge_appearsInBothLists_getsHigherScore() {
        List<SearchResultDto> keyword = List.of(result("log-1", 1.0), result("log-2", 0.8));
        List<SearchResultDto> vector  = List.of(result("log-1", 0.95), result("log-3", 0.7));

        List<SearchResultDto> merged = rrfMergeService.merge(keyword, vector);

        // log-1 appears in both — should have highest RRF score
        assertThat(merged.get(0).getLogId()).isEqualTo("log-1");
        assertThat(merged).hasSize(3);
    }

    @Test
    void merge_deduplicatesResults() {
        List<SearchResultDto> keyword = List.of(result("log-1", 1.0));
        List<SearchResultDto> vector  = List.of(result("log-1", 0.9));

        List<SearchResultDto> merged = rrfMergeService.merge(keyword, vector);
        assertThat(merged).hasSize(1);
    }

    @Test
    void merge_emptyVectorList_returnsKeywordResults() {
        List<SearchResultDto> keyword = List.of(result("log-1", 1.0), result("log-2", 0.8));

        List<SearchResultDto> merged = rrfMergeService.merge(keyword, List.of());
        assertThat(merged).hasSize(2);
    }

    @Test
    void merge_bothEmpty_returnsEmpty() {
        assertThat(rrfMergeService.merge(List.of(), List.of())).isEmpty();
    }
}
