package com.example.mdsearch.service;

import com.example.mdsearch.config.MockConfig;
import com.example.mdsearch.model.MarkdownDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VisitService
 */
class VisitServiceTest {

    private VisitService visitService;

    @BeforeEach
    void setUp() {
        visitService = new MockConfig.MockVisitService();
    }

    @Test
    void testGetDocumentById() {
        // MockVisitService pre-loads documents with IDs mock-doc-0 through mock-doc-9
        MarkdownDocument doc = visitService.getDocumentById("mock-doc-0");

        assertNotNull(doc, "Document should not be null");
        assertEquals("mock-doc-0", doc.getId(), "Document ID should match");
    }

    @Test
    void testGetDocumentByIdNotFound() {
        MarkdownDocument doc = visitService.getDocumentById("non-existent-id");

        assertNull(doc, "Should return null for non-existent document");
    }

    @Test
    void testGetDocumentByPath() {
        MarkdownDocument doc = visitService.getDocumentByPath("/mock/path/doc0.md");

        assertNotNull(doc, "Document should not be null");
        assertEquals("/mock/path/doc0.md", doc.getFilePath(), "File path should match");
    }

    @Test
    void testGetDocumentByPathNotFound() {
        MarkdownDocument doc = visitService.getDocumentByPath("/non/existent/path.md");

        assertNull(doc, "Should return null for non-existent path");
    }

    @Test
    void testGetDocumentsByIds() {
        List<String> ids = Arrays.asList("mock-doc-0", "mock-doc-1", "mock-doc-2");

        List<MarkdownDocument> docs = visitService.getDocumentsByIds(ids);

        assertNotNull(docs, "Document list should not be null");
        assertEquals(3, docs.size(), "Should return 3 documents");
    }

    @Test
    void testGetDocumentsByIdsWithMixedIds() {
        List<String> ids = Arrays.asList("mock-doc-0", "non-existent", "mock-doc-1");

        List<MarkdownDocument> docs = visitService.getDocumentsByIds(ids);

        assertNotNull(docs, "Document list should not be null");
        assertEquals(2, docs.size(), "Should return only existing documents");
    }

    @Test
    void testVisitById() {
        VisitService.VisitResult result = visitService.visitById(
                "mock-doc-0",
                "search query",
                "user123",
                "192.168.1.1"
        );

        assertNotNull(result, "Visit result should not be null");
        assertTrue(result.isSuccess(), "Visit should be successful");
        assertEquals("mock-doc-0", result.getDocId(), "Document ID should match");
        assertNotNull(result.getTitle(), "Title should not be null");
        assertNotNull(result.getContent(), "Content should not be null");
    }

    @Test
    void testVisitByIdNotFound() {
        VisitService.VisitResult result = visitService.visitById(
                "non-existent-id",
                "search query",
                "user123",
                "192.168.1.1"
        );

        assertNotNull(result, "Visit result should not be null");
        assertFalse(result.isSuccess(), "Visit should not be successful");
        assertEquals("Document not found", result.getError(), "Error message should indicate not found");
    }

    @Test
    void testVisitByPath() {
        VisitService.VisitResult result = visitService.visitByPath(
                "/mock/path/doc0.md",
                "search query",
                "user123",
                "192.168.1.1"
        );

        assertNotNull(result, "Visit result should not be null");
        assertTrue(result.isSuccess(), "Visit should be successful");
        assertNotNull(result.getDocId(), "Document ID should not be null");
    }

    @Test
    void testVisitByPathNotFound() {
        VisitService.VisitResult result = visitService.visitByPath(
                "/non/existent/path.md",
                "search query",
                "user123",
                "192.168.1.1"
        );

        assertNotNull(result, "Visit result should not be null");
        assertFalse(result.isSuccess(), "Visit should not be successful");
    }

    @Test
    void testVisitBatchByIds() {
        List<String> ids = Arrays.asList("mock-doc-0", "mock-doc-1");

        List<VisitService.VisitResult> results = visitService.visitBatchByIds(
                ids,
                "search query",
                "user123",
                "192.168.1.1"
        );

        assertNotNull(results, "Results should not be null");
        assertEquals(2, results.size(), "Should return 2 results");
        assertTrue(results.get(0).isSuccess(), "First visit should be successful");
        assertTrue(results.get(1).isSuccess(), "Second visit should be successful");
    }

    @Test
    void testVisitBatchByPaths() {
        List<String> paths = Arrays.asList("/mock/path/doc0.md", "/mock/path/doc1.md");

        List<VisitService.VisitResult> results = visitService.visitBatchByPaths(
                paths,
                "search query",
                "user123",
                "192.168.1.1"
        );

        assertNotNull(results, "Results should not be null");
        assertEquals(2, results.size(), "Should return 2 results");
    }

    @Test
    void testVisitWithNullQuery() {
        VisitService.VisitResult result = visitService.visitById(
                "mock-doc-0",
                null,
                "user123",
                "192.168.1.1"
        );

        assertTrue(result.isSuccess(), "Visit should succeed even with null query");
    }

    @Test
    void testVisitWithNullUserAndIp() {
        VisitService.VisitResult result = visitService.visitById(
                "mock-doc-0",
                "query",
                null,
                null
        );

        assertTrue(result.isSuccess(), "Visit should succeed even with null user and IP");
    }

    @Test
    void testContentTruncation() {
        // Get a document - mock service should handle truncation
        MarkdownDocument doc = visitService.getDocumentById("mock-doc-0");

        assertNotNull(doc, "Document should not be null");
        // MockVisitService truncates content to 50KB
        assertTrue(doc.getContent().length() <= 50 * 1024 + 20,
                "Content should be truncated to ~50KB");
    }

    @Test
    void testVisitResultFields() {
        VisitService.VisitResult result = visitService.visitById(
                "mock-doc-0",
                "query",
                "user",
                "ip"
        );

        assertTrue(result.isSuccess(), "Visit should be successful");
        assertEquals("mock-doc-0", result.getDocId(), "Doc ID should match");
        assertEquals("Mock Document 0", result.getTitle(), "Title should match");
        assertNotNull(result.getContent(), "Content should not be null");
        assertEquals("/mock/path/doc0.md", result.getFilePath(), "File path should match");
        assertNotNull(result.getTags(), "Tags should not be null");
        assertEquals(2, result.getTags().size(), "Should have 2 tags");
    }

    @Test
    void testBatchVisitWithNonExistentId() {
        List<String> ids = Arrays.asList("mock-doc-0", "non-existent", "mock-doc-1");

        List<VisitService.VisitResult> results = visitService.visitBatchByIds(
                ids,
                "query",
                "user",
                "ip"
        );

        assertEquals(3, results.size(), "Should return 3 results");
        assertTrue(results.get(0).isSuccess(), "First should be successful");
        assertFalse(results.get(1).isSuccess(), "Second should fail (not found)");
        assertTrue(results.get(2).isSuccess(), "Third should be successful");
    }
}
