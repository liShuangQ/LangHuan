package com.langhuan.serviceai;

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

    public String easyChatNoMd(String p, String q, String modelName) {
        return this.chatClient.prompt(
                        new Prompt(
                                p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
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
                    1. 读取用户提问 → 根据JSON配置中的每一项 “task(任务中文)、examples(正确的例子)、notExamples(错误的例子)、description(说明)、attention(注意事项)” 中的说明 → 返回对应提问的意图任务 id。
                    2. 若同时命中多条，选描述最具体的一条；**当「add_personal_knowledge_space」与「understand」同时命中时，无论顺序一律优先返回 add_personal_knowledge_space**；仍无法区分，默认返回 chat。
                    3. 不得返回 JSON 中不存在的 id，不得添加解释、引号、空格等任何字符。
                    4. 必须给结果，禁止空输出。
                    """;

            String finalPrompt = String.format(promptTemplate, jsonConfig);

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

    public String documentSegmentation(String modelName, String q) {
        log.info("提取知识信息: {}", q);
        String out = chatClient.prompt(
                        new Prompt(
                                """
                                        你是一个RAG文档提取器，严格按以下规则处理：
                                        1. **文档提取规则**：
                                             a) 移除意图引导词（如"真实应该是"/"记录一条知识"/"加到知识库"等）
                                             b) 移除其他知识无关提示词（如"识别图片中xxx"/"文件中xxx"/"图中xxx"等）
                                             c) 经过“规则a”和“规则b”(上两条规则)后剩余的文档均为所需提取内容
                                             d) 无可用可提取文档直接返回空字符串。不要回答问题，不要编造和扩写信息。
                                        2. **输出规范**：
                                           - 必须返回提取后的纯文字，无任何额外字符/解释
                                        3. **输入输出案例**：
                                            - 输入 （"将文档内容添加到知识库。记录一条知识：xxxxxx"）
                                            - 输出 （"xxxxxx"）
                                            ---
                                            - 输入 （"识别图片中关于经济的信息，添加到知识库。记录一条知识：yyyyyy"）
                                            - 输出 （"yyyyyy"）
                                        """,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .call()
                .content();

        try {
            log.info("文档提取拆分结果解析成功: {}", out);
            return out;
        } catch (Exception e) {
            log.info("文档提取拆分结果解析错误-返回完整信息");
            return q;
        }
    }

    public String documentUnderstand(String q, String modelName) {
        return this.chatClient.prompt(
                        new Prompt(
                                """
                                        根据要求处理文档。
                                        本任务仅涉及文本处理。
                                        请过滤掉所有涉及图像、图片、图表、图示、视觉内容分析的关键词与指令，专注于文字部分的解析与处理。
                                        注意回答不要提及这些要求，只是输出要求的内容。
                                        例如不可携带类似的话（'以下是对所提供文档的纯文字解析，已过滤所有涉及图像、图表、视觉内容分析的相关描述，仅保留可处理的文字信息'）
                                        """,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()))
                .user(q)
                .system("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
                .call().content();
    }
}
