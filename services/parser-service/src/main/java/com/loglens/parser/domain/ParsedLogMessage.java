package com.loglens.parser.domain;

import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class ParsedLogMessage {
    private String logId;
    private String tenantId;
    private String serviceName;
    private Instant timestamp;
    private String level;
    private String message;
    private String rawBody;
    private String sourceFormat;
    private Map<String, String> metadata;
    private String traceId;
    private String spanId;

    // Enriched by parser
    private Map<String, Object> parsedFields = new HashMap<>();
    private String normalizedMessage;
    private long parseDurationMs;
    private String parserVersion = "1.0";
}
