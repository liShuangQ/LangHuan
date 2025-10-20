package com.langhuan.utils.imageunderstanding;

import cn.hutool.json.JSONUtil;
import com.langhuan.utils.http.PostRequestUtils;
import com.langhuan.utils.other.ImgUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    public String understandImage(List<String> imageUrl, String prompt) throws Exception {
        return callApi(imageUrl, prompt);
    }

    @Override
    public String understandImage(MultipartFile[] imageFile, String prompt) throws Exception {
        List<String> base64Urls = new ArrayList<>();
        for (MultipartFile file : imageFile) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("图像文件大小不能超过10MB");
            }
            // 将文件转换为base64编码
            String base64Image = ImgUtil.encodeMultipartFileToBase64(file);

            // 根据文件扩展名确定图片格式
            String imageFormat = ImgUtil.getImageFormat(file);

            // 构造base64 URL
            String base64Url = "data:image/" + imageFormat + ";base64," + base64Image;
            base64Urls.add(base64Url);
        }


        return callApi(base64Urls, prompt);
    }

    /**
     * 调用Qwen-VL模型API
     *
     * @param imageUrl 图像URL或base64编码的图像
     * @param prompt   提示文本
     * @return 图像理解结果
     * @throws Exception 处理过程中的异常
     */
    private String callApi(List<String> imageUrl, String prompt) throws Exception {
        // 构建请求内容
        List<Map<String, Object>> contentList = new ArrayList<>();

        // 添加图像内容
        for (String url : imageUrl) {
            Map<String, Object> imageContent = Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", url)
            );
            contentList.add(imageContent);
        }

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
                        "messages", List.of(message),
                        "stream", false,
//                       enable_thinking 参数开启思考过程，thinking_budget 参数设置最大推理过程 Token 数
                        "extra_body", Map.of(
                                "enable_thinking", false,
                                "thinking_budget", 81920
                        )
                )
        );

        // 发送 POST 请求
        String response = PostRequestUtils.sendPostRequest(base_url, jsonData,
                Map.of("Authorization", "Bearer " + api_key, "Content-Type", "application/json"));

        // 解析响应并返回结果
        QwenVLResponse qwenResponse = JSONUtil.toBean(response, QwenVLResponse.class);
        StringBuilder out = new StringBuilder();
        for (QwenVLResponse.Choice choice : qwenResponse.getChoices()) {
            out.append(choice.getMessage().getContent()).append("\n");
        }
        return out.toString();
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