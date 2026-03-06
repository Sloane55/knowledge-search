package com.example.mdsearch.service.impl;

import com.example.mdsearch.model.MarkdownDocument;
import com.example.mdsearch.repository.DocumentRepository;
import com.example.mdsearch.service.EmbeddingService;
import com.example.mdsearch.service.IndexService;
import com.example.mdsearch.util.MarkdownParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    @Override
    public MarkdownDocument indexDocument(String filePath) throws IOException {
        String content = MarkdownParser.readFileContent(filePath);
        String title = MarkdownParser.extractTitle(content);
        return indexDocument(filePath, content, title, null);
    }

    @Override
    public MarkdownDocument indexDocument(String filePath, String content, String title, List<String> tags) throws IOException {
        log.info("Indexing document: {}", filePath);

        String hash = MarkdownParser.calculateHash(content);

        // Check if document exists with same hash
        List<MarkdownDocument> existing = documentRepository.findByFilePath(filePath);
        if (!existing.isEmpty()) {
            MarkdownDocument existingDoc = existing.get(0);
            if (hash.equals(existingDoc.getFileHash())) {
                log.info("Document unchanged, skipping: {}", filePath);
                return existingDoc;
            }
        }

        // Extract plain text for embedding
        String plainText = MarkdownParser.extractPlainText(content);

        // Generate vectors
        float[] titleVector = embeddingService.embed(title);
        float[] contentVector = embeddingService.embed(truncateForEmbedding(plainText, 8000));

        String docId = existing.isEmpty() ?
                UUID.nameUUIDFromBytes(filePath.getBytes()).toString() :
                existing.get(0).getId();

        MarkdownDocument document = MarkdownDocument.builder()
                .id(docId)
                .title(title)
                .content(content)
                .titleVector(titleVector)
                .contentVector(contentVector)
                .filePath(filePath)
                .tags(tags)
                .createdAt(existing.isEmpty() ? new Date() : existing.get(0).getCreatedAt())
                .updatedAt(new Date())
                .fileHash(hash)
                .build();

        documentRepository.save(document);
        log.info("Document indexed successfully: {}", filePath);

        return document;
    }

    @Override
    public List<MarkdownDocument> indexDocuments(List<String> filePaths) throws IOException {
        List<MarkdownDocument> documents = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                documents.add(indexDocument(filePath));
            } catch (Exception e) {
                log.error("Error indexing document: {}", filePath, e);
            }
        }
        return documents;
    }

    @Override
    public List<MarkdownDocument> indexDirectory(String directoryPath) throws IOException {
        log.info("Indexing directory: {}", directoryPath);

        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(directoryPath);
        log.info("Found {} markdown files", mdFiles.size());

        return indexDocuments(mdFiles);
    }

    @Override
    public void deleteDocument(String id) throws IOException {
        log.info("Deleting document: {}", id);
        documentRepository.deleteById(id);
    }

    @Override
    public MarkdownDocument reindexDocument(String filePath) throws IOException {
        log.info("Re-indexing document: {}", filePath);

        // Delete existing document
        List<MarkdownDocument> existing = documentRepository.findByFilePath(filePath);
        for (MarkdownDocument doc : existing) {
            documentRepository.deleteById(doc.getId());
        }

        // Re-index
        return indexDocument(filePath);
    }

    private String truncateForEmbedding(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
