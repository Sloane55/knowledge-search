package com.example.mdsearch.service.impl;

import com.example.mdsearch.config.SearchProperties;
import com.example.mdsearch.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API Embedding Service - calls external embedding API
 *
 * Enable by setting: mock.embedding-enabled=false (or omit the property)
 * Configure API settings in mdsearch.embedding.*
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "mock.embedding-enabled", havingValue = "false", matchIfMissing = true)
public class ApiEmbeddingService implements EmbeddingService {

    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient httpClient;

    public ApiEmbeddingService(SearchProperties searchProperties) {
        this.searchProperties = searchProperties;
    }

    @PostConstruct
    public void init() {
        SearchProperties.EmbeddingConfig config = searchProperties.getEmbedding();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                .build();

        log.info("Using ApiEmbeddingService - API: {}, model: {}, dimension: {}",
                config.getApiUrl(), config.getModel(), config.getDimension());
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            return new float[searchProperties.getEmbedding().getDimension()];
        }

        SearchProperties.EmbeddingConfig config = searchProperties.getEmbedding();

        try {
            String requestBody = objectMapper.writeValueAsString(new EmbeddingRequest(config.getModel(), text));

            Request request = new Request.Builder()
                    .url(config.getApiUrl())
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    log.error("Embedding API call failed: {} - {}", response.code(), errorBody);
                    throw new RuntimeException("Embedding API call failed: " + response.code());
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode embeddingNode = root.path("data").get(0).path("embedding");

                float[] vector = new float[config.getDimension()];
                for (int i = 0; i < embeddingNode.size() && i < config.getDimension(); i++) {
                    vector[i] = (float) embeddingNode.get(i).asDouble();
                }

                log.debug("Generated embedding for text length: {}, vector dimension: {}", text.length(), vector.length);
                return vector;
            }
        } catch (IOException e) {
            log.error("Failed to call embedding API", e);
            throw new RuntimeException("Failed to call embedding API", e);
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }

    private static class EmbeddingRequest {
        private final String model;
        private final String input;

        public EmbeddingRequest(String model, String input) {
            this.model = model;
            this.input = input;
        }

        public String getModel() {
            return model;
        }

        public String getInput() {
            return input;
        }
    }
}
