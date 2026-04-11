package com.loglens.parser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonLogParser {

    private final ObjectMapper objectMapper;

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

            // Extract known fields if present in JSON body
            if (json.containsKey("level") && parsed.getLevel() == null) {
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
