package com.shuangqi.aiagent7.common;

import java.util.List;

public class Constant {
    // JWT密钥，用于签名和验证token，需要足够长以确保安全性
    public static final String SECRET = "dGhpcyBpcyBhIGJhc2U2NCBrZXkgd2l0aCBmb3IgZnJvbSBzZWN1cmUgcmluZw==";
    // Token过期时间，单位为分钟
    public static final long EXPIRE = 60 * 24 * 24;
    // HTTP请求头中的token字段名
    public static final String HEADER = "Authorization";
    // 访问白名单
    public static final String[] URL_WHITELIST = {"/favicon.ico", "/user/login", "/user/register" };
    // ai系统默认提示词
    public static final String AIDEFAULTSYSTEMPROMPT = """
                        你叫小明。
                        请你对我提供的信息进行专业且深入的分析，无论是文本内容、数据还是概念等方面。
                        用清晰、准确、有条理的语言进行回应，给出全面的解释、合理的建议或精准的判断。
                        帮助我更好地理解相关事物并做出明智的决策或获得更深入的认知。
                        
                        回复格式如下，确保desc内有内容，key只能是desc。
                        {desc:documents}
                        """;
    // ai系统默认安全顾问
    public static final List<String> AIDEFAULTSAFEGUARDADVISOR = List.of();
    //设置返回的最相似结果的数量
    public static final int WITHTOPK = 1;
    //设置相似度阈值，常是一个介于 0 和 1 之间的浮点数，例如 0.5、0.7、0.8 等，如果设置得过高，可能会没有结果返回；如果设置得过低，可能会返回大量不相关的结果
    public static final double WITHSIMILARITYTHRESHOLD = 0.5;
}
