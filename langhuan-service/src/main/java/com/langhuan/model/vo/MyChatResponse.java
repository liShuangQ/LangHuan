package com.langhuan.model.vo;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyChatResponse {
    private String response;
    private String status;
}
