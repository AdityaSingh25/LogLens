package com.loglens.parser.service;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlainTextParser {

    // Matches common log level patterns: [ERROR], ERROR:, ERROR
    private static final Pattern LEVEL_PATTERN =
            Pattern.compile("\\b(TRACE|DEBUG|INFO|WARN|WARNING|ERROR|FATAL)\\b", Pattern.CASE_INSENSITIVE);

    public void enrich(RawLogMessage raw, ParsedLogMessage parsed) {
        String body = raw.getRawBody() != null ? raw.getRawBody() : raw.getMessage();

        // Try to extract level from plain text if not already set
        if (parsed.getLevel() == null || parsed.getLevel().isBlank()) {
            Matcher m = LEVEL_PATTERN.matcher(body);
            if (m.find()) {
                String found = m.group(1).toUpperCase();
                parsed.setLevel("WARNING".equals(found) ? "WARN" : found);
            }
        }

        parsed.setNormalizedMessage(body.trim().replaceAll("\\s+", " "));
        parsed.setParsedFields(Map.of());
        parsed.setSourceFormat("PLAINTEXT");
    }
}
