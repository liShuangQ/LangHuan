package com.shuangqi.aiagent7.model;


import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String systemPrompt;
}
