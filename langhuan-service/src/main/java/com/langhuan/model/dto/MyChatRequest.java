package com.langhuan.model.dto;


import lombok.Data;

@Data
public class MyChatRequest {
    public MyChatRequest(String message, String systemPrompt) {
        this.message = message;
        this.systemPrompt = systemPrompt;
    }

    private String message;
    private String systemPrompt;
}
