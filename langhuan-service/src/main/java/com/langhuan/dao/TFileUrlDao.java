package com.langhuan.dao;

import com.langhuan.model.domain.TFileUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文件URL数据访问层
 * 处理与 t_file_url 表相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
@Slf4j
public class TFileUrlDao {

    private final JdbcTemplate jdbcTemplate;

    public TFileUrlDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据文件ID查询文件URL列表
     *
     * @param fileId 文件ID
     * @return 文件URL列表
     */
    public List<TFileUrl> selectByFileId(Integer fileId) {
        String sql = "SELECT id, f_url FROM t_file_url WHERE file_id = ?";
        return jdbcTemplate.queryForList(sql, new Object[] { fileId }, TFileUrl.class);
    }

    /**
     * 根据文件ID删除文件URL记录
     *
     * @param fileId 文件ID
     * @return 影响行数
     */
    public int deleteByFileId(Integer fileId) {
        String sql = "DELETE FROM t_file_url WHERE file_id = ?";
        return jdbcTemplate.update(sql, fileId);
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
    public int updateFileIdAndStatus(Integer newFileId, String newStatus, Integer oldFileId, String oldStatus) {
        String sql = """
                          UPDATE t_file_url
                          SET file_id = ?, f_status = ?
                          WHERE file_id = ? AND f_status = ?
                """;

        return jdbcTemplate.update(sql,
                newFileId, // 新的 file_id
                newStatus, // 新状态
                oldFileId, // 旧的临时 file_id
                oldStatus // 原状态
        );
    }
}