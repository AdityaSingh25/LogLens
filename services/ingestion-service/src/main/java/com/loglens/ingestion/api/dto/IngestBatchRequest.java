package com.loglens.ingestion.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class IngestBatchRequest {

    @NotEmpty
    @Size(max = 1000)
    @Valid
    private List<IngestLogRequest> logs;
}
