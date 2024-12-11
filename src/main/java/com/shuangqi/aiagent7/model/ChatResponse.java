package com.shuangqi.aiagent7.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String response;
    private String status;
}
