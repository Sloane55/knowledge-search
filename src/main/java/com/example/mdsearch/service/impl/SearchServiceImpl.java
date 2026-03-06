package com.example.mdsearch.service.impl;

import com.example.mdsearch.config.SearchProperties;
import com.example.mdsearch.model.MarkdownDocument;
import com.example.mdsearch.model.SearchRequest;
import com.example.mdsearch.model.SearchResult;
import com.example.mdsearch.repository.DocumentRepository;
import com.example.mdsearch.service.EmbeddingService;
import com.example.mdsearch.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SearchResult search(SearchRequest request) throws IOException {
        log.info("Performing hybrid search for: {}", request.getQuery());

        SearchProperties.SearchConfig config = searchProperties.getSearch();

        double bm25Weight = request.getBm25Weight() != null ? request.getBm25Weight() : config.getBm25Weight();
        double titleVectorWeight = request.getTitleVectorWeight() != null ? request.getTitleVectorWeight() : config.getTitleVectorWeight();
        double contentVectorWeight = request.getContentVectorWeight() != null ? request.getContentVectorWeight() : config.getContentVectorWeight();

        int size = request.getSize() != null ? request.getSize() : config.getSize();

        double totalWeight = bm25Weight + titleVectorWeight + contentVectorWeight;
        if (totalWeight > 0) {
            bm25Weight /= totalWeight;
            titleVectorWeight /= totalWeight;
            contentVectorWeight /= totalWeight;
        }

        log.debug("Using weights - BM25: {}, TitleVector: {}, ContentVector: {}",
                bm25Weight, titleVectorWeight, contentVectorWeight);

        float[] queryVector = embeddingService.embed(request.getQuery());

        Map<String, Double> bm25Results = performBM25Search(request.getQuery(), size * 2, request.getTags());
        log.debug("BM25 search returned {} results", bm25Results.size());

        Map<String, Double> titleVectorResults = performVectorSearch(queryVector, "titleVector", size * 2);
        log.debug("Title vector search returned {} results", titleVectorResults.size());

        Map<String, Double> contentVectorResults = performVectorSearch(queryVector, "contentVector", size * 2);
        log.debug("Content vector search returned {} results", contentVectorResults.size());

        Map<String, Double> combinedScores = new HashMap<>();
        Set<String> allDocIds = new HashSet<>();
        allDocIds.addAll(bm25Results.keySet());
        allDocIds.addAll(titleVectorResults.keySet());
        allDocIds.addAll(contentVectorResults.keySet());

        for (String docId : allDocIds) {
            double bm25Score = bm25Results.getOrDefault(docId, 0.0);
            double titleScore = titleVectorResults.getOrDefault(docId, 0.0);
            double contentScore = contentVectorResults.getOrDefault(docId, 0.0);

            double combinedScore = bm25Weight * bm25Score +
                    titleVectorWeight * titleScore +
                    contentVectorWeight * contentScore;

            combinedScores.put(docId, combinedScore);
        }

        List<String> topDocIds = combinedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<SearchResult.SearchHit> hits = new ArrayList<>();
        for (String docId : topDocIds) {
            Optional<MarkdownDocument> docOpt = documentRepository.findById(docId);
            if (docOpt.isPresent()) {
                MarkdownDocument doc = docOpt.get();
                SearchResult.SearchHit hit = SearchResult.SearchHit.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .content(truncateContent(doc.getContent(), 500))
                        .filePath(doc.getFilePath())
                        .tags(doc.getTags())
                        .score(combinedScores.get(docId))
                        .bm25Score(bm25Results.getOrDefault(docId, 0.0))
                        .titleVectorScore(titleVectorResults.getOrDefault(docId, 0.0))
                        .contentVectorScore(contentVectorResults.getOrDefault(docId, 0.0))
                        .highlight(extractHighlight(doc.getContent(), request.getQuery()))
                        .build();
                hits.add(hit);
            }
        }

        log.info("Hybrid search returned {} results from {} total candidates", hits.size(), allDocIds.size());

        return SearchResult.builder()
                .total(allDocIds.size())
                .hits(hits)
                .maxScore(hits.isEmpty() ? 0 : hits.get(0).getScore())
                .build();
    }

    private Map<String, Double> performBM25Search(String query, int size, List<String> tags) throws IOException {
        // Use fully qualified name for ES SearchRequest
        org.elasticsearch.action.search.SearchRequest esSearchRequest =
            new org.elasticsearch.action.search.SearchRequest(documentRepository.getIndexName());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);

        QueryBuilder queryBuilder;
        if (tags != null && !tags.isEmpty()) {
            queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.multiMatchQuery(query, "title", "content"))
                    .filter(QueryBuilders.termsQuery("tags", tags));
        } else {
            queryBuilder = QueryBuilders.multiMatchQuery(query, "title", "content");
        }

        sourceBuilder.query(queryBuilder);
        esSearchRequest.source(sourceBuilder);

        SearchResponse response = documentRepository.search(esSearchRequest);

        Map<String, Double> results = new LinkedHashMap<>();
        for (SearchHit hit : response.getHits()) {
            results.put(hit.getId(), (double) hit.getScore());
        }

        return normalizeScores(results);
    }

    private Map<String, Double> performVectorSearch(float[] queryVector, String vectorField, int size) throws IOException {
        // Use fully qualified name for ES SearchRequest
        org.elasticsearch.action.search.SearchRequest esSearchRequest =
            new org.elasticsearch.action.search.SearchRequest(documentRepository.getIndexName());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);

        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", queryVector);
        params.put("field", vectorField);

        // ES 7.1.1: Use dotProduct function (available in ES 7.x)
        // Note: dense_vector must be accessed via doc['field'].dotProduct(params.vector)
        // Since our vectors are L2 normalized, dotProduct equals cosine similarity
        String scriptCode =
            "return dotProduct(params.query_vector, doc[params.field]) + 1.0;";

        Script script = new Script(ScriptType.INLINE, "painless", scriptCode, params);

        QueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                QueryBuilders.matchAllQuery(),
                ScoreFunctionBuilders.scriptFunction(script)
        );

        sourceBuilder.query(functionScoreQuery);
        esSearchRequest.source(sourceBuilder);

        try {
            SearchResponse response = documentRepository.search(esSearchRequest);

            Map<String, Double> results = new LinkedHashMap<>();
            for (SearchHit hit : response.getHits()) {
                results.put(hit.getId(), (double) hit.getScore());
            }

            return normalizeScores(results);
        } catch (Exception e) {
            log.error("Vector search failed for field: {}", vectorField, e);
            // Fallback: try alternative script syntax
            return performVectorSearchFallback(queryVector, vectorField, size);
        }
    }

    /**
     * Fallback vector search for ES versions that don't support dotProduct function
     */
    private Map<String, Double> performVectorSearchFallback(float[] queryVector, String vectorField, int size) throws IOException {
        org.elasticsearch.action.search.SearchRequest esSearchRequest =
            new org.elasticsearch.action.search.SearchRequest(documentRepository.getIndexName());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);

        // Build a manual cosine similarity script
        StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append("double dot = 0.0;");
        scriptBuilder.append("double normDoc = 0.0;");
        scriptBuilder.append("double normQuery = 0.0;");
        scriptBuilder.append("List docVec = doc['").append(vectorField).append("'].value;");
        scriptBuilder.append("if (docVec != null && docVec.size() > 0) {");
        scriptBuilder.append("  for (int i = 0; i < params.query_vector.length && i < docVec.size(); i++) {");
        scriptBuilder.append("    double v = ((Number)docVec.get(i)).doubleValue();");
        scriptBuilder.append("    dot += params.query_vector[i] * v;");
        scriptBuilder.append("    normDoc += v * v;");
        scriptBuilder.append("    normQuery += params.query_vector[i] * params.query_vector[i];");
        scriptBuilder.append("  }");
        scriptBuilder.append("  if (normDoc > 0 && normQuery > 0) {");
        scriptBuilder.append("    return dot / (Math.sqrt(normDoc) * Math.sqrt(normQuery)) + 1.0;");
        scriptBuilder.append("  }");
        scriptBuilder.append("}");
        scriptBuilder.append("return 0.0;");

        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", queryVector);

        Script script = new Script(ScriptType.INLINE, "painless", scriptBuilder.toString(), params);

        QueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                QueryBuilders.matchAllQuery(),
                ScoreFunctionBuilders.scriptFunction(script)
        );

        sourceBuilder.query(functionScoreQuery);
        esSearchRequest.source(sourceBuilder);

        try {
            SearchResponse response = documentRepository.search(esSearchRequest);

            Map<String, Double> results = new LinkedHashMap<>();
            for (SearchHit hit : response.getHits()) {
                results.put(hit.getId(), (double) hit.getScore());
            }

            return normalizeScores(results);
        } catch (Exception e) {
            log.error("Vector search fallback also failed for field: {}", vectorField, e);
            return new HashMap<>();
        }
    }

    private Map<String, Double> normalizeScores(Map<String, Double> scores) {
        if (scores.isEmpty()) {
            return scores;
        }

        double maxScore = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double minScore = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double range = maxScore - minScore;

        if (range == 0) {
            return scores;
        }

        Map<String, Double> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            normalized.put(entry.getKey(), (entry.getValue() - minScore) / range);
        }

        return normalized;
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private String extractHighlight(String content, String query) {
        if (content == null || query == null) {
            return null;
        }

        int idx = content.toLowerCase().indexOf(query.toLowerCase());
        if (idx == -1) {
            return truncateContent(content, 200);
        }

        int start = Math.max(0, idx - 50);
        int end = Math.min(content.length(), idx + query.length() + 100);

        return (start > 0 ? "..." : "") +
                content.substring(start, end) +
                (end < content.length() ? "..." : "");
    }
}
