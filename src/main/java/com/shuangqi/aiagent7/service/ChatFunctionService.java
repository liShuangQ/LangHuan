package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.functions.DeviceController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Service
public class ChatFunctionService {

    private final ChatClient chatClient;

    public ChatFunctionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem("回答问题")
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .build();

    }


    public String chat(String q) {

        return chatClient.prompt()
                .user("Turn on the living room lights")
                .functions(FunctionCallback.builder()
                        .description("Control device state")
                        .method("setDeviceState", String.class, boolean.class, ToolContext.class)
                        .targetObject(new DeviceController())
                        .build())
                .toolContext(Map.of("location", "home"))
                .call()
                .content();

//        return chatClient.prompt()
//                .user("What's the weather like in San Francisco, Tokyo, and Paris?")
//                .functions(FunctionCallback.builder()
//                        .description("Get the weather in location")
//                        .function("MockWeatherService", new MockWeatherService())
//                        .inputType(MockWeatherService.Request.class)
//                        .build())
//                .toolContext(Map.of("id", "1"))
//                .call()
//                .content();

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


}
