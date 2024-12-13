package com.shuangqi.aiagent7.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.dto.MyChatRequest;
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

    /**
     * 提示词基础对话
     * @param request
     * @return
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody MyChatRequest request) {
        return Result.success(chatBaseService.chat(request));
    }
    /**
     * 一轮链对话实验(大象装进冰箱要分为几步？)
     * @param message
     * @return
     */

    @PostMapping("/step")
    public Result step(
            @RequestParam(value = "message", defaultValue = "", required = true) String message
    ) throws JsonProcessingException {
        return Result.success(oneRoundStepQuery.chat(message));
    }
}
