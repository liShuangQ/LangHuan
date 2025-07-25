package com.langhuan.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Dao实现
 * @createDate 2025-01-17 15:08:49
 */
@Slf4j
@Repository
public class TRagFileGroupDao {

    private final JdbcPaginationHelper paginationHelper;
    private final JdbcTemplate jdbcTemplate;

    public TRagFileGroupDao(JdbcPaginationHelper paginationHelper, JdbcTemplate jdbcTemplate) {
        this.paginationHelper = paginationHelper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 管理员获取所有文件组枚举列表
     * 
     * @return 文件组枚举列表
     */
    public List<Map<String, Object>> getEnumForAdmin() {
        log.info("getEnumForAdmin");
        String sql = """
                SELECT
                    fg.id,
                    fg.group_name as "groupName"
                FROM t_rag_file_group fg
                LEFT JOIN t_user u ON fg.created_by = u.username
                WHERE 1=1
                """;

        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);

        return queryForList.stream()
                .map(e -> Map.of("id", (Object) e.get("id"), "groupName", (Object) e.get("groupName")))
                .collect(Collectors.toList());
    }

    /**
     * 普通用户根据权限获取文件组枚举列表
     * 
     * @param isRead      是否为读取权限
     * @param currentUser 当前用户
     * @return 文件组枚举列表
     */
    public List<Map<String, Object>> getEnumForUser(Boolean isRead, String currentUser) {
        log.info("getEnumForUser: isRead={}, currentUser={}", isRead, currentUser);
        StringBuilder dataSql = new StringBuilder();
        java.util.List<Object> dataParams = new java.util.ArrayList<>();

        // 普通用户查询文件组（公开的 + 自己创建的 + 被分享的）
        dataSql.append(
                """
                        SELECT DISTINCT
                            fg.id,
                            fg.group_name as "groupName"
                        FROM t_rag_file_group fg
                        LEFT JOIN t_user u ON fg.created_by = u.username
                        LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ?
                        """);
        if (isRead) {
            dataSql.append("AND fgs.can_read = TRUE");
        }
        if (!isRead) {
            dataSql.append("AND fgs.can_update = TRUE OR fgs.can_add = TRUE");
        }

        dataSql.append("""
                                          WHERE
                            fg.visibility = 'public'  -- 公开的文件组
                            OR fg.created_by = ?      -- 自己创建的文件组
                            OR fgs.id IS NOT NULL     -- 被分享的文件组
                """);

        // 添加参数（按顺序）
        dataParams.add(currentUser);
        dataParams.add(currentUser);

        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(dataSql.toString(), dataParams.toArray());

        return queryForList.stream()
                .map(e -> Map.of("id", (Object) e.get("id"), "groupName", (Object) e.get("groupName")))
                .collect(Collectors.toList());
    }

    /**
     * 管理员查询所有文件组分页数据
     * 
     * @param groupName  文件组名称
     * @param groupType  文件组类型
     * @param visibility 可见性
     * @param pageNum    页码
     * @param pageSize   页大小
     * @return 分页查询结果
     */
    public IPage<Map<String, Object>> queryFileGroupsForAdmin(String groupName, String groupType, String visibility,
            int pageNum, int pageSize) {
        log.info("queryFileGroupsForAdmin: groupName={}, groupType={}, visibility={}, pageNum={}, pageSize={}",
                groupName, groupType, visibility, pageNum, pageSize);
        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();
        java.util.List<Object> dataParams = new java.util.ArrayList<>();
        java.util.List<Object> countParams = new java.util.ArrayList<>();

        // 超级管理员查询所有文件组
        dataSql.append("""
                SELECT
                    fg.id,
                    fg.group_name as "groupName",
                    fg.group_type as "groupType",
                    fg.group_desc as "groupDesc",
                    fg.visibility,
                    fg.created_by as "createdBy",
                    fg.created_at as "createdAt",
                    u.name as "userName"
                FROM t_rag_file_group fg
                LEFT JOIN t_user u ON fg.created_by = u.username
                WHERE 1=1
                """);

        countSql.append("""
                SELECT COUNT(*)
                FROM t_rag_file_group fg
                WHERE 1=1
                """);

        // 构建WHERE条件
        if (groupName != null && !groupName.trim().isEmpty()) {
            dataSql.append(" AND fg.group_name LIKE ?");
            countSql.append(" AND fg.group_name LIKE ?");
            dataParams.add("%" + groupName + "%");
            countParams.add("%" + groupName + "%");
        }

        if (groupType != null && !groupType.trim().isEmpty()) {
            dataSql.append(" AND fg.group_type LIKE ?");
            countSql.append(" AND fg.group_type LIKE ?");
            dataParams.add("%" + groupType + "%");
            countParams.add("%" + groupType + "%");
        }

        if (visibility != null && !visibility.trim().isEmpty()) {
            dataSql.append(" AND fg.visibility = ?");
            countSql.append(" AND fg.visibility = ?");
            dataParams.add(visibility);
            countParams.add(visibility);
        }

        dataSql.append(" ORDER BY fg.created_at DESC");

        // 执行分页查询
        return paginationHelper.selectPageForMapWithDifferentParams(
                dataSql.toString(), countSql.toString(),
                dataParams.toArray(), countParams.toArray(),
                pageNum, pageSize);
    }

