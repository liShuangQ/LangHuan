# HanLP Lucene Analyzer

基于 HanLP 1.8.6 portable 的 Lucene 中文分词器实现，已通过编译和运行测试。

## 文件结构

```
src/main/kotlin/com/langhuan/utils/analyzer/
├── HanLPAnalyzer.kt          # 主分析器类
├── HanLPTokenizer.kt         # 分词器实现（完整版）
├── HanLPAnalyzerExample.kt   # 使用示例
├── HanLPTest.kt             # 简单测试类
├── AnalyzerConfig.kt        # Spring 配置类
└── README.md               # 说明文档
```

## 功能特性

- ✅ 支持中文分词，使用 HanLP.segment() 方法
- ✅ 兼容 Lucene 9.x 版本
- ✅ 支持 TextField 字段的索引和搜索
- ✅ 提供词项偏移量、位置增量等 Lucene 属性
- ✅ 自动识别中文词汇并标注类型
- ✅ 通过编译测试和运行测试

## 核心类

### HanLPAnalyzer
主要的分析器类，继承自 `org.apache.lucene.analysis.Analyzer`
- 包含内嵌的 Tokenizer 实现
- 简化的 API 兼容 Lucene 9.x

### HanLPTokenizer
完整的分词器实现（独立文件）
- 支持流式处理
- 完整的 Lucene 属性支持

## 使用方式

### 1. 基本使用

```kotlin
val analyzer = HanLPAnalyzer()
val tokenStream = analyzer.tokenStream("content", StringReader("要分词的文本"))

val termAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)
tokenStream.reset()
while (tokenStream.incrementToken()) {
    val term = termAttribute.toString()
    println("分词结果: $term")
}
tokenStream.end()
analyzer.close()
```

### 2. Spring 依赖注入

```kotlin
@Configuration
class YourConfig {
    @Bean
    fun hanLPAnalyzer(): Analyzer {
        return HanLPAnalyzer()
    }
}

@Service
class YourService {
    @Autowired
    private lateinit var hanLPAnalyzer: Analyzer
}
```

### 3. 运行测试

```bash
# 运行简单测试
./mvnw compile exec:java -Dexec.mainClass="com.langhuan.utils.analyzer.HanLPTest"

# 运行完整示例
./mvnw compile exec:java -Dexec.mainClass="com.langhuan.utils.analyzer.HanLPAnalyzerExample"
```

## 测试结果

测试文本：`"中华人民共和国今天成立了，中国人民从此站起来了！琅嬛福地是一个AI知识管理系统。"`

分词结果：
```
 1: 中华人民共和国
 2: 今天
 3: 成立
 4: 了
 5: ，
 6: 中国
 7: 人民
 8: 从此
 9: 站
10: 起来
11: 了
12: ！
13: 琅嬛
14: 福地
15: 是
16: 一个
17: AI
18: 知识
19: 管理系统
20: 。
```

## 依赖配置

项目中已包含所需依赖：
- HanLP 1.8.6 portable
- Lucene Core 9.11.1
- Lucene Analyzers Common 8.11.1
- Lucene Query Parser 9.11.1

## 性能特性

- HanLP 首次初始化需要一定时间
- 支持大文本处理（内部缓冲区 8192 字符）
- 自动过滤空白和控制字符
- 中文词汇类型自动识别

## 注意事项

1. **资源管理**：使用完毕后记得调用 `close()` 方法释放资源
2. **初始化时间**：HanLP 首次使用需要加载模型，可能略有延迟
3. **编码支持**：确保文本使用 UTF-8 编码
4. **Spring 集成**：建议在 Spring 容器中使用以获得更好的资源管理

## 故障排除

如果遇到编译错误，确保：
- Maven 依赖正确下载
- Kotlin 编译插件版本兼容
- Java 版本为 21