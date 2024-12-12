package com.shuangqi.aiagent7.model.request;


import lombok.Data;

@Data
public class ChatRequest {
    public ChatRequest(String message, String systemPrompt) {
        this.message = message;
        this.systemPrompt = systemPrompt;
    }

    private String message;
    private String systemPrompt;
}
