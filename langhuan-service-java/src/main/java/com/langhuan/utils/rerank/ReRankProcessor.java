package com.langhuan.utils.rerank;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 重排模型处理器接口
 * 定义了所有重排模型处理器的标准接口
 */
public interface ReRankProcessor {
    
    /**
     * 执行重排操作
     * 
     * @param query 查询文本
     * @param documentList 待重排的文档列表
     * @param top_n 返回前n个结果
     * @return 重排后的文档列表，包含相关性评分
     * @throws Exception 处理过程中的异常
     */
    List<Document> rerank(String query, List<Document> documentList, int top_n) throws Exception;
    
    /**
     * 获取该处理器支持的模型名称
     * 
     * @return 模型名称
     */
    String getModelName();
}