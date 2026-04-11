package com.loglens.parser.service;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogfmtParser {

    // Matches key="quoted value" or key=unquoted_value
    private static final Pattern QUOTED   = Pattern.compile("(\\w+)=\"([^\"]*)\"");
    private static final Pattern UNQUOTED = Pattern.compile("(\\w+)=([^\\s\"]+)");

    public boolean canParse(String rawBody) {
        if (rawBody == null) return false;
        return rawBody.contains("=") && !rawBody.trim().startsWith("{");
    }

    public void enrich(RawLogMessage raw, ParsedLogMessage parsed) {
        Map<String, Object> fields = new HashMap<>();
        String body = raw.getRawBody();

        // Extract quoted values first
        Matcher qm = QUOTED.matcher(body);
        while (qm.find()) {
            fields.put(qm.group(1), qm.group(2));
        }

        // Then unquoted (won't re-add already matched keys)
        Matcher um = UNQUOTED.matcher(body);
        while (um.find()) {
            fields.putIfAbsent(um.group(1), um.group(2));
        }

        if (fields.containsKey("level")) {
            parsed.setLevel(fields.remove("level").toString().toUpperCase());
        }
        if (fields.containsKey("msg")) {
            parsed.setNormalizedMessage(fields.remove("msg").toString());
        }
        if (fields.containsKey("trace_id")) {
            parsed.setTraceId(fields.remove("trace_id").toString());
        }

        parsed.setParsedFields(fields);
        parsed.setSourceFormat("LOGFMT");
    }
}
