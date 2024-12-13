package com.shuangqi.aiagent7.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuangqi.aiagent7.model.dto.ChatRequest;
import com.shuangqi.aiagent7.model.vo.MyChatResponse;
import com.shuangqi.aiagent7.model.vo.ElephantExperimentVo;
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
        ElephantExperimentVo elephantExperimentVo = null;
        if (myChatResponse.getStatus().equals("success")) {
            elephantExperimentVo = new ObjectMapper().readValue(myChatResponse.getResponse(), ElephantExperimentVo.class);
        }
        String stepPrompt = "这个问题需要注意什么？请给我纯文字的格式";
        List<String> res = new ArrayList<>();
        if (elephantExperimentVo != null) {
            for (String stepMessage : elephantExperimentVo.getStep()) {
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
