package com.langhuan.dao

import com.baomidou.mybatisplus.core.metadata.IPage
import com.langhuan.utils.pagination.JdbcPaginationHelper
import org.springframework.stereotype.Repository

/**
 * @author lishuangqi
 * @description 针对表【t_chat_feedback】的数据库操作DAO实现
 * @createDate 2025-04-24 14:55:15
 */
@Repository
class TChatFeedbackDao(
    private val paginationHelper: JdbcPaginationHelper
) {

    /**
     * 聊天反馈搜索
     *
     * @param username    用户名
     * @param interaction 交互类型
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    fun searchChatFeedback(
        username: String?,
        interaction: String?,
        pageNum: Int,
        pageSize: Int
    ): IPage<java.util.Map<String, Any>> {
        // 构建查询条件
        val condition = JdbcPaginationHelper.QueryCondition()

        // 添加用户名模糊查询条件
        if (!username.isNullOrEmpty()) {
            condition.like("u.name", username)
        }

        // 添加交互类型精确查询条件
        if (!interaction.isNullOrEmpty()) {
            condition.eq("interaction", interaction)
        }

        // 构建SQL查询语句
        // 使用子查询来处理逗号分隔的use_file_group_id，并聚合文件组名称
        val sql = """
            SELECT cf.*, u.name as user_name, 
                COALESCE(group_names.use_file_group_name, '') as use_file_group_name 
            FROM t_chat_feedback cf 
            LEFT JOIN t_user u ON cf.user_id = u.username 
            LEFT JOIN (
              SELECT cf_inner.id, 
                STRING_AGG(rg.group_name, ',') as use_file_group_name 
              FROM t_chat_feedback cf_inner 
              CROSS JOIN LATERAL unnest(string_to_array(cf_inner.use_file_group_id, ',')) as group_id(id) 
              LEFT JOIN t_rag_file_group rg ON TRIM(group_id.id) = CAST(rg.id AS VARCHAR) 
              WHERE cf_inner.use_file_group_id IS NOT NULL AND cf_inner.use_file_group_id != '' 
              GROUP BY cf_inner.id
            ) group_names ON cf.id = group_names.id
            ${condition.getWhereClause()}
            ORDER BY CASE interaction 
            WHEN 'dislike' THEN 1 
            WHEN 'like' THEN 2 
            WHEN 'end' THEN 3 
            ELSE 4 END, interaction_time DESC
        """.trimIndent()

        // 执行分页查询
        @Suppress("UNCHECKED_CAST")
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum.toLong(), pageSize.toLong()) as IPage<java.util.Map<String, Any>>
    }
}
