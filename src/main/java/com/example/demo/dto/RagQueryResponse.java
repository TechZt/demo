package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RagQueryResponse {
    private String answer;
    private List<SourceDoc> sources;

    @Data
    @AllArgsConstructor
    public static class SourceDoc {
        private Long id;
        private String title;
        private String snippet;
    }
}
