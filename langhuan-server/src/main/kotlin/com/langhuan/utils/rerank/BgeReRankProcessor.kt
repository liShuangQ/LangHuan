package com.langhuan.utils.rerank

import cn.hutool.json.JSONUtil
import com.langhuan.utils.http.PostRequestUtils
import org.springframework.ai.document.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * BGE重排模型处理器示例
 * 展示如何添加新的重排模型支持
 * 注意：这是一个示例实现，实际使用时需要根据具体的BGE模型API格式进行调整
 */
@Component
class BgeReRankProcessor : ReRankProcessor {
    
    @Value("\${rerank.base-url}")
    private lateinit var base_url: String
    
    @Value("\${rerank.api-key}")
    private lateinit var api_key: String
    
    @Value("\${rerank.model}")
    private lateinit var model: String

    @Throws(Exception::class)
    override fun rerank(query: String, documentList: List<Document>, top_n: Int): List<Document> {
        val rankResult = mutableListOf<Document>()
        val documentListStr = documentList.stream().map { obj: Document -> obj.text }.toList()
        
        // 调用BGE模型API（示例格式，实际使用时需要根据具体API调整）
        val bgeResult = callBgeApi(query, documentListStr as List<String>, top_n)
        
        // 处理返回结果
        for (result in bgeResult.results) {
            val document = documentList[result.index]
            document.metadata["relevance_score"] = result.score
            rankResult.add(document)
        }
        
        return rankResult
    }

    override fun getModelName(): String {
        return "bge"
    }
    
    /**
     * 调用BGE模型API（示例实现）
     */
    @Throws(Exception::class)
    private fun callBgeApi(query: String, documentList: List<String>, top_n: Int): BgeReRankResult {
        // 这里模拟BGE模型的API调用格式
        // 实际使用时需要根据具体的BGE模型API文档进行调整
        val jsonData = JSONUtil.toJsonStr(
                mapOf(
                        "model" to model,
                        "query" to query,
                        "documents" to documentList,
                        "top_k" to top_n
                ))
        
        val out = PostRequestUtils.sendPostRequest(base_url, jsonData, 
                mapOf("Authorization" to "Bearer $api_key"))
        
        // 解析JSON响应并转换为BgeReRankResult对象
        return JSONUtil.toBean(out, BgeReRankResult::class.java)
    }
    
    /**
     * BGE重排结果内部类（示例格式）
     */
    data class BgeReRankResult(
        val results: List<BgeResult>
    ) {
        data class BgeResult(
            val index: Int,
            val score: Double,
            val text: String
        )
    }
}
