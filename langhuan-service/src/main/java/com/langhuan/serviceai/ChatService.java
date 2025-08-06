package com.langhuan.serviceai;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.domain.TUserChatWindow;
import com.langhuan.model.dto.RagIntentionClassifierDTO;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.service.*;
import com.langhuan.utils.other.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * <li>聊天记忆管理</li>
 * <li>工具函数调用</li>
 * <li>对话窗口管理</li>
 * </ul>
 *
 * <p>
 * 该类整合了Spring AI框架，使用OpenAI作为底层大模型，
 * 通过JdbcChatMemoryRepository实现聊天记录的持久化存储。
 * </p>
 *
 * @author LangHuan
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ChatService {

    /**
     * 聊天记忆数据访问仓库
     * <p>
     * 用于将聊天记录持久化到数据库中，支持基于JDBC的存储实现。
     * </p>
     */
    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;

    /**
     * RAG(检索增强生成)服务
     *
     * <p>
     * 提供基于向量数据库的文档检索功能，用于增强AI回答的准确性和相关性。
     * </p>
     */
    @Autowired
    RagService ragService;

    /**
     * 用户聊天窗口服务
     *
     * <p>
     * 管理用户的聊天会话窗口，包括创建、更新、查询和删除操作。
     * </p>
     */
    @Autowired
    TUserChatWindowService userChatWindowService;

    /**
     * Spring AI聊天客户端
     *
     * <p>
     * 配置了默认的advisors，包括安全防护、日志记录和记忆管理。
     * </p>
     */
    ChatClient chatClient;

    /**
     * 聊天记忆管理器
     *
     * <p>
     * 管理对话历史的内存存储，支持滑动窗口机制限制存储的消息数量。
     * </p>
     */
    ChatMemory chatMemory;

    @Autowired
    ChatGeneralAssistanceService chatGeneralAssistanceService;
    @Autowired
    TRagFileService fileService;
    @Autowired
    TRagFileGroupService fileGroupService;
    @Autowired
    TUserService userService;

    /**
     * 构造函数
     *
     * <p>
     * 初始化聊天客户端和记忆管理器，配置默认的advisors：
     * </p>
     * <ul>
     * <li>SafeGuardAdvisor - 安全防护advisor，确保输出内容安全</li>
     * <li>SimpleLoggerAdvisor - 日志记录advisor，记录聊天过程</li>
     * <li>MessageChatMemoryAdvisor - 消息记忆advisor，维护对话历史</li>
     * </ul>
     *
     * @param chatClientBuilder Spring AI聊天客户端构建器
     * @param ragService        RAG服务实例
     */
    public ChatService(ChatClient.Builder chatClientBuilder, RagService ragService) {
        this.ragService = ragService;

        // 初始化聊天记忆管理器，配置最大消息数量和持久化仓库
        chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(Constant.MESSAGEWINDOWCHATMEMORYMAX)
                .build();

        // 构建聊天客户端，配置默认advisors
        this.chatClient = chatClientBuilder
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

    /**
     * 设置聊天窗口名称
     *
     * <p>
     * 更新指定会话窗口的显示名称，用于用户界面展示。
     * </p>
     *
     * @param conversationId   会话ID
     * @param conversationName 新的会话名称
     * @return Boolean 更新是否成功
     */
    public Boolean setChatMemoryWindowsName(String conversationId, String conversationName) {
        log.info("ChatMemory-set-windows-name");
        return userChatWindowService.update(new LambdaUpdateWrapper<TUserChatWindow>()
                .eq(TUserChatWindow::getConversationId, conversationId)
                .set(TUserChatWindow::getConversationName, conversationName));
    }

    /**
     * 获取当前用户的所有聊天窗口
     *
     * <p>
     * 查询当前登录用户的所有聊天会话窗口列表。
     * </p>
     *
     * @return List<TUserChatWindow> 用户的聊天窗口列表
     */
    public List<TUserChatWindow> getChatMemoryWindows() {
        log.info("ChatMemory-get-windows");
        // 获取当前登录用户ID
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 查询该用户的所有聊天窗口
        return userChatWindowService.list(
                new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getUserId, currentUserId));
    }

    /**
     * 获取指定会话的聊天历史
     *
     * <p>
     * 从数据库中查询指定会话ID的所有聊天记录。
     * </p>
     *
     * @param id 会话ID
     * @return List<Message> 聊天消息列表
     */
    public List<Message> getChatMemory(String id) {
        log.info("ChatMemory-get: {}", id);
        return chatMemoryRepository.findByConversationId(id);
    }

    /**
     * 初始化聊天内存
     *
     * <p>
     * 将指定会话的历史记录加载到内存中，用于后续的对话记忆管理。
     * 注意：内存中存储的key格式为"用户ID_会话ID"，而数据库中直接使用会话ID。
     * </p>
     *
     * @param id 会话ID
     */
    public void initChatMemory(String id) {
        log.info("ChatMemory-init: {}", id);
        List<Message> byConversationId = chatMemoryRepository.findByConversationId(id);

        // 构建内存存储的key：用户ID_会话ID
        String memoryKey = SecurityContextHolder.getContext().getAuthentication().getName() + '_' + id;

        // 将历史记录加载到内存中
        chatMemory.add(memoryKey, byConversationId);
    }

    /**
     * 保存聊天记录到数据库
     *
     * <p>
     * 将内存中的聊天记录持久化到数据库，包括：
     * </p>
     * <ul>
     * <li>如果会话窗口不存在，则创建新的窗口记录</li>
     * <li>将内存中的消息记录保存到数据库</li>
     * </ul>
     *
     * @param id         会话ID
     * @param windowName 窗口名称
     * @return String 保存结果提示信息
     */
    public String saveChatMemory(String id, String windowName) {
        log.info("ChatMemory-save: {}", id);
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();

        // 检查是否已存在该会话窗口
        long count = userChatWindowService
                .count(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));

        // 如果不存在，创建新的窗口记录
        if (count == 0) {
            userChatWindowService.save(new TUserChatWindow() {
                {
                    setUserId(user_id);
                    setConversationName(windowName);
                    setConversationId(id);
                }
            });
        }

        // 从内存中获取聊天记录并保存到数据库
        // 注意：内存中的key包含用户ID前缀，而数据库存储使用纯会话ID
        List<Message> messages = chatMemory.get(user_id + '_' + id);
        chatMemoryRepository.saveAll(id, messages);
        return "保存成功";
    }

    // 内存优化说明：
    // 当用户不保存窗口直接关闭页面，会导致内存中存在数据但数据库中没有记录，
    // 造成无意义的内存占用。后续需要实现：
    // 1. 窗口无感知的自动保存机制
    // 2. 定期清理无效会话数据
    // 3. 用户登录时清理相关无效内存

    /**
     * 清除指定会话的所有数据
     *
     * <p>
     * 彻底清理指定会话的所有相关数据，包括：
     * </p>
     * <ul>
     * <li>清空内存中的对话历史</li>
     * <li>删除用户聊天窗口记录</li>
     * <li>清空数据库中的对话记录</li>
     * </ul>
     *
     * @param id 会话ID
     * @return String 清除结果提示信息
     */
    public String clearChatMemory(String id) {
        log.info("ChatMemory-clear: {}", id);

        // 获取当前用户ID，用于构建内存key
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 清空内存中的对话历史（key格式：用户ID_会话ID）
        chatMemory.clear(currentUserId + '_' + id);

        // 删除用户聊天窗口记录
        userChatWindowService
                .remove(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));

        // 清空数据库中的对话记录
        chatMemoryRepository.deleteByConversationId(id);

        return "清除成功";
    }

}
