package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.serviceai.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = "请回答我的问题") String p,
                       @RequestParam(name = "q", required = true) String q) {
        return chatService.chat(id, p, q);
    }

    @GetMapping("/clear")
    public String chat(@RequestParam String id) {
        return chatService.clear(id);
    }

}
