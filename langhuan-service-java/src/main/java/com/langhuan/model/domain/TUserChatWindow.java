package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_user_chat_window
 */
@TableName(value ="t_user_chat_window")
@Data
public class TUserChatWindow {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String userId;

    /**
     * 
     */
    private String conversationId;

    /**
     * 
     */
    private String conversationName;

    /**
     * 
     */
    private Date createdTime;

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
        TUserChatWindow other = (TUserChatWindow) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getConversationId() == null ? other.getConversationId() == null : this.getConversationId().equals(other.getConversationId()))
            && (this.getConversationName() == null ? other.getConversationName() == null : this.getConversationName().equals(other.getConversationName()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getConversationId() == null) ? 0 : getConversationId().hashCode());
        result = prime * result + ((getConversationName() == null) ? 0 : getConversationName().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
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
        sb.append(", conversationId=").append(conversationId);
        sb.append(", conversationName=").append(conversationName);
        sb.append(", createdTime=").append(createdTime);
        sb.append("]");
        return sb.toString();
    }
}