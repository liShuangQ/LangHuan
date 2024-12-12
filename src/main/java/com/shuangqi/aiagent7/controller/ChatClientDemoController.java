package com.shuangqi.aiagent7.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class ChatClientDemoController {
    private final ChatClient chatClient;

    public ChatClientDemoController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * call（） 返回值
     * 指定方法 on 后，响应类型有几种不同的选项。call()ChatClient
     * String content()：返回响应的 String 内容
     * ChatResponse chatResponse()：返回包含多个代的对象以及有关响应的元数据，例如用于创建响应的令牌数。ChatResponse
     * entity()返回 Java 类型
     * entity(ParameterizedTypeReference<T> type)：用于返回 OF 实体类型。Collection
     * entity(Class<T> type)：用于返回特定实体类型。
     * entity(StructuredOutputConverter<T> structuredOutputConverter)：用于指定 a 的实例以将 a 转换为实体类型。StructuredOutputConverterString
     * 您还可以调用 method 而不是 .stream()call()
     * -------------------------
     * stream（） 返回值
     * 指定方法 on 后，响应类型有几个选项：stream()ChatClient
     * Flux<String> content()：返回 AI 模型生成的字符串的 a。Flux
     * Flux<ChatResponse> chatResponse()：返回对象的 a，其中包含有关响应的其他元数据。FluxChatResponse
     */

    // ------------------------
    //返回内容
    //使用自动配置的 ChatClient.Builder
    //在最简单的用例中， Spring AI 提供 Spring Boot 自动配置，创建一个原型 bean 供你注入到你的类中。
    //下面是检索对简单用户请求的响应的简单示例。ChatClient.BuilderString
    @GetMapping("/ai1")
    String ai1(String q) {
        return this.chatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    //返回 ChatResponse
    //来自 AI 模型的响应是由类型定义的丰富结构。 它包括有关响应生成方式的元数据，还可以包含多个响应，称为 Generations，每个响应都有自己的元数据。
    //元数据包括用于创建响应的标记数（每个标记大约是一个单词的 3/4）。
    //此信息非常重要，因为托管 AI 模型根据每个请求使用的令牌数量收费。ChatResponse
    @GetMapping("/ai2")
    ChatResponse ai2() {
        return chatClient.prompt(new Prompt("回答我的问题"))
                .user("给我讲个笑话")
                .call()
                .chatResponse();
    }

    //返回实体
    //您通常需要返回从返回的 . 该方法提供此功能。Stringentity()
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

    //流式响应
    //该方法允许您获得异步响应，如下所示：stream()
    @GetMapping("/ai5")
    Flux<String> ai5(String q) {
        return chatClient.prompt()
                .user(q)
                .stream()
                .content();
    }

    // 您还可以使用方法 .ChatResponseFlux<ChatResponse> chatResponse()
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

    // 默认prompt
    @GetMapping("/ai7")
    public Map<String, String> completion(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        return Map.of("completion", this.chatClient.prompt().user(message).call().content());
    }
}
