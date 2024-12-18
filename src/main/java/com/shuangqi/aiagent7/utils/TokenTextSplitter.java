package com.shuangqi.aiagent7.utils;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;

public class TokenTextSplitter {

    public List<Document> apply(String text) {
        List<Document> documents = new ArrayList<>();
        // 使用正则表达式按句号拆分文本
        String[] sentences = text.split("\\.");
        for (String sentence : sentences) {
            if (!sentence.trim().isEmpty()) {
                documents.add(new Document(sentence.trim()));
            }
        }
        return documents;
    }
}
