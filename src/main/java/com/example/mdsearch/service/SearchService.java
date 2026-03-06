package com.example.mdsearch.service;

import com.example.mdsearch.model.SearchRequest;
import com.example.mdsearch.model.SearchResult;

import java.io.IOException;

public interface SearchService {

    /**
     * Hybrid search combining BM25, title vector and content vector search
     * Results are ranked by configurable weights:
     * - bm25Weight: weight for BM25 text search
     * - titleVectorWeight: weight for title vector similarity
     * - contentVectorWeight: weight for content vector similarity
     */
    SearchResult search(SearchRequest request) throws IOException;
}