    /**
     * 普通用户查询文件组分页数据（公开的 + 自己创建的 + 被分享的）
     * 
     * @param groupName   文件组名称
     * @param groupType   文件组类型
     * @param visibility  可见性
     * @param pageNum     页码
     * @param pageSize    页大小
     * @param currentUser 当前用户
     * @return 分页查询结果
     */
    public IPage<Map<String, Object>> queryFileGroupsForUser(String groupName, String groupType, String visibility,
            int pageNum, int pageSize, String currentUser) {
        log.info(
                "queryFileGroupsForUser: groupName={}, groupType={}, visibility={}, pageNum={}, pageSize={}, currentUser={}",
                groupName, groupType, visibility, pageNum, pageSize, currentUser);
        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();
        java.util.List<Object> dataParams = new java.util.ArrayList<>();
        java.util.List<Object> countParams = new java.util.ArrayList<>();

        // 普通用户查询文件组（公开的 + 自己创建的 + 被分享的）
        dataSql.append(
                """
                        SELECT DISTINCT
                            fg.id,
                            fg.group_name as "groupName",
                            fg.group_type as "groupType",
                            fg.group_desc as "groupDesc",
                            fg.visibility,
                            fg.created_by as "createdBy",
                            fg.created_at as "createdAt",
                            u.name as "userName"
                        FROM t_rag_file_group fg
                        LEFT JOIN t_user u ON fg.created_by = u.username
                        LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ? AND fgs.can_read = TRUE
                        WHERE
                            fg.visibility = 'public'  -- 公开的文件组
                            OR fg.created_by = ?      -- 自己创建的文件组
                            OR fgs.id IS NOT NULL     -- 被分享的文件组
                        """);

        countSql.append(
                """
                        SELECT COUNT(DISTINCT fg.id)
                        FROM t_rag_file_group fg
                        LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ? AND fgs.can_read = TRUE
                        WHERE
                            fg.visibility = 'public'  -- 公开的文件组
                            OR fg.created_by = ?      -- 自己创建的文件组
                            OR fgs.id IS NOT NULL     -- 被分享的文件组
                        """);

        // 添加基础参数（按顺序）
        dataParams.add(currentUser); // LEFT JOIN中的shared_with
        dataParams.add(currentUser); // WHERE中的created_by

        // count查询的参数
        countParams.add(currentUser); // LEFT JOIN中的shared_with
        countParams.add(currentUser); // WHERE中的created_by

        // 构建额外的WHERE条件
        if (groupName != null && !groupName.trim().isEmpty()) {
            dataSql.append(" AND fg.group_name LIKE ?");
            countSql.append(" AND fg.group_name LIKE ?");
            dataParams.add("%" + groupName + "%");
            countParams.add("%" + groupName + "%");
        }

        if (groupType != null && !groupType.trim().isEmpty()) {
            dataSql.append(" AND fg.group_type LIKE ?");
            countSql.append(" AND fg.group_type LIKE ?");
            dataParams.add("%" + groupType + "%");
            countParams.add("%" + groupType + "%");
        }

        if (visibility != null && !visibility.trim().isEmpty()) {
            dataSql.append(" AND fg.visibility = ?");
            countSql.append(" AND fg.visibility = ?");
            dataParams.add(visibility);
            countParams.add(visibility);
        }

        dataSql.append(" ORDER BY fg.created_at DESC");

        // 执行分页查询
        return paginationHelper.selectPageForMapWithDifferentParams(
                dataSql.toString(), countSql.toString(),
                dataParams.toArray(), countParams.toArray(),
                pageNum, pageSize);
    }
}
