package com.example.mdsearch.service;

import com.example.mdsearch.model.MarkdownDocument;

import java.io.IOException;
import java.util.List;

public interface IndexService {

    /**
     * Index a single markdown document
     */
    MarkdownDocument indexDocument(String filePath) throws IOException;

    /**
     * Index a document with provided content
     */
    MarkdownDocument indexDocument(String filePath, String content, String title, List<String> tags) throws IOException;

    /**
     * Index multiple documents
     */
    List<MarkdownDocument> indexDocuments(List<String> filePaths) throws IOException;

    /**
     * Index all markdown files in a directory
     */
    List<MarkdownDocument> indexDirectory(String directoryPath) throws IOException;

    /**
     * Delete a document by ID
     */
    void deleteDocument(String id) throws IOException;

    /**
     * Re-index a document (update if exists)
     */
    MarkdownDocument reindexDocument(String filePath) throws IOException;
}
