package com.loglens.ingestion.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngestLogResponse {
    private String logId;
    private String acceptedAt;
}
