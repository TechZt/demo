package com.example.demo.controller;

import com.example.demo.dto.DocAddRequest;
import com.example.demo.dto.RagQueryRequest;
import com.example.demo.dto.RagQueryResponse;
import com.example.demo.model.KnowledgeDoc;
import com.example.demo.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    /**
     * POST /api/rag/docs
     * 添加知识库文档。
     * {"title":"茅台2024年报摘要","content":"...","source":"官方年报"}
     */
    @PostMapping("/docs")
    public ResponseEntity<KnowledgeDoc> addDoc(@RequestBody DocAddRequest request) {
        return ResponseEntity.ok(ragService.addDoc(request));
    }

    /**
     * GET /api/rag/docs
     * 列出知识库所有文档。
     */
    @GetMapping("/docs")
    public ResponseEntity<List<KnowledgeDoc>> listDocs() {
        return ResponseEntity.ok(ragService.listDocs());
    }

    /**
     * DELETE /api/rag/docs/{id}
     * 删除指定文档并重建索引。
     */
    @DeleteMapping("/docs/{id}")
    public ResponseEntity<Void> deleteDoc(@PathVariable Long id) {
        ragService.deleteDoc(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/rag/query
     * RAG 查询：检索相关文档 + 生成回答。
     * {"query":"茅台最新营收情况","topK":3}
     */
    @PostMapping("/query")
    public ResponseEntity<RagQueryResponse> query(@RequestBody RagQueryRequest request) {
        return ResponseEntity.ok(ragService.query(request));
    }
}
