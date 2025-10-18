package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*

/**
 * 系统通知表，用于存储发送给用户的各类通知信息
 * @TableName t_notifications
 */
@TableName(value = "t_notifications")
data class TNotifications(
    /**
     * 通知唯一标识，使用UUID确保全局唯一性
     */
    @TableId(value = "id")
    var id: Int? = null,

    /**
     * 接收通知的用户ID，关联到users表，用户删除时级联删除通知
     */
    @TableField(value = "user_id")
    var userId: String? = null,

    /**
     * 
     */
    @TableField(value = "template_id")
    var templateId: String? = null,

    /**
     * 通知标题，简要描述通知内容
     */
    @TableField(value = "title")
    var title: String? = null,

    /**
     * 通知详细内容，支持HTML格式
     */
    @TableField(value = "content")
    var content: String? = null,

    /**
     * 通知级别，分为info(信息)、warning(警告)、error(错误)、critical(严重)
     */
    @TableField(value = "notification_level")
    var notificationLevel: String? = null,

    /**
     * 通知类型，分为system(系统)、reminder(提醒)、alert(警报)、message(消息)、update(更新)
     */
    @TableField(value = "notification_type")
    var notificationType: String? = null,

    /**
     * 通知读取状态，true表示已读，false表示未读
     */
    @TableField(value = "is_read")
    var isRead: Boolean? = null,

    /**
     * 通知归档状态，true表示已归档(用户手动隐藏)，false表示未归档
     */
    @TableField(value = "is_archived")
    var isArchived: Boolean? = null,

    /**
     * 关联业务对象ID，如订单ID、任务ID等，用于业务对象关联
     */
    @TableField(value = "reference_id")
    var referenceId: String? = null,

    /**
     * 关联业务对象类型，如order、task等，与reference_id配合使用
     */
    @TableField(value = "reference_type")
    var referenceType: String? = null,

    /**
     * 通知过期时间，超过此时间的通知将被视为无效
     */
    @TableField(value = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var expiresAt: Date? = null,

    /**
     * 通知创建时间戳，自动记录通知创建时间
     */
    @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var createdAt: Date? = null
)
