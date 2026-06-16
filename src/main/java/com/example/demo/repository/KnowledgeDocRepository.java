package com.example.demo.repository;

import com.example.demo.model.KnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {
}
