package com.shuangqi.aiagent7.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuangqi.aiagent7.model.request.ChatRequest;
import com.shuangqi.aiagent7.model.response.MyChatResponse;
import com.shuangqi.aiagent7.model.response.ElephantExperimentRes;
import com.shuangqi.aiagent7.service.base.ChatBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OneRoundStepQuery {
    private final ChatBaseService chatBaseService;

    public OneRoundStepQuery(ChatBaseService chatBaseService) {
        this.chatBaseService = chatBaseService;
    }

    public List<String> chat(String message) throws JsonProcessingException {
        String prompt = "回答时数据使用step作为key，全部的步骤作为一个数组。例如{step:[步骤一,步骤二]}这种格式给我";
        MyChatResponse myChatResponse = chatBaseService.chat(new ChatRequest(message, prompt));
        ElephantExperimentRes elephantExperimentRes = null;
        if (myChatResponse.getStatus().equals("success")) {
            elephantExperimentRes = new ObjectMapper().readValue(myChatResponse.getResponse(), ElephantExperimentRes.class);
        }
        String stepPrompt = "这个问题需要注意什么？请给我纯文字的格式";
        List<String> res = new ArrayList<>();
        if (elephantExperimentRes != null) {
            for (String stepMessage : elephantExperimentRes.getStep()) {
                MyChatResponse stepResponse = chatBaseService.chat(new ChatRequest(stepMessage, stepPrompt));
                if (stepResponse.getStatus().equals("success")) {
                    res.add(stepResponse.getResponse());
                }
            }
        } else {
            return res;
        }
        return res;
    }
}
