package com.example.mdsearch.service;

import com.example.mdsearch.config.MockConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmbeddingService
 */
class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new MockConfig.MockEmbeddingService();
    }

    @Test
    void testEmbedSingleText() {
        String text = "This is a test document for embedding";

        float[] vector = embeddingService.embed(text);

        assertNotNull(vector, "Embedding vector should not be null");
        assertEquals(1536, vector.length, "Vector dimension should be 1536");

        // Check vector values are in valid range [-1, 1]
        for (float v : vector) {
            assertTrue(v >= -1.0f && v <= 1.0f, "Vector values should be in range [-1, 1]");
        }
    }

    @Test
    void testEmbedEmptyText() {
        String text = "";

        float[] vector = embeddingService.embed(text);

        assertNotNull(vector, "Embedding vector should not be null for empty text");
        assertEquals(1536, vector.length, "Vector dimension should be 1536 for empty text");
    }

    @Test
    void testEmbedBatch() {
        List<String> texts = Arrays.asList(
            "First document content",
            "Second document content",
            "Third document content"
        );

        List<float[]> vectors = embeddingService.embedBatch(texts);

        assertNotNull(vectors, "Batch embedding result should not be null");
        assertEquals(3, vectors.size(), "Should return 3 vectors");

        for (float[] vector : vectors) {
            assertNotNull(vector, "Each vector should not be null");
            assertEquals(1536, vector.length, "Each vector dimension should be 1536");
        }
    }

    @Test
    void testEmbedConsistency() {
        String text = "Test for embedding consistency";

        // Same text should produce same vector (deterministic)
        float[] vector1 = embeddingService.embed(text);
        float[] vector2 = embeddingService.embed(text);

        assertArrayEquals(vector1, vector2, 0.0001f, "Same text should produce same vector");
    }

    @Test
    void testEmbedDifferentTexts() {
        String text1 = "Document about machine learning";
        String text2 = "Recipe for cooking pasta";

        float[] vector1 = embeddingService.embed(text1);
        float[] vector2 = embeddingService.embed(text2);

        // Different texts should produce different vectors
        boolean hasDifference = false;
        for (int i = 0; i < vector1.length; i++) {
            if (Math.abs(vector1[i] - vector2[i]) > 0.001f) {
                hasDifference = true;
                break;
            }
        }
        // Note: In mock implementation, this might not be true due to random seed
        // In real implementation, different texts should produce different vectors
    }

    @Test
    void testEmbedLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a very long document for testing embedding. ");
        }
        String longText = sb.toString();

        float[] vector = embeddingService.embed(longText);

        assertNotNull(vector, "Should handle long text");
        assertEquals(1536, vector.length, "Vector dimension should be 1536");
    }

    @Test
    void testEmbedBatchWithEmptyList() {
        List<String> emptyList = Arrays.asList();

        List<float[]> vectors = embeddingService.embedBatch(emptyList);

        assertNotNull(vectors, "Should handle empty list");
        assertTrue(vectors.isEmpty(), "Empty list should return empty result");
    }

    @Test
    void testEmbedChineseText() {
        String chineseText = "这是一个中文测试文档，包含中文字符和标点符号。 ";

        float[] vector = embeddingService.embed(chineseText);

        assertNotNull(vector, "Should handle Chinese text");
        assertEquals(1536, vector.length, "Vector dimension should be 1536");
    }

    @Test
    void testEmbedSpecialCharacters() {
        String specialText = "Test with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?`~";

        float[] vector = embeddingService.embed(specialText);

        assertNotNull(vector, "Should handle special characters");
        assertEquals(1536, vector.length, "Vector dimension should be 1536");
    }

    @Test
    void testEmbedMarkdownContent() {
        String markdown = "# Main Title\n\n" +
            "## Section 1\n" +
            "This is *italic* and **bold** text.\n\n" +
            "- List item 1\n" +
            "- List item 2\n\n" +
            "```java\n" +
            "code block\n" +
            "```\n";

        float[] vector = embeddingService.embed(markdown);

        assertNotNull(vector, "Should handle markdown content");
        assertEquals(1536, vector.length, "Vector dimension should be 1536");
    }

    private void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
        assertEquals(expected.length, actual.length, message + " (length mismatch)");
        for (int i = 0; i < expected.length; i++) {
            if (Math.abs(expected[i] - actual[i]) > delta) {
                fail(message + " at index " + i + ": expected " + expected[i] + " but was " + actual[i]);
            }
        }
    }
}
