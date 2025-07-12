package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileGroupMapper;
import com.langhuan.utils.pagination.JdbcPaginationHelper;

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

    public TRagFileGroupService(JdbcPaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    public List<Map<String, Object>> getEnum() {
        List<TRagFileGroup> list = super.list();
        return list.stream()
                .map(e -> Map.of("id", (Object) e.getId(), "groupName", (Object) e.getGroupName()))
                .collect(Collectors.toList());

    }

    public IPage<Map<String, Object>> queryFileGroups(String groupName, String groupType, int pageNum, int pageSize) {
        log.info("queryFileGroups: groupName='{}', groupType='{}', pageNum={}, pageSize={}",
                groupName, groupType, pageNum, pageSize);

        // 构建动态查询条件
        java.util.List<Object> params = new java.util.ArrayList<>();
        StringBuilder whereClause = new StringBuilder();

        // 添加groupName条件
        if (groupName != null && !groupName.trim().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append("f.group_name LIKE ?");
            params.add("%" + groupName + "%");
        }

        // 添加groupType条件
        if (groupType != null && !groupType.trim().isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append("f.group_type LIKE ?");
            params.add("%" + groupType + "%");
        }

        // 构建完整的SQL
        String sql = """
                SELECT f.id as id,
                f.group_name as "groupName",
                f.group_type as "groupType",
                f.group_desc as "groupDesc",
                f.created_at as "createdAt",
                f.created_by as "createdBy",
                u.name as "userName"
                FROM t_rag_file_group f
                """;
        if (whereClause.length() > 0) {
            sql += " WHERE " + whereClause.toString();
        }
        sql += " LEFT JOIN t_user u ON f.created_by = u.username";
        sql += " ORDER BY f.created_at DESC";

        // 执行分页查询
        return paginationHelper.selectPageForMap(sql, params.toArray(), pageNum, pageSize);
    }
}
