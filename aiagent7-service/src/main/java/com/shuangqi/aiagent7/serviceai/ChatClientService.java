package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.service.TPromptsService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatClientService {

    private final ChatClient chatClient;

    public ChatClientService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
//                        new ReReadingAdvisor(),  // re2能力
                        new MySimplelogAdvisor()
                )
                .build();
    }

    public String chat(String q) {
        return this.chatClient.prompt(
                        new Prompt(
                                "回答我的问题"
                        ))
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .user(q)
                .call()
                .content();
    }

    public String chat1(String q) {
        return this.chatClient.prompt(
                        new Prompt(
                                "回答我的问题",
                                OpenAiChatOptions.builder()
                                        .model("deepseek-r1:1.5b")
                                        .build()
                        ))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .call()
                .content();
    }

    public Flux<String> stream(String q) {
        return chatClient.prompt()
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .stream()
                .content();
    }

    public ChatResponse chatWithPrompt(String p, String q) {
        return chatClient.prompt(new Prompt(p))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .call()
                .chatResponse();
    }

    public record ActorFilms(String actor, List<String> movies) {
    }

    public ActorFilms chatWithPromptArray(String p, String q) {
        List<Message> promptList = new ArrayList<>();
        for (String string : p.split(",")) {
            promptList.add(new UserMessage(string));
        }
        return chatClient.prompt(new Prompt(promptList))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .call()
                .entity(ActorFilms.class);
    }

    public List<ActorFilms> chatWithPromptListActorFilms(String p, String q) {
        q = q.isEmpty() ? "Generate the filmography of 5 movies for Tom Hanks and Bill Murray." : q;
        return chatClient.prompt(p)
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
    }

    public Map<String, String> chatWithPromptMap(String p, String q) {
        return Map.of("completion", this.chatClient.prompt(p).user(q).system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT")).call().content());
    }
}
