package com.loglens.parser.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Object> parsedFields = new HashMap<>();
    private String normalizedMessage;
    private long parseDurationMs;
    private String parserVersion = "1.0";

    public String getLogId()        { return logId; }
    public String getTenantId()     { return tenantId; }
    public String getServiceName()  { return serviceName; }
    public Instant getTimestamp()   { return timestamp; }
    public String getLevel()        { return level; }
    public String getMessage()      { return message; }
    public String getRawBody()      { return rawBody; }
    public String getSourceFormat() { return sourceFormat; }
    public Map<String, String> getMetadata() { return metadata; }
    public String getTraceId()      { return traceId; }
    public String getSpanId()       { return spanId; }
    public Map<String, Object> getParsedFields()  { return parsedFields; }
    public String getNormalizedMessage()          { return normalizedMessage; }
    public long getParseDurationMs()              { return parseDurationMs; }
    public String getParserVersion()              { return parserVersion; }

    public void setLogId(String v)        { logId = v; }
    public void setTenantId(String v)     { tenantId = v; }
    public void setServiceName(String v)  { serviceName = v; }
    public void setTimestamp(Instant v)   { timestamp = v; }
    public void setLevel(String v)        { level = v; }
    public void setMessage(String v)      { message = v; }
    public void setRawBody(String v)      { rawBody = v; }
    public void setSourceFormat(String v) { sourceFormat = v; }
    public void setMetadata(Map<String, String> v) { metadata = v; }
    public void setTraceId(String v)      { traceId = v; }
    public void setSpanId(String v)       { spanId = v; }
    public void setParsedFields(Map<String, Object> v)  { parsedFields = v; }
    public void setNormalizedMessage(String v)          { normalizedMessage = v; }
    public void setParseDurationMs(long v)              { parseDurationMs = v; }
    public void setParserVersion(String v)              { parserVersion = v; }
}
