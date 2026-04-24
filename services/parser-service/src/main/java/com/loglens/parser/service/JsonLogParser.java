package com.loglens.parser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JsonLogParser {

    private static final Logger log = LoggerFactory.getLogger(JsonLogParser.class);

    private final ObjectMapper objectMapper;

    public JsonLogParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean canParse(String rawBody) {
        if (rawBody == null) return false;
        String trimmed = rawBody.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    @SuppressWarnings("unchecked")
    public void enrich(RawLogMessage raw, ParsedLogMessage parsed) {
        try {
            Map<String, Object> json = objectMapper.readValue(raw.getRawBody(), Map.class);
            Map<String, Object> fields = new HashMap<>(json);

            if (json.containsKey("level")) {
                parsed.setLevel(String.valueOf(json.get("level")).toUpperCase());
                fields.remove("level");
            }
            if (json.containsKey("message")) fields.remove("message");
            if (json.containsKey("msg")) {
                parsed.setNormalizedMessage(String.valueOf(json.get("msg")));
                fields.remove("msg");
            }
            if (json.containsKey("trace_id")) {
                parsed.setTraceId(String.valueOf(json.get("trace_id")));
                fields.remove("trace_id");
            }

            parsed.setParsedFields(fields);
            parsed.setSourceFormat("JSON");
        } catch (Exception e) {
            log.debug("JSON parse failed for log {}, falling back", raw.getLogId());
            parsed.setParsedFields(Map.of());
        }
    }
}
