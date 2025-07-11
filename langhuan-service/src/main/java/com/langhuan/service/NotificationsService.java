package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langhuan.model.domain.TNotifications;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务类
 * 处理通知相关的业务逻辑
 *
 * @author lishuangqi
 */
@Service
@Slf4j
public class NotificationsService {

    private final TNotificationsService tNotificationsService;

    public NotificationsService(TNotificationsService tNotificationsService) {
        this.tNotificationsService = tNotificationsService;
    }

    /**
     * 创建通知
     * 支持创建个人通知（指定用户ID）或全局通知（用户ID为空）
     * 可以批量创建多个用户的通知
     *
     * @param notification 通知信息
     * @param userIds      用户ID列表，为空时创建全局通知
     * @return 创建结果消息
     */
    public String createNotification(TNotifications notification, String userIds) {
        List<String> idList = Arrays.stream(userIds.split(","))
                .map(String::trim)
                .toList();
        
        // 设置创建时间
        notification.setCreatedAt(new Date());
        // 设置默认状态
        notification.setIsRead(false);
        notification.setIsArchived(false);
        
        if (idList.isEmpty() || idList.get(0).isEmpty()) {
            // 创建全局通知，user_id为空，全局都是默认已读
            notification.setUserId(null);
            notification.setIsRead(true);
            tNotificationsService.save(notification);
            return "全局通知创建成功";
        } else {
            // 创建个人通知，为每个用户创建一条记录
            int successCount = 0;
            for (String userId : idList) {
                TNotifications userNotification = new TNotifications();
                // 复制通知内容
                userNotification.setUserId(userId);
                userNotification.setTemplateId(notification.getTemplateId());
                userNotification.setTitle(notification.getTitle());
                userNotification.setContent(notification.getContent());
                userNotification.setNotificationLevel(notification.getNotificationLevel());
                userNotification.setNotificationType(notification.getNotificationType());
                userNotification.setIsRead(false);
                userNotification.setIsArchived(false);
                userNotification.setReferenceId(notification.getReferenceId());
                userNotification.setReferenceType(notification.getReferenceType());
                userNotification.setExpiresAt(notification.getExpiresAt());
                userNotification.setCreatedAt(new Date());

                if (tNotificationsService.save(userNotification)) {
                    successCount++;
                }
            }
            return String.format("成功创建 %d 条个人通知", successCount);
        }
    }

    /**
     * 删除通知（支持单个和批量删除）
     *
     * @param ids 通知ID列表，逗号分隔的字符串
     * @return 是否删除成功
     */
    public boolean deleteNotifications(String ids) {
        List<Integer> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        if (idList.isEmpty()) {
            return false;
        }

        return tNotificationsService.removeByIds(idList);
    }

    /**
     * 归档通知（支持单个和批量归档）
     *
     * @param ids 通知ID列表，逗号分隔的字符串
     * @return 是否归档成功
     */
    public boolean archiveNotifications(String ids) {
        List<Integer> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        if (idList.isEmpty()) {
            return false;
        }

        LambdaUpdateWrapper<TNotifications> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(TNotifications::getId, idList)
                .set(TNotifications::getIsArchived, true);

        return tNotificationsService.update(updateWrapper);
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 是否标记成功
     */
    public boolean markNotificationAsRead(Integer id) {
        LambdaUpdateWrapper<TNotifications> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TNotifications::getId, id)
                .isNotNull(TNotifications::getUserId) // 只有个人通知可以标记已读
                .set(TNotifications::getIsRead, true);

        return tNotificationsService.update(updateWrapper);
    }

    /**
     * 获取用户通知列表
     * 返回指定用户的个人通知和全局通知，按通知级别排序
     * 排序优先级：critical > error > warning > info
     *
     * @param userId            用户ID
     * @param includeRead       是否包含已读通知
     * @param notificationLevel 通知级别过滤
     * @param notificationType  通知类型过滤
     * @param pageNum           页码
     * @param pageSize          每页大小
     * @return 通知分页列表
     */
    public Page<TNotifications> getUserNotifications(String userId, Boolean includeRead, 
            String notificationLevel, String notificationType, int pageNum, int pageSize) {
        Page<TNotifications> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TNotifications> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(
                wrapper -> wrapper.eq(TNotifications::getUserId, userId).or().isNull(TNotifications::getUserId).or()
                        .eq(TNotifications::getUserId, ""))
                .eq(includeRead != null, TNotifications::getIsRead, includeRead)
                .eq(TNotifications::getIsArchived, false)
                .eq(notificationLevel != null, TNotifications::getNotificationLevel, notificationLevel)
                .eq(notificationType != null, TNotifications::getNotificationType, notificationType)
                .and(wrapper -> wrapper.isNull(TNotifications::getExpiresAt).or()
                        .gt(TNotifications::getExpiresAt, new Date()))
                .last("ORDER BY " +
                        "CASE " +
                        "WHEN user_id = '" + userId + "' AND is_read = false THEN 1 " + // 个人未读
                        "WHEN user_id IS NULL OR user_id = '' THEN 2 " + // 系统通知
                        "WHEN user_id = '" + userId + "' AND is_read = true THEN 3 " + // 个人已读
                        "ELSE 4 " +
                        "END, " +
                        "CASE " +
                        "WHEN notification_level = 'critical' THEN 1 " +
                        "WHEN notification_level = 'warning' THEN 2 " +
                        "WHEN notification_level = 'error' THEN 3 " +
                        "WHEN notification_level = 'info' THEN 4 " +
                        "ELSE 5 " +
                        "END, " +
                        "created_at DESC");

        return tNotificationsService.page(page, queryWrapper);
    }

