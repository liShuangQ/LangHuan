package com.langhuan.serviceai

import com.langhuan.common.BusinessException
import com.langhuan.common.Constant
import com.langhuan.service.TPromptsService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.ChatMemoryRepository
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class StanfordChatService {

    companion object {
        private val log = LoggerFactory.getLogger(StanfordChatService::class.java)
    }

    private lateinit var chatClient: ChatClient
    private lateinit var chatMemory: ChatMemory

    @Autowired
    lateinit var chatMemoryRepository: ChatMemoryRepository

    constructor(chatClientBuilder: ChatClient.Builder) {
        this.chatClient = chatClientBuilder
            .defaultAdvisors(
                SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                SimpleLoggerAdvisor()
                // 注意：MessageChatMemoryAdvisor将在initChatMemory中添加
            )
            .build()
    }

    @PostConstruct
    fun initChatMemory() {
        chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(20)
            .build()
            
        // 重新构建chatClient，添加MessageChatMemoryAdvisor
        chatClient = chatClient.mutate()
            .defaultAdvisors(
                SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build()
    }

    fun chat(id: String, p: String, q: String, modelName: String): String {
        return try {
            this.chatClient.prompt(
                Prompt(
                    p,
                    OpenAiChatOptions.builder()
                        .model(modelName)
                        .build()
                )
            )
                .user(q)
                .advisors { a -> a.param(ChatMemory.CONVERSATION_ID, id) }
                .system(TPromptsService.getCachedTPromptsByMethodName("StanfordChatService") as String)
                .call().chatResponse()?.result?.output?.text ?: ""
        } catch (e: Exception) {
            log.error("advisor-error: {}", e.message)
            throw BusinessException("抱歉，我暂时无法回答这个问题。")
        }
    }
}