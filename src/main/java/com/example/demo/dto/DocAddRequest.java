package com.example.demo.dto;

import lombok.Data;

@Data
public class DocAddRequest {
    private String title;
    private String content;
    private String source;
}
