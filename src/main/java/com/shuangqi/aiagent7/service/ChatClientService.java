package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.advisors.ReReadingAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
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
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new ReReadingAdvisor(),
                        new MySimplelogAdvisor()
                )
                .build();
    }

    public String chat(String q) {
        return this.chatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    public Flux<String> stream(String q) {
        return chatClient.prompt()
                .user(q)
                .stream()
                .content();
    }

    public ChatResponse chatWithPrompt(String p, String q) {
        return chatClient.prompt(new Prompt(p))
                .user(q)
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
                .call()
                .entity(ActorFilms.class);
    }

    public List<ActorFilms> chatWithPromptListActorFilms(String p, String q) {
        q = q.isEmpty() ? "Generate the filmography of 5 movies for Tom Hanks and Bill Murray." : q;
        return chatClient.prompt(p)
                .user(q)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
    }

    public Map<String, String> chatWithPromptMap(String p, String q) {
        return Map.of("completion", this.chatClient.prompt(p).user(q).call().content());
    }
}
