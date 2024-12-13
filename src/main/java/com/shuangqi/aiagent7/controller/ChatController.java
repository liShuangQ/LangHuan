package com.shuangqi.aiagent7.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.dto.ChatRequest;
import com.shuangqi.aiagent7.service.OneRoundStepQuery;
import com.shuangqi.aiagent7.service.base.ChatBaseService;
import org.springframework.web.bind.annotation.*;

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
    public Result chat(@RequestBody ChatRequest request) {
        return Result.success(chatBaseService.chat(request));
    }

    @PostMapping("/step")
    public Result step(
            @RequestParam(value = "message", defaultValue = "", required = true) String message
    ) throws JsonProcessingException {
        return Result.success(oneRoundStepQuery.chat(message));
    }
}
