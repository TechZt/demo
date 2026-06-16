package com.example.demo.dto;

import com.example.demo.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatHistoryResponse {
    private String sessionId;
    private List<ChatMessage> messages;
}
