package com.shuangqi.aiagent7.utils.rag;

import java.util.ArrayList;
import java.util.List;

public class SlidingWindowTextSplitter implements TextSplitter {

    private final int windowSize;
    private final int overlapSize;

    public SlidingWindowTextSplitter(int windowSize, int overlapSize) {
        this.windowSize = windowSize;
        this.overlapSize = overlapSize;
    }


    @Override
    public List<String> apply(String text) {
        List<String> documents = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + windowSize, length);
            String chunk = text.substring(start, end);
            documents.add(chunk);
            start += windowSize - overlapSize;
        }

        return documents;
    }
}