package com.example.mdsearch.service;

import java.util.List;

public interface EmbeddingService {

    /**
     * Get embedding vector for a single text
     */
    float[] embed(String text);

    /**
     * Get embedding vectors for multiple texts
     */
    List<float[]> embedBatch(List<String> texts);
}
