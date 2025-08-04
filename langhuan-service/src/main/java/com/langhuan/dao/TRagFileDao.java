package com.langhuan.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file】的数据库操作DAO
 * @createDate 2025-03-15 13:41:30
 */
@Repository
@Slf4j
public class TRagFileDao {
    
    private final JdbcTemplate jdbcTemplate;
    private final JdbcPaginationHelper paginationHelper;
    
    public TRagFileDao(JdbcTemplate jdbcTemplate, JdbcPaginationHelper paginationHelper) {
        this.jdbcTemplate = jdbcTemplate;
        this.paginationHelper = paginationHelper;
    }
    
    /**
     * 管理员查询文件列表（无权限限制）
     * 
     * @param fileName      文件名（模糊查询）
     * @param fileType      文件类型（模糊查询）
     * @param fileGroupName 文件组名（模糊查询）
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @return 分页结果
     */
    public IPage<Map<String, Object>> queryFilesForAdmin(String fileName, String fileType, String fileGroupName, 
                                                          int pageNum, int pageSize) {
        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();
        List<Object> dataParams = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();
        
        // 超级管理员查询所有文件
        dataSql.append("""
                SELECT
                    f.id,
                    f.file_name,
                    f.file_type,
                    f.file_size,
                    f.document_num,
                    f.file_desc,
                    f.file_group_id,
                    f.uploaded_by,
                    f.uploaded_at,
                    fg.group_name,
                    fg.visibility as group_visibility,
                    u.name as user_name,
                    'admin' as permission_type,
                    TRUE as can_read,
                    TRUE as can_add,
                    TRUE as can_update,
                    TRUE as can_delete
                FROM t_rag_file f
                INNER JOIN t_rag_file_group fg ON f.file_group_id = fg.id::VARCHAR
                LEFT JOIN t_user u ON f.uploaded_by = u.username
                WHERE 1=1
                """);

        countSql.append("""
                SELECT COUNT(*)
                FROM t_rag_file f
                INNER JOIN t_rag_file_group fg ON f.file_group_id = fg.id::VARCHAR
                WHERE 1=1
                """);
        
        // 添加动态查询条件
        addQueryConditions(dataSql, countSql, dataParams, countParams, fileName, fileType, fileGroupName);
        
        dataSql.append(" ORDER BY f.uploaded_at DESC");
        
        return paginationHelper.selectPageForMapWithDifferentParams(
                dataSql.toString(), countSql.toString(),
                dataParams.toArray(), countParams.toArray(),
                pageNum, pageSize);
    }
    
