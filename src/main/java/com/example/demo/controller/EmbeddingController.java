package com.example.demo.controller;

import com.example.demo.dto.EmbeddingRequest;
import com.example.demo.dto.EmbeddingResponse;
import com.example.demo.dto.SimilarityRequest;
import com.example.demo.dto.SimilarityResponse;
import com.example.demo.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * POST /api/embedding
     * 将单条文本转换为向量。
     * 请求体：{"text": "贵州茅台是白酒龙头股"}
     */
    @PostMapping
    public ResponseEntity<EmbeddingResponse> embed(@RequestBody EmbeddingRequest request) {
        return ResponseEntity.ok(embeddingService.embed(request.getText()));
    }

    /**
     * POST /api/embedding/similarity
     * 计算多文本两两余弦相似度，返回 NxN 矩阵（值域 -1~1，越接近 1 越相似）。
     * 请求体：{"texts": ["文本A", "文本B", "文本C"]}
     */
    @PostMapping("/similarity")
    public ResponseEntity<SimilarityResponse> similarity(@RequestBody SimilarityRequest request) {
        return ResponseEntity.ok(embeddingService.similarity(request.getTexts()));
    }
}
