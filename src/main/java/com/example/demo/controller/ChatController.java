package com.example.demo.controller;

import com.example.demo.dto.ChatHistoryResponse;
import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * POST /api/chat
     * 发送消息，获取 AI 回复。
     * sessionId 为空时自动创建新会话，返回的 sessionId 用于后续多轮对话。
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/chat/sessions/{sessionId}
     * 获取指定会话的历史消息。
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatHistoryResponse> getHistory(@PathVariable String sessionId) {
        ChatHistoryResponse history = new ChatHistoryResponse(
                sessionId,
                chatService.getHistory(sessionId)
        );
        return ResponseEntity.ok(history);
    }

    /**
     * DELETE /api/chat/sessions/{sessionId}
     * 清除指定会话及其历史记录。
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
