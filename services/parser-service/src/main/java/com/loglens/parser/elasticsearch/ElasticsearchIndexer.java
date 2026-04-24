package com.loglens.parser.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest;
import com.loglens.parser.domain.ParsedLogMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class ElasticsearchIndexer {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexer.class);

    private final ElasticsearchClient esClient;
    private final IndexNameResolver indexNameResolver;

    public ElasticsearchIndexer(ElasticsearchClient esClient, IndexNameResolver indexNameResolver) {
        this.esClient = esClient;
        this.indexNameResolver = indexNameResolver;
    }

    @PostConstruct
    public void registerIndexTemplate() {
        try (InputStream is = getClass().getResourceAsStream("/es-index-template.json")) {
            if (is != null) {
                esClient.indices().putIndexTemplate(PutIndexTemplateRequest.of(b -> b
                        .withJson(is)
                        .name("loglens-logs-template")
                ));
                log.info("Registered ES index template: loglens-logs-template");
            }
        } catch (Exception e) {
            log.warn("Could not register ES index template: {}", e.getMessage());
        }
    }

    public void index(ParsedLogMessage parsed) {
        String indexName = indexNameResolver.resolve(parsed.getTenantId());
        try {
            Map<String, Object> doc = buildDocument(parsed);
            esClient.index(i -> i
                    .index(indexName)
                    .id(parsed.getLogId())
                    .document(doc)
            );
            log.debug("Indexed log {} to {}", parsed.getLogId(), indexName);
        } catch (Exception e) {
            log.error("Failed to index log {} to ES index {}", parsed.getLogId(), indexName, e);
            // Do not rethrow — ES indexing failure should not poison the Kafka consumer
        }
    }

    private Map<String, Object> buildDocument(ParsedLogMessage parsed) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("log_id", parsed.getLogId());
        doc.put("tenant_id", parsed.getTenantId());
        doc.put("service_name", parsed.getServiceName());
        doc.put("timestamp", parsed.getTimestamp() != null ? parsed.getTimestamp().toString() : null);
        doc.put("level", parsed.getLevel());
        doc.put("message", parsed.getMessage());
        doc.put("normalized_message", parsed.getNormalizedMessage());
        doc.put("parsed_fields", parsed.getParsedFields());
        doc.put("trace_id", parsed.getTraceId());
        return doc;
    }
}
