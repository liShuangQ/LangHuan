# 图像理解模型工厂模式使用说明

## 概述
本模块采用工厂模式实现可扩展的图像理解模型支持。通过定义统一的接口和工厂类，可以轻松添加新的图像理解模型而无需修改现有代码。

## 核心组件

### 1. ImageUnderstandingProcessor 接口
所有图像理解模型处理器的基类接口，定义了标准的方法：
- `understandImage(List<String> imageUrl, String prompt)`: 执行图像理解操作，支持多个图像URL
- `understandImage(MultipartFile[] imageFile, String prompt)`: 执行图像理解操作（文件上传方式），支持多个文件
- `getModelName()`: 获取支持的模型名称

### 2. ImageUnderstandingProcessorFactory 工厂类
负责根据模型名称创建对应的处理器实例，支持模糊匹配。

### 3. 具体处理器实现
- `QwenVLProcessor`: Qwen-VL 模型处理器

## 如何添加新的图像理解模型

### 步骤1：创建新的处理器类
实现 `ImageUnderstandingProcessor` 接口：

```java
@Component
public class NewModelImageUnderstandingProcessor implements ImageUnderstandingProcessor {
   
}
```

### 步骤2：定义模型返回结果类
根据模型的API返回格式创建对应的POJO类：

```java
@Data
public class QwenVLResponse {
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
```

### 步骤3：配置模型参数
在 `application.yml` 中配置新模型的参数：

```yaml
image_understanding:
  model: qwen-vl-plus  # 模型名称
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
  api-key: your-api-key
```

## 支持的输入方式

图像理解模型支持两种输入方式：

1. **URL方式**：直接提供图像的URL地址
2. **文件上传方式**：上传本地文件，系统会自动转换为base64编码

对于文件上传方式，系统会自动根据文件扩展名添加相应的MIME类型前缀：
- PNG图片：`data:image/png;base64,{base64_image}`
- JPEG图片：`data:image/jpeg;base64,{base64_image}`
- WEBP图片：`data:image/webp;base64,{base64_image}`

### 文件大小限制

上传的图像文件大小不能超过10MB。如果文件超过此限制，系统将抛出 `IllegalArgumentException` 异常。

## 使用示例

```java
@Service
public class SomeService {
    @Autowired
    private ImageUnderstandingProcessorFactory processorFactory;
    
    public void exampleWithUrl() throws Exception {
        ImageUnderstandingProcessor processor = processorFactory.getProcessor();
        List<String> imageUrls = Arrays.asList("http://example.com/image1.jpg", "http://example.com/image2.jpg");
        String result = processor.understandImage(imageUrls, "请描述这些图片的内容");
    }
    
    public void exampleWithFiles(MultipartFile[] imageFiles) throws Exception {
        ImageUnderstandingProcessor processor = processorFactory.getProcessor();
        String result = processor.understandImage(imageFiles, "请描述这些图片的内容");
    }
}
```

## 扩展性说明

1. **自动注册**：所有实现 `ImageUnderstandingProcessor` 接口并被 `@Component` 注解的类会自动注册到工厂中
2. **模糊匹配**：工厂支持模型名称的模糊匹配，例如配置为 `qwen-vl-plus` 会匹配到 `qwen-vl` 处理器
3. **统一输出**：无论使用哪种模型，最终都返回统一的字符串格式的结果
4. **异常处理**：如果找不到对应的处理器，会抛出 `IllegalArgumentException`

## 最佳实践

1. **错误处理**：在处理器中妥善处理API调用异常
2. **日志记录**：添加适当的日志记录便于调试
3. **配置管理**：将模型相关的配置集中管理
4. **测试覆盖**：为新添加的处理器编写单元测试
