package com.example.mdsearch.service;

import com.example.mdsearch.config.MockConfig;
import com.example.mdsearch.model.SearchRequest;
import com.example.mdsearch.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SearchService
 */
class SearchServiceTest {

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new MockConfig.MockSearchService();
    }

    @Test
    void testSearchWithBasicQuery() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("machine learning")
                .size(10)
                .build();

        SearchResult result = searchService.search(request);

        assertNotNull(result, "Search result should not be null");
        assertTrue(result.getTotal() >= 0, "Total should be non-negative");
        assertNotNull(result.getHits(), "Hits should not be null");
        assertTrue(result.getMaxScore() >= 0, "Max score should be non-negative");
    }

    @Test
    void testSearchWithSizeLimit() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("test query")
                .size(3)
                .build();

        SearchResult result = searchService.search(request);

        assertNotNull(result, "Search result should not be null");
        assertTrue(result.getHits().size() <= 3, "Hits should not exceed requested size");
    }

    @Test
    void testSearchWithTags() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("document")
                .size(10)
                .tags(java.util.Arrays.asList("java", "spring"))
                .build();

        SearchResult result = searchService.search(request);

        assertNotNull(result, "Search result should not be null");
    }

    @Test
    void testSearchWithCustomWeights() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("custom weight test")
                .size(10)
                .bm25Weight(0.5)
                .titleVectorWeight(0.3)
                .contentVectorWeight(0.2)
                .build();

        SearchResult result = searchService.search(request);

        assertNotNull(result, "Search result should not be null");
    }

    @Test
    void testBm25Search() throws IOException {
        SearchResult result = searchService.bm25Search("bm25 test query", 5);

        assertNotNull(result, "BM25 search result should not be null");
        assertNotNull(result.getHits(), "Hits should not be null");
    }

    @Test
    void testVectorSearch() throws IOException {
        SearchResult result = searchService.vectorSearch("vector test query", 5);

        assertNotNull(result, "Vector search result should not be null");
        assertNotNull(result.getHits(), "Hits should not be null");
    }

    @Test
    void testSearchHitContent() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("test")
                .size(1)
                .build();

        SearchResult result = searchService.search(request);

        if (!result.getHits().isEmpty()) {
            SearchResult.SearchHit hit = result.getHits().get(0);
            assertNotNull(hit.getId(), "Hit ID should not be null");
            assertNotNull(hit.getTitle(), "Hit title should not be null");
            assertNotNull(hit.getContent(), "Hit content should not be null");
            assertNotNull(hit.getFilePath(), "Hit file path should not be null");
            assertTrue(hit.getScore() >= 0, "Hit score should be non-negative");
        }
    }

    @Test
    void testSearchWithEmptyQuery() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("")
                .size(10)
                .build();

        SearchResult result = searchService.search(request);

        assertNotNull(result, "Search result should not be null even with empty query");
    }

    @Test
    void testSearchWithNullSize() throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query("test query")
                .build();

        SearchResult result = searchService.search(request);

        assertNotNull(result, "Search result should not be null with default size");
    }

    @Test
    void testSearchResultStructure() throws IOException {
        SearchResult result = searchService.bm25Search("structure test", 10);

        assertEquals(5, result.getTotal(), "Mock result should have 5 total");
        assertFalse(result.getHits().isEmpty(), "Should have hits");
        assertEquals(1.0, result.getMaxScore(), 0.001, "Max score should be 1.0");
    }
}
