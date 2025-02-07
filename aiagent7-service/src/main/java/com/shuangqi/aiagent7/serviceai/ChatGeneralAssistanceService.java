package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatGeneralAssistanceService {

    private final ChatClient chatClient;

    public ChatGeneralAssistanceService(ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder
                .defaultAdvisors(
                        new MySimplelogAdvisor()
                )
                .build();
    }


    public String otherQuestionsRecommended(String q) {
        return chatClient
                .prompt("""
                                请根据用户输入的问题，理解其含义，生成两个相关的推荐问题。
                                请直接输出推荐问题，无需解释或额外对话。
                                                        
                                以JSON格式返回。
                                确保你的回答遵循以下结构：
                                {
                                    "desc": "['问题一'，'问题二']"
                                }
                        """)
                .user(q)
                .call()
                .content();
    }

    public String optimizePromptWords(String q) {
        return chatClient.prompt("""
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
                .user(q)
                .call()
                .content();
    }

    public String parameterMatching(String q) {
        return chatClient.prompt("""
                        你是一个智能参数提取助手，能够将自然语言中的信息提取并匹配到给定的JSON格式接口入参中。
                        请根据用户提供的自然语言描述，提取出与JSON格式接口入参对应的参数值。
                        对于时间相关的参数（如“今天”、“明天”、“3天后”等），需要将其转换为 年月日时分秒 格式（例如：2023-10-05 00:00:00）。
                        如果没有匹配到合适的参数，对应的key的值就为空。
                                                
                        输入格式：
                        自然语言描述：一段描述需要提取参数的自然语言文本。
                        JSON格式接口入参：一个JSON对象，包含需要匹配的key。
                        
                        输出格式：
                        直接输出一个与输入 JSON 格式接口入参相同的 JSON 对象，其中 key 对应的值为从自然语言中提取的参数值。时间参数需要转换为 年月日时分秒 格式。如果没有匹配到合适的参数，对应的 key 的值为空字符串。
                        """)
                .user(q)
                .call()
                .content();
    }

}
