package com.example.mdsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "mdsearch")
public class SearchProperties {

    private IndexConfig index = new IndexConfig();
    private SearchConfig search = new SearchConfig();
    private DirectoryConfig directory = new DirectoryConfig();
    private EmbeddingConfig embedding = new EmbeddingConfig();

    @Data
    public static class IndexConfig {
        private String name = "markdown_docs";
    }

    @Data
    public static class SearchConfig {
        @Min(0) @Max(1)
        private double bm25Weight = 0.4;
        @Min(0) @Max(1)
        private double titleVectorWeight = 0.3;
        @Min(0) @Max(1)
        private double contentVectorWeight = 0.3;
        private int size = 10;
    }

    @Data
    public static class DirectoryConfig {
        private long scanInterval = 60000;
        private String[] excludePatterns = new String[0];
    }

    @Data
    public static class EmbeddingConfig {
        private String apiUrl = "https://api.openai.com/v1/embeddings";
        private String apiKey;
        private String model = "text-embedding-ada-002";
        private int dimension = 1536;
        private int timeout = 30000;
    }
}
