package com.langhuan.utils.rerank;

import cn.hutool.json.JSONUtil;
import com.langhuan.utils.http.PostRequestUtils;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BGE重排模型处理器示例
 * 展示如何添加新的重排模型支持
 * 注意：这是一个示例实现，实际使用时需要根据具体的BGE模型API格式进行调整
 */
@Component
public class BgeReRankProcessor implements ReRankProcessor {
    
    @Value("${rerank.base-url}")
    private String base_url;
    
    @Value("${rerank.api-key}")
    private String api_key;
    
    @Value("${rerank.model}")
    private String model;

    @Override
    public List<Document> rerank(String query, List<Document> documentList, int top_n) throws Exception {
        List<Document> rankResult = new ArrayList<>();
        List<String> documentListStr = documentList.stream().map(Document::getText).toList();
        
        // 调用BGE模型API（示例格式，实际使用时需要根据具体API调整）
        BgeReRankResult bgeResult = callBgeApi(query, documentListStr, top_n);
        
        // 处理返回结果
        for (BgeReRankResult.BgeResult result : bgeResult.getResults()) {
            Document document = documentList.get(result.getIndex());
            document.getMetadata().put("relevance_score", result.getScore());
            rankResult.add(document);
        }
        
        return rankResult;
    }
    
    @Override
    public String getModelName() {
        return "bge";
    }
    
    /**
     * 调用BGE模型API（示例实现）
     */
    private BgeReRankResult callBgeApi(String query, List<String> documentList, int top_n) throws Exception {
        // 这里模拟BGE模型的API调用格式
        // 实际使用时需要根据具体的BGE模型API文档进行调整
        String jsonData = JSONUtil.toJsonStr(
                Map.of(
                        "model", model,
                        "query", query,
                        "documents", documentList,
                        "top_k", top_n));
        
        String out = PostRequestUtils.sendPostRequest(base_url, jsonData, 
                Map.of("Authorization", "Bearer " + api_key));
        
        // 解析JSON响应并转换为BgeReRankResult对象
        return JSONUtil.toBean(out, BgeReRankResult.class);
    }
    
    /**
     * BGE重排结果内部类（示例格式）
     */
    @lombok.Data
    public static class BgeReRankResult {
        private List<BgeResult> results;
        
        @lombok.Data
        public static class BgeResult {
            private int index;
            private double score;
            private String text;
        }
    }
}