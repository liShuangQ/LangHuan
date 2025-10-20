package com.langhuan.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 向量存储数据访问层
 * 处理与 vector_store_rag 表相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
@Slf4j
public class VectorStoreRagDao {

    private final JdbcTemplate jdbcTemplate;

    public VectorStoreRagDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据文档ID删除向量存储记录
     *
     * @param documentId 文档ID
     * @return 影响行数
     */
    public int deleteByDocumentId(String documentId) {
        String sql = "DELETE FROM vector_store_rag WHERE id = ?::uuid";
        return jdbcTemplate.update(sql, documentId);
    }

    /**
     * 根据文档ID查询向量存储记录
     *
     * @param documentId 文档ID
     * @return 查询结果列表
     */
    public List<Map<String, Object>> selectByDocumentId(String documentId) {
        String sql = "SELECT * FROM vector_store_rag WHERE id = ?::uuid";
        return jdbcTemplate.queryForList(sql, documentId);
    }

    /**
     * 根据文件ID删除向量存储记录
     *
     * @param fileId 文件ID
     * @return 影响行数
     */
    public int deleteByFileId(String fileId) {
        String sql = """
                        DELETE FROM vector_store_rag
                        WHERE metadata ->> 'fileId' = ?;
                """;
        return jdbcTemplate.update(sql, fileId);
    }

    /**
     * 根据文件ID更新组ID
     *
     * @param groupId 组ID
     * @param fileId  文件ID
     * @return 影响行数
     */
    public int updateGroupIdByFileId(String groupId, String fileId) {
        String sql = """
                        UPDATE vector_store_rag
                        SET metadata = jsonb_set(metadata::jsonb, '{groupId}', to_jsonb(?))
                        WHERE metadata ->> 'fileId' = ?;
                """;
        return jdbcTemplate.update(sql, groupId, fileId);
    }

    /**
     * 根据文档ID更新排名
     *
     * @param documentId 文档ID
     * @param rank       排名
     * @return 影响行数
     */
    public int updateRankByDocumentId(String documentId, Integer rank) {
        String sql = """
                        UPDATE vector_store_rag
                        SET metadata = jsonb_set(metadata::jsonb, '{rank}', to_jsonb(?))
                        WHERE id = ?::uuid;
                """;
        return jdbcTemplate.update(sql, rank, documentId);
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
    public List<Map<String, Object>> selectByFileIdAndContent(String fileId, String content, int pageSize, int offset) {
        String sql = """
                        SELECT * FROM vector_store_rag WHERE metadata ->> 'fileId' = ? AND content LIKE ?
                        LIMIT ? OFFSET ?;
                """;
        return jdbcTemplate.queryForList(sql, fileId, "%" + content + "%", pageSize, offset);
    }

    /**
     * 根据文件ID和内容统计向量存储记录数
     *
     * @param fileId  文件ID
     * @param content 内容
     * @return 记录数
     */
    public Integer countByFileIdAndContent(String fileId, String content) {
        String sql = """
                        SELECT COUNT(*) FROM vector_store_rag WHERE metadata ->> 'fileId' = ? AND content LIKE ?;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, fileId, "%" + content + "%");
    }

    /**
     * 根据文档ID列表查询向量存储记录
     *
     * @param documentIds 文档ID列表
     * @return 查询结果列表
     */
    public List<Map<String, Object>> selectByIds(List<String> documentIds) {
        if (documentIds.isEmpty()) {
            return List.of();
        }

        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT vs.id,vs.content,fg.group_name AS groupName,vs.metadata->>'filename' AS filename FROM vector_store_rag vs LEFT JOIN t_rag_file_group fg ON vs.metadata->>'groupId' = CAST(fg.id AS TEXT) WHERE vs.id IN (");
        for (int i = 0; i < documentIds.size(); i++) {
            sqlBuilder.append("?::uuid");
            if (i < documentIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(");");
        String sql = sqlBuilder.toString();

        return jdbcTemplate.queryForList(sql, documentIds.toArray());
    }
}