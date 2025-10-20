package com.langhuan.utils.rag.splitter;

import java.util.ArrayList;
import java.util.List;

public class SlidingWindowTextSplitter implements TextSplitter  {

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

        // 分批处理，减少内存占用
        int batchSize = 1000; // 每批处理的大小可以根据实际情况调整
        List<String> batch = new ArrayList<>(batchSize);

        while (start < length) {
            int end = Math.min(start + windowSize, length);
            String chunk = text.substring(start, end);
            batch.add(chunk);
            start += windowSize - overlapSize;

            // 当达到一批的大小时，将批处理的结果添加到最终结果中
            if (batch.size() >= batchSize) {
                documents.addAll(batch);
                batch.clear();
            }
        }

        // 添加剩余的批处理结果
        documents.addAll(batch);

        return documents;
    }
}
