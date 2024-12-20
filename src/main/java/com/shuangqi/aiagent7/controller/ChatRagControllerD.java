package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.service.ChatRagService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/rag")
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

    @PostMapping("/addRagVector")
    public Result addRagVector(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.success("文件为空，请选择一个有效的文件");
        }
        return Result.success(chatRagService.addRagVector(file));
    }


}
