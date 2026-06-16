package com.example.demo.dto;

import lombok.Data;

@Data
public class RagQueryRequest {
    private String query;
    private Integer topK = 3;
}
