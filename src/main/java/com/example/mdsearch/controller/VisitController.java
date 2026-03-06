package com.example.mdsearch.controller;

import com.example.mdsearch.model.ApiResponse;
import com.example.mdsearch.model.MarkdownDocument;
import com.example.mdsearch.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/visit")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    // ============ Document Access APIs ============

    /**
     * Get document by ID (content truncated to 50KB)
     */
    @GetMapping("/doc/{docId}")
    public ApiResponse<MarkdownDocument> getDocumentById(@PathVariable String docId) throws IOException {
        MarkdownDocument doc = visitService.getDocumentById(docId);
        if (doc == null) {
            return ApiResponse.error("Document not found");
        }
        return ApiResponse.success(doc);
    }

    /**
     * Get document by file path/URL (content truncated to 50KB)
     */
    @GetMapping("/doc/path")
    public ApiResponse<MarkdownDocument> getDocumentByPath(@RequestParam String filePath) throws IOException {
        MarkdownDocument doc = visitService.getDocumentByPath(filePath);
        if (doc == null) {
            return ApiResponse.error("Document not found");
        }
        return ApiResponse.success(doc);
    }

    /**
     * Get multiple documents by IDs (content truncated to 50KB)
     */
    @PostMapping("/doc/batch")
    public ApiResponse<List<MarkdownDocument>> getDocumentsByIds(@RequestBody List<String> docIds) throws IOException {
        List<MarkdownDocument> docs = visitService.getDocumentsByIds(docIds);
        return ApiResponse.success(docs);
    }

    // ============ Visit APIs (get document content) ============

    /**
     * Visit document by ID - returns document content
     */
    @PostMapping("/{docId}")
    public ApiResponse<VisitService.VisitResult> visitById(
            @PathVariable String docId,
            @RequestParam(required = false) String query,
            HttpServletRequest httpRequest) throws IOException {
        String userId = httpRequest.getHeader("X-User-Id");
        String ipAddress = getClientIp(httpRequest);

        VisitService.VisitResult result = visitService.visitById(docId, query, userId, ipAddress);
        if (!result.isSuccess()) {
            return ApiResponse.error(result.getError());
        }
        return ApiResponse.success(result);
    }

    /**
     * Visit document by file path/URL - returns document content
     */
    @PostMapping("/path")
    public ApiResponse<VisitService.VisitResult> visitByPath(
            @RequestParam String filePath,
            @RequestParam(required = false) String query,
            HttpServletRequest httpRequest) throws IOException {
        String userId = httpRequest.getHeader("X-User-Id");
        String ipAddress = getClientIp(httpRequest);

        VisitService.VisitResult result = visitService.visitByPath(filePath, query, userId, ipAddress);
        if (!result.isSuccess()) {
            return ApiResponse.error(result.getError());
        }
        return ApiResponse.success(result);
    }

    /**
     * Batch visit multiple documents by IDs
     */
    @PostMapping("/batch/ids")
    public ApiResponse<List<VisitService.VisitResult>> visitBatchByIds(
            @RequestBody BatchVisitRequest request,
            HttpServletRequest httpRequest) throws IOException {
        String userId = httpRequest.getHeader("X-User-Id");
        String ipAddress = getClientIp(httpRequest);

        List<VisitService.VisitResult> results = visitService.visitBatchByIds(
                request.getDocIds(),
                request.getQuery(),
                userId,
                ipAddress
        );
        return ApiResponse.success(results);
    }

    /**
     * Batch visit multiple documents by file paths
     */
    @PostMapping("/batch/paths")
    public ApiResponse<List<VisitService.VisitResult>> visitBatchByPaths(
            @RequestBody BatchVisitRequest request,
            HttpServletRequest httpRequest) throws IOException {
        String userId = httpRequest.getHeader("X-User-Id");
        String ipAddress = getClientIp(httpRequest);

        List<VisitService.VisitResult> results = visitService.visitBatchByPaths(
                request.getFilePaths(),
                request.getQuery(),
                userId,
                ipAddress
        );
        return ApiResponse.success(results);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @lombok.Data
    public static class BatchVisitRequest {
        private List<String> docIds;
        private List<String> filePaths;
        private String query;
    }
}
