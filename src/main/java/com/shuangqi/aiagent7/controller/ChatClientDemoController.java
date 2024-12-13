package com.shuangqi.aiagent7.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatDemo")
public class ChatClientDemoController {
    private final ChatClient chatClient;

    /**
     * 构造方法：创建一个默认客户端
     *
     * @param chatClientBuilder ChatClient 构建器
     */
//    public ChatClientDemoController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }

    /**
     * 构造方法：创建一个新的客户端
     * - UserMessage: 用户消息，指用户输入的消息，如提问的问题。
     * - SystemMessage: 系统限制性消息，权重较大，AI 会优先依据 SystemMessage 回复。
     * - AssistantMessage: 大模型回复的消息。
     * - FunctionMessage: 函数调用消息，开发中一般用不到。
     *
     * @param chatClientBuilder ChatClient 构建器
     */
    public ChatClientDemoController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem("""
                        你叫小明，你是一名非常友好的朋友，你博学多识，你会回答我的各种问题。
                        """)
                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(chatMemory), // CHAT MEMORY
//                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()), // RAG
//                        new SimpleLoggerAdvisor()
                )
//                .defaultFunctions("getBookingDetails", "changeBooking", "cancelBooking") // FUNCTION CALLING
                .build();
    }

    /**
     * call() 返回值说明：
     * - content(): 返回响应的 String 内容
     * - chatResponse(): 返回包含多个代的对象及响应元数据，如用于创建响应的令牌数
     * - entity(): 返回 Java 类型
     * - entity(ParameterizedTypeReference<T> type): 返回 Collection 实体类型
     * - entity(Class<T> type): 返回特定实体类型
     * - entity(StructuredOutputConverter<T> structuredOutputConverter): 将字符串转换为实体类型
     * - stream() 方法：返回异步响应
     */

    // 演示方法
    // 返回响应的 String 内容
    @GetMapping("/ai1")
    String ai1(String q) {
        return this.chatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    // 返回 ChatResponse
    // 来自 AI 模型的响应是丰富的结构，包含生成方式的元数据和多个响应（Generations）。
    // 元数据包括用于创建响应的令牌数，这对于计费非常重要。
    @GetMapping("/ai2")
    ChatResponse ai2() {
        return chatClient.prompt(new Prompt("回答我的问题"))
                .user("给我讲个笑话")
                .call()
                .chatResponse();
    }

    // 返回实体
    // 通常需要返回从 AI 模型返回的字符串转换成的 Java 实体。
    record ActorFilms(String actor, List<String> movies) {
    }

    @GetMapping("/ai3")
    ActorFilms ai3() {
        return chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);
    }

    @GetMapping("/ai4")
    List<ActorFilms> ai4() {
        return chatClient.prompt()
                .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
    }

    // 流式响应
    // 该方法允许您获得异步响应。
    @GetMapping("/ai5")
    Flux<String> ai5(String q) {
        return chatClient.prompt()
                .user(q)
                .stream()
                .content();
    }

    // Map
    @GetMapping("/ai6")
    public Map<String, String> ai6(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        return Map.of("completion", this.chatClient.prompt().user(message).call().content());
    }


//您还可以使用方法 .ChatResponseFlux<ChatResponse> chatResponse()
//将来，我们将提供一种便捷的方法，让您使用 reactive 方法返回 Java 实体。 同时，您应该使用 Structured Output Converter 转换聚合响应显式，如下所示。 这也演示了 Fluent API 中参数的使用，这将在文档的后面部分更详细地讨论。stream()
//    @GetMapping("/ai6")
//    List<ActorFilms> ai6() {
//        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorsFilms>>() {});
//
//        Flux<String> flux = this.chatClient.prompt()
//                .user(u -> u.text("""
//                        Generate the filmography for a random actor.
//                        {format}
//                      """)
//                        .param("format", this.converter.getFormat()))
//                .stream()
//                .content();
//
//        String content = this.flux.collectList().block().stream().collect(Collectors.joining());
//
//        List<ActorFilms> actorFilms = this.converter.convert(this.content);
//    }
}
