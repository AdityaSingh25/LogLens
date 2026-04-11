package com.loglens.parser.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.uris:http://localhost:9200}")
    private String esUri;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        URI uri = URI.create(esUri);
        RestClient restClient = RestClient.builder(
                new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())
        ).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
