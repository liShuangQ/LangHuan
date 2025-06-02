package com.langhuan.common;

import java.util.List;
import java.util.Map;

public class Constant {
    // JWT密钥，用于签名和验证token，需要足够长以确保安全性
    public static final String SECRET = "dGhpcyBpcyBhIGJhc2U2NCBrZXkgd2l0aCBmb3IgZnJvbSBzZWN1cmUgcmluZw==";
    // ADMIN的默认管理员账号的密码（明文）
    public static final String ADMIN_PASSWORD = "asb#1234";
    // Token过期时间，单位为分钟
    public static final long EXPIRE = 60 * 24 * 24;
    // HTTP请求头中的token字段名
    public static final String HEADER = "Authorization";
    // 访问白名单
    public static final String[] URL_WHITELIST = {"/favicon.ico", "/user/login", "/user/register", "/service/user/login", "/service/user/register", "/test/*"};
    // ai的rag默认拆分规则
    public static final String DEFAULT_RAG_SPLIT_PATTERN = "(?:={6})\\s*"; // "[;；]+\\s*"
    // ai的rag默认导出的分隔符
    public static final String DEFAULT_RAG_EXPORT_SPLIT = "======";
    // ai的单id最大记忆消息数
    public static final int MESSAGEWINDOWCHATMEMORYMAX = 10;
    // ai系统默认提示词数据库定时刷新时间
    public static final int AIDEFAULTSYSTEMPROMPTRECTIME = 1000 * 60 * 60 * 2;
    // ai系统默认提示词
    public static final String AINULLDEFAULTSYSTEMPROMPT = "你是一个人工智能，请根据要求回答用户的问题。";
    // ai系统默认安全顾问 （后期考虑放数据库）
    public static final List<String> AIDEFAULTSAFEGUARDADVISOR = List.of();
    // ai系统默认问答顾问提示词
    public static final String AIDEFAULTQUESTIONANSWERADVISORRPROMPT = """
            上下文信息rag，在下面用 --------------------- 包围。
            鉴于上下文信息rag和提供的历史信息而非先验知识，回复用户，并且不要体现这个信息是在上下文信息rag中拿到的。
            如果问题和上下文信息rag无关或者答案不在上下文信息rag中，则你自己回答这个问题。并且在回答中不要提示没找到信息，无需解释。
            ---------------------
            {question_answer_context}
            ---------------------
            """;
    //设置返回的最相似结果的数量
    public static final int RAGWITHTOPK = 5;
    //设置相似度阈值，常是一个介于 0 和 1 之间的浮点数，例如 0.5、0.7、0.8 等，如果设置得过高，可能会没有结果返回；如果设置得过低，可能会返回大量不相关的结果
    public static final double RAGWITHSIMILARITYTHRESHOLD = 0.3;
    public static final List<Map<String, Object>> CATEGORYENUM = List.of(
            Map.of("label", "默认", "value", "default"),
            Map.of("label", "系统", "value", "system"),
            Map.of("label", "提示词", "value", "prompt")
    );
    //rerank的topn
    public static final int RAGRERANKTOPN = 2;
}
