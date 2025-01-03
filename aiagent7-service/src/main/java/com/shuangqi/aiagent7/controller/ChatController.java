package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.serviceai.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public Result chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = "请回答我的问题") String p,
                       @RequestParam(name = "q", required = true) String q) {
        return Result.success(chatService.chat(id, p, q));
    }

    @GetMapping("/chatFlux")
    public Flux<String>chatFlux(@RequestParam(name = "id", required = true) String id,
                             @RequestParam(name = "p", required = true, defaultValue = "请回答我的问题") String p,
                             @RequestParam(name = "q", required = true) String q) {
        return chatService.chatFlux(id, p, q);
    }

    @GetMapping("/clear")
    public Result chat(@RequestParam String id) {
        return Result.success(chatService.clear(id));
    }

}
