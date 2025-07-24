package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileGroupMapper;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import com.langhuan.utils.other.SecurityUtils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Service实现
 * @createDate 2025-01-17 15:08:49
 */
@Slf4j
@Service
public class TRagFileGroupService extends ServiceImpl<TRagFileGroupMapper, TRagFileGroup> {

    private final JdbcPaginationHelper paginationHelper;
    private final JdbcTemplate jdbcTemplate;

    public TRagFileGroupService(JdbcPaginationHelper paginationHelper, JdbcTemplate jdbcTemplate) {
        this.paginationHelper = paginationHelper;
        this.jdbcTemplate = jdbcTemplate;
    }

    // HACK 和下面的查询的sql相关
    public List<Map<String, Object>> getEnum(Boolean isRead) {
        boolean isAdmin = SecurityUtils.hasAdminRole();
        String currentUser = SecurityUtils.getCurrentUsername();
        StringBuilder dataSql = new StringBuilder();
        java.util.List<Object> dataParams = new java.util.ArrayList<>();
        if (isAdmin) {
            // 超级管理员查询所有文件组
            dataSql.append("""
                    SELECT
                        fg.id,
                        fg.group_name as "groupName"
                    FROM t_rag_file_group fg
                    LEFT JOIN t_user u ON fg.created_by = u.username
                    WHERE 1=1
                    """);
        } else {
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

            // 添加CASE语句中的参数（按顺序）
            dataParams.add(currentUser);
            dataParams.add(currentUser);
        }

        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(dataSql.toString(), dataParams.toArray());

        queryForList.stream()
                .map(e -> Map.of("id", (Object) e.get("id"), "groupName", (Object) e.get("groupName")))
                .collect(Collectors.toList());
        return queryForList;
    }

    /**
     * 根据权限查询文件组
     * 超级管理员可以查询所有文件组
     * 普通用户只能查询公开的、自己创建的和被分享的文件组
     */
    public IPage<Map<String, Object>> queryFileGroups(String groupName, String groupType, String visibility,
            int pageNum, int pageSize) {
        log.info("queryFileGroups: groupName='{}', groupType='{}', visibility='{}', pageNum={}, pageSize={}",
                groupName, groupType, visibility, pageNum, pageSize);

        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();

        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();
        java.util.List<Object> dataParams = new java.util.ArrayList<>();
        java.util.List<Object> countParams = new java.util.ArrayList<>();

        if (isAdmin) {
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

        } else {
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

            // 添加CASE语句中的参数（按顺序）
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
        }

        dataSql.append(" ORDER BY fg.created_at DESC");

        // 执行分页查询
        return paginationHelper.selectPageForMapWithDifferentParams(
                dataSql.toString(), countSql.toString(),
                dataParams.toArray(), countParams.toArray(),
                pageNum, pageSize);
    }
}
