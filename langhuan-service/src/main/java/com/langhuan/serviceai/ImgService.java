package com.langhuan.serviceai;

import cn.hutool.core.lang.UUID;
import com.langhuan.model.pojo.ChatRestOption;
import com.langhuan.service.MinioService;
import com.langhuan.utils.imageunderstanding.ImageUnderstandingProcessorFactory;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImgService {

    @Value("${minio.img-bucket-name}")
    private String bucket;
    @Value("${minio.folder.chat-memory-img}")
    private String chatMemoryImgFold;
    @Autowired
    private MinioService minioService;
    @Autowired
    private ImageUnderstandingProcessorFactory imageUnderstandingProcessorFactory;


    @PostConstruct
    public void init() throws Exception {
        // 在初始化方法中调用 ensureBucketExists
        if (minioService != null) {
            minioService.ensureBucketExists(bucket);
        }
    }

    /**
     * 图片理解，同时把图片存入minio
     *
     * @param chatRestOption 聊天请求选项对象，包含聊天ID、问题内容、是否使用RAG等配置
     * @param imageFiles     图片文件
     * @return 返回存入minio后的问题和图片理解结果
     */
    public ChatImageUnderstandingRes chat_imageUnderstanding(ChatRestOption chatRestOption, MultipartFile[] imageFiles) throws Exception {
        StringBuilder out = new StringBuilder();
        String prompt = chatRestOption.getUserMessage() + "\n" + "请使用中文回答，回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；";
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
        String chatOutStr = out.toString();

        return new ChatImageUnderstandingRes() {{
            setChatInStr(minioChatImgs.toString());
            setChatOutStr(chatOutStr);
        }};
    }

    /**
     * 图片理解，主要用于提取信息
     *
     * @param imageFiles 图片文件
     * @param imgPrompt  提示词，传递空字符串有默认值
     * @return 提取的图片信息
     */
    public List<String> chat_imageUnderstandingToText(MultipartFile[] imageFiles, String imgPrompt) throws Exception {
        if (imgPrompt.isEmpty()) {
            imgPrompt = """
                    请对图片进行详细解析，提取其中的知识信息：
                    如果图片是文字内容，请完整、准确地提取所有文字信息，确保无遗漏。
                    如果图片是流程图、结构图、思维导图等图形，请用清晰的文字描述其结构、逻辑关系、关键节点和流程步骤，确保信息完整、逻辑清晰，便于后续存储为文字知识。
                    如果图片包含图表、表格、数据，请提取其中的关键数据、指标、趋势，并用简洁的文字进行说明。
                    如果图片是混合类型（如图文结合），请分别提取文字和图形信息，并整合为一段完整的文字描述。
                    请确保提取的内容准确、简洁、结构化，便于直接存入知识库。
                    输出格式建议如下：
                    
                    【信息】：
                    （用清晰、简洁的文字描述图片中的知识信息。这里直接呈现知识信息。）
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

    @Data
    public static class ChatImageUnderstandingRes {
        private String chatInStr;
        private String chatOutStr;
    }
}
