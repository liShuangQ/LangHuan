package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.model.pojo.MyChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@RestController
@RequestMapping("/advisorDemo")
public class AdvisorDemoController {
    private final ChatClient chatClient;

    public AdvisorDemoController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    /**
     * 动态筛选表达式
     * 在运行时使用 advisor context 参数更新筛选条件表达式：SearchRequestFILTER_EXPRESSION
     *
     * @return
     */
    @GetMapping("/advisor1")
    String advisor1() {

        return this.chatClient.prompt()
                .user("Please answer my question XYZ")
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "type == 'Spring'"))
                .call()
                .content();
    }

    @GetMapping("/advisor2")
    ChatResponse advisor2() {
        ChatClient.Builder chatClientBuilder = null;
        return chatClientBuilder.build().prompt()
                .advisors(new SimpleLoggerAdvisor())
                .user("Tell me a joke?")
                .call()
                .chatResponse();
    }

}
