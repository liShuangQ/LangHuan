package com.langhuan.serviceai;

import cn.hutool.json.JSONUtil;
import com.langhuan.model.pojo.GteReRankResult;
import com.langhuan.model.pojo.GteReRankResult.Result;
import com.langhuan.utils.http.PostRequestUtils;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReRankModelService {
    @Value("${rerank.base-url}")
    private String base_url;
    @Value("${rerank.api-key}")
    private String api_key;
    @Value("${rerank.model}")
    private String model;

    public List<Document> chat(String query, List<Document> documentList, int top_n) throws Exception {
        List<Document> rankResult = new ArrayList<>();
        List<String> documentListStr = documentList.stream().map(Document::getText).toList();
        if (model.indexOf("gte") >= 0) {
            GteReRankResult gte = gte(query, documentListStr, top_n);
            List<Result> results = gte.getOutput().getResults();
            for (Result result : results) {
                Document document = documentList.get(result.getIndex());
                document.getMetadata().put("relevance_score", result.getRelevance_score());
                rankResult.add(document);
            }
        }
        return rankResult;
    }

    public GteReRankResult gte(String query, List<String> documentList, int top_n) throws Exception {
        String jsonData = JSONUtil.toJsonStr(
                Map.of(
                        "model", model,
                        "input", Map.of(
                                "query", query,
                                "documents", documentList),
                        "parameters", Map.of(
                                "return_documents", true,
                                "top_n", top_n)));
        String out = PostRequestUtils.sendPostRequest(base_url, jsonData, Map.of("Authorization", "Bearer " + api_key));

        // 解析JSON响应并转换为ReRankResult对象
        GteReRankResult reRankResult = JSONUtil.toBean(out, GteReRankResult.class);

        return reRankResult;
    }
}
