package com.langhuan.serviceai;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;

import com.langhuan.model.dto.RagIntentionClassifierDTO;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.service.*;
import com.langhuan.utils.other.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    private ChatMemoryService chatMemoryService;
    @Autowired
    RagService ragService;
    @Autowired
    ChatGeneralAssistanceService chatGeneralAssistanceService;
    @Autowired
    TRagFileService fileService;
    @Autowired
    TRagFileGroupService fileGroupService;
    @Autowired
    TUserService userService;

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
                        // MessageChatMemoryAdvisor
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
    public ChatModelResult chat(ChatRestOption chatRestOption) throws Exception {
        // 这里由于记忆在存入的时候 会自己在前面拼接用户名 然后窗口表存储的是用户名_会话ID，避免出现用户名重复的情况 采用下面操作
        chatRestOption.setChatId(chatRestOption.getChatId().indexOf("_") > 0
                ? chatRestOption.getChatId().substring(chatRestOption.getChatId().indexOf("_") + 1)
                : chatRestOption.getChatId());
        RagIntentionClassifierDTO ragIntentionClassifierDTO = ragIntentionClassifier(chatRestOption.getModelName(),
                chatRestOption.getQuestion());
        try {
            if (ragIntentionClassifierDTO.getIsAdd()) {
                return toAddDocuments(ragIntentionClassifierDTO);
            } else {
                return toChat(chatRestOption);
            }
        } catch (Exception e) {
            return new ChatModelResult() {
                {
                    {
                        setChat("个人空间意图识别错误");
                        setRag(List.of());
                    }
                }
            };
        }
    }

    public RagIntentionClassifierDTO ragIntentionClassifier(String modelName, String q) {
        String s = chatGeneralAssistanceService.ragIntentionClassifier(modelName, q);
        return JSONUtil.toBean(s, RagIntentionClassifierDTO.class);
    }

    public ChatModelResult toAddDocuments(RagIntentionClassifierDTO ragIntentionClassifierDTO) throws Exception {
        String user = SecurityUtils.getCurrentUsername();
        String setFileGroupName = user + "_知识空间文件组";
        String setFileName = user + "_知识空间";
        TRagFile ragFile = new TRagFile();

        List<TRagFileGroup> fileGroupList = fileGroupService
                .list(new LambdaQueryWrapper<TRagFileGroup>().eq(TRagFileGroup::getGroupName, setFileGroupName));

        if (fileGroupList.isEmpty()) {
            TRagFileGroup ragFileGroup = new TRagFileGroup();
            ragFileGroup.setGroupName(setFileGroupName);
            ragFileGroup.setGroupType("个人知识空间");
            ragFileGroup.setGroupDesc("对话知识空间生成");
            ragFileGroup.setCreatedBy(user);
            ragFileGroup.setVisibility("private");
            fileGroupService.save(ragFileGroup);
            TRagFileGroup fileGroup = fileGroupService.getOne(new LambdaQueryWrapper<TRagFileGroup>()
                    .eq(TRagFileGroup::getGroupName, setFileGroupName));
            ragFile.setFileGroupId(String.valueOf(fileGroup.getId()));
        } else {
            // 如果文件组已存在，使用现有文件组的ID
            TRagFileGroup existingGroup = fileGroupList.get(0);
            ragFile.setFileGroupId(String.valueOf(existingGroup.getId()));
        }

        // 文件操作
        List<TRagFile> fileList = fileService
                .list(new LambdaQueryWrapper<TRagFile>().eq(TRagFile::getFileName, setFileName));
        if (fileList.isEmpty()) {
            ragFile.setFileName(setFileName);
            ragFile.setFileType("对话知识空间");
            ragFile.setFileSize("无");
            ragFile.setDocumentNum(String.valueOf(ragIntentionClassifierDTO.getDocuments().size()));
            ragFile.setFileDesc(setFileName + "。在对话中产生。");
            // fileGroupId已经在上面设置过了
            ragFile.setUploadedBy(user);
            fileService.save(ragFile);
        } else {
            TRagFile first = fileList.getFirst();
            first.setDocumentNum(String.valueOf(
                    Integer.parseInt(first.getDocumentNum()) + ragIntentionClassifierDTO.getDocuments().size()));
            ragFile = first;
            fileService.updateById(first);
        }
        ragService.writeDocumentsToVectorStore(ragIntentionClassifierDTO.getDocuments(), ragFile);
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat("已被存入个人空间");
        chatModelResult.setRag(List.of());
        return chatModelResult;
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
                chatRestOption.getQuestion());

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

    /**
     * RAG模式聊天方法
     *
     * <p>
     * 基于检索增强生成技术，先从向量数据库中检索相关文档，
     * 然后将检索结果作为上下文提供给AI模型，生成更准确的回答。
     * </p>
     *
     * @param id        聊天会话ID，用于标识对话历史
     * @param p         系统提示词，用于指导AI的行为和回答风格
     * @param q         用户问题内容
     * @param groupId   RAG文档分组ID，用于限定检索范围
     * @param modelName AI模型名称，如gpt-3.5-turbo、gpt-4等
     * @param isReRank  是否启用重排序功能，提升检索结果的相关性
     * @param tools     工具函数回调数组，支持函数调用功能
     * @return ChatModelResult 聊天结果，包含AI回复和检索到的相关文档
     * @throws Exception 当RAG检索或AI调用失败时抛出
     */
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

        // 构建并发送AI请求
        String chat = this.chatClient.prompt(
                new Prompt(
                        ragPrompt + "\n" + p,
                        OpenAiChatOptions.builder()
                                .model(modelName)
                                .build()))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
                .advisors(a -> a.param(chatMemory.CONVERSATION_ID, id))
                .toolCallbacks(tools)
                .call().content();

        // 构建返回结果，包含AI回复和检索到的文档
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat(chat);
        chatModelResult.setRag(documentList);
        return chatModelResult;
    }

    /**
     * 普通聊天模式（非RAG）
     *
     * <p>
     * 不使用检索增强生成，直接基于系统提示词和用户问题进行对话。
     * 适用于不需要外部知识库支持的一般性对话场景。
     * </p>
     *
     * @param id        聊天会话ID，用于标识对话历史
     * @param p         系统提示词，用于指导AI的行为和回答风格
     * @param q         用户问题内容
     * @param modelName AI模型名称，如gpt-3.5-turbo、gpt-4等
     * @param tools     工具函数回调数组，支持函数调用功能
     * @return ChatModelResult 聊天结果，包含AI回复（无相关文档）
     */
    public ChatModelResult noRagChat(String id, String p, String q, String modelName, ToolCallback[] tools) {

        // 构建并发送AI请求，不包含RAG上下文
        String chat = this.chatClient.prompt(
                new Prompt(
                        p,
                        OpenAiChatOptions.builder()
                                .model(modelName)
                                .build()))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
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
