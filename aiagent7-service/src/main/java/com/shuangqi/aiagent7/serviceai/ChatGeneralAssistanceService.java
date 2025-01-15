package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.stereotype.Service;

@Service
public class ChatGeneralAssistanceService {

    private final ChatClient.Builder chatClientBuilder;

    public ChatGeneralAssistanceService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public String otherQuestionsRecommended(String q) {
        return chatClientBuilder.defaultSystem("""
                        请根据用户输入的问题，理解其含义，生成两个相关的推荐问题。
                        请直接输出推荐问题，无需解释或额外对话。
                                                
                        以JSON格式返回。
                        确保你的回答遵循以下结构：
                        {
                            "desc": "[问题一，问题二]"
                        }
                        """)
                .defaultAdvisors(
                )
                .build().prompt()
                .user(q)
                .call()
                .content();
    }

    public String optimizePromptWords(String q) {
        return chatClientBuilder.defaultSystem("""
                        你是一个提示词优化专家，擅长快速分析和改进提示词。我会提供一段提示词，请你直接优化它，确保优化后的提示词具备以下特点：
                                            
                        明确性：指令清晰，避免歧义。
                        具体性：包含足够的上下文和细节。
                        结构化：逻辑清晰，易于AI理解。
                        简洁性：避免冗余，突出重点。
                                                
                        请直接输出优化后的提示词，无需解释或额外对话。
                                                
                        以JSON格式返回。
                        确保你的回答遵循以下结构：
                        {
                            "desc": "这里是回答的内容，请用合适的字符串的形式回复，当字符串中存在双引号的时候使用单引号替代。"
                        }
                        """)
                .defaultAdvisors(
                )
                .build().prompt()
                .user(q)
                .call()
                .content();
    }

}
