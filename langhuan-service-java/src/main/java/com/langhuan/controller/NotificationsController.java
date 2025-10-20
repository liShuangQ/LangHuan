package com.langhuan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TNotifications;
import com.langhuan.service.NotificationsService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    private final NotificationsService notificationsService;

    public NotificationsController(NotificationsService notificationsService) {
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
            String result = notificationsService.createNotification(notification, userIds);
            return Result.success(result);
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
            boolean success = notificationsService.deleteNotifications(ids);
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
            boolean success = notificationsService.archiveNotifications(ids);
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
    // @PreAuthorize("hasAuthority('/notifications/markRead')")
    public Result<String> markNotificationAsRead(@RequestParam Integer id) {
        if (id == null) {
            return Result.error("通知ID不能为空");
        }

        boolean success = notificationsService.markNotificationAsRead(id);
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
            Page<TNotifications> result = notificationsService.getUserNotifications(
                    userId, includeRead, notificationLevel, notificationType, pageNum, pageSize);
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
            Object statistics = notificationsService.getNotificationStatistics(userId);
            return Result.success(statistics);
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
    // @PreAuthorize("hasAuthority('/notifications/statistics')")
    @PostMapping("/getPersonalUnreadCount")
    public Result<Long> getPersonalUnreadCount(@RequestParam(name = "userId", required = true) String userId) {
        try {
            long count = notificationsService.getPersonalUnreadCount(userId);
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
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "notificationLevel", required = false) String notificationLevel,
            @RequestParam(name = "notificationType", required = false) String notificationType,
            @RequestParam(name = "isRead", required = false) Boolean isRead,
            @RequestParam(name = "isArchived", required = false) Boolean isArchived,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            return Result.success(notificationsService.getAllNotifications(
                username, notificationLevel, notificationType, isRead, isArchived, pageNum, pageSize));
        } catch (Exception e) {
            return Result.error("获取所有通知列表失败: " + e.getMessage());
        }
    }

}
