package com.langhuan.serviceai;

import cn.hutool.core.lang.UUID;
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
import com.langhuan.service.MinioService;
import com.langhuan.service.TPromptsService;
import com.langhuan.service.TRagFileGroupService;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.imageunderstanding.ImageUnderstandingProcessorFactory;
import com.langhuan.utils.other.ImgUtil;
import com.langhuan.utils.other.SecurityUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private ImageUnderstandingProcessorFactory imageUnderstandingProcessorFactory;
    @Autowired
    private MinioService minioService;
    @Value("${minio.img-bucket-name}")
    private String bucket;
    @Value("${minio.folder.chat-memory-img}")
    private String chatMemoryImgFold;

    @PostConstruct
    public void init() throws Exception {
        // 在初始化方法中调用 ensureBucketExists
        if (minioService != null) {
            minioService.ensureBucketExists(bucket);
        }
    }

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
    public ChatModelResult chat(ChatRestOption chatRestOption) throws Exception {
        try {
            // 提取用户消息（question）中是不是带md格式的图片信息
            ImgUtil.MarkdownImageResult imageResult = ImgUtil.extractMarkdownImageUrls(chatRestOption.getQuestion());
            // 图片理解，当前只是纯理解 不走rag等。
            if (imageResult != null && !imageResult.getUrls().isEmpty()) {
                return toImageUnderstanding(chatRestOption, imageResult);
            }
            // 文字理解
            // 是否意图识别 （后续考虑意图识别放在最前面，例如是要理解图片？还是添加个人知识空间？还是将图片内容添加到个人知识库？ 如果都不识别成功，就默认走文字对话。）
            if (Constant.RAGADDDOCUMENTINTENTION) {
                String s = chatGeneralAssistanceService.ragAddDocumentIntentionClassifier(chatRestOption.getModelName(),
                        chatRestOption.getQuestion());
                RagIntentionClassifierDTO ragIntentionClassifierDTO = JSONUtil.toBean(s,
                        RagIntentionClassifierDTO.class);
                return ragIntentionClassifierDTO.getIsAdd() ? toAddDocuments(ragIntentionClassifierDTO)
                        : toChat(chatRestOption);
            }
            // 文字
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

    public Message createdMessage(String text, Map<String, Object> metadata, MessageType messageType) {
        return new Message() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return metadata;
            }

            @Override
            public MessageType getMessageType() {
                return messageType;
            }
        };
    }

    public ChatModelResult toAddDocuments(RagIntentionClassifierDTO ragIntentionClassifierDTO) throws Exception {
        String user = SecurityUtils.getCurrentUsername();
        // HACK 和前端约定的知识空间文件组名称
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

    public ChatModelResult toImageUnderstanding(ChatRestOption chatRestOption, ImgUtil.MarkdownImageResult imageResult) throws Exception {
        StringBuilder out = new StringBuilder();
        String prompt = imageResult.getCleanedContent() + "\n" + "请回答中文";
        StringBuilder minioChatImgs = new StringBuilder();
        for (String item : imageResult.getUrls()) {
            String objectName = chatMemoryImgFold + "/" + chatRestOption.getChatId() + "/" + UUID.randomUUID().toString() + "." + ImgUtil.getImageFormatFromBase64(item);
            minioService.handleUpload(objectName, ImgUtil.base64ToInputStream(item), -1, bucket);
            minioChatImgs.append("![img](url)".replace("url", minioService.generateMinioUrl(objectName, bucket))).append("\n");
            out.append(imageUnderstandingProcessorFactory.getProcessor().understandImage(item,
                    prompt)).append("\n");
        }
        String chatInStr = minioChatImgs.toString() + imageResult.getCleanedContent();
        String chatOutStr = out.toString();

        // 手动设置记忆
        chatMemory.add(chatRestOption.getChatId(),
                createdMessage(chatInStr, Map.of(), MessageType.USER));

        chatMemory.add(chatRestOption.getChatId(),
                createdMessage(chatOutStr, Map.of(), MessageType.ASSISTANT));
        return new ChatModelResult() {
            {
                {
                    setChat(chatOutStr);
                    setRag(List.of());
                }
            }
        };
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
