package com.langhuan.dao

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * 向量存储数据访问层
 * 处理与 vector_store_rag 表相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
class VectorStoreRagDao(
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(VectorStoreRagDao::class.java)
    }

    /**
     * 根据文档ID删除向量存储记录
     *
     * @param documentId 文档ID
     * @return 影响行数
     */
    fun deleteByDocumentId(documentId: String): Int {
        val sql = "DELETE FROM vector_store_rag WHERE id = ?::uuid"
        return jdbcTemplate.update(sql, documentId)
    }

    /**
     * 根据文档ID查询向量存储记录
     *
     * @param documentId 文档ID
     * @return 查询结果列表
     */
    fun selectByDocumentId(documentId: String): List<Map<String, Any>> {
        val sql = "SELECT * FROM vector_store_rag WHERE id = ?::uuid"
        return jdbcTemplate.queryForList(sql, documentId)
    }

    /**
     * 根据文件ID删除向量存储记录
     *
     * @param fileId 文件ID
     * @return 影响行数
     */
    fun deleteByFileId(fileId: String): Int {
        val sql = """
            DELETE FROM vector_store_rag
            WHERE metadata ->> 'fileId' = ?;
        """.trimIndent()
        return jdbcTemplate.update(sql, fileId)
    }

    /**
     * 根据文件ID更新组ID
     *
     * @param groupId 组ID
     * @param fileId  文件ID
     * @return 影响行数
     */
    fun updateGroupIdByFileId(groupId: String, fileId: String): Int {
        val sql = """
            UPDATE vector_store_rag
            SET metadata = jsonb_set(metadata::jsonb, '{groupId}', to_jsonb(?))
            WHERE metadata ->> 'fileId' = ?;
        """.trimIndent()
        return jdbcTemplate.update(sql, groupId, fileId)
    }

    /**
     * 根据文档ID更新排名
     *
     * @param documentId 文档ID
     * @param rank       排名
     * @return 影响行数
     */
    fun updateRankByDocumentId(documentId: String, rank: Int): Int {
        val sql = """
            UPDATE vector_store_rag
            SET metadata = jsonb_set(metadata::jsonb, '{rank}', to_jsonb(?))
            WHERE id = ?::uuid;
        """.trimIndent()
        return jdbcTemplate.update(sql, rank, documentId)
    }

    /**
     * 根据文件ID和内容查询向量存储记录
     *
     * @param fileId    文件ID
     * @param content   内容
     * @param pageSize  每页大小
     * @param offset    偏移量
     * @return 查询结果列表
     */
    fun selectByFileIdAndContent(fileId: String, content: String, pageSize: Int, offset: Int): List<Map<String, Any>> {
        val sql = """
            SELECT * FROM vector_store_rag WHERE metadata ->> 'fileId' = ? AND content LIKE ?
            LIMIT ? OFFSET ?;
        """.trimIndent()
        return jdbcTemplate.queryForList(sql, fileId, "%$content%", pageSize, offset)
    }

    /**
     * 根据文件ID和内容统计向量存储记录数
     *
     * @param fileId  文件ID
     * @param content 内容
     * @return 记录数
     */
    fun countByFileIdAndContent(fileId: String, content: String): Int {
        val sql = """
            SELECT COUNT(*) FROM vector_store_rag WHERE metadata ->> 'fileId' = ? AND content LIKE ?;
        """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Int::class.java, fileId, "%$content%") ?: 0
    }

    /**
     * 根据文档ID列表查询向量存储记录
     *
     * @param documentIds 文档ID列表
     * @return 查询结果列表
     */
    fun selectByIds(documentIds: List<String>): List<Map<String, Any>> {
        if (documentIds.isEmpty()) {
            return emptyList()
        }

        val sqlBuilder = StringBuilder(
            "SELECT vs.id,vs.content,fg.group_name AS groupName,vs.metadata->>'filename' AS filename FROM vector_store_rag vs LEFT JOIN t_rag_file_group fg ON vs.metadata->>'groupId' = CAST(fg.id AS TEXT) WHERE vs.id IN ("
        )
        for (i in documentIds.indices) {
            sqlBuilder.append("?::uuid")
            if (i < documentIds.size - 1) {
                sqlBuilder.append(",")
            }
        }
        sqlBuilder.append(");")
        val sql = sqlBuilder.toString()

        return jdbcTemplate.queryForList(sql, *documentIds.toTypedArray())
    }
}
