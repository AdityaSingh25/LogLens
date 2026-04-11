package com.loglens.parser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loglens.parser.domain.ParsedLogMessage;
import com.loglens.parser.domain.RawLogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLogParserTest {

    private JsonLogParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonLogParser(new ObjectMapper());
    }

    @Test
    void canParse_validJson_returnsTrue() {
        assertThat(parser.canParse("{\"level\":\"ERROR\",\"message\":\"timeout\"}")).isTrue();
    }

    @Test
    void canParse_plainText_returnsFalse() {
        assertThat(parser.canParse("ERROR: something went wrong")).isFalse();
    }

    @Test
    void enrich_extractsLevelAndFields() {
        RawLogMessage raw = new RawLogMessage();
        raw.setLogId("log-1");
        raw.setRawBody("{\"level\":\"WARN\",\"message\":\"disk full\",\"host\":\"server-1\"}");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parsed.setLevel("INFO"); // should be overridden

        parser.enrich(raw, parsed);

        assertThat(parsed.getLevel()).isEqualTo("WARN");
        assertThat(parsed.getParsedFields()).containsKey("host");
        assertThat(parsed.getSourceFormat()).isEqualTo("JSON");
    }

    @Test
    void enrich_malformedJson_doesNotCrash() {
        RawLogMessage raw = new RawLogMessage();
        raw.setLogId("log-2");
        raw.setRawBody("{not valid json");

        ParsedLogMessage parsed = new ParsedLogMessage();
        parser.enrich(raw, parsed); // should not throw

        assertThat(parsed.getParsedFields()).isEmpty();
    }
}
