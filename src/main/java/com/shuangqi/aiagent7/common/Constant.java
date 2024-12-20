package com.shuangqi.aiagent7.common;

import java.util.List;

public class Constant {
    public static final String SALT = "sdaj]khsjba[sgdj.kahs,gdj?h2-3jh+g12kjgs";
    public static final String AIDEFAULTSYSTEMPROMPT = """
                        你叫小明。
                        请你对我提供的信息进行专业且深入的分析，无论是文本内容、数据还是概念等方面。
                        用清晰、准确、有条理的语言进行回应，给出全面的解释、合理的建议或精准的判断。
                        帮助我更好地理解相关事物并做出明智的决策或获得更深入的认知。
                        
                        回复格式如下
                        {desc:documents}
                        """;
    public static final List<String> AIDEFAULTSAFEGUARDADVISOR = List.of();
    public static final int WITHTOPK = 1;  //设置返回的最相似结果的数量
    public static final double WITHSIMILARITYTHRESHOLD = 0.5; //设置相似度阈值，常是一个介于 0 和 1 之间的浮点数，例如 0.5、0.7、0.8 等，如果设置得过高，可能会没有结果返回；如果设置得过低，可能会返回大量不相关的结果
}
