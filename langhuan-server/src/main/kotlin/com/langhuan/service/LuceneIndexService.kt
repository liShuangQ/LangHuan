package com.langhuan.service

import com.langhuan.model.pojo.SearchResult
import com.langhuan.utils.analyzer.HanLPAnalyzer
import jakarta.annotation.PostConstruct
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lucene 索引服务
 *
 * 负责从 PostgreSQL 数据库读取文档数据并构建 Lucene 倒排索引。
 * 支持定时重建索引和手动触发重建功能。
 *
 * @author Langhuan System
 * @since 1.0.0
 */
@Service
class LuceneIndexService(
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(LuceneIndexService::class.java)
    }

    @Value("\${lucene.index-path}")
    private lateinit var indexPath: String

    private val analyzer: Analyzer = HanLPAnalyzer()
    private var indexWriter: IndexWriter? = null
    private var indexDirectory: Directory? = null

    /**
     * 服务启动时初始化
     *
     * 检查索引是否存在，如果不存在则自动创建初始索引。
     */
    @PostConstruct
    fun initializeIndex() {
        log.info("开始初始化 Lucene 索引")

        try {
            // 确保索引目录存在
            val indexDir = Paths.get(indexPath)
            if (!Files.exists(indexDir)) {
                Files.createDirectories(indexDir)
                log.info("创建索引目录: $indexPath")
            }

            // 检查索引是否存在
            if (!isIndexExists()) {
                log.info("索引不存在，开始创建初始索引")
                val documentCount = rebuildIndex()
                log.info("初始索引创建完成，处理文档数: {}", documentCount)
            } else {
                val docCount = getIndexDocumentCount()
                log.info("索引已存在，当前文档数: {}", docCount)
            }

        } catch (e: Exception) {
            log.error("初始化索引失败", e)
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 检查索引是否存在
     */
    private fun isIndexExists(): Boolean {
        return try {
            val indexDir = Paths.get(indexPath)
            if (!Files.exists(indexDir)) {
                return false
            }

            // 尝试打开索引目录检查是否包含索引文件
            val directory = FSDirectory.open(indexDir)
            val result = DirectoryReader.indexExists(directory)
            directory.close()
            result
        } catch (e: Exception) {
            log.debug("检查索引存在性失败: {}", e.message)
            false
        }
    }

    /**
     * 重建索引
     *
     * 删除现有索引目录，从数据库重新读取所有有效文档并构建新的索引。
     *
     * @return 重建的文档数量
     */
    fun rebuildIndex(): Int {
        val startTime = System.currentTimeMillis()
        log.info("开始重建 Lucene 索引，索引路径: $indexPath")

        try {
            // 关闭现有索引资源
            closeIndexResources()

            // 删除现有索引目录
            deleteIndexDirectory()

            // 创建新的索引目录和写入器
            createIndexWriter()

            // 从数据库读取文档并构建索引
            val documentCount = buildIndexFromDatabase()

            // 提交并关闭索引写入器
            commitAndCloseWriter()

            val endTime = System.currentTimeMillis()
            val duration = (endTime - startTime) / 1000.0

            log.info("Lucene 索引重建完成，共处理 {} 个文档，耗时 {:.2f} 秒", documentCount, duration)
            return documentCount

        } catch (e: Exception) {
            log.error("重建 Lucene 索引失败", e)
            closeIndexResources()
            throw RuntimeException("重建索引失败: ${e.message}", e)
        }
    }

    /**
     * 搜索文档
     *
     * 根据关键词搜索相关文档，返回包含相关度分数的搜索结果。
     * 使用支持中文分词的全文搜索，而非精确匹配。
     *
     * @param query 搜索关键词
     * @param maxResults 最大返回结果数
     * @return 包含ID和分数的搜索结果列表
     */
    fun searchDocuments(query: String, groupId: String, fileId: String, maxResults: Int = 10): List<SearchResult> {
        if (query.isBlank()) {
            return emptyList()
        }

        ensureIndexReaderAvailable()

        return try {
            val indexReader = DirectoryReader.open(indexDirectory!!)
            val indexSearcher = IndexSearcher(indexReader)

            // 使用 QueryParser 创建支持中文分词的查询
            val queryParser = QueryParser("content", analyzer)
            val luceneQuery = queryParser.parse(query.trim())

            val builder = BooleanQuery.Builder()
            builder.add(luceneQuery, BooleanClause.Occur.MUST)
            if (!groupId.isEmpty()) {
                val groupFilter1: Query = TermQuery(Term("groupId", groupId))
                builder.add(groupFilter1, BooleanClause.Occur.FILTER)
            }
            if (!fileId.isEmpty()) {
                val groupFilter2: Query = TermQuery(Term("fileId", fileId))
                builder.add(groupFilter2, BooleanClause.Occur.FILTER)
            }
            val finalQuery: Query? = builder.build()

            val topDocs = indexSearcher.search(finalQuery, maxResults)

            val result = mutableListOf<SearchResult>()
            for (scoreDoc in topDocs.scoreDocs) {
                val storedFields = indexSearcher.storedFields()
                val document = storedFields.document(scoreDoc.doc)
                val id = document.get("id")
                if (id != null) {
                    // 使用原始分数，Lucene 分数已经是标准化过的
                    val normalizedScore = scoreDoc.score
                    result.add(SearchResult(id, normalizedScore, scoreDoc.doc))
                }
            }

            indexReader.close()
            log.debug("BM25搜索 '{}' 返回 {} 个结果", query, result.size)
            result

        } catch (e: Exception) {
            log.error("BM25搜索文档失败: query='{}'", query, e)
            emptyList()
        }
    }

    /**
     * 获取索引文档总数
     *
     * @return 索引中的文档总数
     */
    fun getIndexDocumentCount(): Long {
        ensureIndexReaderAvailable()

        return try {
            val indexReader = DirectoryReader.open(indexDirectory!!)
            val count = indexReader.numDocs().toLong()
            indexReader.close()
            count
        } catch (e: Exception) {
            log.error("获取索引文档总数失败", e)
            0L
        }
    }

    /**
     * 删除旧索引目录
     */
    private fun deleteIndexDirectory() {
        try {
            val indexDir = File(indexPath)
            if (indexDir.exists()) {
                log.info("删除现有索引目录: $indexPath")
                Files.walk(Paths.get(indexPath))
                    .sorted(Comparator.reverseOrder())
                    .forEach { path ->
                        try {
                            Files.delete(path)
                        } catch (e: IOException) {
                            log.warn("删除文件失败: $path", e)
                        }
                    }
            }
        } catch (e: Exception) {
            log.warn("删除索引目录失败: $indexPath", e)
        }
    }

    /**
     * 创建索引写入器
     */
    private fun createIndexWriter() {
        try {
            // 确保索引目录存在
            val indexDir = Paths.get(indexPath)
            if (!Files.exists(indexDir)) {
                Files.createDirectories(indexDir)
                log.info("创建索引目录: $indexPath")
            }

            // 创建索引目录
            indexDirectory = FSDirectory.open(indexDir)

            // 在创建 IndexWriterConfig 时自定义相似度
//            val similarity = BM25Similarity(1.2f, 0.75f) // k1, b 参数
//            indexConfig.similarity = similarity

            // 配置索引写入器
            val indexConfig = IndexWriterConfig(analyzer)
            indexConfig.openMode = IndexWriterConfig.OpenMode.CREATE


            // 创建索引写入器
            indexWriter = IndexWriter(indexDirectory!!, indexConfig)
            log.info("索引写入器创建成功，索引路径: $indexPath")

        } catch (e: Exception) {
            log.error("创建索引写入器失败", e)
            throw RuntimeException("创建索引写入器失败: ${e.message}", e)
        }
    }

    /**
     * 从数据库构建索引
     */
    private fun buildIndexFromDatabase(): Int {
        log.info("开始从数据库读取文档数据")
        val documentCount = AtomicInteger(0)

        try {
            // 分页查询避免内存溢出
            val pageSize = 1000
            var offset = 0
            var hasMoreData = true

            while (hasMoreData) {
                val sql = """
                    SELECT id, content, metadata->>'groupId' AS groupId, metadata->>'fileId' AS fileId
                    FROM vector_store_rag
                    WHERE content IS NOT NULL
                    AND TRIM(content) != ''
                    ORDER BY id
                    LIMIT ? OFFSET ?
                """.trimIndent()

                val documents = jdbcTemplate.query(sql, arrayOf(pageSize, offset)) { rs, _ ->
                    val id = rs.getString("id")
                    val groupId = rs.getString("groupId")
                    val fileId = rs.getString("fileId")
                    val content = rs.getString("content")
                    DocumentData(id, groupId, fileId, content)
                }

                if (documents.isEmpty()) {
                    hasMoreData = false
                } else {
                    // 批量添加文档到索引
                    documents.forEach { doc ->
                        addDocumentToIndex(doc)
                        documentCount.incrementAndGet()
                    }

                    offset += pageSize
                    log.debug("已处理 {} 个文档", documentCount.get())
                }
            }

            log.info("从数据库读取文档完成，共 {} 个有效文档", documentCount.get())
            return documentCount.get()

        } catch (e: Exception) {
            log.error("从数据库构建索引失败", e)
            throw RuntimeException("从数据库构建索引失败: ${e.message}", e)
        }
    }

    /**
     * 添加单个文档到索引
     */
    private fun addDocumentToIndex(doc: DocumentData) {
        try {
            val document = Document()

            // 添加 ID 字段（存储但不分词）
            val idField = StringField("id", doc.id, Field.Store.YES)
            document.add(idField)


            // 添加分组id字段（存储但不分词）
            val groupIdField: Field = StringField("groupId", doc.groupId, Field.Store.YES)
            document.add(groupIdField)


            // 添加文件id字段（存储但不分词）
            val fileIdField: Field = StringField("fileId", doc.fileId, Field.Store.YES)
            document.add(fileIdField)


            // 添加内容字段（分词但不存储）
            val contentField = TextField("content", doc.content, Field.Store.NO)
            document.add(contentField)

            // 将文档添加到索引
            indexWriter!!.addDocument(document)

        } catch (e: Exception) {
            log.error("添加文档到索引失败: id={}", doc.id, e)
        }
    }

    /**
     * 提交并关闭索引写入器
     */
    private fun commitAndCloseWriter() {
        try {
            indexWriter?.let { writer ->
                writer.commit()
                log.info("索引提交成功")
            }
        } catch (e: Exception) {
            log.error("索引提交失败", e)
        } finally {
            closeIndexResources()
        }
    }

    /**
     * 关闭索引资源
     */
    private fun closeIndexResources() {
        try {
            indexWriter?.close()
            log.debug("索引写入器已关闭")
        } catch (e: Exception) {
            log.warn("关闭索引写入器失败", e)
        } finally {
            indexWriter = null
        }

        try {
            indexDirectory?.close()
            log.debug("索引目录已关闭")
        } catch (e: Exception) {
            log.warn("关闭索引目录失败", e)
        } finally {
            indexDirectory = null
        }
    }

    /**
     * 确保索引读取器可用
     */
    private fun ensureIndexReaderAvailable() {
        if (indexDirectory == null) {
            try {
                // 确保索引目录存在
                val indexDir = Paths.get(indexPath)
                if (!Files.exists(indexDir)) {
                    Files.createDirectories(indexDir)
                    log.info("创建索引目录: $indexPath")
                }

                indexDirectory = FSDirectory.open(indexDir)
            } catch (e: Exception) {
                throw RuntimeException("无法打开索引目录: $indexPath", e)
            }
        }
    }

    /**
     * 文档数据类
     */
    private data class DocumentData(
        val id: String,
        val groupId: String,
        val fileId: String,
        val content: String
    )
}