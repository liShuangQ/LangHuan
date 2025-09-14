# 重排模型工厂模式使用说明

## 概述
本模块采用工厂模式实现可扩展的重排模型支持。通过定义统一的接口和工厂类，可以轻松添加新的重排模型而无需修改现有代码。

## 核心组件

### 1. ReRankProcessor 接口
所有重排模型处理器的基类接口，定义了标准的方法：
- `rerank(String query, List<Document> documentList, int top_n)`: 执行重排操作
- `getModelName()`: 获取支持的模型名称

### 2. ReRankProcessorFactory 工厂类
负责根据模型名称创建对应的处理器实例，支持模糊匹配。

### 3. 具体处理器实现
- `GteReRankProcessor`: GTE模型处理器
- `BgeReRankProcessor`: BGE模型处理器（示例）

## 如何添加新的重排模型

### 步骤1：创建新的处理器类
实现 `ReRankProcessor` 接口：

```java
@Component
public class NewModelReRankProcessor implements ReRankProcessor {
    
    @Override
    public List<Document> rerank(String query, List<Document> documentList, int top_n) throws Exception {
        // 实现具体的重排逻辑
        // 1. 调用模型API
        // 2. 解析返回结果
        // 3. 更新文档的相关性评分
        // 4. 返回重排后的文档列表
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
public class NewModelReRankResult {
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
rerank:
  model: newmodel-v1  # 模型名称
  base-url: https://api.newmodel.com/rerank
  api-key: your-api-key
```

## 使用示例

```java
@Service
public class SomeService {
    @Autowired
    private ReRankModelService reRankModelService;
    
    public void example() throws Exception {
        List<Document> documents = // ... 获取文档列表
        String query = "用户查询";
        int topN = 5;
        
        // 执行重排
        List<Document> rankedDocs = reRankModelService.chat(query, documents, topN);
        
        // 使用重排结果
        for (Document doc : rankedDocs) {
            Double score = (Double) doc.getMetadata().get("relevance_score");
            System.out.println("文档: " + doc.getText() + ", 相关性评分: " + score);
        }
    }
}
```

## 扩展性说明

1. **自动注册**：所有实现 `ReRankProcessor` 接口并被 `@Component` 注解的类会自动注册到工厂中
2. **模糊匹配**：工厂支持模型名称的模糊匹配，例如配置为 `gte-large` 会匹配到 `gte` 处理器
3. **统一输出**：无论使用哪种模型，最终都返回统一的 `List<Document>` 格式，相关性评分存储在文档的 metadata 中
4. **异常处理**：如果找不到对应的处理器，会抛出 `IllegalArgumentException`

## 最佳实践

1. **错误处理**：在处理器中妥善处理API调用异常
2. **日志记录**：添加适当的日志记录便于调试
3. **配置管理**：将模型相关的配置集中管理
4. **测试覆盖**：为新添加的处理器编写单元测试
