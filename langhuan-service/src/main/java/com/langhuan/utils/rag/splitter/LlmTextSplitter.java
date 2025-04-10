package com.langhuan.utils.rag.splitter;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.langhuan.serviceai.ChatGeneralAssistanceService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LlmTextSplitter {

    private final int windowSize;
    private final String modelName;
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;

    public LlmTextSplitter(int windowSize, String modelName, ChatGeneralAssistanceService chatGeneralAssistanceService) {
        this.windowSize = windowSize;
        this.modelName = modelName;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
    }

    public List<String> apply(String text) {
        List<String> documents = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += windowSize) {
            String chunk = text.substring(i, Math.min(length, i + windowSize));
            log.info("llmOutString:{}", chunk);
            String llmOutString = this.chatGeneralAssistanceService.llmTextSplitter(modelName, chunk);
            llmOutString = llmOutString.replace("```json", "");
            llmOutString = llmOutString.replace("```", "");
            JSONArray content = JSONObject.parseObject(llmOutString).getJSONArray("content");
            JSONObject.parseObject(llmOutString).getJSONArray("content");
            List<String> list = content.stream()
                    .map(Object::toString)
                    .toList();
            documents.addAll(list);
        }
        return documents;
    }


}
