package com.example.qyuanes.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


@Slf4j
@Configuration
@EnableScheduling
public class ElasticSearchConfig {
    
    private final ElasticsearchProperties elasticsearchProperties;

    public ElasticSearchConfig(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    /**
     * 创建ElasticSearch客户端
     * @return ElasticsearchClient实例
     */
    @Bean  // Spring注解：将方法返回值注册为Spring容器中的Bean
    public ElasticsearchClient elasticsearchClient() {
        List<String> uris = elasticsearchProperties.getUris();
        String uriStr = uris != null && !uris.isEmpty() ? uris.get(0) : "http://localhost:9200";
        
        try {
            URI uri = URI.create(uriStr);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 9200 : uri.getPort();
            String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
            
            log.info("Initializing ElasticsearchClient with host: {} port: {} scheme: {}", host, port, scheme);
            // 创建RestClient http通信
            RestClient restClient = RestClient.builder(
                new HttpHost(host, port, scheme)
            ).build();

            // 配置ObjectMapper以支持Java 8时间类型（OffsetDateTime等）
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // 创建JacksonJsonpMapper，使用配置好的ObjectMapper
            JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

            // 创建ElasticsearchTransport
            ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);

            // 创建并返回ElasticsearchClient
            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            log.error("Failed to create ElasticsearchClient", e);
            throw new RuntimeException("Failed to create ElasticsearchClient", e);
        }
    }
}