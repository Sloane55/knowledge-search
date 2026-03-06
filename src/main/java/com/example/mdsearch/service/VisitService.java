package com.example.mdsearch.service;

import com.example.mdsearch.model.MarkdownDocument;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public interface VisitService {

    /**
     * Get document by ID, auto-truncate content to 50KB
     */
    MarkdownDocument getDocumentById(String docId);

    /**
     * Get document by file path, auto-truncate content to 50KB
     */
    MarkdownDocument getDocumentByPath(String filePath);

    /**
     * Get multiple documents by IDs, auto-truncate content to 50KB
     */
    List<MarkdownDocument> getDocumentsByIds(List<String> docIds);

    /**
     * Visit a document by ID - get document content
     */
    VisitResult visitById(String docId, String query, String userId, String ipAddress);

    /**
     * Visit a document by file path - get document content
     */
    VisitResult visitByPath(String filePath, String query, String userId, String ipAddress);

    /**
     * Batch visit multiple documents by IDs
     */
    List<VisitResult> visitBatchByIds(List<String> docIds, String query, String userId, String ipAddress);

    /**
     * Batch visit multiple documents by file paths
     */
    List<VisitResult> visitBatchByPaths(List<String> filePaths, String query, String userId, String ipAddress);

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class VisitResult {
        private String docId;
        private String title;
        private String content;
        private String filePath;
        private List<String> tags;
        private boolean success;
        private String error;
    }
}
