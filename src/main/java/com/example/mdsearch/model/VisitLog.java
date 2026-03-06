package com.example.mdsearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitLog {

    private String id;
    private String documentId;
    private String query;
    private String userId;
    private Date visitTime;
    private String ipAddress;
}
