package com.example.demo.service;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final Map<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();

    @Value("${deepseek.prompt-template}")
    private Resource promptTemplateResource;

    private String systemPrompt;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @PostConstruct
    private void loadPrompt() throws IOException {
        systemPrompt = StreamUtils.copyToString(promptTemplateResource.getInputStream(), StandardCharsets.UTF_8);
    }

    public ChatResponse chat(ChatRequest request) {
        String sessionId = (request.getSessionId() == null || request.getSessionId().isEmpty())
                ? UUID.randomUUID().toString()
                : request.getSessionId();

        List<ChatMessage> history = sessions.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(new ChatMessage("user", request.getMessage()));

        String reply = callDeepSeekApi(history);

        history.add(new ChatMessage("assistant", reply));
        return new ChatResponse(sessionId, reply, "assistant");
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return Collections.unmodifiableList(
                sessions.getOrDefault(sessionId, Collections.emptyList())
        );
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

    private String callDeepSeekApi(List<ChatMessage> messages) {
        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", systemPrompt);
        msgs.add(system);
        msgs.addAll(toMsgList(messages));
        return deepSeekClient.call(msgs);
    }

    private List<Map<String, String>> toMsgList(List<ChatMessage> messages) {
        return messages.stream()
                .map(m -> {
                    Map<String, String> msg = new HashMap<>();
                    msg.put("role", m.getRole());
                    msg.put("content", m.getContent());
                    return msg;
                })
                .collect(Collectors.toList());
    }
}
