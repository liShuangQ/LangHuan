package com.langhuan.utils.rerank;

import cn.hutool.json.JSONUtil;
import com.langhuan.utils.http.PostRequestUtils;
import lombok.Data;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GTE重排模型处理器
 * 处理GTE系列模型的重排请求
 */
@Component
public class GteReRankProcessor implements ReRankProcessor {
    
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
        
        // 调用GTE模型API
        GteReRankResult gteResult = callGteApi(query, documentListStr, top_n);
        
        // 处理返回结果
        List<GteReRankResult.Result> results = gteResult.getOutput().getResults();
        for (GteReRankResult.Result result : results) {
            Document document = documentList.get(result.getIndex());
            document.getMetadata().put("relevance_score", result.getRelevance_score());
            rankResult.add(document);
        }
        
        return rankResult;
    }
    
    @Override
    public String getModelName() {
        return "gte";
    }
    
    /**
     * 调用GTE模型API
     */
    private GteReRankResult callGteApi(String query, List<String> documentList, int top_n) throws Exception {
        String jsonData = JSONUtil.toJsonStr(
                Map.of(
                        "model", model,
                        "input", Map.of(
                                "query", query,
                                "documents", documentList),
                        "parameters", Map.of(
                                "return_documents", true,
                                "top_n", top_n)));
        
        String out = PostRequestUtils.sendPostRequest(base_url, jsonData, 
                Map.of("Authorization", "Bearer " + api_key));
        
        // 解析JSON响应并转换为GteReRankResult对象
        return JSONUtil.toBean(out, GteReRankResult.class);
    }


    @Data
    public class GteReRankResult {
        private String request_id;
        private GteReRankResult.Output output;
        private GteReRankResult.Usage usage;

        @Data
        public static class Output {
            private List<GteReRankResult.Result> results;
        }

        @Data
        public static class Result {
            private GteReRankResult.Document document;
            private int index;
            private double relevance_score;
        }

        @Data
        public static class Document {
            private String text;
        }

        @Data
        public static class Usage {
            private int total_tokens;
        }
    }
}