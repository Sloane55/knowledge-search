package com.example.mdsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    @NotBlank(message = "Query cannot be blank")
    private String query;

    @Min(1) @Max(100)
    @Builder.Default
    private Integer size = 10;

    private List<String> tags;

    private Double bm25Weight;
    private Double titleVectorWeight;
    private Double contentVectorWeight;

    @Builder.Default
    private boolean recordVisit = true;
}
