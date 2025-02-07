package com.shuangqi.aiagent7.controllerai;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.serviceai.ChatGeneralAssistanceService;
import com.shuangqi.aiagent7.serviceai.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
// 常规接口接口使用 “/chat” 开头，规范用途。当前作用为通用对话
public class ChatController {
    private final ChatService chatService;
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;
    @Value("${spring.ai.openai.chat.options.model}")
    private String defaultModelName;

    public ChatController(ChatService chatService, ChatGeneralAssistanceService chatGeneralAssistanceService) {
        this.chatService = chatService;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
    }

    //    NOTE:Flux<String>会和Security的拦截器冲突，所以要设置白名单  "/chat/chatFlux"
    @GetMapping("/chat/chat")
    public Result chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = ".") String p,
                       @RequestParam(name = "q", required = true) String q,
                       @RequestParam(name = "isRag", required = true) Boolean isRag,
                       @RequestParam(name = "ragType", required = true, defaultValue = "") String ragType,
                       @RequestParam(name = "isFunction", required = true) Boolean isFunction,
                       @RequestParam(name = "modelName", required = true, defaultValue = "") String modelName
    ) {
        if (modelName.isEmpty()) {
            modelName = defaultModelName;
        }
        return Result.success(Map.of(
                "chat", chatService.chat(id, p, q, isRag, ragType, isFunction, modelName),
                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
        ));
    }

    @GetMapping("/chat/getPrompt")
    public Result getPrompt(
            @RequestParam(name = "q", required = true) String q) {
        return Result.success(chatGeneralAssistanceService.optimizePromptWords(q));
    }

    @GetMapping("/chat/clearChatMemory")
    public Result chat(@RequestParam String id) {
        return Result.success(chatService.clearChatMemory(id));
    }

    @GetMapping("/onlyRag/chat")
    public Result onlyRagChat(@RequestParam(name = "id", required = true) String id,
                              @RequestParam(name = "p", required = true, defaultValue = ".") String p,
                              @RequestParam(name = "q", required = true) String q,
                              @RequestParam(name = "isRag", required = true) Boolean isRag,
                              @RequestParam(name = "ragType", required = true, defaultValue = "") String ragType,
                              @RequestParam(name = "isFunction", required = true) Boolean isFunction
    ) {
        return Result.success(Map.of(
                "chat", chatService.ragSearch(q, ragType)
//                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
        ));
    }


    @PostMapping("/chatModel/getModelList")
    public Result getModelList() {
        return Result.success(List.of("qwen2.5:3b", "deepseek-r1:1.5b"));
    }
}
