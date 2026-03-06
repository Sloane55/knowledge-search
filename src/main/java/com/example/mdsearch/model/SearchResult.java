package com.example.mdsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private long total;
    private List<SearchHit> hits;
    private double maxScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchHit {
        private String id;
        private String title;
        private String content;
        private String filePath;
        private List<String> tags;
        private double score;
        private double bm25Score;
        private double titleVectorScore;
        private double contentVectorScore;
        private String highlight;
    }
}
