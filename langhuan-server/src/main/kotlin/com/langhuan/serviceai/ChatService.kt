package com.langhuan.serviceai

import com.langhuan.common.BusinessException
import com.langhuan.common.Constant
import com.langhuan.functionTools.RestRequestTools
import com.langhuan.model.domain.TChatFeedback
import com.langhuan.model.pojo.ChatModelResult
import com.langhuan.model.pojo.ChatRestOption
import com.langhuan.service.TChatFeedbackService
import com.langhuan.service.TPromptsService
import com.langhuan.utils.chatMemory.ChatMemoryUtils
import com.langhuan.utils.other.FileUtil
import com.langhuan.utils.other.SecurityUtils
import com.langhuan.utils.rag.config.SplitConfig
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.support.ToolCallbacks
import org.springframework.ai.tool.ToolCallback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * AI聊天服务类
 * @author LangHuan
 * @version 1.0
 * @since 2024-01-01
 */
@Service
class ChatService(
    chatClientBuilder: ChatClient.Builder,
    private val ragService: RagService,
    private val ragCallBackService: RagCallBackService
) {
    companion object {
        private val log = LoggerFactory.getLogger(ChatService::class.java)
    }

    lateinit var chatClient: ChatClient
    lateinit var chatMemory: ChatMemory

    @Autowired
    lateinit var chatGeneralAssistanceService: ChatGeneralAssistanceService

    @Autowired
    lateinit var imgService: ImgService

    @Autowired
    lateinit var tChatFeedbackService: TChatFeedbackService

    init {
        this.chatClient = chatClientBuilder.build()
    }

    // 添加Advisor
    @Autowired
    fun setAdvisor(chatMemoryService: ChatMemoryService) {
        this.chatMemory = chatMemoryService.getChatMemory()
        // 系统级提示词
        var system: String? = TPromptsService.getCachedTPromptsByMethodName("AINULLDEFAULTSYSTEMPROMPT")
        if (system == null) {
            system = Constant.AINULLDEFAULTSYSTEMPROMPT
        }
        this.chatClient = this.chatClient.mutate()
            .defaultAdvisors(
                SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                SimpleLoggerAdvisor(),
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
            )
            .defaultSystem(system)
            .build()
    }

    /**
     * 聊天服务主方法
     *
     * <p>
     * 根据聊天配置选项，决定使用RAG模式还是普通聊天模式。
     * 支持工具函数调用和动态提示词配置。
     * </p>
     *
     * @param chatRestOption 聊天请求选项对象，包含聊天ID、问题内容、是否使用RAG等配置
     * @return ChatModelResult 聊天结果对象，包含AI回复内容和相关文档列表
     * @throws BusinessException 当AI服务异常时抛出，向用户显示友好的错误消息
     */
    @Throws(Exception::class)
    fun chat(chatRestOption: ChatRestOption, accessory: Array<MultipartFile>?): ChatModelResult {
        return try {
            var fileCategory: FileUtil.FileCategory;
            if (accessory == null || accessory.isEmpty()) {
                fileCategory = FileUtil.categorizeFiles(arrayOf())
            } else {
                fileCategory = FileUtil.categorizeFiles(accessory)
            }
            val imageRes = fileCategory.images
            val documentRes = fileCategory.documents

            // ------------------------------------------------------------------------------
            // 只要有文件就调用解读，不调用RAG
            if (imageRes?.isNotEmpty() == true || documentRes?.isNotEmpty() == true) {
                // 设置记忆信息 拼接处理结果
                val memoryIn = StringBuilder()
                val memoryOut = StringBuilder()

                // 处理图 - 模型解析理解后的
                imageRes?.let {
                    if (it.isNotEmpty()) {
                        val imageUnderstandingRes = imgService.chat_imageUnderstanding(chatRestOption, imageRes)
                        memoryIn.append(imageUnderstandingRes.chatInStr)
                        memoryOut.append(imageUnderstandingRes.chatOutStr)
                    }
                }
                // 处理文档 - 使用工具拆出的 没使用模型
                var documentResStr = StringBuilder()
                val documentFileNames = StringBuilder()
                if (documentRes != null) {
                    for (file in documentRes) {
                        documentFileNames.append(file.originalFilename).append("\n")
                        // 将文件内容读取到字节数组中，避免InputStream多次读取问题
                        val fileBytes = file.bytes
                        val tikaDocumentReader =
                            TikaDocumentReader(org.springframework.core.io.ByteArrayResource(fileBytes))
                        val documents = tikaDocumentReader.read()
                        for (s in documents) {
                            documentResStr.append(s.formattedContent).append("\n")
                        }
                    }
                }
                // 将信息解析后的信息给大语言模型理解，图信息已经被模型理解过了，这里单独解读下文档信息
                // XXX 考虑是不是把图片信息也一起给模型处理
                var documentChatResStr = ""
                if (documentResStr.isNotEmpty()) {
                    var userPrompt = "文档内容如下：\n$documentResStr\n-------------------".replace(
                        "source: Invalid source URI: Byte array resource [resource loaded from byte array] cannot be resolved to URL",
                        ""
                    ).trim()
                    chatRestOption.isRag = false;
                    chatRestOption.prompt = userPrompt;
                    documentChatResStr = toChat(chatRestOption).chat.toString();
//                    documentChatResStr =
//                        chatGeneralAssistanceService.documentUnderstand(userPrompt, chatRestOption.modelName) as String
                }
                if (documentChatResStr.isNotEmpty()) {
                    memoryIn.append("\n").append("**").append(documentFileNames).append("**")
                    memoryOut.append("\n").append(documentChatResStr)
                }
                //  整合记忆信息
                memoryIn.append("\n").append(chatRestOption.userMessage)
                chatMemory.add(
                    chatRestOption.chatId as String,
                    ChatMemoryUtils.createdMessage(memoryIn.toString(), emptyMap(), MessageType.USER)
                )
                chatMemory.add(
                    chatRestOption.chatId as String,
                    ChatMemoryUtils.createdMessage(memoryOut.toString(), emptyMap(), MessageType.ASSISTANT)
                )
                return ChatModelResult().apply {
                    chat = memoryOut.toString()
                    rag = emptyList()
                }
            }

            // ------------------------------------------------------------------------------
            // 意图识别
            val intention = chatGeneralAssistanceService.chatIntentionClassifier(
                chatRestOption.modelName,
                chatRestOption.userMessage as String
            )
            // ------------------------------------------------------------------------------
            // 处理对应意图 注意分支内要return
            if (intention == "chat") {
                return toChat(chatRestOption)
            }
            if (intention == "add_personal_knowledge_space") {
                val simulationThink = StringBuilder()
                simulationThink.append("<think>")
                simulationThink.append("意图识别结果：添加信息到知识库（add_personal_knowledge_space）").append("\n")
                simulationThink.append("提取知识").append("\n")
                simulationThink.append("识别结果：").append("\n")
                simulationThink.append("---------").append("\n")
                // 处理对话中文字
                val textDocument = listOf(
                    chatGeneralAssistanceService.documentSegmentation(
                        chatRestOption.modelName ?: "",
                        chatRestOption.userMessage ?: ""
                    )
                )
                // 处理图
                val imageDocument = imageRes?.let {
                    if (it.isNotEmpty()) {
                        imgService.chat_imageUnderstandingToText(imageRes, chatRestOption.userMessage ?: "")
                    } else {
                        ""
                    }
                }
                // 处理文档
                val docDocument = mutableListOf<String>()
                if (documentRes != null) {
                    for (file in documentRes) {
                        // HACK 当前默认使用 ====== 的拆分方式，后续考虑模型拆分等
                        val documentSplitConfig =
                            SplitConfig("PatternTokenTextSplitter", mapOf("splitPattern" to "(?:={6})\\s*"))
                        val documents = ragService.readAndSplitDocument(file, documentSplitConfig)
                        // TODO 检测单块大小 太大按固定数拆分
                        docDocument.addAll(documents)
                    }
                }
                // 集合全部的文档
                val allDocument = mutableListOf<String>()
                allDocument.addAll(textDocument.filterNotNull())
                imageDocument?.let { allDocument.add(it) }
                allDocument.addAll(docDocument)
                val filteredDocument = allDocument.filter { it.isNotEmpty() }
                // 打印文档
                for (string in filteredDocument) {
                    simulationThink.append(string)
                        .append("\n")
                        .append("---")
                        .append("\n")
                }
                simulationThink.append("---------").append("\n")
                if (filteredDocument.isNotEmpty()) {
                    simulationThink.append("添加到知识库").append("\n")
                    val isAddDocuments = ragService.addDocumentToMySpace(filteredDocument)
                    if (isAddDocuments) {
                        simulationThink.append("添加到知识库成功").append("\n")
                        simulationThink.append("</think>")
                        return ChatModelResult().apply {
                            chat = simulationThink.toString() + "已成功添加到个人知识空间"
                            rag = emptyList()
                        }
                    } else {
                        simulationThink.append("添加到知识库失败").append("\n")
                        simulationThink.append("</think>")
                        return ChatModelResult().apply {
                            chat = "添加到个人知识空间失败"
                            rag = emptyList()
                        }
                    }
                } else {
                    return ChatModelResult().apply {
                        chat = "未从文档中提取到知识信息，请重试"
                        rag = emptyList()
                    }
                }
            }
            return toChat(chatRestOption)
        } catch (e: Exception) {
            ChatModelResult().apply {
                chat = e.message ?: "未知错误"
                rag = emptyList()
            }
        }
    }

    fun toChat(chatRestOption: ChatRestOption): ChatModelResult {
        // 根据配置决定是否启用工具函数调用
        val tools = if (chatRestOption.isFunction == true) ToolCallbacks.from(RestRequestTools::class.java)
        else ToolCallbacks.from()
        return try {
            // 根据是否启用RAG选择不同的聊天模式
            if (chatRestOption.isRag == true) {
                this.isRagChat(
                    chatRestOption.chatId ?: "", chatRestOption.prompt ?: "",
                    chatRestOption.userMessage ?: "", chatRestOption.ragGroupId ?: "",
                    chatRestOption.modelName ?: "", chatRestOption.isReRank ?: false, tools
                )
            } else {
                this.noRagChat(
                    chatRestOption.chatId ?: "", chatRestOption.prompt ?: "",
                    chatRestOption.userMessage ?: "", chatRestOption.modelName ?: "", tools
                )
            }
        } catch (e: Exception) {
            log.error("advisor-error: {}", e.message)
            throw BusinessException("抱歉，我暂时无法回答这个问题。")
        }
    }

    @Throws(Exception::class)
    fun isRagChat(
        id: String, p: String, q: String, groupId: String, modelName: String, isReRank: Boolean,
        tools: Array<ToolCallback>
    ): ChatModelResult {
        // 获取RAG专用提示词模板，优先使用缓存配置
        var aiDefaultQuestionAnswerAdvisorPrompt: String? = TPromptsService
            .getCachedTPromptsByMethodName("AIDEFAULTQUESTIONANSWERADVISORRPROMPT")
        if (aiDefaultQuestionAnswerAdvisorPrompt == null) {
            aiDefaultQuestionAnswerAdvisorPrompt = Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT
        }

        // 手动获取召回信息
        val documentList = ragCallBackService.ragSearch(q, groupId, "", isReRank)

        // 将检索到的文档内容拼接成上下文字符串
        val ragContents = StringBuilder()
        for (document in documentList) {
            ragContents.append(document.text).append(";").append("\n")
        }

        // 替换模板变量，构建包含检索上下文的提示词
        val ragPrompt =
            aiDefaultQuestionAnswerAdvisorPrompt.replace("{question_answer_context}", ragContents.toString())

        // 构建附带用户其他提示词，发送AI请求
        val chatResponse = this.chatClient.prompt(
            Prompt(
                ragPrompt + "\n" + p,
                OpenAiChatOptions.builder()
                    .model(modelName)
                    .build()
            )
        )
            .user(q)
            .advisors { a -> a.param(ChatMemory.CONVERSATION_ID, id) }
            .toolCallbacks(*tools)
            .call()

        val chat = chatResponse.content()

        // 构建返回结果，包含AI回复和检索到的文档
        val chatModelResult = ChatModelResult()
        chatModelResult.chat = chat
        chatModelResult.rag = documentList
        return chatModelResult
    }

    fun noRagChat(id: String, p: String, q: String, modelName: String, tools: Array<ToolCallback>): ChatModelResult {

        // 构建并发送AI请求，不包含RAG上下文
        val chatResponse = this.chatClient.prompt(
            Prompt(
                p,
                OpenAiChatOptions.builder()
                    .model(modelName)
                    .build()
            )
        )
            .user(q)
            .advisors { a -> a.param(ChatMemory.CONVERSATION_ID, id) }
            .toolCallbacks(*tools)
            .call()

        val chat = chatResponse.content()

        // 构建返回结果，RAG文档列表为空
        val chatModelResult = ChatModelResult()
        chatModelResult.chat = chat
        chatModelResult.rag = ArrayList()
        return chatModelResult
    }
}
