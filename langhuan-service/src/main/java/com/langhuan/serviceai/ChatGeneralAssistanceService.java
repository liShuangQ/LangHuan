package com.langhuan.serviceai;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.common.Constant;
import com.langhuan.service.TPromptsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ChatGeneralAssistanceService {

    private final ChatClient chatClient;

    public ChatGeneralAssistanceService(ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor())
                .build();
    }

    public String easyChat(String p, String q, String modelName) {
        return this.chatClient.prompt(
                        new Prompt(
                                p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .system("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
                .call().content();
    }

    public String tools(String p) {
        return chatClient
                .prompt(p)
                .call()
                .content();
    }

    public String otherQuestionsRecommended(String q) {
        return chatClient
                .prompt(TPromptsService.getCachedTPromptsByMethodName("otherQuestionsRecommended"))
                .user(q)
                .system("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
                .call()
                .content();
    }

    public String optimizePromptWords(String q) {

        return chatClient.prompt(TPromptsService.getCachedTPromptsByMethodName("optimizePromptWords"))
                .user(q)
                .system("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
                .call()
                .content();
    }

    public String parameterMatching(String q) {
        return chatClient.prompt(TPromptsService.getCachedTPromptsByMethodName("parameterMatching"))
                .user(q)
                .system("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
                .call()
                .content();
    }

    public String llmTextSplitter(String modelName, String q) {
        return chatClient.prompt(
                        new Prompt(
                                TPromptsService.getCachedTPromptsByMethodName("llmTextSplitter"),
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .system("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
                .call()
                .content();
    }

    public String chatIntentionClassifier(String modelName, String q) {
        log.info("开始意图识别");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonConfig = objectMapper.writeValueAsString(Constant.INTENTIONTYPE);

            String promptTemplate = """
                    你是一名意图分类器，只按下方 JSON 配置进行映射并返回对应的 id，不得输出任何额外字符。
                    当无法确定时一律返回：chat。
                    
                    === 当前配置 ===
                    %s
                    === 配置结束 ===
                    
                    【运行规则】
                    1. 读取用户提问 → 匹配 task 或 examples 或 description 中的关键词 → 返回对应 id。 
                    2. 若同时命中多条，选描述最具体的一条；仍无法区分，默认返回 chat。 
                    3. 不得返回 JSON 中不存在的 id，不得添加解释、引号、空格等任何字符。 
                    4. 必须给结果，禁止空输出。
                    """;

            String finalPrompt = String.format(promptTemplate, jsonConfig);

            log.info("意图识别项目: {}", finalPrompt);

            String modelOut = chatClient.prompt(
                            new Prompt(
                                    finalPrompt,
                                    OpenAiChatOptions.builder()
                                            .model(modelName)
                                            .build()))
                    .user(q)
                    .call()
                    .content();
            log.info("意图识别结果: {}", modelOut);

            // 检查Constant.INTENTIONTYPE中是否包含modelOut
            boolean containsId = Constant.INTENTIONTYPE.stream()
                    .anyMatch(item -> item.getId().equals(modelOut));

            // 有就返回modelOut，没有就返回Constant.DEFAULTINTENTIONTYPEID
            String o = containsId ? modelOut : Constant.DEFAULTINTENTIONTYPEID;
            log.info("实际返回意图识别结果: {}", o);
            return o;
        } catch (JsonProcessingException e) {
            // 如果JSON序列化失败，使用默认的信息
            return Constant.DEFAULTINTENTIONTYPEID;
        } catch (Exception e) {
            // 如果发生其他异常，使用默认的信息
            return Constant.DEFAULTINTENTIONTYPEID;
        }
    }

    public List<String> documentSegmentation(String modelName, String q) {
        log.info("提取知识信息: {}", q);
        String out = chatClient.prompt(
                        new Prompt(
                                """
                                        你是一个RAG文档提取拆分器，严格按以下规则处理：
                                        1. **文档提取规则**：
                                             a) 移除意图引导词（如"真实应该是"/"记录一条知识"/"加到知识库"等）
                                             b) 保留核心文本作为单个chunk（例：输入"将水的沸点是100℃加到知识库" → 提取"水的沸点是100℃"）
                                             c) 多段文本按句子含义拆分为多个chunk（例：输入"添加知识：1.xxx 2.yyy" → 提取  ["xxx","yyy"] ）
                                             d) 无可用知识信息返回空数组
                                        
                                        2. **输出规范**：
                                           - 必须返回纯JSON，无任何额外字符/解释
                                           - 格式严格遵循：
                                             ```json
                                             ["chunk1","chunk2"]
                                             ```
                                           - 示例：
                                             ["水的沸点是100℃","xxx","yyy"]
                                        """,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .call()
                .content();

        try {
            log.info("文档提取拆分结果解析成功: {}", out);
            return JSONUtil.parseArray(out).toList(String.class);
        } catch (Exception e) {
            log.info("文档提取拆分结果解析错误-返回完整信息");
            return List.of(q);
        }
    }
}
