package com.shuangqi.aiagent7.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shuangqi.aiagent7.model.request.ChatRequest;
import com.shuangqi.aiagent7.model.response.ChatResponse;
import com.shuangqi.aiagent7.service.OneRoundStepQuery;
import com.shuangqi.aiagent7.service.base.ChatBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatBaseService chatBaseService;
    private final OneRoundStepQuery oneRoundStepQuery;

    public ChatController(ChatBaseService chatBaseService, OneRoundStepQuery oneRoundStepQuery) {
        this.chatBaseService = chatBaseService;
        this.oneRoundStepQuery = oneRoundStepQuery;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatBaseService.chat(request);
    }

    @PostMapping("/step")
    public List<String> step(
            @RequestParam(value = "message", defaultValue = "", required = true) String message
    ) throws JsonProcessingException {
        return oneRoundStepQuery.chat(message);
    }
}
