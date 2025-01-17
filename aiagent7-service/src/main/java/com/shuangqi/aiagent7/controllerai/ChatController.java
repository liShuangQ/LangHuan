package com.shuangqi.aiagent7.controllerai;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.serviceai.ChatGeneralAssistanceService;
import com.shuangqi.aiagent7.serviceai.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;

    public ChatController(ChatService chatService, ChatGeneralAssistanceService chatGeneralAssistanceService) {
        this.chatService = chatService;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
    }

    //    NOTE:Flux<String>会和Security的拦截器冲突，所以要设置白名单  "/chat/chatFlux"
    @GetMapping("/chat")
    public Result chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = ".") String p,
                       @RequestParam(name = "q", required = true) String q,
                       @RequestParam(name = "isRag", required = true) Boolean isRag,
                       @RequestParam(name = "ragType", required = true, defaultValue = "") String ragType,
                       @RequestParam(name = "isFunction", required = true) Boolean isFunction
    ) {

        return Result.success(Map.of(
                "chat", chatService.chat(id, p, q, isRag, ragType, isFunction),
                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
        ));
    }

    @GetMapping("/getPrompt")
    public Result getPrompt(
            @RequestParam(name = "q", required = true) String q) {
        return Result.success(chatGeneralAssistanceService.optimizePromptWords(q));
    }

    @GetMapping("/clear")
    public Result chat(@RequestParam String id) {
        return Result.success(chatService.clear(id));
    }


}
