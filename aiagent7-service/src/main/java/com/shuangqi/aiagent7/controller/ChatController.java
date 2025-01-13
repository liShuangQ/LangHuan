package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.serviceai.ChatPromptService;
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
    private final ChatPromptService chatPromptService;

    public ChatController(ChatService chatService, ChatPromptService chatPromptService) {
        this.chatService = chatService;
        this.chatPromptService = chatPromptService;
    }

    @GetMapping("/chat")
    public Result chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = "请回答我的问题") String p,
                       @RequestParam(name = "q", required = true) String q) {
        return Result.success(chatService.chat(id, p, q));
    }

    @GetMapping("/getPrompt")
    public Result getPrompt(
            @RequestParam(name = "q", required = true) String q) {
        return Result.success(chatPromptService.optimizePromptWords(q));
    }

    @GetMapping("/clear")
    public Result chat(@RequestParam String id) {
        return Result.success(chatService.clear(id));
    }


    //    NOTE:Flux<String>会和Security的拦截器冲突，所以要设置白名单  "/chat/chatFlux"
    @GetMapping("/chatFlux")
    public Flux<String> chatFlux(@RequestParam(name = "id", required = true) String id,
                                 @RequestParam(name = "p", required = true, defaultValue = "请回答我的问题") String p,
                                 @RequestParam(name = "q", required = true) String q) {
        return chatService.chatFlux(id, p, q);
    }

}
