package com.shuangqi.aiagent7.model.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyChatResponse {
    private String response;
    private String status;
}
