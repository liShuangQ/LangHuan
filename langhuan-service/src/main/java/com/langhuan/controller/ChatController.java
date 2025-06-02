package com.langhuan.controller;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.serviceai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final RagService ragService;
    @Value("${spring.ai.openai.chat.options.model}")
    private String defaultModelName;
    @Value("${spring.ai.openai.base-url}")
    private String openApiUrl;
    @Value("${spring.ai.openai.api-key}")
    private String openApiKey;

    public ChatController(ChatService chatService, ChatGeneralAssistanceService chatGeneralAssistanceService, StanfordChatService stanfordChatService, RagService ragService ) {
        this.chatService = chatService;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
        this.stanfordChatService = stanfordChatService;
        this.ragService = ragService;
    }

    //    NOTE:Flux<String>会和Security的拦截器冲突，所以要设置白名单  "/chat/chatFlux"
    @PostMapping("/chat/chat")
    public Result chat(@RequestParam(name = "id", required = true) String id,
                       @RequestParam(name = "p", required = true, defaultValue = ".") String p,
                       @RequestParam(name = "q", required = true) String q,
                       @RequestParam(name = "isRag", required = true) Boolean isRag,
                       @RequestParam(name = "groupId", required = true, defaultValue = "") String groupId,
                       @RequestParam(name = "isFunction", required = true) Boolean isFunction,
                       @RequestParam(name = "modelName", required = true, defaultValue = "") String modelName
    ) {
        id = SecurityContextHolder.getContext().getAuthentication().getName() + "_" + id;

        if (modelName.isEmpty()) {
            modelName = defaultModelName;
        }

        ChatRestOption chatRestOption = new ChatRestOption();
        chatRestOption.setChatId(id);
        chatRestOption.setPrompt(p);
        chatRestOption.setQuestion(q);
        chatRestOption.setIsRag(isRag);
        chatRestOption.setRagGroupId(groupId);
        chatRestOption.setIsFunction(isFunction);
        chatRestOption.setModelName(modelName);

        ChatModelResult chatModelResult = chatService.chat(chatRestOption);

        String chat = chatModelResult.getChat();
        if (chat.startsWith("***tools***")) {
            log.info("***tools***,工具询问二次询问模型");
            chat = chatGeneralAssistanceService.tools(chat);
        }

        return Result.success(Map.of(
                "chat", chat,
                "rag", chatModelResult.getRag(),
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

        String chat = chatGeneralAssistanceService.easyChat(p, q, modelName);
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

    @PostMapping("/chat/setChatMemoryWindowsName")
    public Result setChatMemoryWindowsName(
            @RequestParam(name = "id", required = true) String id,
            @RequestParam(name = "name", required = true) String name
    ) {
        return Result.success(chatService.setChatMemoryWindowsName(id, name));
    }

    @PostMapping("/chat/getChatMemoryWindows")
    public Result getChatMemoryWindows() {
        return Result.success(chatService.getChatMemoryWindows());
    }

    @PostMapping("/chat/getChatMemory")
    public Result getChatMemory(@RequestParam String id) {
        chatService.initChatMemory(id);
        return Result.success(chatService.getChatMemory(id));
    }

    @PostMapping("/chat/saveChatMemory")
    public Result saveChatMemory(@RequestParam String id,
                                 @RequestParam String name) {
        return Result.success(chatService.saveChatMemory(id, name));
    }

    @PostMapping("/chat/clearChatMemory")
    public Result clearChatMemory(@RequestParam String id) {
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

        List<Document> documentList = ragService.ragSearch(q, groupId, "");
        StringBuilder contents = new StringBuilder();
        int i = 0;
        for (Document document : documentList) {
            i += 1;
            contents.append("<p>").append(i).append(":").append("&nbsp;").append(document.getText()).append("</p>");
        }
        return Result.success(Map.of(
                "chat", contents.toString()
//                "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
        ));
    }


    @PostMapping("/chatModel/getModelList")
    public Result getModelList() throws IOException {
        HttpResponse<String> response = null;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openApiUrl + "/v1/models"))
                .header("Authorization", openApiKey)
                .build();

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Result.success(JSONObject.parseObject(response.body()));

        } catch (Exception e) {
            log.error("获取模型列表失败", e);
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
