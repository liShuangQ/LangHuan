package com.langhuan.serviceai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.langhuan.common.BusinessException
import com.langhuan.common.Constant
import com.langhuan.config.VectorStoreConfig
import com.langhuan.dao.VectorStoreRagDao
import com.langhuan.model.pojo.SearchResult
import com.langhuan.service.LuceneIndexService
import com.langhuan.utils.other.NumberTool
import com.langhuan.utils.rerank.ReRankProcessorFactory
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service


@Service
class RagCallBackService(
    vectorStoreConfig: VectorStoreConfig,
    private val reRankProcessorFactory: ReRankProcessorFactory,
    private val luceneIndexService: LuceneIndexService,
    private val vectorStoreRagDao: VectorStoreRagDao,
) {
    companion object {
        private val log = LoggerFactory.getLogger(RagCallBackService::class.java)
        private val objectMapper = jacksonObjectMapper()
    }

    private val ragVectorStore: VectorStore = vectorStoreConfig.ragVectorStore()


    @Throws(Exception::class)
    fun ragSearch(text: String, groupId: String, fileId: String, isReRank: Boolean?): List<Document> {
        val embeddingCallBack: List<Document> = embeddingCallBack(text, groupId, fileId)
        val bm25CallBack = bm25CallBack(text, groupId, fileId)
        var searchDocuments: MutableList<Document> =
            mergeDocumentsWithScores(embeddingCallBack, bm25CallBack).toMutableList();
        searchDocuments = rankLinearWeighting(searchDocuments).toMutableList();
        // 使用rerank模型
        if (isReRank ?: false) {
            searchDocuments = searchDocuments.subList(0, minOf(Constant.RAGRERANKTOPK, searchDocuments.size))
            return reRankCallBack(text, searchDocuments);
        } else {
            searchDocuments = searchDocuments.filter {
                (it.metadata["weightedScore"] as? Double ?: 0.0) >= Constant.LINEARWEIGHTINGNUM
            }.toMutableList();
            return searchDocuments.subList(0, minOf(Constant.LLM_RAG_TOPN, searchDocuments.size))
        }
    }

    fun embeddingCallBack(text: String, groupId: String, fileId: String): List<Document> {
        val searchDocuments: MutableList<Document> = ArrayList()
        try {
            if (groupId.isEmpty()) {
                val searchResults = ragVectorStore.similaritySearch(
                    SearchRequest.builder().query(text).topK(Constant.RAGCALLBACKTOPK)
                        .similarityThreshold(Constant.RAGWITHSIMILARITYTHRESHOLD).build() // 单独设置多一些
                )
                searchDocuments.addAll(searchResults)
            } else {
                // HACK: 要么组查询 要么文件查询 文件id是唯一的
                val sql: String
                sql = if (fileId.isNotEmpty()) {
                    "fileId == '$fileId'"
                } else {
                    // 处理多个groupId的情况，支持逗号分割
                    val groupIds = groupId.split(",").toTypedArray()
                    // 过滤掉空字符串元素，处理 ",1,2" 这种情况
                    val validGroupIds: MutableList<String> = ArrayList()
                    for (id in groupIds) {
                        val trimmedId = id.trim { it <= ' ' }
                        if (trimmedId.isNotEmpty()) {
                            validGroupIds.add(trimmedId)
                        }
                    }

                    if (validGroupIds.isEmpty()) {
                        // 如果没有有效的groupId，抛出异常
                        throw BusinessException("groupId不能为空")
                    } else if (validGroupIds.size == 1) {
                        // 单个有效groupId的情况
                        "groupId == '" + validGroupIds[0] + "'"
                    } else {
                        // 多个有效groupId的情况，构建OR条件
                        val sqlBuilder = StringBuilder("(")
                        for (i in validGroupIds.indices) {
                            if (i > 0) {
                                sqlBuilder.append(" OR ")
                            }
                            sqlBuilder.append("groupId == '").append(validGroupIds[i]).append("'")
                        }
                        sqlBuilder.append(")")
                        sqlBuilder.toString()
                    }
                }
                val searchResults = ragVectorStore.similaritySearch(
                    SearchRequest.builder().query(text).topK(Constant.RAGCALLBACKTOPK)
                        .similarityThreshold(Constant.RAGWITHSIMILARITYTHRESHOLD)
                        .filterExpression(sql)// 设置过滤条件
                        .build()
                )
                searchDocuments.addAll(searchResults)
            }
        } catch (e: Exception) {
            RagCallBackService.Companion.log.error("EmbeddingCallBack error: {}", e.message)
            throw BusinessException("查询失败")
        }

        return searchDocuments;
    }

    fun bm25CallBack(text: String, groupId: String, fileId: String): MutableList<Document> {
        val searchDocumentIds: List<SearchResult> =
            luceneIndexService.searchDocuments(text, groupId, fileId, Constant.RAGCALLBACKTOPK)
        val selectByIdsDocuments = vectorStoreRagDao.selectRagByIds(searchDocumentIds.map { it.id })
        val bm25Result: MutableList<Document> = mutableListOf()
        for (it1 in selectByIdsDocuments) {
            for (it2 in searchDocumentIds) {
                if (it1["id"].toString() == it2.id) {
                    val it1m = it1["metadata"] as PGobject
                    val it1mMap: MutableMap<String, Any> = objectMapper.readValue(
                        it1m.value.toString(),
                        object : TypeReference<MutableMap<String, Any>>() {}
                    )
                    it1mMap["bm25Score"] = it2.score
                    bm25Result.add(
                        Document.builder()
                            .id(it2.id)
                            .text(it1["content"] as String)
                            .metadata(it1mMap as MutableMap<String, Any>)
                            .build()
                    )
                }
            }
        }
        return bm25Result;
    }

    fun reRankCallBack(text: String, rankSearchDocuments: List<Document>): List<Document> {
        val rerankedDocuments =
            reRankProcessorFactory.getProcessor().rerank(text, rankSearchDocuments, Constant.LLM_RAG_TOPN)
        return rerankedDocuments;
    }


    /**
     * 合并两个文档列表，保留所有文档并融合重复ID的分数，为缺失的分数补充默认值0
     */
    private fun mergeDocumentsWithScores(embeddingDocs: List<Document>, bm25Docs: List<Document>): List<Document> {
        val documentMap = mutableMapOf<String, Document>()

        // 处理向量召回结果
        for (doc in embeddingDocs) {
            val metadata = doc.metadata.toMutableMap()
            // 确保有bm25Score字段，缺失则设为0
            if (!metadata.containsKey("bm25Score")) {
                metadata["bm25Score"] = 0.0
            }

            documentMap[doc.id] = Document.builder()
                .id(doc.id)
                .text(doc.text)
                .score(doc.score) // 向量分数
                .metadata(metadata)
                .build()
        }

        // 处理BM25召回结果
        for (doc in bm25Docs) {
            val docId = doc.id
            val bm25Score = doc.metadata["bm25Score"] ?: 0.0

            if (documentMap.containsKey(docId)) {
                // 如果已存在，合并BM25分数到现有文档
                val existingDoc = documentMap[docId]!!
                val mergedMetadata = existingDoc.metadata.toMutableMap().apply {
                    this["bm25Score"] = bm25Score // 确保设置BM25分数
                }

                documentMap[docId] = Document.builder()
                    .id(existingDoc.id)
                    .text(existingDoc.text)
                    .score(existingDoc.score) // 保留向量分数
                    .metadata(mergedMetadata)
                    .build()
            } else {
                // 如果不存在，添加BM25文档，并补充缺失的向量分数
                val metadata = doc.metadata.toMutableMap()
                // 确保保留原始的bm25Score
                metadata["bm25Score"] = bm25Score

                documentMap[docId] = Document.builder()
                    .id(doc.id)
                    .text(doc.text)
                    .score(0.0) // 补充向量分数为0（因为这是BM25召回的文档）
                    .metadata(metadata)
                    .build()
            }
        }

        // 确保所有文档都有完整的分数字段
        return documentMap.values.map { doc ->
            val metadata = doc.metadata.toMutableMap()
            if (!metadata.containsKey("bm25Score")) {
                metadata["bm25Score"] = 0.0
            }

            Document.builder()
                .id(doc.id)
                .text(doc.text)
                .score(doc.score ?: 0.0) // 确保向量分数不为null
                .metadata(metadata)
                .build()
        }.toList()
    }


    /**
     * 线性加权法
     * score（Double类型） -> spring ai的得分（向量得分）
     * searchDocuments.get(0).get("score"); 通用嵌入模型理论大概率 （0-1的区间）
     *
     * bm25Score（Double类型） -> bm25得分
     * searchDocuments.get(0).getMetadata().get("bm25Score")  （0-n的区间）
     *
     * normalizationRank（Double类型） -> 手工排名 - 代码内计算而来
     * searchDocuments.get(0).getMetadata().get("rank")   (0-100的区间)
     */
    @Throws(Exception::class)
    fun rankLinearWeighting(documents: List<Document>): List<Document> {
        if (documents.isEmpty()) {
            return documents
        }

        // 收集所有文档的三种分数，用于query-wise Min-Max归一化
        val springAiScores = mutableListOf<Double>()
        val bm25Scores = mutableListOf<Double>()
        val rankScores = mutableListOf<Double>()

        // 提取所有原始分数
        for (doc in documents) {
            // Spring AI相似度分数（0-1区间）
            val springAiScore = NumberTool.convertToDouble(doc.score ?: 0, 0.0)
            springAiScores.add(springAiScore)

            // BM25分数（0-n区间）
            val bm25ScoreObj = doc.metadata["bm25Score"] ?: 0
            val bm25Score = NumberTool.convertToDouble(bm25ScoreObj, 0.0)
            bm25Scores.add(bm25Score)

            // 手工排名分数（0-100区间，转换为0-1）
            val rank = doc.metadata["rank"] as? Int ?: 0
            val rankScore = rank * 0.01
            rankScores.add(rankScore.toDouble())
        }

        // 分别对三种分数进行query-wise Min-Max归一化
        val normalizedSpringAiScores = applyMinMaxNormalization(springAiScores)
        val normalizedBm25Scores = applyMinMaxNormalization(bm25Scores)
        val normalizedRankScores = applyMinMaxNormalization(rankScores)

        // 应用线性加权计算综合得分
        for ((index, doc) in documents.withIndex()) {
            // 获取归一化后的分数
            val normalizedSpringAiScore = normalizedSpringAiScores[index]
            val normalizedBm25Score = normalizedBm25Scores[index]
            val normalizedRankScore = normalizedRankScores[index]

            // 线性加权计算综合得分
            // 权重顺序：{springai根据向量距离的得分，bm25得分，手工排名}
            val weightedScore = (
                    Constant.LINEARWEIGHTING[0] * normalizedSpringAiScore +
                            Constant.LINEARWEIGHTING[1] * normalizedBm25Score +
                            Constant.LINEARWEIGHTING[2] * normalizedRankScore)

            // 将归一化后的分数和综合得分存储到metadata中
            doc.metadata["normalizedSpringAiScore"] = normalizedSpringAiScore
            doc.metadata["normalizedBm25Score"] = normalizedBm25Score
            doc.metadata["normalizedRankScore"] = normalizedRankScore
            doc.metadata["weightedScore"] = weightedScore
        }

        // 根据综合得分降序排序
        val sortedDocuments = documents.sortedWith { doc1: Document, doc2: Document ->
            val score1 = doc1.metadata["weightedScore"] as? Double ?: 0.0
            val score2 = doc2.metadata["weightedScore"] as? Double ?: 0.0
            score2.compareTo(score1) // 降序排序
        }
        return sortedDocuments
    }

    /**
     * 对分数列表进行query-wise Min-Max归一化
     *
     * @param scores 原始分数列表
     * @return 归一化后的分数列表，所有值在[0,1]范围内
     */
    private fun applyMinMaxNormalization(scores: List<Double>): List<Double> {
        if (scores.isEmpty()) {
            return scores
        }

        val maxScore = scores.maxOrNull() ?: 0.0
        val minScore = scores.minOrNull() ?: 0.0

        // 如果所有分数相同，则返回默认归一化值0.5
        if (maxScore == minScore) {
            return List(scores.size) { 0.5 }
        }

        // 应用Min-Max归一化: (score - min) / (max - min)
        return scores.map { score ->
            (score - minScore) / (maxScore - minScore)
        }
    }
}