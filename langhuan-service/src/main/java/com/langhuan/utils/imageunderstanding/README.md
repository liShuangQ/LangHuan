# 图像理解模型工厂模式使用说明

## 概述
本模块采用工厂模式实现可扩展的图像理解模型支持。通过定义统一的接口和工厂类，可以轻松添加新的图像理解模型而无需修改现有代码。

## 核心组件

### 1. ImageUnderstandingProcessor 接口
所有图像理解模型处理器的基类接口，定义了标准的方法：
- `understandImage(String imageUrl, String prompt)`: 执行图像理解操作
- `understandImage(MultipartFile imageFile, String prompt)`: 执行图像理解操作（文件上传方式）
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
    
    @Override
    public String understandImage(String imageUrl, String prompt) throws Exception {
        // 实现具体的图像理解逻辑
        // 1. 调用模型API
        // 2. 解析返回结果
        // 3. 返回图像理解结果
    }
    
    @Override
    public String understandImage(MultipartFile imageFile, String prompt) throws Exception {
        // 实现具体的图像理解逻辑（文件上传方式）
        // 1. 将文件转换为base64编码
        // 2. 调用模型API
        // 3. 解析返回结果
        // 4. 返回图像理解结果
    }
    
    @Override
    public String getModelName() {
        return "newmodel"; // 返回模型标识符
    }
}
```

### 步骤2：定义模型返回结果类
根据模型的API返回格式创建对应的POJO类：

```java
@Data
public class NewModelImageUnderstandingResult {
    private List<NewModelResult> results;
    
    @Data
    public static class NewModelResult {
        private int index;
        private double score;
        // 其他字段...
    }
}
```

### 步骤3：配置模型参数
在 `application.yml` 中配置新模型的参数：

```yaml
image_understanding:
  model: newmodel-v1  # 模型名称
  base-url: https://api.newmodel.com/image-understanding
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
    private ImageUnderstandingService imageUnderstandingService;
    
    public void example() throws Exception {
        // URL方式
        String imageUrl = "https://example.com/image.jpg";
        String prompt = "图片中有什么？";
        String result = imageUnderstandingService.understandImage(imageUrl, prompt);
        System.out.println("图像理解结果: " + result);
        
        // 文件上传方式
        File imageFile = new File("path/to/image.jpg");
        String result2 = imageUnderstandingService.understandImage(imageFile, prompt);
        System.out.println("图像理解结果: " + result2);
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