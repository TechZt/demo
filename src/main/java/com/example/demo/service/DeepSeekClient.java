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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeepSeekClient {

    @Value("${deepseek.api.key}")
    private String apiKey;
    @Value("${deepseek.api.url}")
    private String apiUrl;
    @Value("${deepseek.model}")
    private String model;
    @Value("${deepseek.max-tokens}")
    private int maxTokens;

    @Autowired
    private RestTemplate restTemplate;

    @Retryable(
            value = {RestClientException.class, Exception.class},
            maxAttemptsExpression = "${deepseek.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${deepseek.retry.initial-delay:1000}", multiplier = 2)
    )
    public String call(List<Map<String, String>> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("messages", messages);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.debug("调用 DeepSeek API，model={}, messages={}", model, messages.size());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl, new HttpEntity<>(body, headers), Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    @Recover
    public String fallback(Exception e, List<Map<String, String>> messages) {
        log.error("DeepSeek API 多次重试后仍失败: {}", e.getMessage());
        return "⚠️ AI 服务暂时不可用（已重试3次），请稍后再试。如需帮助请联系人工客服。";
    }
}
