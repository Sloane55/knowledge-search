package com.example.mdsearch.service;

import com.example.mdsearch.config.MockConfig;
import com.example.mdsearch.model.MarkdownDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IndexService
 */
class IndexServiceTest {

    private IndexService indexService;

    @BeforeEach
    void setUp() {
        indexService = new MockConfig.MockIndexService();
    }

    @Test
    void testIndexSingleDocument() throws IOException {
        String filePath = "/test/docs/sample.md";

        MarkdownDocument doc = indexService.indexDocument(filePath);

        assertNotNull(doc, "Document should not be null");
        assertNotNull(doc.getId(), "Document ID should not be null");
        assertNotNull(doc.getFilePath(), "File path should not be null");
        assertEquals(filePath, doc.getFilePath(), "File path should match");
    }

    @Test
    void testIndexDocumentWithContent() throws IOException {
        String filePath = "/test/docs/with-content.md";
        String content = "This is the document content.";
        String title = "Test Document";
        List<String> tags = Arrays.asList("test", "java");

        MarkdownDocument doc = indexService.indexDocument(filePath, content, title, tags);

        assertNotNull(doc, "Document should not be null");
        assertEquals(title, doc.getTitle(), "Title should match");
        assertEquals(content, doc.getContent(), "Content should match");
        assertEquals(2, doc.getTags().size(), "Should have 2 tags");
        assertTrue(doc.getTags().contains("test"), "Should contain 'test' tag");
        assertTrue(doc.getTags().contains("java"), "Should contain 'java' tag");
    }

    @Test
    void testIndexDocumentWithNullTitle() throws IOException {
        String filePath = "/test/docs/no-title.md";
        String content = "Content without title";

        MarkdownDocument doc = indexService.indexDocument(filePath, content, null, null);

        assertNotNull(doc, "Document should not be null");
        assertEquals("Untitled", doc.getTitle(), "Should default to 'Untitled'");
        assertNotNull(doc.getTags(), "Tags should not be null");
        assertTrue(doc.getTags().isEmpty(), "Tags should be empty when null provided");
    }

    @Test
    void testIndexMultipleDocuments() throws IOException {
        List<String> filePaths = Arrays.asList(
                "/test/docs/doc1.md",
                "/test/docs/doc2.md",
                "/test/docs/doc3.md"
        );

        List<MarkdownDocument> docs = indexService.indexDocuments(filePaths);

        assertNotNull(docs, "Document list should not be null");
        assertEquals(3, docs.size(), "Should index 3 documents");

        for (MarkdownDocument doc : docs) {
            assertNotNull(doc.getId(), "Each document should have an ID");
        }
    }

    @Test
    void testIndexDirectory() throws IOException {
        String directoryPath = "/test/markdown-files";

        List<MarkdownDocument> docs = indexService.indexDirectory(directoryPath);

        assertNotNull(docs, "Document list should not be null");
        assertFalse(docs.isEmpty(), "Should index at least one document");
    }

    @Test
    void testDeleteDocument() throws IOException {
        // First index a document
        String filePath = "/test/docs/to-delete.md";
        MarkdownDocument doc = indexService.indexDocument(filePath);
        String docId = doc.getId();

        // Delete it - should not throw
        assertDoesNotThrow(() -> indexService.deleteDocument(docId),
                "Delete should not throw exception");
    }

    @Test
    void testReindexDocument() throws IOException {
        String filePath = "/test/docs/reindex.md";

        // First index
        MarkdownDocument doc1 = indexService.indexDocument(filePath);

        // Reindex
        MarkdownDocument doc2 = indexService.reindexDocument(filePath);

        assertNotNull(doc2, "Reindexed document should not be null");
        assertEquals(doc1.getId(), doc2.getId(), "ID should remain the same after reindex");
    }

    @Test
    void testIndexedDocumentHasTimestamp() throws IOException {
        String filePath = "/test/docs/timestamp-test.md";

        MarkdownDocument doc = indexService.indexDocument(filePath);

        assertNotNull(doc.getCreatedAt(), "Created timestamp should not be null");
        assertNotNull(doc.getUpdatedAt(), "Updated timestamp should not be null");
    }

    @Test
    void testIndexedDocumentHasFileHash() throws IOException {
        String filePath = "/test/docs/hash-test.md";

        MarkdownDocument doc = indexService.indexDocument(filePath);

        assertNotNull(doc.getFileHash(), "File hash should not be null");
    }

    @Test
    void testIndexDocumentWithEmptyContent() throws IOException {
        String filePath = "/test/docs/empty.md";

        MarkdownDocument doc = indexService.indexDocument(filePath, "", "Empty Doc", null);

        assertNotNull(doc, "Document should be created even with empty content");
        assertEquals("", doc.getContent(), "Content should be empty string");
    }

    @Test
    void testIndexDocumentWithSpecialCharactersInPath() throws IOException {
        String filePath = "/test/docs/special-chars-@#$%.md";

        MarkdownDocument doc = indexService.indexDocument(filePath);

        assertNotNull(doc, "Should handle special characters in file path");
        assertEquals(filePath, doc.getFilePath(), "File path should be preserved");
    }

    @Test
    void testIndexDocumentWithChinesePath() throws IOException {
        String filePath = "/test/docs/\u4e2d\u6587\u6587\u6863.md";

        MarkdownDocument doc = indexService.indexDocument(filePath);

        assertNotNull(doc, "Should handle Chinese characters in file path");
    }
}
