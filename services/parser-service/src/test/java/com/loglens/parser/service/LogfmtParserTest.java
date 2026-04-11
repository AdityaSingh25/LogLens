package com.loglens.parser.service;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogfmtParserTest {

    private LogfmtParser parser;

    @BeforeEach
    void setUp() {
        parser = new LogfmtParser();
    }

    @Test
    void canParse_logfmt_returnsTrue() {
        assertThat(parser.canParse("level=error msg=\"timeout\" host=server-1")).isTrue();
    }

    @Test
    void canParse_jsonBody_returnsFalse() {
        assertThat(parser.canParse("{\"level\":\"error\"}")).isFalse();
    }

    @Test
    void enrich_extractsQuotedAndUnquotedValues() {
        RawLogMessage raw = new RawLogMessage();
        raw.setRawBody("level=error msg=\"payment gateway timeout\" host=server-1 duration=342ms");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parser.enrich(raw, parsed);

        assertThat(parsed.getLevel()).isEqualTo("ERROR");
        assertThat(parsed.getNormalizedMessage()).isEqualTo("payment gateway timeout");
        assertThat(parsed.getParsedFields()).containsKey("host");
        assertThat(parsed.getParsedFields()).containsKey("duration");
        assertThat(parsed.getSourceFormat()).isEqualTo("LOGFMT");
    }

    @Test
    void enrich_handlesSpecialCharactersInQuotedValue() {
        RawLogMessage raw = new RawLogMessage();
        raw.setRawBody("level=warn msg=\"user john@example.com logged in\" tenant_id=abc-123");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parser.enrich(raw, parsed);

        assertThat(parsed.getNormalizedMessage()).isEqualTo("user john@example.com logged in");
        assertThat(parsed.getParsedFields()).containsKey("tenant_id");
    }
}
