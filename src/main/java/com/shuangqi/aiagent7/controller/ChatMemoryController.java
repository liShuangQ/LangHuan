package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.model.pojo.MyChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@RestController
@RequestMapping("/chatMemory")
public class ChatMemoryController {
    private final ChatClient chatClient;

    public ChatMemoryController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem("""
                        你叫小明。
                        请你对我提供的信息进行专业且深入的分析，无论是文本内容、数据还是概念等方面。
                        用清晰、准确、有条理的语言进行回应，给出全面的解释、合理的建议或精准的判断。
                        帮助我更好地理解相关事物并做出明智的决策或获得更深入的认知。
                        """)
                .defaultAdvisors(
                        // MessageChatMemoryAdvisor：查询对象会话ID的历史消息添加到提示词文本中
                        // PromptChatMemoryAdvisor：检索到的内存中的历史消息将添加到提示的系统文本中；
                        // VectorStoreChatMemoryAdvisor：检索向量数据库中的历史消息将添加到提示的系统文本中。
                        // SimpleLoggerAdvisor: 日志
                        new MessageChatMemoryAdvisor(new MyChatMemory()),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }


    @GetMapping("/chat")
    String chat(String id, String q) {
        // .chatResponse():
        //这个方法可能返回一个代表整个聊天响应的对象。这个对象包含了与用户交互后的所有相关信息，包括但不限于对话的结果、状态、错误信息等。
        //.getResult():
        //调用 .chatResponse() 后，你可能会得到一个包含多个属性的对象。其中，getResult() 方法可能是用来获取这次交互的具体结果。这通常是一个更具体的对象，它承载了对话系统处理完请求后生成的内容，比如意图识别结果、实体提取结果等。
        //.getOutput():
        //继续链式调用 getOutput() 方法，目的是为了从结果对象中提取出最终要展示给用户的输出内容。这部分通常是经过处理和格式化后的文本消息或者其他形式的数据，可以直接用于回应用户
        return this.chatClient.prompt()
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                ).call().chatResponse().getResult().getOutput().getContent();
    }

    @GetMapping("/chatStream")
    Flux<String> chatStream(String id, String q) {
        log.info("advisor-chatStream: {}", "用户id-" + id + ":" + q);
        return this.chatClient.prompt()
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                ).stream().content();
    }

    @GetMapping("/clear")
    String clear(String id) {
        log.info("advisor-clear: {}", "用户id-" + id);
        new MyChatMemory().clear(id);
        return "清除成功";
    }

}
