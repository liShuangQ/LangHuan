package com.langhuan.utils.rerank

import cn.hutool.json.JSONUtil
import com.langhuan.utils.http.PostRequestUtils
import org.springframework.ai.document.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * GTE重排模型处理器
 * 处理GTE系列模型的重排请求
 */
@Component
class GteReRankProcessor : ReRankProcessor {
    
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
        
        // 调用GTE模型API
        val gteResult = callGteApi(query, documentListStr as List<String>, top_n)
        
        // 处理返回结果
        val results = gteResult.output?.results
        if (results != null) {
            for (result in results) {
                val document = documentList[result.index]
                document.metadata["relevance_score"] = result.relevance_score
                rankResult.add(document)
            }
        }
        
        return rankResult
    }
    
    override fun getModelName(): String {
        return "gte"
    }
    
    /**
     * 调用GTE模型API
     */
    @Throws(Exception::class)
    private fun callGteApi(query: String, documentList: List<String>, top_n: Int): GteReRankResult {
        val jsonData = JSONUtil.toJsonStr(
                mapOf(
                        "model" to model,
                        "input" to mapOf(
                                "query" to query,
                                "documents" to documentList
                        ),
                        "parameters" to mapOf(
                                "return_documents" to true,
                                "top_n" to top_n
                        )))
        
        val out = PostRequestUtils.sendPostRequest(base_url, jsonData, 
                mapOf("Authorization" to "Bearer $api_key"))
        
        // 解析JSON响应并转换为GteReRankResult对象
        return JSONUtil.toBean(out, GteReRankResult::class.java)
    }


    class GteReRankResult {
        var request_id: String? = null
        var output: Output? = null
        var usage: Usage? = null

        class Output {
            var results: List<Result>? = null
        }

        class Result {
            var document: Document? = null
            var index: Int = 0
            var relevance_score: Double = 0.0
        }

        class Document {
            var text: String? = null
        }

        class Usage {
            var total_tokens: Int = 0
        }
    }
}
