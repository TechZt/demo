package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class EmbeddingClient {

    @Value("${deepseek.api.key}")
    private String apiKey;
    @Value("${deepseek.embedding.url}")
    private String embeddingUrl;
    @Value("${deepseek.embedding.model}")
    private String embeddingModel;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 批量将文本列表转为向量，一次 API 调用返回所有结果，顺序与入参一致。
     */
    @Retryable(
            value = {RestClientException.class, Exception.class},
            maxAttemptsExpression = "${deepseek.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${deepseek.retry.initial-delay:1000}", multiplier = 2)
    )
    public List<List<Double>> embedBatch(List<String> texts) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", embeddingModel);
        body.put("input", texts);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.debug("调用 Embedding API，model={}, texts={}", embeddingModel, texts.size());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                embeddingUrl, new HttpEntity<>(body, headers), Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

        // API 返回结果按 index 排序，确保顺序正确
        data.sort(Comparator.comparingInt(d -> (Integer) d.get("index")));

        List<List<Double>> result = new ArrayList<>();
        for (Map<String, Object> item : data) {
            @SuppressWarnings("unchecked")
            List<Double> embedding = (List<Double>) item.get("embedding");
            result.add(embedding);
        }
        return result;
    }

    @Recover
    public List<List<Double>> fallback(Exception e, List<String> texts) {
        log.error("Embedding API 多次重试后仍失败: {}", e.getMessage());
        throw new RuntimeException("Embedding 服务暂时不可用，请稍后重试", e);
    }
}
