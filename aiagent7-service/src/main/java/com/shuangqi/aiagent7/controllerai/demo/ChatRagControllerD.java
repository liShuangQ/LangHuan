package com.shuangqi.aiagent7.controllerai.demo;

import com.shuangqi.aiagent7.serviceai.demo.ChatRagService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chatRag")
public class ChatRagControllerD {
    private final ChatRagService chatRagService;

    public ChatRagControllerD(ChatRagService chatRagService) {
        this.chatRagService = chatRagService;
    }

    @GetMapping("/string")
    public String string(@RequestParam String q) {
        return chatRagService.chat(q);
    }


    @GetMapping("/search")
    public String search(@RequestParam String q) {
        return chatRagService.ragSearch(q);
    }

    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam String p, @RequestParam String q) {
        return chatRagService.stream(q);
    }

    @GetMapping("/chatWithPrompt/ChatResponse")
    public ChatResponse chatWithPrompt(@RequestParam String p, @RequestParam String q) {
        return chatRagService.chatWithPrompt(p, q);
    }


}
