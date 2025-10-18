package com.langhuan.dao

import com.langhuan.model.domain.TFileUrl
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * 文件URL数据访问层
 * 处理与 t_file_url 表相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
class TFileUrlDao(
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(TFileUrlDao::class.java)
    }

    /**
     * 根据文件ID查询文件URL列表
     *
     * @param fileId 文件ID
     * @return 文件URL列表
     */
    fun selectByFileId(fileId: Int): List<TFileUrl> {
        val sql = "SELECT id, file_id, f_url, f_status FROM t_file_url WHERE file_id = ?"
        return jdbcTemplate.query(sql, { rs, _ ->
            TFileUrl(
                id = rs.getInt("id"),
                fileId = rs.getInt("file_id"),
                fUrl = rs.getString("f_url"),
                fStatus = rs.getString("f_status")
            )
        }, fileId)
    }

    /**
     * 根据文件ID删除文件URL记录
     *
     * @param fileId 文件ID
     * @return 影响行数
     */
    fun deleteByFileId(fileId: Int): Int {
        val sql = "DELETE FROM t_file_url WHERE file_id = ?"
        return jdbcTemplate.update(sql, fileId)
    }

    /**
     * 批量更新文件URL状态
     *
     * @param newFileId    新文件ID
     * @param newStatus    新状态
     * @param oldFileId    旧文件ID
     * @param oldStatus    旧状态
     * @return 影响行数
     */
    fun updateFileIdAndStatus(newFileId: Int, newStatus: String, oldFileId: Int, oldStatus: String): Int {
        val sql = """
            UPDATE t_file_url
            SET file_id = ?, f_status = ?
            WHERE file_id = ? AND f_status = ?
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            newFileId, // 新的 file_id
            newStatus, // 新状态
            oldFileId, // 旧的临时 file_id
            oldStatus // 原状态
        )
    }
}
