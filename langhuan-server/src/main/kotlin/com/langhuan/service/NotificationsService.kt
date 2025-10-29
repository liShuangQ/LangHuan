package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.langhuan.dao.NotificationsDao
import com.langhuan.model.domain.TNotifications
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

/**
 * 通知服务类
 * 处理通知相关的业务逻辑
 *
 * @author lishuangqi
 */
@Service
class NotificationsService {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationsService::class.java)
    }

    @Autowired
    private val tNotificationsService: TNotificationsService? = null
    @Autowired
    private val notificationsDao: NotificationsDao? = null

    /**
     * 创建通知
     * 支持创建个人通知（指定用户ID）或全局通知（用户ID为空）
     * 可以批量创建多个用户的通知
     *
     * @param notification 通知信息
     * @param userIds      用户ID列表，为空时创建全局通知
     * @return 创建结果消息
     */
    fun createNotification(notification: TNotifications, userIds: String): String {
        val idList = userIds.split(",")
            .map { it.trim() }

        // 设置创建时间
        notification.createdAt = Date()
        // 设置默认状态
        notification.isRead = false
        notification.isArchived = false

        return if (idList.isEmpty() || idList[0].isEmpty()) {
            // 创建全局通知，user_id为空，全局都是默认已读
            notification.userId = null
            notification.isRead = true
            tNotificationsService!!.save(notification)
            "全局通知创建成功"
        } else {
            // 创建个人通知，为每个用户创建一条记录
            var successCount = 0
            for (userId in idList) {
                val userNotification = TNotifications()
                // 复制通知内容
                userNotification.userId = userId
                userNotification.templateId = notification.templateId
                userNotification.title = notification.title
                userNotification.content = notification.content
                userNotification.notificationLevel = notification.notificationLevel
                userNotification.notificationType = notification.notificationType
                userNotification.isRead = false
                userNotification.isArchived = false
                userNotification.referenceId = notification.referenceId
                userNotification.referenceType = notification.referenceType
                userNotification.expiresAt = notification.expiresAt
                userNotification.createdAt = Date()

                if (tNotificationsService!!.save(userNotification)) {
                    successCount++
                }
            }
            String.format("成功创建 %d 条个人通知", successCount)
        }
    }

    /**
     * 删除通知（支持单个和批量删除）
     *
     * @param ids 通知ID列表，逗号分隔的字符串
     * @return 是否删除成功
     */
    fun deleteNotifications(ids: String): Boolean {
        val idList = ids.split(",")
            .map { it.trim() }
            .map { it.toInt() }

        if (idList.isEmpty()) {
            return false
        }

        return tNotificationsService!!.removeByIds(idList)
    }

    /**
     * 归档通知（支持单个和批量归档）
     *
     * @param ids 通知ID列表，逗号分隔的字符串
     * @return 是否归档成功
     */
    fun archiveNotifications(ids: String): Boolean {
        val idList = ids.split(",")
            .map { it.trim() }
            .map { it.toInt() }

        if (idList.isEmpty()) {
            return false
        }

        val updateWrapper = UpdateWrapper<TNotifications>()
        updateWrapper.`in`("id", idList)
            .set("is_archived", true)

        return tNotificationsService!!.update(updateWrapper)
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 是否标记成功
     */
    fun markNotificationAsRead(id: Int): Boolean {
        val updateWrapper = UpdateWrapper<TNotifications>()
        updateWrapper.eq("id", id)
            .isNotNull("user_id") // 只有个人通知可以标记已读
            .set("is_read", true)

        return tNotificationsService!!.update(updateWrapper)
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
    fun getUserNotifications(
        userId: String?,
        includeRead: Boolean?,
        notificationLevel: String?,
        notificationType: String?,
        pageNum: Int,
        pageSize: Int
    ): Page<TNotifications> {
        val page = Page<TNotifications>(pageNum.toLong(), pageSize.toLong())
        val queryWrapper = QueryWrapper<TNotifications>()
        queryWrapper.and(
            { wrapper ->
                wrapper.eq("user_id", userId)
                    .or()
                    .isNull("user_id")
                    .or()
                    .eq("user_id", "")
            }
        )
            .eq(includeRead != null, "is_read", includeRead)
            .eq("is_archived", false)
            .eq(notificationLevel != null, "notification_level", notificationLevel)
            .eq(notificationType != null, notificationType, notificationType)
            .and({ wrapper ->
                wrapper.isNull("expires_at")
                    .or()
                    .gt("expires_at", Date())
            })
            .last(
                ("ORDER BY " +
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
                    "created_at DESC")
            )

        return tNotificationsService!!.page(page, queryWrapper)
    }

    /**
     * 获取通知统计信息
     * 返回用户的未读通知数量统计
     *
     * @param userId 用户ID
     * @return 统计信息对象
     */
    fun getNotificationStatistics(userId: String?): Any {
        val queryWrapper = QueryWrapper<TNotifications>()

        // 查询条件：个人通知或全局通知
        queryWrapper.and(
            { wrapper ->
                wrapper.eq("user_id", userId)
                    .or()
                    .isNull("user_id")
                    .or()
                    .eq("user_id", "")
            }
        )
            .eq("is_archived", false)
            .and({ wrapper ->
                wrapper.isNull("expires_at")
                    .or()
                    .gt("expires_at", Date())
            })

        // 一次性查询所有符合条件的通知
        val notifications = tNotificationsService!!.list(queryWrapper)

        // 在代码中统计各级别数量
        val unreadCount = notifications.size.toLong()
        val criticalCount = notifications.stream()
            .filter { n: TNotifications -> "critical" == n.notificationLevel }
            .count()
        val errorCount = notifications.stream()
            .filter { n: TNotifications -> "error" == n.notificationLevel }
            .count()
        val warningCount = notifications.stream()
            .filter { n: TNotifications -> "warning" == n.notificationLevel }
            .count()
        val infoCount = notifications.stream()
            .filter { n: TNotifications -> "info" == n.notificationLevel }
            .count()

        return object {
            val totalUnread = unreadCount
            val critical = criticalCount
            val error = errorCount
            val warning = warningCount
            val info = infoCount
        }
    }

    /**
     * 获取个人未读通知数量
     * 返回指定用户的个人未读通知数量（不包含全局通知）
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    fun getPersonalUnreadCount(userId: String?): Long {
        val queryWrapper = QueryWrapper<TNotifications>()
        // 查询条件：个人未读通知 + 未归档 + 未过期
        queryWrapper.eq("user_id", userId)
            .eq("is_read", false)
            .eq("is_archived", false)
            .and({ wrapper ->
                wrapper.isNull("expires_at")
                    .or()
                    .gt("expires_at", Date())
            })

        return tNotificationsService!!.count(queryWrapper)
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
    fun getAllNotifications(
        username: String?,
        notificationLevel: String?,
        notificationType: String?,
        isRead: Boolean?,
        isArchived: Boolean?,
        pageNum: Int,
        pageSize: Int
    ): IPage<Map<String, Any>> {

        // 委托给DAO层处理
        return notificationsDao!!.getAllNotifications(
            username, notificationLevel, notificationType,
            isRead, isArchived, pageNum, pageSize
        )
    }
}
