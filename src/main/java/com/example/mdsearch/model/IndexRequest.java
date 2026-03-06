package com.example.mdsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexRequest {

    @NotBlank
    private String filePath;

    private String content;

    private String title;

    private List<String> tags;
}
