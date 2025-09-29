package com.langhuan.serviceai;

import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.service.TPromptsService;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.chatMemory.ChatMemoryUtils;
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
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private ImgService imgService;


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
            // 意图识别
            String intention = chatGeneralAssistanceService.chatIntentionClassifier(chatRestOption.getModelName(), chatRestOption.getUserMessage());
            // HACK 注意当前下面的能力都没判断附件是不是全是图片类型，但是前端有校验。以后加其他类型这里要注意
            // TODO 未来考虑是不是添加文件类型分类器，当前附件都是图片直接调用图片模型没问题；但是如果有其他文件类型，考虑怎么意图识别和执行对应方法和拼接回答
            // TODO 只需要区分是不是要添加知识即可，自动根据附件文件类型区分和拼接知识
            // TODO 考虑意图能力是不是要原子化，例如先做什么后做什么
            switch (intention) {
                case "chat":
                    return toChat(chatRestOption);
                case "understand_image":
                    ImgService.ChatImageUnderstandingRes chatImageUnderstandingRes = imgService.chat_imageUnderstanding(chatRestOption, accessory);
                    // 手动设置记忆
                    chatMemory.add(chatRestOption.getChatId(),
                            ChatMemoryUtils.createdMessage(chatImageUnderstandingRes.getChatInStr(), Map.of(), MessageType.USER));

                    chatMemory.add(chatRestOption.getChatId(),
                            ChatMemoryUtils.createdMessage(chatImageUnderstandingRes.getChatOutStr(), Map.of(), MessageType.ASSISTANT));
                    return new ChatModelResult() {{
                        setChat(chatImageUnderstandingRes.getChatOutStr());
                        setRag(List.of());
                    }};
                case "add_personal_knowledge_space":
                    StringBuilder simulationThink1 = new StringBuilder();
                    simulationThink1.append("<think>");
                    simulationThink1.append("意图识别结果：添加文字信息到知识库").append("\n");
                    simulationThink1.append("文字提取知识").append("\n");
                    simulationThink1.append("文字识别结果：").append("\n");
                    simulationThink1.append("---------").append("\n");
                    List<String> documentSegmentation1 = chatGeneralAssistanceService.documentSegmentation(chatRestOption.getModelName(), chatRestOption.getUserMessage());
                    for (String string : documentSegmentation1) {
                        simulationThink1.append(string)
                                .append("\n")
                                .append("---")
                                .append("\n");
                    }
                    simulationThink1.append("---------").append("\n");
                    if (!documentSegmentation1.isEmpty()) {
                        simulationThink1.append("添加到知识库").append("\n");
                        Boolean isAddDocuments = ragService.addDocumentToMySpace(documentSegmentation1);
                        if (isAddDocuments) {
                            simulationThink1.append("添加到知识库成功").append("\n");
                            simulationThink1.append("</think>");
                            return new ChatModelResult() {{
                                setChat(simulationThink1 + "已成功添加到个人知识空间");
                                setRag(List.of());
                            }};
                        } else {
                            simulationThink1.append("添加到知识库失败").append("\n");
                            simulationThink1.append("</think>");
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
                case "add_image_content_to_knowledge_base":
                    // XXX 当前一个图片的信息就是一个知识，不使用模型拆分了
//                    List<String> documentSegmentation2 = chatGeneralAssistanceService.documentSegmentation(chatRestOption.getModelName(), imgInfo);
                    StringBuilder simulationThink2 = new StringBuilder();
                    simulationThink2.append("<think>");
                    simulationThink2.append("意图识别结果：添加图片知识到知识库").append("\n");
                    simulationThink2.append("图片提取知识").append("\n");
                    simulationThink2.append("图片识别结果：").append("\n");
                    simulationThink2.append("---------").append("\n");
                    List<String> imageUnderstandingToText = imgService.chat_imageUnderstandingToText(accessory, "");
                    for (String string : imageUnderstandingToText) {
                        simulationThink2.append(string)
                                .append("\n")
                                .append("---")
                                .append("\n");
                    }
                    simulationThink2.append("---------").append("\n");
                    if (!imageUnderstandingToText.isEmpty()) {
                        simulationThink2.append("添加到知识库").append("\n");

                        Boolean isAddDocuments = ragService.addDocumentToMySpace(imageUnderstandingToText);
                        if (isAddDocuments) {
                            simulationThink2.append("添加到知识库成功").append("\n");
                            simulationThink2.append("</think>");
                            return new ChatModelResult() {{
                                setChat(simulationThink2 + "已成功添加到个人知识空间");
                                setRag(List.of());
                            }};
                        } else {
                            simulationThink2.append("添加到知识库失败").append("\n");
                            simulationThink2.append("</think>");
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
                default:
                    return toChat(chatRestOption);
            }
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
