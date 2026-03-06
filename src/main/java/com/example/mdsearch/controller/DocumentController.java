package com.example.mdsearch.controller;

import com.example.mdsearch.model.ApiResponse;
import com.example.mdsearch.model.IndexRequest;
import com.example.mdsearch.model.MarkdownDocument;
import com.example.mdsearch.service.DirectoryScanService;
import com.example.mdsearch.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IndexService indexService;
    private final DirectoryScanService directoryScanService;

    @PostMapping
    public ApiResponse<MarkdownDocument> indexDocument(@RequestBody @Valid IndexRequest request) {
        try {
            MarkdownDocument doc = indexService.indexDocument(
                    request.getFilePath(),
                    request.getContent(),
                    request.getTitle(),
                    request.getTags()
            );
            return ApiResponse.success("Document indexed successfully", doc);
        } catch (IOException e) {
            return ApiResponse.error("Failed to index document: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public ApiResponse<List<MarkdownDocument>> indexDocuments(@RequestBody List<String> filePaths) {
        try {
            List<MarkdownDocument> docs = indexService.indexDocuments(filePaths);
            return ApiResponse.success("Indexed " + docs.size() + " documents", docs);
        } catch (IOException e) {
            return ApiResponse.error("Failed to index documents: " + e.getMessage());
        }
    }

    @PostMapping("/directory")
    public ApiResponse<DirectoryScanService.ScanResult> indexDirectory(@RequestParam String path) {
        try {
            DirectoryScanService.ScanResult result = directoryScanService.scanAndIndex(path);
            return ApiResponse.success("Directory indexed successfully", result);
        } catch (IOException e) {
            return ApiResponse.error("Failed to index directory: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable String id) {
        try {
            indexService.deleteDocument(id);
            return ApiResponse.success("Document deleted successfully", null);
        } catch (IOException e) {
            return ApiResponse.error("Failed to delete document: " + e.getMessage());
        }
    }

    @PostMapping("/{filePath}/reindex")
    public ApiResponse<MarkdownDocument> reindexDocument(@PathVariable String filePath) {
        try {
            MarkdownDocument doc = indexService.reindexDocument(filePath);
            return ApiResponse.success("Document re-indexed successfully", doc);
        } catch (IOException e) {
            return ApiResponse.error("Failed to re-index document: " + e.getMessage());
        }
    }

    @GetMapping("/scan/status")
    public ApiResponse<DirectoryScanService.ScanStatus> getScanStatus() {
        return ApiResponse.success(directoryScanService.getStatus());
    }
}
