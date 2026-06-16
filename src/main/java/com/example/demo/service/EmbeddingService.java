package com.example.demo.service;

import com.example.demo.dto.EmbeddingResponse;
import com.example.demo.dto.SimilarityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmbeddingService {

    @Autowired
    private EmbeddingClient embeddingClient;

    public EmbeddingResponse embed(String text) {
        List<List<Double>> vectors = embeddingClient.embedBatch(Collections.singletonList(text));
        List<Double> vector = vectors.get(0);
        return new EmbeddingResponse(text, vector, vector.size());
    }

    public SimilarityResponse similarity(List<String> texts) {
        if (texts == null || texts.size() < 2) {
            throw new IllegalArgumentException("至少需要 2 条文本");
        }

        List<List<Double>> vectors = embeddingClient.embedBatch(texts);
        int n = texts.size();

        List<List<Double>> matrix = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                row.add(i == j ? 1.0 : cosineSimilarity(vectors.get(i), vectors.get(j)));
            }
            matrix.add(row);
        }

        return new SimilarityResponse(texts, matrix);
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot   += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
