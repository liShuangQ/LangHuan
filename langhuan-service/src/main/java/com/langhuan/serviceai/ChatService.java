package com.langhuan.serviceai;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.service.MinioService;
import com.langhuan.service.TPromptsService;
import com.langhuan.service.TRagFileGroupService;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.imageunderstanding.ImageUnderstandingProcessorFactory;
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
    public ChatModelResult chat(ChatRestOption chatRestOption, MultipartFile[] accessory) throws Exception {
        try {
            // 意图识别
            String intention = chatGeneralAssistanceService.chatIntentionClassifier(chatRestOption.getModelName(), chatRestOption.getUserMessage());
            // HACK 注意当前下面的能力都没判断附件是不是全是图片类型，但是前端有校验。以后加其他类型这里要注意
            // TODO 未来考虑是不是添加文件类型分类器，当前附件都是图片直接调用图片模型没问题；但是如果有其他文件类型，考虑怎么意图识别和执行对应方法和拼接回答
            // TODO 考虑意图能力是不是要原子化，例如先做什么后做什么
            switch (intention) {
                case "chat":
                    return toChat(chatRestOption);
                case "understand_image":
                    return toImageUnderstanding(chatRestOption, accessory);
                case "add_personal_knowledge_space":
                    StringBuilder simulationThink1 = new StringBuilder();
                    simulationThink1.append("<think>");
                    simulationThink1.append("意图识别结果：添加文字信息到知识库").append("\n");
                    simulationThink1.append("文字提取知识：").append("\n");
                    simulationThink1.append("文字识别结果：").append("\n");
                    List<String> documentSegmentation1 = chatGeneralAssistanceService.documentSegmentation(chatRestOption.getModelName(), chatRestOption.getUserMessage());
                    for (String string : documentSegmentation1) {
                        simulationThink1.append(string)
                                .append("\n")
                                .append("-----------------------------------------------------")
                                .append("\n");
                    }
                    if (!documentSegmentation1.isEmpty()) {
                        simulationThink1.append("添加到知识库").append("\n");
                        simulationThink1.append("添加到知识库成功").append("\n");
                        simulationThink1.append("</think>");
                        ChatModelResult addDocuments = toAddDocuments(documentSegmentation1);
                        return new ChatModelResult() {{
                            setChat(simulationThink1 + addDocuments.getChat());
                            setRag(List.of());
                        }};
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
                    simulationThink2.append("图片提取知识：").append("\n");
                    simulationThink2.append("图片识别结果：").append("\n");
                    List<String> imageUnderstandingToText = toImageUnderstandingToText(chatRestOption, accessory, "");
                    for (String string : imageUnderstandingToText) {
                        simulationThink2.append(string)
                                .append("\n")
                                .append("-----------------------------------------------------")
                                .append("\n");
                    }
                    if (!imageUnderstandingToText.isEmpty()) {
                        simulationThink2.append("添加到知识库").append("\n");
                        simulationThink2.append("添加到知识库成功").append("\n");
                        simulationThink2.append("</think>");
                        ChatModelResult addDocuments = toAddDocuments(imageUnderstandingToText);
                        return new ChatModelResult() {{
                            setChat(simulationThink2 + addDocuments.getChat());
                            setRag(List.of());
                        }};
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

    public ChatModelResult toAddDocuments(List<String> documents) throws Exception {
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
            ragFile.setDocumentNum(String.valueOf(documents.size()));
            ragFile.setFileDesc(setFileName + "。在对话中产生。");
            // fileGroupId已经在上面设置过了
            ragFile.setUploadedBy(user);
            fileService.save(ragFile);
        } else {
            TRagFile first = fileList.getFirst();
            first.setDocumentNum(String.valueOf(
                    Integer.parseInt(first.getDocumentNum()) + documents.size()));
            ragFile = first;
            fileService.updateById(first);
        }
        ragService.writeDocumentsToVectorStore(documents, ragFile);
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat("已被存入个人空间");
        chatModelResult.setRag(List.of());
        return chatModelResult;
    }

    public ChatModelResult toImageUnderstanding(ChatRestOption chatRestOption, MultipartFile[] imageFiles) throws Exception {
        StringBuilder out = new StringBuilder();
        String prompt = chatRestOption.getUserMessage() + "\n" + "请使用中文回答";
        StringBuilder minioChatImgs = new StringBuilder();
        for (MultipartFile item : imageFiles) {
            // 构建地址
            String objectName = chatMemoryImgFold + "/" + chatRestOption.getChatId() + "/" + UUID.randomUUID().toString() + item.getOriginalFilename();
            // 上传地址
            minioService.handleUpload(objectName, item.getInputStream(), -1, bucket);
            // 获取minio地址
            minioChatImgs.append("![img](url)".replace("url", minioService.generateMinioUrl(objectName, bucket))).append("\n");
            // 模型回答
            out.append(imageUnderstandingProcessorFactory.getProcessor().understandImage(item,
                    prompt)).append("\n");
        }
        String chatInStr = minioChatImgs + chatRestOption.getUserMessage();
        String chatOutStr = out.toString();

        // 手动设置记忆
        chatMemory.add(chatRestOption.getChatId(),
                createdMessage(chatInStr, Map.of(), MessageType.USER));

        chatMemory.add(chatRestOption.getChatId(),
                createdMessage(chatOutStr, Map.of(), MessageType.ASSISTANT));
        return new ChatModelResult() {{
            setChat(chatOutStr);
            setRag(List.of());
        }};
    }

    public List<String> toImageUnderstandingToText(ChatRestOption chatRestOption, MultipartFile[] imageFiles, String imgPrompt) throws Exception {
        if (imgPrompt.isEmpty()) {
            imgPrompt = """
                    请对图片进行详细解析，提取其中的知识信息：
                    如果图片是文字内容，请完整、准确地提取所有文字信息，确保无遗漏。
                    如果图片是流程图、结构图、思维导图等图形，请用清晰的文字描述其结构、逻辑关系、关键节点和流程步骤，确保信息完整、逻辑清晰，便于后续存储为文字知识。
                    如果图片包含图表、表格、数据，请提取其中的关键数据、指标、趋势，并用简洁的文字进行说明。
                    如果图片是混合类型（如图文结合），请分别提取文字和图形信息，并整合为一段完整的文字描述。
                    请确保提取的内容准确、简洁、结构化，便于直接存入知识库。
                    输出格式建议如下：
                    
                    （用清晰、简洁的文字描述图片中的知识信息）
                    【关键词】：
                    （提取3-5个关键词，便于后续检索）
                    
                    请根据上述要求处理以下图片。
                    """;
        }
        List<String> out = new ArrayList<>();
        String prompt = imgPrompt + "\n" + "请使用中文回答";
        for (MultipartFile item : imageFiles) {
            out.add(imageUnderstandingProcessorFactory.getProcessor().understandImage(item,
                    prompt));
        }

        return out;
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
