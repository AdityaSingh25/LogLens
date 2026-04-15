package com.loglens.query.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NlQueryTranslatorService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    private static final String SYSTEM_PROMPT = """
            You translate natural language log search queries into structured filters.
            Return JSON only. Fields:
            - keywords (string): key terms to search for
            - level (string|null): one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or null
            - time_hint (string|null): relative time like "last 1 hour", or null
            - service_name (string|null): specific service name, or null
            - field_filters (object|null): specific field key-value pairs, or null

            Examples:
            User: "payment failures in the last hour"
            {"keywords":"payment failure","level":"ERROR","time_hint":"last 1 hour","service_name":null,"field_filters":null}

            User: "database connection timeout errors in auth service"
            {"keywords":"database connection timeout","level":"ERROR","time_hint":null,"service_name":"auth-service","field_filters":null}

            User: "slow queries warn level"
            {"keywords":"slow queries","level":"WARN","time_hint":null,"service_name":null,"field_filters":null}
            """;

    public NlQueryTranslatorService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public TranslatedQuery translate(String naturalLanguageQuery) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", naturalLanguageQuery)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "temperature", 0
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) throw new RuntimeException("Null response from OpenAI");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            return parseTranslation(content, naturalLanguageQuery);
        } catch (Exception e) {
            log.warn("NL translation failed for query '{}', falling back to keyword-only: {}", naturalLanguageQuery, e.getMessage());
            return TranslatedQuery.keywordOnly(naturalLanguageQuery);
        }
    }

    @SuppressWarnings("unchecked")
    private TranslatedQuery parseTranslation(String json, String originalQuery) {
        try {
            // Simple JSON parsing using Jackson via Spring's ObjectMapper
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(json, Map.class);
            return TranslatedQuery.builder()
                    .keywords((String) parsed.getOrDefault("keywords", originalQuery))
                    .level((String) parsed.get("level"))
                    .serviceName((String) parsed.get("service_name"))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse NL translation JSON, using keyword fallback");
            return TranslatedQuery.keywordOnly(originalQuery);
        }
    }

    public record TranslatedQuery(String keywords, String level, String serviceName) {
        public static TranslatedQuery keywordOnly(String query) {
            return new TranslatedQuery(query, null, null);
        }

        public static TranslatedQueryBuilder builder() { return new TranslatedQueryBuilder(); }

        public static class TranslatedQueryBuilder {
            private String keywords, level, serviceName;
            public TranslatedQueryBuilder keywords(String v) { keywords = v; return this; }
            public TranslatedQueryBuilder level(String v) { level = v; return this; }
            public TranslatedQueryBuilder serviceName(String v) { serviceName = v; return this; }
            public TranslatedQuery build() { return new TranslatedQuery(keywords, level, serviceName); }
        }
    }
}
