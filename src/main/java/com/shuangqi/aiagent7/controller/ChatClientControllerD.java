package com.shuangqi.aiagent7.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatClientControllerD {
    private final ChatClient chatClient;

    public ChatClientControllerD(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem("""
                        你叫小明。
                        请你对我提供的信息进行专业且深入的分析，无论是文本内容、数据还是概念等方面。
                        用清晰、准确、有条理的语言进行回应，给出全面的解释、合理的建议或精准的判断。
                        帮助我更好地理解相关事物并做出明智的决策或获得更深入的认知。
                        """)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
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

    /**
     * 基础单次对话
     *
     * @param q
     * @return
     */
    @GetMapping("/string")
    String chat(String q) {
        return this.chatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    /**
     * 基础单次对话-流式响应
     *
     * @param q
     * @return
     */
    @GetMapping("/stream")
    Flux<String> stream(String q) {
        return chatClient.prompt()
                .user(q)
                .stream()
                .content();
    }

    /**
     * 基础单次对话带提示词，返回 ChatResponse
     *
     * @param p
     * @param q
     * @return 返回 ChatResponse 来自 AI 模型的响应是丰富的结构，包含生成方式的元数据和多个响应（Generations）。元数据包括用于创建响应的令牌数，这对于计费非常重要。
     */
    @GetMapping("/chatWithPrompt/ChatResponse")
    ChatResponse chatWithPrompt(String p, String q) {
        return chatClient.prompt(new Prompt(p))
                .user(q)
                .call()
                .chatResponse();
    }


    // 返回实体
    // 通常需要返回从 AI 模型返回的字符串转换成的 Java 实体。
    record ActorFilms(String actor, List<String> movies) {
    }

    /**
     * 基础单次对话带提示词，返回实体
     *
     * @param p
     * @param q
     * @return
     */
    @GetMapping("/chatWithPromptArray/ActorFilms")
    ActorFilms chatWithPromptArray(String p, String q) {
        List<Message> promptList = new ArrayList<>();
        for (String string : p.split(",")) {
            promptList.add(new UserMessage(string));
        }
        return chatClient.prompt(new Prompt(promptList))
                .user(q)
                .call()
                .entity(ActorFilms.class);
    }

    /**
     * 基础单次对话带提示词，返回实体List
     *
     * @param p
     * @param q
     * @return
     */
    @GetMapping("/chatWithPrompt/ListActorFilms")
    List<ActorFilms> chatWithPromptListActorFilms(String p, String q) {
        q = q.isEmpty() ? "Generate the filmography of 5 movies for Tom Hanks and Bill Murray." : q;
        return chatClient.prompt(p)
                .user(q)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
    }


    /**
     * 基础单次对话带提示词，返回Map
     *
     * @param p
     * @param q
     * @return
     */
    @GetMapping("/chatWithPrompt/Map")
    public Map<String, String> chatWithPromptMap(String p, String q) {
        return Map.of("completion", this.chatClient.prompt(p).user(q).call().content());
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
