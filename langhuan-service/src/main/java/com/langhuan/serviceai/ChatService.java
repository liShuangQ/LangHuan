package com.langhuan.serviceai;

import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.service.TChatFeedbackService;
import com.langhuan.service.TPromptsService;
import com.langhuan.utils.chatMemory.ChatMemoryUtils;
import com.langhuan.utils.other.FileUtil;
import com.langhuan.utils.other.SecurityUtils;
import com.langhuan.utils.rag.config.SplitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI聊天服务类
 *
 * <p>
 * 提供完整的AI聊天功能，包括：
 * </p>
 * <ul>
 * <li>基础聊天对话</li>
 * <li>基于RAG(检索增强生成)的智能问答</li>
 * <li>工具函数调用</li>
 * <li>意图识别和文档添加</li>
 * </ul>
 *
 * <p>
 * 该类整合了Spring AI框架，使用OpenAI作为底层大模型。
 * </p>
 *
 * @author LangHuan
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ChatService {
    ChatClient chatClient;
    ChatMemory chatMemory;

    @Autowired
    RagService ragService;
    @Autowired
    ChatGeneralAssistanceService chatGeneralAssistanceService;
    @Autowired
    ImgService imgService;
    @Autowired
    TChatFeedbackService tChatFeedbackService;


    public ChatService(ChatClient.Builder chatClientBuilder, RagService ragService) {
        this.ragService = ragService;
        this.chatClient = chatClientBuilder.build();
    }

    // 添加Advisor
    @Autowired
    public void setAdvisor(ChatMemoryService chatMemoryService) {
        this.chatMemory = chatMemoryService.getChatMemory();
        this.chatClient = this.chatClient.mutate()
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
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
    public ChatModelResult chat(ChatRestOption chatRestOption, MultipartFile[] accessory) throws Exception {
        try {
            // 区分文件类型 交给不同的方法处理
            FileUtil.FileCategory fileCategory = FileUtil.categorizeFiles(accessory);
            MultipartFile[] imageRes = fileCategory.getImages();
            MultipartFile[] documentRes = fileCategory.getDocuments();
            // 意图识别
            String intention = chatGeneralAssistanceService.chatIntentionClassifier(chatRestOption.getModelName(), chatRestOption.getUserMessage());
            // 处理对应意图 注意分支内要return
            if (Objects.equals(intention, "chat")) {
                return toChat(chatRestOption);
            }
            if (Objects.equals(intention, "understand")) {
                if (accessory.length == 0) {
                    return toChat(chatRestOption);
                }
                // 设置记忆信息 拼接处理结果
                StringBuilder memoryIn = new StringBuilder();
                StringBuilder memoryOut = new StringBuilder();

                // 处理图 - 模型解析理解后的
                ImgService.ChatImageUnderstandingRes imageUnderstandingRes = imgService.chat_imageUnderstanding(chatRestOption, imageRes);
                memoryIn.append(imageUnderstandingRes.getChatInStr());
                memoryOut.append(imageUnderstandingRes.getChatOutStr());
                // 处理文档 - 使用工具拆出的 没使用模型
                StringBuilder documentResStr = new StringBuilder();
                StringBuilder documentFileNames = new StringBuilder();
                for (MultipartFile file : documentRes) {
                    documentFileNames.append(file.getOriginalFilename()).append("\n");
                    // 将文件内容读取到字节数组中，避免InputStream多次读取问题
                    byte[] fileBytes = file.getBytes();
                    TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new org.springframework.core.io.ByteArrayResource(fileBytes));
                    List<Document> documents = tikaDocumentReader.read();
                    for (Document s : documents) {
                        documentResStr.append(s.getFormattedContent()).append("\n");
                    }
                }
                // 将信息解析后的信息给大语言模型理解，图信息已经被模型理解过了，这里单独解读下文档信息
                // XXX 考虑是不是把图片信息也一起给模型处理
                String documentChatResStr = "";
                if (!documentResStr.isEmpty()) {
                    String userPrompt = chatRestOption.getUserMessage() + "\n" + "文档如下：" + "\n" + documentResStr;
                    documentChatResStr = chatGeneralAssistanceService.documentUnderstand(userPrompt, chatRestOption.getModelName());
                }
                if (!documentChatResStr.isEmpty()) {
                    memoryIn.append("\n").append("**").append(documentFileNames).append("**");
                    memoryOut.append("\n").append(documentChatResStr);
                }
                //  整合记忆信息
                memoryIn.append("\n").append(chatRestOption.getUserMessage());
                chatMemory.add(chatRestOption.getChatId(),
                        ChatMemoryUtils.createdMessage(memoryIn.toString(), Map.of(), MessageType.USER));
                chatMemory.add(chatRestOption.getChatId(),
                        ChatMemoryUtils.createdMessage(memoryOut.toString(), Map.of(), MessageType.ASSISTANT));
                return new ChatModelResult() {{
                    setChat(memoryOut.toString());
                    setRag(List.of());
                }};

            }
            if (Objects.equals(intention, "add_personal_knowledge_space")) {
                StringBuilder simulationThink = new StringBuilder();
                simulationThink.append("<think>");
                simulationThink.append("意图识别结果：添加信息到知识库（add_personal_knowledge_space）").append("\n");
                simulationThink.append("提取知识").append("\n");
                simulationThink.append("识别结果：").append("\n");
                simulationThink.append("---------").append("\n");
                // 处理对话中文字
                List<String> textDocument = chatGeneralAssistanceService.documentSegmentation(chatRestOption.getModelName(), chatRestOption.getUserMessage());
                // 处理图
                List<String> imageDocument = imgService.chat_imageUnderstandingToText(imageRes, chatRestOption.getUserMessage());
                // 处理文档
                List<String> docDocument = new ArrayList<>();
                for (MultipartFile file : documentRes) {
                    // HACK 当前默认使用 ====== 的拆分方式，后续考虑模型拆分等
                    SplitConfig documentSplitConfig = new SplitConfig("PatternTokenTextSplitter", Map.of("splitPattern", "(?:={6})\\s*"));
                    List<String> documents = ragService.readAndSplitDocument(file, documentSplitConfig);
                    // TODO 检测单块大小 太大按固定数拆分
                    docDocument.addAll(documents);
                }
                // 集合全部的文档
                List<String> allDocument = new ArrayList<>();
                allDocument.addAll(textDocument);
                allDocument.addAll(imageDocument);
                allDocument.addAll(docDocument);
                // 打印文档
                for (String string : allDocument) {
                    simulationThink.append(string)
                            .append("\n")
                            .append("---")
                            .append("\n");
                }
                simulationThink.append("---------").append("\n");
                if (!allDocument.isEmpty()) {
                    simulationThink.append("添加到知识库").append("\n");
                    Boolean isAddDocuments = ragService.addDocumentToMySpace(allDocument);
                    if (isAddDocuments) {
                        simulationThink.append("添加到知识库成功").append("\n");
                        simulationThink.append("</think>");
                        return new ChatModelResult() {{
                            setChat(simulationThink + "已成功添加到个人知识空间");
                            setRag(List.of());
                        }};
                    } else {
                        simulationThink.append("添加到知识库失败").append("\n");
                        simulationThink.append("</think>");
                        return new ChatModelResult() {{
                            setChat("添加到个人知识空间失败");
                            setRag(List.of());
                        }};
                    }
                } else {
                    return new ChatModelResult() {{
                        setChat("未从文档中提取到知识信息，请重试");
                        setRag(List.of());
                    }};
                }
            }
            if (intention.contains("feedback_information")) {
                TChatFeedback tChatFeedback = new TChatFeedback();
                tChatFeedback.setUserId(SecurityUtils.getCurrentUsername());
                tChatFeedback.setSuggestion(chatRestOption.getUserMessage());
                tChatFeedback.setQuestionId(chatRestOption.getChatId());
                tChatFeedback.setQuestionContent("对话中反馈" + "对话id:" + chatRestOption.getChatId());
                tChatFeedback.setAnswerContent("对话中反馈" + "对话id:" + chatRestOption.getChatId());
                tChatFeedback.setKnowledgeBaseIds("");
                if (Objects.equals(intention, "feedback_information_yes")) {
                    tChatFeedback.setInteraction("like");
                }
                if (Objects.equals(intention, "feedback_information_no")) {
                    tChatFeedback.setInteraction("dislike");
                }
                boolean save = tChatFeedbackService.save(tChatFeedback);
                if (!save) {
                    return new ChatModelResult() {{
                        setChat("反馈信息保存失败");
                        setRag(List.of());
                    }};
                }
                return new ChatModelResult() {{
                    setChat("感谢支持，您的反馈已被提交。请继续使用");
                    setRag(List.of());
                }};
            }
            return toChat(chatRestOption);
        } catch (Exception e) {
            return new ChatModelResult() {
                {
                    {
                        setChat(e.getMessage());
                        setRag(List.of());
                    }
                }
            };
        }
    }

    public ChatModelResult toChat(ChatRestOption chatRestOption) {
        // 根据配置决定是否启用工具函数调用
        ToolCallback[] tools = chatRestOption.getIsFunction() ? ToolCallbacks.from(new RestRequestTools())
                : ToolCallbacks.from();

        // 获取用户提示词模板，优先使用缓存中的配置，否则使用默认值
        String AINULLDEFAULTUSERPROMPT = null;
        AINULLDEFAULTUSERPROMPT = TPromptsService
                .getCachedTPromptsByMethodName("AINULLDEFAULTUSERPROMPT");
        if (AINULLDEFAULTUSERPROMPT == null) {
            AINULLDEFAULTUSERPROMPT = Constant.AINULLDEFAULTUSERPROMPT;
        }

        // 替换模板变量，生成最终的用户提示词
        String userPrompt = AINULLDEFAULTUSERPROMPT.replace("{user_prompt}",
                chatRestOption.getUserMessage());

        try {
            // 根据是否启用RAG选择不同的聊天模式
            if (chatRestOption.getIsRag()) {
                return this.isRagChat(chatRestOption.getChatId(), chatRestOption.getPrompt(),
                        userPrompt, chatRestOption.getRagGroupId(),
                        chatRestOption.getModelName(), chatRestOption.getIsReRank(), tools);
            } else {
                return this.noRagChat(chatRestOption.getChatId(), chatRestOption.getPrompt(),
                        userPrompt, chatRestOption.getModelName(), tools);
            }
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

    public ChatModelResult isRagChat(String id, String p, String q, String groupId, String modelName, Boolean isReRank,
                                     ToolCallback[] tools) throws Exception {
        // 获取RAG专用提示词模板，优先使用缓存配置
        String AIDEFAULTQUESTIONANSWERADVISORRPROMPT = TPromptsService
                .getCachedTPromptsByMethodName("AIDEFAULTQUESTIONANSWERADVISORRPROMPT");
        if (AIDEFAULTQUESTIONANSWERADVISORRPROMPT == null) {
            AIDEFAULTQUESTIONANSWERADVISORRPROMPT = Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT;
        }

        // 使用排序后的结果手动喂给ai
        List<Document> documentList = ragService.ragSearch(q, groupId, "", isReRank);

        // 将检索到的文档内容拼接成上下文字符串
        StringBuilder ragContents = new StringBuilder();
        for (Document document : documentList) {
            ragContents.append(document.getText()).append(";").append("\n");
        }

        // 替换模板变量，构建包含检索上下文的提示词
        String ragPrompt = AIDEFAULTQUESTIONANSWERADVISORRPROMPT
                .replace("{question_answer_context}", ragContents.toString());

        String system = TPromptsService.getCachedTPromptsByMethodName("ChatService");
        if (system == null) {
            system = Constant.AINULLDEFAULTSYSTEMPROMPT;
        }
        // 构建并发送AI请求
        String chat = this.chatClient.prompt(
                        new Prompt(
                                ragPrompt + "\n" + p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .system(system)
                .advisors(a -> a.param(chatMemory.CONVERSATION_ID, id))
                .toolCallbacks(tools)
                .call().content();

        // 构建返回结果，包含AI回复和检索到的文档
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat(chat);
        chatModelResult.setRag(documentList);
        return chatModelResult;
    }

    public ChatModelResult noRagChat(String id, String p, String q, String modelName, ToolCallback[] tools) {

        String system = TPromptsService.getCachedTPromptsByMethodName("ChatService");
        if (system == null) {
            system = Constant.AINULLDEFAULTSYSTEMPROMPT;
        }
        // 构建并发送AI请求，不包含RAG上下文
        String chat = this.chatClient.prompt(
                        new Prompt(
                                p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .system(system)
                .advisors(a -> a.param(chatMemory.CONVERSATION_ID, id))
                .toolCallbacks(tools)
                .call().content();

        // 构建返回结果，RAG文档列表为空
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat(chat);
        chatModelResult.setRag(new ArrayList<>());
        return chatModelResult;
    }

}
