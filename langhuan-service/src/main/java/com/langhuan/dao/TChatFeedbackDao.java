package com.langhuan.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_chat_feedback】的数据库操作DAO实现
 * @createDate 2025-04-24 14:55:15
 */
@Repository
@RequiredArgsConstructor
public class TChatFeedbackDao {

    private final JdbcPaginationHelper paginationHelper;

    /**
     * 聊天反馈搜索
     *
     * @param username    用户名
     * @param interaction 交互类型
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    public IPage<Map<String, Object>> searchChatFeedback(String username, String interaction, int pageNum,
            int pageSize) {
        // 构建查询条件
        JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition();

        // 添加用户名模糊查询条件
        if (username != null && !username.isEmpty()) {
            condition.like("u.name", username);
        }

        // 添加交互类型精确查询条件
        if (interaction != null && !interaction.isEmpty()) {
            condition.eq("interaction", interaction);
        }

        // 构建SQL查询语句
        String sql = "SELECT cf.*, u.name as user_name, rg.group_name as use_file_group_name FROM t_chat_feedback cf"
                + " LEFT JOIN t_user u ON cf.user_id = u.username"
                + " LEFT JOIN t_rag_file_group rg ON cf.use_file_group_id = CAST(rg.id AS VARCHAR)"
                + condition.getWhereClause() +
                " ORDER BY CASE interaction " +
                " WHEN 'dislike' THEN 1 " +
                " WHEN 'like' THEN 2 " +
                " WHEN 'end' THEN 3 " +
                " ELSE 4 END, interaction_time DESC";

        // 执行分页查询
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum, pageSize);
    }
}