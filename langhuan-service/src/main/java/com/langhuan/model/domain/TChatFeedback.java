package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_chat_feedback
 */
@TableName(value ="t_chat_feedback")
@Data
public class TChatFeedback {
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
    private Object userInfo;

    /**
     * 
     */
    private String questionId;

    /**
     * 
     */
    private String questionContent;

    /**
     * 
     */
    private String answerContent;

    /**
     * 
     */
    private String interaction;

    /**
     * 
     */
    private Date interactionTime;

    /**
     * 
     */
    private String knowledgeBaseIds;

    /**
     * 
     */
    private String suggestion;

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
        TChatFeedback other = (TChatFeedback) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserInfo() == null ? other.getUserInfo() == null : this.getUserInfo().equals(other.getUserInfo()))
            && (this.getQuestionId() == null ? other.getQuestionId() == null : this.getQuestionId().equals(other.getQuestionId()))
            && (this.getQuestionContent() == null ? other.getQuestionContent() == null : this.getQuestionContent().equals(other.getQuestionContent()))
            && (this.getAnswerContent() == null ? other.getAnswerContent() == null : this.getAnswerContent().equals(other.getAnswerContent()))
            && (this.getInteraction() == null ? other.getInteraction() == null : this.getInteraction().equals(other.getInteraction()))
            && (this.getInteractionTime() == null ? other.getInteractionTime() == null : this.getInteractionTime().equals(other.getInteractionTime()))
            && (this.getKnowledgeBaseIds() == null ? other.getKnowledgeBaseIds() == null : this.getKnowledgeBaseIds().equals(other.getKnowledgeBaseIds()))
            && (this.getSuggestion() == null ? other.getSuggestion() == null : this.getSuggestion().equals(other.getSuggestion()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserInfo() == null) ? 0 : getUserInfo().hashCode());
        result = prime * result + ((getQuestionId() == null) ? 0 : getQuestionId().hashCode());
        result = prime * result + ((getQuestionContent() == null) ? 0 : getQuestionContent().hashCode());
        result = prime * result + ((getAnswerContent() == null) ? 0 : getAnswerContent().hashCode());
        result = prime * result + ((getInteraction() == null) ? 0 : getInteraction().hashCode());
        result = prime * result + ((getInteractionTime() == null) ? 0 : getInteractionTime().hashCode());
        result = prime * result + ((getKnowledgeBaseIds() == null) ? 0 : getKnowledgeBaseIds().hashCode());
        result = prime * result + ((getSuggestion() == null) ? 0 : getSuggestion().hashCode());
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
        sb.append(", userInfo=").append(userInfo);
        sb.append(", questionId=").append(questionId);
        sb.append(", questionContent=").append(questionContent);
        sb.append(", answerContent=").append(answerContent);
        sb.append(", interaction=").append(interaction);
        sb.append(", interactionTime=").append(interactionTime);
        sb.append(", knowledgeBaseIds=").append(knowledgeBaseIds);
        sb.append(", suggestion=").append(suggestion);
        sb.append("]");
        return sb.toString();
    }
}