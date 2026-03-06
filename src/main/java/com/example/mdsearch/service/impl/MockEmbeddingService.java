package com.example.mdsearch.service.impl;

import com.example.mdsearch.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock Embedding Service - generates deterministic vectors based on text content
 * Used for testing without external API dependencies
 *
 * Enable by setting: mock.embedding-enabled=true
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "mock.embedding-enabled", havingValue = "true", matchIfMissing = false)
public class MockEmbeddingService implements EmbeddingService {

    private static final int DIMENSION = 1536;

    public MockEmbeddingService() {
        log.info("Using MockEmbeddingService (dimension: {})", DIMENSION);
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            return new float[DIMENSION];
        }

        float[] vector = new float[DIMENSION];

        // Use text hash as seed for deterministic results
        int seed = text.hashCode();
        Random random = new Random(seed);

        // Generate random values
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = random.nextFloat() * 2 - 1; // Range: -1 to 1
        }

        // L2 normalization
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < DIMENSION; i++) {
                vector[i] /= norm;
            }
        }

        log.debug("Generated mock embedding for text length: {}", text.length());
        return vector;
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }
}
