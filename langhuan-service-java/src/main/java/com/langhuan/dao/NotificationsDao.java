package com.langhuan.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 通知数据访问层
 * 处理通知相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
@Slf4j
public class NotificationsDao {

    private final JdbcPaginationHelper paginationHelper;

    public NotificationsDao(JdbcPaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
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
    public IPage<Map<String, Object>> getAllNotifications(String username, String notificationLevel,
                                                           String notificationType, Boolean isRead, Boolean isArchived, 
                                                           int pageNum, int pageSize) {

        // 构建查询条件
        JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition();

        // 添加查询条件
        if (username != null && !username.isEmpty()) {
            condition.like("u.name", username);
        }
        if (notificationLevel != null && !notificationLevel.isEmpty()) {
            condition.eq("notification_level", notificationLevel);
        }
        if (notificationType != null && !notificationType.isEmpty()) {
            condition.eq("notification_type", notificationType);
        }
        if (isRead != null) {
            condition.eq("is_read", isRead);
        }
        if (isArchived != null) {
            condition.eq("is_archived", isArchived);
        }

        // 构建SQL查询语句
        String sql = "SELECT n.*, u.name as user_name FROM t_notifications n "
                + " LEFT JOIN t_user u ON n.user_id = u.username"
                + condition.getWhereClause()
                + " ORDER BY is_read ASC, is_archived ASC, created_at DESC";

        // 执行分页查询
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum, pageSize);
    }
}