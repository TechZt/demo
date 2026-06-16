package com.example.demo.service;

import com.example.demo.dto.DocAddRequest;
import com.example.demo.dto.RagQueryRequest;
import com.example.demo.dto.RagQueryResponse;
import com.example.demo.model.KnowledgeDoc;
import com.example.demo.repository.KnowledgeDocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    private KnowledgeDocRepository docRepository;
    @Autowired
    private TfIdfRetriever retriever;
    @Autowired
    private DeepSeekClient deepSeekClient;

    @Value("${deepseek.prompt-template}")
    private Resource promptTemplateResource;
    private String systemPrompt;

    @PostConstruct
    private void init() throws IOException {
        systemPrompt = StreamUtils.copyToString(promptTemplateResource.getInputStream(), StandardCharsets.UTF_8);
        retriever.buildIndex(docRepository.findAll());
    }

    public KnowledgeDoc addDoc(DocAddRequest req) {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(req.getTitle());
        doc.setContent(req.getContent());
        doc.setSource(req.getSource());
        doc.setCreatedAt(LocalDateTime.now());
        KnowledgeDoc saved = docRepository.save(doc);
        retriever.buildIndex(docRepository.findAll());
        return saved;
    }

    public List<KnowledgeDoc> listDocs() {
        return docRepository.findAll();
    }

    public void deleteDoc(Long id) {
        docRepository.deleteById(id);
        retriever.buildIndex(docRepository.findAll());
    }

    public RagQueryResponse query(RagQueryRequest req) {
        int topK = req.getTopK() != null ? req.getTopK() : 3;
        List<KnowledgeDoc> retrieved = retriever.retrieve(req.getQuery(), topK);

        // 构建上下文
        String augmentedQuery;
        if (!retrieved.isEmpty()) {
            StringBuilder ctx = new StringBuilder();
            for (int i = 0; i < retrieved.size(); i++) {
                ctx.append(String.format("[%d] %s\n%s\n\n", i + 1, retrieved.get(i).getTitle(), retrieved.get(i).getContent()));
            }
            augmentedQuery = "参考以下资料：\n\n" + ctx + "请回答：" + req.getQuery();
        } else {
            augmentedQuery = req.getQuery();
        }

        // 构建消息列表（含 system prompt）
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", systemPrompt);
        messages.add(system);
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", augmentedQuery);
        messages.add(user);

        String answer = deepSeekClient.call(messages);

        List<RagQueryResponse.SourceDoc> sources = retrieved.stream()
                .map(d -> new RagQueryResponse.SourceDoc(
                        d.getId(),
                        d.getTitle(),
                        d.getContent().length() > 120 ? d.getContent().substring(0, 120) + "..." : d.getContent()
                ))
                .collect(Collectors.toList());

        return new RagQueryResponse(answer, sources);
    }
}
