package com.example.mdsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkdownDocument {

    private String id;
    private String title;
    private String content;
    private float[] titleVector;
    private float[] contentVector;
    private String filePath;
    private List<String> tags;
    private Date createdAt;
    private Date updatedAt;
    private String fileHash;
}
