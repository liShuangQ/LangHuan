package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.util.stream.Collectors;
import java.util.Arrays;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TNotifications;
import com.langhuan.service.TNotificationsService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 系统通知控制器
 * 提供通知的创建、删除、归档、标记完成、查询等功能
 *
 * @author lishuangqi
 * @description 系统通知管理控制器，支持个人通知和全局通知
 */
@RestController
@RequestMapping(path = "/notifications")
@Slf4j
public class NotificationsController {

    private final TNotificationsService notificationsService;

    public NotificationsController(TNotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    /**
     * 创建通知
     * 支持创建个人通知（指定用户ID）或全局通知（用户ID为空）
     * 可以批量创建多个用户的通知
     *
     * @param notification 通知信息
     * @param userIds      用户ID列表，为空时创建全局通知
     * @return 创建结果
     */
    @PreAuthorize("hasAuthority('/notifications/create')")
    @PostMapping("/create")
    public Result createNotification(
            @RequestBody TNotifications notification,
            @RequestParam(name = "userIds", required = false) String userIds) {
        try {
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
                notificationsService.save(notification);
                return Result.success("全局通知创建成功");
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

                    if (notificationsService.save(userNotification)) {
                        successCount++;
                    }
                }
                return Result.success(String.format("成功创建 %d 条个人通知", successCount));
            }
        } catch (Exception e) {
            return Result.error("创建通知失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知（支持单个和批量删除）
     * 前端说明：
     * - 传入通知ID列表进行删除，使用逗号分隔的字符串形式
     * - 支持单个删除（传入一个ID）或批量删除（传入多个ID，用逗号分隔）
     * - 删除后通知将从数据库中永久移除
     *
     * @param ids 通知ID列表，逗号分隔的字符串
     * @return 删除结果
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('/notifications/delete')")
    public Result<String> deleteNotifications(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return Result.error("通知ID不能为空");
        }

        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (idList.isEmpty()) {
                return Result.error("通知ID不能为空");
            }

            boolean success = notificationsService.removeByIds(idList);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (NumberFormatException e) {
            return Result.error("通知ID格式错误");
        }
    }

    /**
     * 归档通知（支持单个和批量归档）
     * 前端说明：
     * - 传入通知ID列表进行归档，使用逗号分隔的字符串形式
     * - 支持单个归档（传入一个ID）或批量归档（传入多个ID，用逗号分隔）
     * - 归档后的通知不会在普通列表中显示，但仍保留在数据库中
     *
     * @param ids 通知ID列表，逗号分隔的字符串
     * @return 归档结果
     */
    @PostMapping("/archive")
    @PreAuthorize("hasAuthority('/notifications/archive')")
    public Result<String> archiveNotifications(@RequestParam String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            return Result.error("通知ID不能为空");
        }

        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (idList.isEmpty()) {
                return Result.error("通知ID不能为空");
            }

            LambdaUpdateWrapper<TNotifications> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(TNotifications::getId, idList)
                    .set(TNotifications::getIsArchived, true);

            boolean success = notificationsService.update(updateWrapper);
            return success ? Result.success("归档成功") : Result.error("归档失败");
        } catch (NumberFormatException e) {
            return Result.error("通知ID格式错误");
        }
    }

    /**
     * 标记通知为已读
     * 前端说明：
     * - 传入通知ID进行已读标记
     * - 只有个人通知可以标记为已读
     * - 全局通知无法标记已读状态
     *
     * @param id 通知ID
     * @return 标记结果
     */
    @PostMapping("/mark-read")
    @PreAuthorize("hasAuthority('/notifications/markRead')")
    public Result<String> markNotificationAsRead(@RequestParam Integer id) {
        if (id == null) {
            return Result.error("通知ID不能为空");
        }

        LambdaUpdateWrapper<TNotifications> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TNotifications::getId, id)
                .isNotNull(TNotifications::getUserId) // 只有个人通知可以标记已读
                .set(TNotifications::getIsRead, true);

        boolean success = notificationsService.update(updateWrapper);
        return success ? Result.success("标记已读成功") : Result.error("标记已读失败或该通知为全局通知");
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
     * @return 通知列表
     */
    // @PreAuthorize("hasAuthority('/notifications/list')")
    @PostMapping("/getUserNotifications")
    public Result getUserNotifications(
            @RequestParam(name = "userId", required = true) String userId,
            @RequestParam(name = "includeRead", required = false) Boolean includeRead,
            @RequestParam(name = "notificationLevel", required = false) String notificationLevel,
            @RequestParam(name = "notificationType", required = false) String notificationType,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
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

            Page<TNotifications> result = notificationsService.page(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知统计信息
     * 返回用户的未读通知数量统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    // @PreAuthorize("hasAuthority('/notifications/statistics')")
    @PostMapping("/getStatistics")
    public Result getNotificationStatistics(@RequestParam(name = "userId", required = true) String userId) {
        try {
            LambdaQueryWrapper<TNotifications> queryWrapper = new LambdaQueryWrapper<>();

            // 查询条件：个人通知或全局通知
            queryWrapper.and(
                    wrapper -> wrapper.eq(TNotifications::getUserId, userId).or().isNull(TNotifications::getUserId).or()
                            .eq(TNotifications::getUserId, ""))
                    .eq(TNotifications::getIsArchived, false)
                    .and(wrapper -> wrapper.isNull(TNotifications::getExpiresAt).or()
                            .gt(TNotifications::getExpiresAt, new Date()));

            // 一次性查询所有符合条件的通知
            List<TNotifications> notifications = notificationsService.list(queryWrapper);

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

            return Result.success(new Object() {
                public final long totalUnread = unreadCount;
                public final long critical = criticalCount;
                public final long error = errorCount;
                public final long warning = warningCount;
                public final long info = infoCount;
            });
        } catch (Exception e) {
            return Result.error("获取通知统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取个人未读通知数量
     * 返回指定用户的个人未读通知数量（不包含全局通知）
     *
     * @param userId 用户ID
     * @return 未读数量
     */
//    @PreAuthorize("hasAuthority('/notifications/statistics')")
    @PostMapping("/getPersonalUnreadCount")
    public Result<Long> getPersonalUnreadCount(@RequestParam(name = "userId", required = true) String userId) {
        try {
            LambdaQueryWrapper<TNotifications> queryWrapper = new LambdaQueryWrapper<>();
            // 查询条件：个人未读通知 + 未归档 + 未过期
            queryWrapper.eq(TNotifications::getUserId, userId)
                    .eq(TNotifications::getIsRead, false)
                    .eq(TNotifications::getIsArchived, false)
                    .and(wrapper -> wrapper.isNull(TNotifications::getExpiresAt)
                            .or()
                            .gt(TNotifications::getExpiresAt, new Date()));

            long count = notificationsService.count(queryWrapper);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取未读数量失败: " + e.getMessage());
        }
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
     * @return 通知列表
     */
    @PreAuthorize("hasAuthority('/notifications/admin/list')")
    @PostMapping("/admin/getAllNotifications")
    public Result getAllNotifications(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "notificationLevel", required = false) String notificationLevel,
            @RequestParam(name = "notificationType", required = false) String notificationType,
            @RequestParam(name = "isRead", required = false) Boolean isRead,
            @RequestParam(name = "isArchived", required = false) Boolean isArchived,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
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

            Page<TNotifications> result = notificationsService.page(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取所有通知列表失败: " + e.getMessage());
        }
    }

}
