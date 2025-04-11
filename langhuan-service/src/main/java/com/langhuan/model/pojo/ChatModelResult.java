package com.langhuan.model.pojo;

import lombok.Data;

import org.springframework.ai.document.Document;
import java.util.List;

@Data
public class ChatModelResult {
    private String chat;
    private List<Document> rag;
}
