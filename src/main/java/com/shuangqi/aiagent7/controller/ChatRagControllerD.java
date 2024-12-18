package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.service.ChatRagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/addVector")
    public Result addVector(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.success("文件为空，请选择一个有效的文件");
        }
        return Result.success(chatRagService.addVector(file));
    }

}
