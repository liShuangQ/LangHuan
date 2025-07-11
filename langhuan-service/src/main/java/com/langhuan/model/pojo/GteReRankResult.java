package com.langhuan.model.pojo;

import java.util.List;

import lombok.Data;

/**
 * ReRank模型返回结果
 */
@Data
public class GteReRankResult {
    private String request_id;
    private Output output;
    private Usage usage;
    
    @Data
    public static class Output {
        private List<Result> results;
    }
    
    @Data
    public static class Result {
        private Document document;
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
