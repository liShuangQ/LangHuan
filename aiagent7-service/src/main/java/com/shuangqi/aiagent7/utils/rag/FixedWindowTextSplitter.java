package com.shuangqi.aiagent7.utils.rag;

import java.util.ArrayList;
import java.util.List;

public class FixedWindowTextSplitter implements TextSplitter {

    private final int windowSize;

    public FixedWindowTextSplitter(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public List<String> apply(String text) {
        List<String> documents = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += windowSize) {
            String chunk = text.substring(i, Math.min(length, i + windowSize));
            documents.add(chunk);
        }
        return documents;
    }


}