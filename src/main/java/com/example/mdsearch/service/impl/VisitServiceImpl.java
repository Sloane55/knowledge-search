package com.example.mdsearch.service.impl;

import com.example.mdsearch.model.MarkdownDocument;
import com.example.mdsearch.repository.DocumentRepository;
import com.example.mdsearch.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final DocumentRepository documentRepository;

    private static final int MAX_CONTENT_SIZE = 50 * 1024; // 50KB

    @Override
    public MarkdownDocument getDocumentById(String docId) {
        try {
            Optional<MarkdownDocument> doc = documentRepository.findById(docId);
            return doc.map(this::truncateContent).orElse(null);
        } catch (Exception e) {
            log.error("Error getting document by id: {}", docId, e);
            return null;
        }
    }

    @Override
    public MarkdownDocument getDocumentByPath(String filePath) {
        try {
            List<MarkdownDocument> docs = documentRepository.findByFilePath(filePath);
            if (docs.isEmpty()) {
                return null;
            }
            return truncateContent(docs.get(0));
        } catch (Exception e) {
            log.error("Error getting document by path: {}", filePath, e);
            return null;
        }
    }

    @Override
    public List<MarkdownDocument> getDocumentsByIds(List<String> docIds) {
        List<MarkdownDocument> documents = new ArrayList<>();
        for (String docId : docIds) {
            MarkdownDocument doc = getDocumentById(docId);
            if (doc != null) {
                documents.add(doc);
            }
        }
        return documents;
    }

    @Override
    public VisitResult visitById(String docId, String query, String userId, String ipAddress) {
        MarkdownDocument doc = getDocumentById(docId);
        if (doc == null) {
            return VisitResult.builder()
                    .docId(docId)
                    .success(false)
                    .error("Document not found")
                    .build();
        }

        return VisitResult.builder()
                .docId(docId)
                .title(doc.getTitle())
                .content(doc.getContent())
                .filePath(doc.getFilePath())
                .tags(doc.getTags())
                .success(true)
                .build();
    }

    @Override
    public VisitResult visitByPath(String filePath, String query, String userId, String ipAddress) {
        MarkdownDocument doc = getDocumentByPath(filePath);
        if (doc == null) {
            return VisitResult.builder()
                    .filePath(filePath)
                    .success(false)
                    .error("Document not found")
                    .build();
        }
        return visitById(doc.getId(), query, userId, ipAddress);
    }

    @Override
    public List<VisitResult> visitBatchByIds(List<String> docIds, String query, String userId, String ipAddress) {
        List<VisitResult> results = new ArrayList<>();
        for (String docId : docIds) {
            try {
                results.add(visitById(docId, query, userId, ipAddress));
            } catch (Exception e) {
                log.error("Error visiting document: {}", docId, e);
                results.add(VisitResult.builder()
                        .docId(docId)
                        .success(false)
                        .error(e.getMessage())
                        .build());
            }
        }
        return results;
    }

    @Override
    public List<VisitResult> visitBatchByPaths(List<String> filePaths, String query, String userId, String ipAddress) {
        List<VisitResult> results = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                results.add(visitByPath(filePath, query, userId, ipAddress));
            } catch (Exception e) {
                log.error("Error visiting document by path: {}", filePath, e);
                results.add(VisitResult.builder()
                        .filePath(filePath)
                        .success(false)
                        .error(e.getMessage())
                        .build());
            }
        }
        return results;
    }

    private MarkdownDocument truncateContent(MarkdownDocument doc) {
        if (doc == null || doc.getContent() == null) {
            return doc;
        }

        String content = doc.getContent();
        if (content.length() > MAX_CONTENT_SIZE) {
            doc.setContent(content.substring(0, MAX_CONTENT_SIZE) + "\n... (truncated)");
        }
        return doc;
    }
}
