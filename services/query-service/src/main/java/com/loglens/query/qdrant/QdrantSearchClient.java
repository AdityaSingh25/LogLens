package com.loglens.query.qdrant;

import com.loglens.query.api.dto.SearchResponse.SearchResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class QdrantSearchClient {

    private final WebClient webClient;

    public QdrantSearchClient(@Value("${qdrant.url:http://localhost:6333}") String qdrantUrl) {
        this.webClient = WebClient.builder().baseUrl(qdrantUrl).build();
    }

    public List<SearchResultDto> search(String tenantId, float[] queryVector, int limit) {
        String collectionName = "logs-" + tenantId;
        try {
            Map<String, Object> body = Map.of(
                    "vector", queryVector,
                    "limit", limit,
                    "with_payload", true,
                    "filter", Map.of(
                            "must", List.of(
                                    Map.of("key", "tenant_id", "match", Map.of("value", tenantId))
                            )
                    )
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/collections/" + collectionName + "/points/search")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("result")) return List.of();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("result");

            return results.stream().map(r -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) r.getOrDefault("payload", Map.of());
                double score = ((Number) r.getOrDefault("score", 0.0)).doubleValue();
                return SearchResultDto.builder()
                        .logId((String) payload.getOrDefault("log_id", r.get("id").toString()))
                        .tenantId(tenantId)
                        .serviceName((String) payload.get("service_name"))
                        .timestamp((String) payload.get("timestamp"))
                        .level((String) payload.get("level"))
                        .message("")  // payload doesn't store full message; ES result will have it
                        .score(score)
                        .vectorScore(score)
                        .build();
            }).toList();

        } catch (Exception e) {
            log.warn("Qdrant search failed for tenant {}: {}", tenantId, e.getMessage());
            return List.of();
        }
    }
}
