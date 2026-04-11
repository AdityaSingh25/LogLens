package com.loglens.parser.service;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogParserServiceImpl {

    private final JsonLogParser jsonParser;
    private final LogfmtParser logfmtParser;
    private final PlainTextParser plainTextParser;

    public ParsedLogMessage parse(RawLogMessage raw) {
        long start = System.currentTimeMillis();

        ParsedLogMessage parsed = new ParsedLogMessage();
        parsed.setLogId(raw.getLogId());
        parsed.setTenantId(raw.getTenantId());
        parsed.setServiceName(raw.getServiceName());
        parsed.setTimestamp(raw.getTimestamp());
        parsed.setLevel(raw.getLevel());
        parsed.setMessage(raw.getMessage());
        parsed.setRawBody(raw.getRawBody());
        parsed.setMetadata(raw.getMetadata());
        parsed.setTraceId(raw.getTraceId());
        parsed.setSpanId(raw.getSpanId());
        parsed.setNormalizedMessage(raw.getMessage()); // default

        String rawBody = raw.getRawBody();

        if (jsonParser.canParse(rawBody)) {
            jsonParser.enrich(raw, parsed);
        } else if (logfmtParser.canParse(rawBody)) {
            logfmtParser.enrich(raw, parsed);
        } else {
            plainTextParser.enrich(raw, parsed);
        }

        // Ensure normalized message is always set
        if (parsed.getNormalizedMessage() == null || parsed.getNormalizedMessage().isBlank()) {
            parsed.setNormalizedMessage(raw.getMessage());
        }

        parsed.setParseDurationMs(System.currentTimeMillis() - start);
        log.debug("Parsed log {} using format {}", raw.getLogId(), parsed.getSourceFormat());
        return parsed;
    }
}
