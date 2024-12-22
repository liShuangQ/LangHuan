package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.service.ChatFunctionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/function")
public class ChatFunctionControllerD {
    private final ChatFunctionService chatFunctionService;

    public ChatFunctionControllerD(ChatFunctionService chatFunctionService) {
        this.chatFunctionService = chatFunctionService;
    }

    @GetMapping("/string")
    public String string(@RequestParam String q) {
        return chatFunctionService.chat(q);
    }


    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam String p, @RequestParam String q) {
        return chatFunctionService.stream(q);
    }


}
