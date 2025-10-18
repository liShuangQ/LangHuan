package com.langhuan.dao

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group_share】的数据库操作Dao实现
 * @createDate 2025-07-22 11:35:31
 */
@Repository
class TRagFileGroupShareDao(
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(TRagFileGroupShareDao::class.java)
    }

    /**
     * 获取文件组的分享列表（使用JdbcTemplate联表查询用户名）
     * 
     * @param fileGroupId 文件组ID
     * @param sharedWith  被分享的用户名
     * @return 分享列表（包含用户名信息）
     */
    fun getFileGroupShares(fileGroupId: Int?, sharedWith: String?): List<Map<String, Any>> {
        log.info("Getting file group shares: fileGroupId={}, sharedWith={}", fileGroupId, sharedWith)
        
        val sql = StringBuilder()
        val params = ArrayList<Any>()
        
        sql.append("""
            SELECT 
                fgs.id,
                fgs.file_group_id as "fileGroupId",
                fgs.shared_with as "sharedWith",
                fgs.can_read as "canRead",
                fgs.can_add as "canAdd",
                fgs.can_update as "canUpdate",
                fgs.can_delete as "canDelete",
                fgs.shared_by as "sharedBy",
                fgs.shared_at as "sharedAt",
                u1.name as "sharedWithUserName",
                u2.name as "sharedByUserName"
            FROM t_rag_file_group_share fgs
            LEFT JOIN t_user u1 ON fgs.shared_with = u1.username
            LEFT JOIN t_user u2 ON fgs.shared_by = u2.username
            WHERE 1=1
            """.trimIndent())
        
        // 动态添加查询条件
        if (fileGroupId != null) {
            sql.append(" AND fgs.file_group_id = ?")
            params.add(fileGroupId)
        }
        
        if (!sharedWith.isNullOrBlank()) {
            sql.append(" AND fgs.shared_with = ?")
            params.add(sharedWith)
        }
        
        sql.append(" ORDER BY fgs.shared_at DESC")
        
        return jdbcTemplate.queryForList(sql.toString(), params.toArray())
    }
}
