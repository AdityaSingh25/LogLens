package com.loglens.parser.service;

import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlainTextParserTest {

    private PlainTextParser parser;

    @BeforeEach
    void setUp() {
        parser = new PlainTextParser();
    }

    @Test
    void enrich_extractsLevelFromText() {
        RawLogMessage raw = new RawLogMessage();
        raw.setMessage("ERROR: database connection failed");
        raw.setRawBody("ERROR: database connection failed");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parser.enrich(raw, parsed);

        assertThat(parsed.getLevel()).isEqualTo("ERROR");
        assertThat(parsed.getSourceFormat()).isEqualTo("PLAINTEXT");
    }

    @Test
    void enrich_normalizesWhitespace() {
        RawLogMessage raw = new RawLogMessage();
        raw.setMessage("  INFO   multiple   spaces  ");
        raw.setRawBody("  INFO   multiple   spaces  ");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parser.enrich(raw, parsed);

        assertThat(parsed.getNormalizedMessage()).isEqualTo("INFO multiple spaces");
    }

    @Test
    void enrich_handlesMissingLevel_doesNotCrash() {
        RawLogMessage raw = new RawLogMessage();
        raw.setMessage("some plain log without level");
        raw.setRawBody("some plain log without level");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parser.enrich(raw, parsed); // should not throw

        assertThat(parsed.getNormalizedMessage()).isNotBlank();
    }
}
