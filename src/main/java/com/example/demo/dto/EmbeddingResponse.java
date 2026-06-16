package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EmbeddingResponse {
    private String text;
    private List<Double> vector;
    private int dimension;
}
