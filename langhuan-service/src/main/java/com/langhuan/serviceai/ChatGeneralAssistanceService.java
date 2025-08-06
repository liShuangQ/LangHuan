package com.langhuan.serviceai;

import com.langhuan.common.Constant;
import com.langhuan.service.TPromptsService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ChatGeneralAssistanceService {

        private final ChatClient chatClient;

        public ChatGeneralAssistanceService(ChatClient.Builder chatClientBuilder) {
                chatClient = chatClientBuilder
                                .defaultAdvisors(
                                                new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                                                new SimpleLoggerAdvisor())
                                .defaultSystem("回答必须使用 Markdown 格式（如标题、列表、加粗等），不得嵌套任何 JSON、XML 等结构化格式，不得使用```markdown  ```代码块标记；")
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
                                .call()
                                .content();
        }

        public String optimizePromptWords(String q) {

                return chatClient.prompt(TPromptsService.getCachedTPromptsByMethodName("optimizePromptWords"))
                                .user(q)
                                .call()
                                .content();
        }

        public String parameterMatching(String q) {
                return chatClient.prompt(TPromptsService.getCachedTPromptsByMethodName("parameterMatching"))
                                .user(q)
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
                                .call()
                                .content();
        }

        public String ragIntentionClassifier(String modelName, String q) {
                return chatClient.prompt(
                                new Prompt(
                                                """
                                                                你是一个RAG意图分类器，严格按以下规则处理：
                                                                1. **添加意图识别**：当用户输入满足以下任一模式时判定为添加意图（isAdd=true）：
                                                                   - 纠正类：包含"不对"/"错了"等否定词 + 正确信息（例："你说的不对，真实应该是XXX"）
                                                                   - 显性指令：包含"记录"/"添加"/"加到知识库"等关键词（例："将XXX加入知识库"）
                                                                   - 知识更新：明确提供新知识（例："更新知识：XXX"）
                                                                   *其他情况均视为非添加意图*

                                                                2. **文档提取规则**：
                                                                   - 仅当isAdd=true时提取文本，按以下原则处理：
                                                                     a) 移除意图引导词（如"真实应该是"/"记录一条知识"/"加到知识库"等）
                                                                     b) 保留核心文本作为单个chunk（例：输入"将水的沸点是100℃加到知识库" → 提取"水的沸点是100℃"）
                                                                     c) 多段文本合并为单个chunk（例：输入"添加两条知识：1.xxx 2.yyy" → 提取"1.xxx 2.yyy"）
                                                                   - 非添加意图返回空数组

                                                                3. **输出规范**：
                                                                   - 必须返回纯JSON，无任何额外字符/解释
                                                                   - 格式严格遵循：
                                                                     ```json
                                                                     {"isAdd": boolean, "documents": [string]}
                                                                     ```
                                                                   - 示例：
                                                                     {"isAdd": true, "documents": ["太阳系有八大行星"]}
                                                                     {"isAdd": false, "documents": []}

                                                                请处理用户输入：
                                                                """,
                                                OpenAiChatOptions.builder()
                                                                .model(modelName)
                                                                .build()))
                                .user(q)
                                .call()
                                .content();
        }

}
