package com.langhuan.controllerai;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.serviceai.ChatService;
import com.langhuan.serviceai.StanfordChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
// 常规接口接口使用 “/chat” 开头，规范用途。当前作用为通用对话
public class ChatController {
    private final ChatService chatService;
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;
    private final StanfordChatService stanfordChatService;
    @Value("${spring.ai.openai.chat.options.model}")
    private String defaultModelName;
    @Value("${spring.ai.openai.base-url}")
    private String oneApiUrl;
    @Value("${spring.ai.openai.api-key}")
    private String oneApiKey;

    public ChatController(ChatService chatService, ChatGeneralAssistanceService chatGeneralAssistanceService, StanfordChatService stanfordChatService) {
        this.chatService = chatService;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
        this.stanfordChatService = stanfordChatService;
    }

    //    NOTE:Flux<String>会和Security的拦截器冲突，所以要设置白名单  "/chat/chatFlux"
    @PostMapping("/chat/chat")
    public Result chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = ".") String p,
                       @RequestParam(name = "q", required = true) String q,
                       @RequestParam(name = "isRag", required = true) Boolean isRag,
                       @RequestParam(name = "groupId", required = true, defaultValue = "") String groupId,
                       @RequestParam(name = "isFunction", required = true) Boolean isFunction,
                       @RequestParam(name = "modelName", required = true, defaultValue = "") String modelName,
                       @RequestParam(name = "chatMemoryRetrieveSize", required = true, defaultValue = "7") int chatMemoryRetrieveSize
    ) {
        if (modelName.isEmpty()) {
            modelName = defaultModelName;
        }

        String chat = chatService.chat(id, p, q, isRag, groupId, isFunction, modelName, chatMemoryRetrieveSize);

        if (chat.startsWith("***tools***")) {
            log.info("***tools***,工具询问二次询问模型");
            chat = chatGeneralAssistanceService.tools(chat);
        }

        return Result.success(Map.of(
                "chat", chat,
//                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
                "recommend", List.of()
        ));
    }

    @PostMapping("/chat/easyChat")
    public Result easyChat(
            @RequestParam(name = "p", required = true, defaultValue = ".") String p,
            @RequestParam(name = "q", required = true) String q,
            @RequestParam(name = "modelName", required = true, defaultValue = "") String modelName

    ) {
        if (modelName.isEmpty()) {
            modelName = defaultModelName;
        }

        String chat = chatService.easyChat(p, q, modelName);
        return Result.success(Map.of(
                "chat", chat,
//                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
                "recommend", List.of()
        ));
    }

    @PostMapping("/chat/getPrompt")
    public Result getPrompt(
            @RequestParam(name = "q", required = true) String q) {
        return Result.success(chatGeneralAssistanceService.optimizePromptWords(q));
    }

    @PostMapping("/chat/clearChatMemory")
    public Result chat(@RequestParam String id) {
        return Result.success(chatService.clearChatMemory(id));
    }

    @PostMapping("/onlyRag/chat")
    public Result onlyRagChat(@RequestParam(name = "id", required = true) String id,
                              @RequestParam(name = "p", required = true, defaultValue = ".") String p,
                              @RequestParam(name = "q", required = true) String q,
                              @RequestParam(name = "isRag", required = true) Boolean isRag,
                              @RequestParam(name = "groupId", required = true, defaultValue = "") String groupId,
                              @RequestParam(name = "isFunction", required = true) Boolean isFunction
    ) {
        return Result.success(Map.of(
                "chat", chatService.ragSearch(q, groupId)
//                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
        ));
    }


    @PostMapping("/chatModel/getModelList")
    public Result getModelList() throws IOException {
        HttpResponse<String> response = null;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oneApiUrl + "/v1/models"))
                .header("Authorization", oneApiKey)
                .build();

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Result.success(JSONObject.parseObject(response.body()));

        } catch (Exception e) {
            return Result.success(Map.of("data", List.of(Map.of("id", defaultModelName))));
        }

    }

    @PostMapping("/chat/stanford")
    public Result stanford(
            @RequestParam(name = "id", required = true) String id,
            @RequestParam(name = "p", required = true, defaultValue = ".") String p,
            @RequestParam(name = "q", required = true) String q,
            @RequestParam(name = "modelName", required = true, defaultValue = "") String modelName
    ) {
        return Result.success(stanfordChatService.chat(id, p, q, modelName));
    }
}