    /**
     * 普通用户查询文件列表（带权限控制）
     * 
     * @param currentUser   当前用户
     * @param fileName      文件名（模糊查询）
     * @param fileType      文件类型（模糊查询）
     * @param fileGroupName 文件组名（模糊查询）
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @return 分页结果
     */
    public IPage<Map<String, Object>> queryFilesForUser(String currentUser, String fileName, String fileType, 
                                                         String fileGroupName, int pageNum, int pageSize) {
        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();
        List<Object> dataParams = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();
        
        // 普通用户查询文件（根据文件组权限）
        dataSql.append("""
                SELECT DISTINCT
                    f.id,
                    f.file_name,
                    f.file_type,
                    f.file_size,
                    f.document_num,
                    f.file_desc,
                    f.file_group_id,
                    f.uploaded_by,
                    f.uploaded_at,
                    fg.group_name,
                    fg.visibility as group_visibility,
                    u.name as user_name,
                    CASE
                        WHEN fg.created_by = ? THEN 'group_owner'
                        WHEN fg.visibility = 'public' THEN 'public'
                        WHEN fgs.id IS NOT NULL THEN 'shared'
                        ELSE 'none'
                    END as permission_type,
                    CASE
                        WHEN fg.created_by = ? THEN TRUE
                        WHEN fg.visibility = 'public' THEN TRUE
                        WHEN fgs.can_read = TRUE THEN TRUE
                        ELSE FALSE
                    END as can_read,
                    CASE
                        WHEN fg.created_by = ? THEN TRUE
                        WHEN fg.visibility = 'public' AND fg.created_by = ? THEN TRUE
                        WHEN fgs.can_add = TRUE THEN TRUE
                        ELSE FALSE
                    END as can_add,
                    CASE
                        WHEN fg.created_by = ? THEN TRUE
                        WHEN fg.visibility = 'public' AND fg.created_by = ? THEN TRUE
                        WHEN fgs.can_update = TRUE THEN TRUE
                        ELSE FALSE
                    END as can_update,
                    CASE
                        WHEN fg.created_by = ? THEN TRUE
                        WHEN fg.visibility = 'public' AND fg.created_by = ? THEN TRUE
                        WHEN fgs.can_delete = TRUE THEN TRUE
                        ELSE FALSE
                    END as can_delete
                FROM t_rag_file f
                INNER JOIN t_rag_file_group fg ON f.file_group_id = fg.id::VARCHAR
                LEFT JOIN t_user u ON f.uploaded_by = u.username
                LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ?
                WHERE
                    fg.visibility = 'public'  -- 公开文件组的文件
                    OR fg.created_by = ?      -- 自己创建的文件组的文件
                    OR fgs.id IS NOT NULL     -- 被分享文件组的文件
                """);

        countSql.append("""
                SELECT COUNT(DISTINCT f.id)
                FROM t_rag_file f
                INNER JOIN t_rag_file_group fg ON f.file_group_id = fg.id::VARCHAR
                LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ?
                WHERE
                    fg.visibility = 'public'  -- 公开文件组的文件
                    OR fg.created_by = ?      -- 自己创建的文件组的文件
                    OR fgs.id IS NOT NULL     -- 被分享文件组的文件
                """);
        
        // 添加CASE语句中的参数（按顺序）
        dataParams.add(currentUser); // permission_type CASE中的owner判断
        dataParams.add(currentUser); // can_read CASE中的owner判断
        dataParams.add(currentUser); // can_add CASE中的owner判断
        dataParams.add(currentUser); // can_add CASE中的public创建者判断
        dataParams.add(currentUser); // can_update CASE中的owner判断
        dataParams.add(currentUser); // can_update CASE中的public创建者判断
        dataParams.add(currentUser); // can_delete CASE中的owner判断
        dataParams.add(currentUser); // can_delete CASE中的public创建者判断
        dataParams.add(currentUser); // LEFT JOIN中的shared_with
        dataParams.add(currentUser); // WHERE中的created_by

        // count查询的参数
        countParams.add(currentUser); // LEFT JOIN中的shared_with
        countParams.add(currentUser); // WHERE中的created_by
        
        // 添加动态查询条件
        addQueryConditions(dataSql, countSql, dataParams, countParams, fileName, fileType, fileGroupName);
        
        dataSql.append(" ORDER BY f.uploaded_at DESC");
        
        return paginationHelper.selectPageForMapWithDifferentParams(
                dataSql.toString(), countSql.toString(),
                dataParams.toArray(), countParams.toArray(),
                pageNum, pageSize);
    }
    
    /**
     * 根据文件ID查询向量存储内容
     * 
     * @param fileId 文件ID
     * @return 内容列表
     */
    public List<Map<String, Object>> queryVectorContentByFileId(Integer fileId) {
        String sql = "SELECT content FROM vector_store_rag WHERE metadata ->> 'fileId' = ? ;";
        return jdbcTemplate.queryForList(sql, fileId.toString());
    }

    /**
     * 查询所有有效的 fileId（用于导出）
     */
    public List<Integer> queryAllFileIds() {
        String sql = "SELECT DISTINCT CAST(metadata ->> 'fileId' AS INTEGER) AS file_id " +
                "FROM vector_store_rag " +
                "WHERE metadata ->> 'fileId' IS NOT NULL";
        return jdbcTemplate.queryForList(sql, Integer.class);
    }
    
    /**
     * 添加动态查询条件
     * 
     * @param dataSql       数据查询SQL
     * @param countSql      计数查询SQL
     * @param dataParams    数据查询参数
     * @param countParams   计数查询参数
     * @param fileName      文件名
     * @param fileType      文件类型
     * @param fileGroupName 文件组名
     */
    private void addQueryConditions(StringBuilder dataSql, StringBuilder countSql, 
                                   List<Object> dataParams, List<Object> countParams,
                                   String fileName, String fileType, String fileGroupName) {
        if (StringUtils.hasText(fileName)) {
            dataSql.append(" AND f.file_name LIKE ?");
            countSql.append(" AND f.file_name LIKE ?");
            dataParams.add("%" + fileName + "%");
            countParams.add("%" + fileName + "%");
        }

        if (StringUtils.hasText(fileType)) {
            dataSql.append(" AND f.file_type LIKE ?");
            countSql.append(" AND f.file_type LIKE ?");
            dataParams.add("%" + fileType + "%");
            countParams.add("%" + fileType + "%");
        }

        if (StringUtils.hasText(fileGroupName)) {
            dataSql.append(" AND fg.group_name LIKE ?");
            countSql.append(" AND fg.group_name LIKE ?");
            dataParams.add("%" + fileGroupName + "%");
            countParams.add("%" + fileGroupName + "%");
        }
    }
}
