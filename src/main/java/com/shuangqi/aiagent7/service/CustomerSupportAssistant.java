package com.shuangqi.aiagent7.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class CustomerSupportAssistant {

    private final ChatClient chatClient;

    /**
     * 创建一个新的客户端
     * UserMessage：用户消息，指用户输入的消息，比如提问的问题。
     * SystemMessage：系统限制性消息，这种消息比较特殊，权重很大，AI会优先依据SystemMessage里的内容进行回复。在设定Chat角色时，可以用的到，下篇文章分析
     * AssistantMessage：大模型回复的消息。
     * FunctionMessage：函数调用消息，开发中一般使用不到，一般无需关心。
     *
     * @param builder
     */
//    VectorStore vectorStore,
//    ChatMemory chatMemory
    public CustomerSupportAssistant(ChatClient.Builder builder ) {

        this.chatClient = builder
                .defaultSystem("""
                    You are a customer chat support agent of an airline named "Funnair". Respond in a friendly,
                    helpful, and joyful manner.

                    Before providing information about a booking or cancelling a booking, you MUST always
                    get the following information from the user: booking number, customer first name and last name.

                    Before changing a booking you MUST ensure it is permitted by the terms.

                    If there is a charge for the change, you MUST ask the user to consent before proceeding.
                    """)
                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(chatMemory), // CHAT MEMORY
//                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()), // RAG
                        new SimpleLoggerAdvisor())
                .defaultFunctions("getBookingDetails", "changeBooking", "cancelBooking") // FUNCTION CALLING
                .build();
    }

    public Flux<String> chat(String chatId, String userMessageContent) {

        return this.chatClient.prompt()
                .user(userMessageContent)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream().content();
    }

}