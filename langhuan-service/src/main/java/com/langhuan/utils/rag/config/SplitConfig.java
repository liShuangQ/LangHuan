package com.langhuan.utils.rag.config;

import java.util.Map;

/**
 * 统一参数配置
 */
<<<<<<< HEAD

=======
>>>>>>> f79417a (feat(RAG): 实现ETL管道重构及组件化)
public class SplitConfig {
    private String splitMethod;
    private Map<String, Object> methodData;

    public SplitConfig(String splitMethod, Map<String, Object> methodData) {
        this.splitMethod = splitMethod;
        this.methodData = methodData;
    }

    public String getSplitMethod() { return splitMethod; }
    public Map<String, Object> getMethodData() { return methodData; }
}