    /**
     * 获取通知统计信息
     * 返回用户的未读通知数量统计
     *
     * @param userId 用户ID
     * @return 统计信息对象
     */
    public Object getNotificationStatistics(String userId) {
        LambdaQueryWrapper<TNotifications> queryWrapper = new LambdaQueryWrapper<>();

        // 查询条件：个人通知或全局通知
        queryWrapper.and(
                wrapper -> wrapper.eq(TNotifications::getUserId, userId).or().isNull(TNotifications::getUserId).or()
                        .eq(TNotifications::getUserId, ""))
                .eq(TNotifications::getIsArchived, false)
                .and(wrapper -> wrapper.isNull(TNotifications::getExpiresAt).or()
                        .gt(TNotifications::getExpiresAt, new Date()));

        // 一次性查询所有符合条件的通知
        List<TNotifications> notifications = tNotificationsService.list(queryWrapper);

        // 在代码中统计各级别数量
        long unreadCount = notifications.size();
        long criticalCount = notifications.stream()
                .filter(n -> "critical".equals(n.getNotificationLevel()))
                .count();
        long errorCount = notifications.stream()
                .filter(n -> "error".equals(n.getNotificationLevel()))
                .count();
        long warningCount = notifications.stream()
                .filter(n -> "warning".equals(n.getNotificationLevel()))
                .count();
        long infoCount = notifications.stream()
                .filter(n -> "info".equals(n.getNotificationLevel()))
                .count();

        return new Object() {
            public final long totalUnread = unreadCount;
            public final long critical = criticalCount;
            public final long error = errorCount;
            public final long warning = warningCount;
            public final long info = infoCount;
        };
    }

    /**
     * 获取个人未读通知数量
     * 返回指定用户的个人未读通知数量（不包含全局通知）
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public long getPersonalUnreadCount(String userId) {
        LambdaQueryWrapper<TNotifications> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件：个人未读通知 + 未归档 + 未过期
        queryWrapper.eq(TNotifications::getUserId, userId)
                .eq(TNotifications::getIsRead, false)
                .eq(TNotifications::getIsArchived, false)
                .and(wrapper -> wrapper.isNull(TNotifications::getExpiresAt)
                        .or()
                        .gt(TNotifications::getExpiresAt, new Date()));

        return tNotificationsService.count(queryWrapper);
    }

    /**
     * 获取所有通知列表（管理员功能）
     * 管理员可以查看所有通知，支持多条件筛选
     *
     * @param userId            用户ID过滤
     * @param notificationLevel 通知级别过滤
     * @param notificationType  通知类型过滤
     * @param isRead            已读状态过滤
     * @param isArchived        归档状态过滤
     * @param pageNum           页码
     * @param pageSize          每页大小
     * @return 通知分页列表
     */
    public Page<TNotifications> getAllNotifications(String userId, String notificationLevel, 
            String notificationType, Boolean isRead, Boolean isArchived, int pageNum, int pageSize) {
        Page<TNotifications> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TNotifications> queryWrapper = new LambdaQueryWrapper<TNotifications>()
                .eq(userId != null && !userId.isEmpty(), TNotifications::getUserId, userId)
                .eq(notificationLevel != null && !notificationLevel.isEmpty(), TNotifications::getNotificationLevel,
                        notificationLevel)
                .eq(notificationType != null && !notificationType.isEmpty(), TNotifications::getNotificationType,
                        notificationType)
                .eq(isRead != null, TNotifications::getIsRead, isRead)
                .eq(isArchived != null, TNotifications::getIsArchived, isArchived)
                .orderByAsc(TNotifications::getIsRead)
                .orderByAsc(TNotifications::getIsArchived)
                .orderByDesc(TNotifications::getCreatedAt);

        return tNotificationsService.page(page, queryWrapper);
    }
}
