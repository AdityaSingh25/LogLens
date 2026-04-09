package com.loglens.ingestion.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class IngestLogRequest {

    @NotBlank
    private String serviceName;

    @NotNull
    @Pattern(regexp = "TRACE|DEBUG|INFO|WARN|ERROR|FATAL")
    private String level;

    @NotBlank
    private String message;

    private String timestamp; // ISO-8601, optional — defaults to now

    private Map<String, String> metadata;

    private String traceId;

    private String spanId;
}
