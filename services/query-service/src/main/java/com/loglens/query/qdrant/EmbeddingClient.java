package com.loglens.query.qdrant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EmbeddingClient {

    private final WebClient webClient;
    private final String model;

    public EmbeddingClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.embedding-model:text-embedding-3-small}") String model
    ) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * Returns null on failure — vector search degrades gracefully.
     */
    public float[] embed(String text) {
        try {
            Map<String, Object> body = Map.of("model", model, "input", text);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/v1/embeddings")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            @SuppressWarnings("unchecked")
            List<Double> embedding = (List<Double>) data.get(0).get("embedding");

            float[] result = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                result[i] = embedding.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            log.warn("Query embedding failed: {}", e.getMessage());
            return null;
        }
    }
}
