package com.langhuan.serviceai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.langhuan.utils.http.PostRequestUtils;

import cn.hutool.json.JSONUtil;

public class ReRankModelService {
    @Value("${rerank.base_url}")
    private String base_url;
    @Value("${rerank.api-key}")
    private String api_key;
    @Value("${rerank.model")
    private String model;

    public String chat(String query, List<String> documentList) throws Exception {
        String jsonData = JSONUtil.toJsonStr(Map.of("query", query, "documentList", documentList));
        String out = PostRequestUtils.sendPostRequest(base_url, jsonData, Map.of("Authorization", "Bearer " + api_key));
        return out;
    }
}
