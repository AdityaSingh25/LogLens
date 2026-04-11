package com.loglens.parser.domain;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class RawLogMessage {
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
}
