package com.example.mdsearch.repository;

import com.example.mdsearch.config.SearchProperties;
import com.example.mdsearch.model.MarkdownDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentRepository {

    private final RestHighLevelClient esClient;
    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String indexName;

    @PostConstruct
    public void init() {
        this.indexName = searchProperties.getIndex().getName();
        createIndexIfNotExists();
    }

    private static final int DIMENSION = 1536;

    private void createIndexIfNotExists() {
        try {
            String source = "{\n" +
                    "  \"mappings\": {\n" +
                    "    \"properties\": {\n" +
                    "      \"id\": { \"type\": \"keyword\" },\n" +
                    "      \"title\": { \"type\": \"text\" },\n" +
                    "      \"content\": { \"type\": \"text\" },\n" +
                    "      \"titleVector\": { \"type\": \"dense_vector\", \"dims\": " + DIMENSION + " },\n" +
                    "      \"contentVector\": { \"type\": \"dense_vector\", \"dims\": " + DIMENSION + " },\n" +
                    "      \"filePath\": { \"type\": \"keyword\" },\n" +
                    "      \"tags\": { \"type\": \"keyword\" },\n" +
                    "      \"createdAt\": { \"type\": \"date\" },\n" +
                    "      \"updatedAt\": { \"type\": \"date\" },\n" +
                    "      \"fileHash\": { \"type\": \"keyword\" }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            // Use low-level client to create index
            org.elasticsearch.client.Request request = new org.elasticsearch.client.Request(
                    "PUT", "/" + indexName);
            request.setJsonEntity(source);
            org.elasticsearch.client.Response response = esClient.getLowLevelClient()
                    .performRequest(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                log.info("Index {} created successfully", indexName);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("resource_already_exists_exception")) {
                log.info("Index {} already exists", indexName);
            } else {
                log.error("Error creating index: {}", e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void save(MarkdownDocument document) throws IOException {
        Map<String, Object> source = objectMapper.convertValue(document, Map.class);
        IndexRequest request = new IndexRequest(indexName)
                .id(document.getId())
                .source(source);

        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        log.debug("Document indexed: {}", response.getId());
    }

    public void saveAll(List<MarkdownDocument> documents) throws IOException {
        for (MarkdownDocument doc : documents) {
            save(doc);
        }
    }

    public Optional<MarkdownDocument> findById(String id) throws IOException {
        GetRequest request = new GetRequest(indexName, id);
        GetResponse response = esClient.get(request, RequestOptions.DEFAULT);

        if (response.isExists()) {
            MarkdownDocument doc = objectMapper.readValue(response.getSourceAsString(), MarkdownDocument.class);
            return Optional.of(doc);
        }
        return Optional.empty();
    }

    public void deleteById(String id) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, id);
        esClient.delete(request, RequestOptions.DEFAULT);
    }

    public List<MarkdownDocument> findByFilePath(String filePath) throws IOException {
        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("filePath", filePath));
        request.source(sourceBuilder);

        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
        List<MarkdownDocument> results = new ArrayList<>();
        response.getHits().forEach(hit -> {
            try {
                MarkdownDocument doc = objectMapper.readValue(hit.getSourceAsString(), MarkdownDocument.class);
                results.add(doc);
            } catch (IOException e) {
                log.error("Error parsing document", e);
            }
        });
        return results;
    }

    public SearchResponse search(SearchRequest request) throws IOException {
        return esClient.search(request, RequestOptions.DEFAULT);
    }

    public String getIndexName() {
        return indexName;
    }
}
