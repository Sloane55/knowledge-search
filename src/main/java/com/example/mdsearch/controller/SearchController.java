package com.example.mdsearch.controller;

import com.example.mdsearch.model.ApiResponse;
import com.example.mdsearch.model.SearchRequest;
import com.example.mdsearch.model.SearchResult;
import com.example.mdsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Hybrid search - combines BM25, title vector and content vector search
     * Results are ranked by configurable weights (default: bm25=0.4, titleVector=0.3, contentVector=0.3)
     *
     * Request body:
     * - query: search query string (required)
     * - size: number of results (default: 10)
     * - tags: filter by tags (optional)
     * - bm25Weight: weight for BM25 score (optional, uses config default)
     * - titleVectorWeight: weight for title vector score (optional, uses config default)
     * - contentVectorWeight: weight for content vector score (optional, uses config default)
     */
    @PostMapping
    public ApiResponse<SearchResult> search(@RequestBody @Valid SearchRequest request,
                                            HttpServletRequest httpRequest) throws IOException {
        SearchResult result = searchService.search(request);
        return ApiResponse.success(result);
    }

    /**
     * GET version of hybrid search for convenience
     */
    @GetMapping
    public ApiResponse<SearchResult> searchGet(@RequestParam String query,
                                               @RequestParam(required = false, defaultValue = "10") Integer size,
                                               @RequestParam(required = false) Double bm25Weight,
                                               @RequestParam(required = false) Double titleVectorWeight,
                                               @RequestParam(required = false) Double contentVectorWeight,
                                               HttpServletRequest httpRequest) throws IOException {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .size(size)
                .bm25Weight(bm25Weight)
                .titleVectorWeight(titleVectorWeight)
                .contentVectorWeight(contentVectorWeight)
                .build();

        SearchResult result = searchService.search(request);
        return ApiResponse.success(result);
    }
}
