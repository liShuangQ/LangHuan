package com.langhuan.controller

import cn.hutool.json.JSONUtil
import com.langhuan.common.ApiLog
import com.langhuan.common.Constant
import com.langhuan.common.Result
import com.langhuan.model.pojo.ChatModelResult
import com.langhuan.model.pojo.ChatRestOption
import com.langhuan.serviceai.*
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@RestController
class ChatController(
    private val chatService: ChatService,
    private val chatMemoryService: ChatMemoryService,
    private val chatGeneralAssistanceService: ChatGeneralAssistanceService,
    private val stanfordChatService: StanfordChatService,
    private val ragService: RagService
) {

    companion object {
        private val log = LoggerFactory.getLogger(ChatController::class.java)
    }

    @Value("\${spring.ai.openai.chat.options.model}")
    private lateinit var defaultModelName: String

    @Value("\${spring.ai.openai.base-url}")
    private lateinit var openApiUrl: String

    @Value("\${spring.ai.openai.api-key}")
    private lateinit var openApiKey: String

    // NOTE:Flux<String>会和Security的拦截器冲突，所以要设置白名单 "/chat/chatFlux"
    @ApiLog(apiName = "聊天", description = "聊天", logResponse = true, logRequest = true)
    @PostMapping("/chat/chat")
    @Throws(Exception::class)
    fun chat(
        @RequestParam(name = "option", required = true) option: String,
        @RequestParam(name = "accessory", required = false) accessory: Array<MultipartFile>?
    ): Result<*> {
        val chatRestOption = JSONUtil.toBean(option, ChatRestOption::class.java)

        chatRestOption.modelName?.let {
            if (it.isEmpty()) {
                chatRestOption.modelName = defaultModelName
            }
        }
        //  系统 system prompt userMessage
        val chatModelResult = chatService.chat(chatRestOption, accessory)

        val chat = chatModelResult.chat
        // HACK 关于工具的开发
//        if (chat.startsWith("***tools***")) {
//            log.info("***tools***,工具询问二次询问模型")
//            chat = chatGeneralAssistanceService.tools(chat)
//        }

        return Result.success(
            mapOf(
                "chat" to chat,
                "rag" to chatModelResult.rag,
                // "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
                "recommend" to emptyList<Any>()
            )
        )
    }

    @PostMapping("/chat/easyChat")
    fun easyChat(
        @RequestParam(name = "p", required = true, defaultValue = ".") p: String,
        @RequestParam(name = "q", required = true) q: String,
        @RequestParam(name = "modelName", required = true, defaultValue = "") modelName: String
    ): Result<*> {
        val actualModelName = if (modelName.isEmpty()) defaultModelName else modelName

        val chat = chatGeneralAssistanceService.easyChat(p, q, actualModelName)
        return Result.success(
            mapOf(
                "chat" to chat,
                // "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
                "recommend" to emptyList<Any>()
            )
        )
    }

    @PostMapping("/chat/getPrompt")
    fun getPrompt(@RequestParam(name = "q", required = true) q: String): Result<*> {
        return Result.success(chatGeneralAssistanceService.optimizePromptWords(q))
    }

    @PostMapping("/chat/setChatMemoryWindowsName")
    fun setChatMemoryWindowsName(
        @RequestParam(name = "id", required = true) id: String,
        @RequestParam(name = "name", required = true) name: String
    ): Result<*> {
        return Result.success(chatMemoryService.setChatMemoryWindowsName(id, name))
    }

    @PostMapping("/chat/getChatMemoryWindows")
    fun getChatMemoryWindows(): Result<Any> {
        return Result.success(chatMemoryService.getChatMemoryWindows())
    }

    @PostMapping("/chat/getChatMemoryMessages")
    fun getChatMemoryMessages(@RequestParam id: String): Result<*> {
        return Result.success(chatMemoryService.getChatMemoryMessages(id))
    }

    @PostMapping("/chat/saveChatMemory")
    fun saveChatMemory(
        @RequestParam id: String,
        @RequestParam name: String
    ): Result<*> {
        return Result.success(chatMemoryService.saveChatMemory(id, name))
    }

    @PostMapping("/chat/clearChatMemory")
    @Throws(Exception::class)
    fun clearChatMemory(@RequestParam id: String): Result<*> {
        return Result.success(chatMemoryService.clearChatMemory(id))
    }

    @PostMapping("/onlyRag/chat")
    @Throws(Exception::class)
    fun onlyRagChat(
        @RequestParam(name = "id", required = true) id: String,
        @RequestParam(name = "p", required = true, defaultValue = ".") p: String,
        @RequestParam(name = "q", required = true) q: String,
        @RequestParam(name = "isRag", required = true) isRag: Boolean,
        @RequestParam(name = "groupId", required = true, defaultValue = "") groupId: String,
        @RequestParam(name = "isFunction", required = true) isFunction: Boolean
    ): Result<*> {
        val documentList = ragService.ragSearch(q, groupId, "", Constant.ISRAGRERANK)
        val contents = StringBuilder()
        var i = 0
        for (document in documentList) {
            i += 1
            contents.append("<p>").append(i).append(":").append("&nbsp;").append(document.text).append("</p>")
        }
        return Result.success(
            mapOf(
                "chat" to contents.toString()
                // "recommend", chatGeneralAssistanceService.otherQuestionsRecommended(q)
            )
        )
    }

    @PostMapping("/chatModel/getModelList")
    @Throws(IOException::class)
    fun getModelList(): Result<*> {
        var response: HttpResponse<String>? = null
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$openApiUrl/v1/models"))
            .header("Authorization", openApiKey)
            .build()

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return Result.success(JSONUtil.parseObj(response.body()))

        } catch (e: Exception) {
            log.error("获取模型列表失败", e)
            return Result.success(mapOf("data" to listOf(mapOf("id" to defaultModelName))))
        }
    }

    @PostMapping("/chat/stanford")
    fun stanford(
        @RequestParam(name = "id", required = true) id: String,
        @RequestParam(name = "p", required = true, defaultValue = ".") p: String,
        @RequestParam(name = "q", required = true) q: String,
        @RequestParam(name = "modelName", required = true, defaultValue = "") modelName: String
    ): Result<*> {
        return Result.success(stanfordChatService.chat(id, p, q, modelName))
    }
}
