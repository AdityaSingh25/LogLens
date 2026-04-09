package com.loglens.ingestion.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IngestBatchResponse {
    private int accepted;
    private int rejected;
    private List<String> logIds;
}
