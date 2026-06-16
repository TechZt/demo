package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SimilarityResponse {
    private List<String> texts;
    /** NxN 相似度矩阵，matrix[i][j] = texts[i] 与 texts[j] 的余弦相似度 */
    private List<List<Double>> similarityMatrix;
}
