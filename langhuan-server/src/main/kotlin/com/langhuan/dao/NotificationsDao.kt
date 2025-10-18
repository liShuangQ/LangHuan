package com.langhuan.dao

import com.baomidou.mybatisplus.core.metadata.IPage
import com.langhuan.utils.pagination.JdbcPaginationHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

/**
 * 通知数据访问层
 * 处理通知相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
class NotificationsDao(
    private val paginationHelper: JdbcPaginationHelper
) {
    companion object {
        private val log = LoggerFactory.getLogger(NotificationsDao::class.java)
    }
    /**
     * 获取所有通知列表（管理员功能）
     * 管理员可以查看所有通知，支持多条件筛选
     *
     * @param username            用户名过滤
     * @param notificationLevel   通知级别过滤
     * @param notificationType    通知类型过滤
     * @param isRead              已读状态过滤
     * @param isArchived          归档状态过滤
     * @param pageNum             页码
     * @param pageSize            每页大小
     * @return 通知分页列表
     */
    fun getAllNotifications(
        username: String?,
        notificationLevel: String?,
        notificationType: String?,
        isRead: Boolean?,
        isArchived: Boolean?,
        pageNum: Int,
        pageSize: Int
    ): IPage<java.util.Map<String, Any>> {

        // 构建查询条件
        val condition = JdbcPaginationHelper.QueryCondition()

        // 添加查询条件
        if (!username.isNullOrEmpty()) {
            condition.like("u.name", username)
        }
        if (!notificationLevel.isNullOrEmpty()) {
            condition.eq("notification_level", notificationLevel)
        }
        if (!notificationType.isNullOrEmpty()) {
            condition.eq("notification_type", notificationType)
        }
        if (isRead != null) {
            condition.eq("is_read", isRead)
        }
        if (isArchived != null) {
            condition.eq("is_archived", isArchived)
        }

        // 构建SQL查询语句
        val sql = """
            SELECT n.*, u.name as user_name FROM t_notifications n 
            LEFT JOIN t_user u ON n.user_id = u.username
            ${condition.getWhereClause()}
            ORDER BY is_read ASC, is_archived ASC, created_at DESC
        """.trimIndent()

        // 执行分页查询
        @Suppress("UNCHECKED_CAST")
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum.toLong(), pageSize.toLong()) as IPage<java.util.Map<String, Any>>
    }
}
