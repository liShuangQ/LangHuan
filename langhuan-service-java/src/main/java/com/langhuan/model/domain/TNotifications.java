package com.langhuan.model.domain;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * 系统通知表，用于存储发送给用户的各类通知信息
 * @TableName t_notifications
 */
@TableName(value ="t_notifications")
@Data
public class TNotifications {
    /**
     * 通知唯一标识，使用UUID确保全局唯一性
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 接收通知的用户ID，关联到users表，用户删除时级联删除通知
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 
     */
    @TableField(value = "template_id")
    private String templateId;

    /**
     * 通知标题，简要描述通知内容
     */
    @TableField(value = "title")
    private String title;

    /**
     * 通知详细内容，支持HTML格式
     */
    @TableField(value = "content")
    private String content;

    /**
     * 通知级别，分为info(信息)、warning(警告)、error(错误)、critical(严重)
     */
    @TableField(value = "notification_level")
    private String notificationLevel;

    /**
     * 通知类型，分为system(系统)、reminder(提醒)、alert(警报)、message(消息)、update(更新)
     */
    @TableField(value = "notification_type")
    private String notificationType;

    /**
     * 通知读取状态，true表示已读，false表示未读
     */
    @TableField(value = "is_read")
    private Boolean isRead;

    /**
     * 通知归档状态，true表示已归档(用户手动隐藏)，false表示未归档
     */
    @TableField(value = "is_archived")
    private Boolean isArchived;

    /**
     * 关联业务对象ID，如订单ID、任务ID等，用于业务对象关联
     */
    @TableField(value = "reference_id")
    private String referenceId;

    /**
     * 关联业务对象类型，如order、task等，与reference_id配合使用
     */
    @TableField(value = "reference_type")
    private String referenceType;

    /**
     * 通知过期时间，超过此时间的通知将被视为无效
     */
    @TableField(value = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiresAt;

    /**
     * 通知创建时间戳，自动记录通知创建时间
     */
    @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TNotifications other = (TNotifications) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTemplateId() == null ? other.getTemplateId() == null : this.getTemplateId().equals(other.getTemplateId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getNotificationLevel() == null ? other.getNotificationLevel() == null : this.getNotificationLevel().equals(other.getNotificationLevel()))
            && (this.getNotificationType() == null ? other.getNotificationType() == null : this.getNotificationType().equals(other.getNotificationType()))
            && (this.getIsRead() == null ? other.getIsRead() == null : this.getIsRead().equals(other.getIsRead()))
            && (this.getIsArchived() == null ? other.getIsArchived() == null : this.getIsArchived().equals(other.getIsArchived()))
            && (this.getReferenceId() == null ? other.getReferenceId() == null : this.getReferenceId().equals(other.getReferenceId()))
            && (this.getReferenceType() == null ? other.getReferenceType() == null : this.getReferenceType().equals(other.getReferenceType()))
            && (this.getExpiresAt() == null ? other.getExpiresAt() == null : this.getExpiresAt().equals(other.getExpiresAt()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTemplateId() == null) ? 0 : getTemplateId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getNotificationLevel() == null) ? 0 : getNotificationLevel().hashCode());
        result = prime * result + ((getNotificationType() == null) ? 0 : getNotificationType().hashCode());
        result = prime * result + ((getIsRead() == null) ? 0 : getIsRead().hashCode());
        result = prime * result + ((getIsArchived() == null) ? 0 : getIsArchived().hashCode());
        result = prime * result + ((getReferenceId() == null) ? 0 : getReferenceId().hashCode());
        result = prime * result + ((getReferenceType() == null) ? 0 : getReferenceType().hashCode());
        result = prime * result + ((getExpiresAt() == null) ? 0 : getExpiresAt().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", templateId=").append(templateId);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", notificationLevel=").append(notificationLevel);
        sb.append(", notificationType=").append(notificationType);
        sb.append(", isRead=").append(isRead);
        sb.append(", isArchived=").append(isArchived);
        sb.append(", referenceId=").append(referenceId);
        sb.append(", referenceType=").append(referenceType);
        sb.append(", expiresAt=").append(expiresAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}