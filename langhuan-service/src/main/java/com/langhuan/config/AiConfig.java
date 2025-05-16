package com.langhuan.config;

import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean
    public BatchingStrategy customTokenCountBatchingStrategy() {
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE,  // 指定用于分词的编码类型。使用此编码类型来准确估计令牌计数
                2048,                      // 设置最大输入令牌计数。此值应小于或等于嵌入模型的最大上下文窗口大小。
                0.1                        // 设置预留百分比。从最大输入令牌计数中要预留的令牌的百分比。这会为处理过程中可能增加的令牌计数创建缓冲区。
        );
    }
    /**
     *    带参数的默认系统文本
     *    Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, String voice) {
     * 		return Map.of("completion",
     * 				this.chatClient.prompt()
     * 						.system(sp -> sp.param("voice", voice))
     * 						.user(message)
     * 						.call()
     * 						.content());
     *    }
     */
//    @Bean
//    ChatClient chatClient(ChatClient.Builder builder) {
//        return builder.defaultSystem("You are a friendly chat bot that answers question in the voice of a {voice}")
//                .build();
//    }

    /**
     其他默认值
     在该级别，您可以指定默认提示配置。ChatClient.Builder

     defaultOptions(ChatOptions chatOptions)：传入类中定义的可移植选项或特定于模型的选项，例如 .有关特定于模型的实现的更多信息，请参阅 JavaDocs。ChatOptionsOpenAiChatOptionsChatOptions

     defaultFunction(String name, String description, java.util.function.Function<I, O> function)：用于在用户文本中引用函数。该函数解释了函数的用途，并帮助 AI 模型选择正确的函数以实现准确响应。参数是模型将在必要时执行的 Java 函数实例。namedescriptionfunction

     defaultFunctions(String…​ functionNames)：在应用程序上下文中定义的 'java.util.Function' 的 bean 名称。

     defaultUser(String text)、 、 ：这些方法允许您定义用户文本。允许您使用 lambda 指定用户文本和任何默认参数。defaultUser(Resource text)defaultUser(Consumer<UserSpec> userSpecConsumer)Consumer<UserSpec>

     defaultAdvisors(Advisor…​ advisor)：顾问程序允许修改用于创建 .该实现通过在 prompt 后附加与用户文本相关的上下文信息来启用模式。PromptQuestionAnswerAdvisorRetrieval Augmented Generation

     defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer)：此方法允许您定义 a 以使用 配置多个 advisor。顾问可以修改用于创建最终 .允许您指定一个 lambda 来添加顾问，例如 ，它支持根据用户文本在提示后附加相关上下文信息。ConsumerAdvisorSpecPromptConsumer<AdvisorSpec>QuestionAnswerAdvisorRetrieval Augmented Generation

     您可以在运行时使用不带前缀的相应方法覆盖这些默认值。default

     options(ChatOptions chatOptions)

     function(String name, String description, java.util.function.Function<I, O> function)

     functionTools(String…​ functionNames)

     user(String text), ,user(Resource text)user(Consumer<UserSpec> userSpecConsumer)

     advisors(Advisor…​ advisor)

     advisors(Consumer<AdvisorSpec> advisorSpecConsumer)


     */


}