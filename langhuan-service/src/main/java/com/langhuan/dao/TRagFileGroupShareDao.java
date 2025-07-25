package com.langhuan.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group_share】的数据库操作Dao实现
 * @createDate 2025-07-22 11:35:31
 */
@Slf4j
@Repository
public class TRagFileGroupShareDao {

    private final JdbcTemplate jdbcTemplate;

    public TRagFileGroupShareDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取文件组的分享列表（使用JdbcTemplate联表查询用户名）
     * 
     * @param fileGroupId 文件组ID
     * @param sharedWith  被分享的用户名
     * @return 分享列表（包含用户名信息）
     */
    public List<Map<String, Object>> getFileGroupShares(Integer fileGroupId, String sharedWith) {
        log.info("Getting file group shares: fileGroupId={}, sharedWith={}", fileGroupId, sharedWith);
        
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
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
                """);
        
        // 动态添加查询条件
        if (fileGroupId != null) {
            sql.append(" AND fgs.file_group_id = ?");
            params.add(fileGroupId);
        }
        
        if (sharedWith != null && !sharedWith.trim().isEmpty()) {
            sql.append(" AND fgs.shared_with = ?");
            params.add(sharedWith);
        }
        
        sql.append(" ORDER BY fgs.shared_at DESC");
        
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

}
