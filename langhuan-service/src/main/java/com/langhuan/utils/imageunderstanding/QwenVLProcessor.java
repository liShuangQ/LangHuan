package com.langhuan.utils.imageunderstanding;

import cn.hutool.json.JSONUtil;
import com.langhuan.utils.http.PostRequestUtils;
import com.langhuan.utils.other.ImgUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Qwen-VL 图像理解模型处理器
 * 处理阿里云 Qwen-VL 系列模型的图像理解请求
 */
@Component
public class QwenVLProcessor implements ImageUnderstandingProcessor {
    
    // 10MB大小限制
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    
    @Value("${image_understanding.base-url}")
    private String base_url;
    
    @Value("${image_understanding.api-key}")
    private String api_key;
    
    @Value("${image_understanding.model}")
    private String model;
    
    @Override
    public String understandImage(String imageUrl, String prompt) throws Exception {
        return callApi(imageUrl, prompt);
    }
    
    @Override
    public String understandImage(File imageFile, String prompt) throws Exception {
        // 检查文件大小
        if (imageFile.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("图像文件大小不能超过10MB");
        }
        
        // 将文件转换为base64编码
        String base64Image = ImgUtil.encodeFileToBase64(imageFile);
        
        // 根据文件扩展名确定图片格式
        String imageFormat = ImgUtil.getImageFormat(imageFile);
        
        // 构造base64 URL
        String base64Url = "data:image/" + imageFormat + ";base64," + base64Image;
        
        return callApi(base64Url, prompt);
    }
    
    /**
     * 调用Qwen-VL模型API
     * 
     * @param imageUrl 图像URL或base64编码的图像
     * @param prompt 提示文本
     * @return 图像理解结果
     * @throws Exception 处理过程中的异常
     */
    private String callApi(String imageUrl, String prompt) throws Exception {
        // 构建请求内容
        List<Map<String, Object>> contentList = new ArrayList<>();
        
        // 添加图像内容
        Map<String, Object> imageContent = Map.of(
            "type", "image_url",
            "image_url", Map.of("url", imageUrl)
        );
        contentList.add(imageContent);
        
        // 添加文本提示
        Map<String, Object> textContent = Map.of(
            "type", "text",
            "text", prompt
        );
        contentList.add(textContent);
        
        // 构建消息
        Map<String, Object> message = Map.of(
            "role", "user",
            "content", contentList
        );
        
        // 构建请求数据
        String jsonData = JSONUtil.toJsonStr(
            Map.of(
                "model", model,
                "messages", List.of(message)
            )
        );
        
        // 发送 POST 请求
        String response = PostRequestUtils.sendPostRequest(base_url, jsonData, 
                Map.of("Authorization", "Bearer " + api_key, "Content-Type", "application/json"));
        
        // 解析响应并返回结果
        QwenVLResponse qwenResponse = JSONUtil.toBean(response, QwenVLResponse.class);
        return qwenResponse.getChoices().get(0).getMessage().getContent();
    }
    

    @Override
    public String getModelName() {
        return "qwen-vl";
    }
    
    /**
     * Qwen-VL 模型响应结果类
     */
    @Data
    public static class QwenVLResponse {
        private List<Choice> choices;
        private String object;
        private Usage usage;
        private Long created;
        private String system_fingerprint;
        private String model;
        private String id;
        
        @Data
        public static class Choice {
            private Message message;
            private String finish_reason;
            private Integer index;
            private Object logprobs;
            
            @Data
            public static class Message {
                private String content;
                private String role;
            }
        }
        
        @Data
        public static class Usage {
            private Integer prompt_tokens;
            private Integer completion_tokens;
            private Integer total_tokens;
            private PromptTokensDetails prompt_tokens_details;
            
            @Data
            public static class PromptTokensDetails {
                private Integer cached_tokens;
            }
        }
    }
}