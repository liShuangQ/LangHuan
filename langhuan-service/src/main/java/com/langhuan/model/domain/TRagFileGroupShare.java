package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 文件组分享表
 * @TableName t_rag_file_group_share
 */
@TableName(value ="t_rag_file_group_share")
@Data
public class TRagFileGroupShare {
    /**
     * 主键ID
     */
    @TableId
    private Integer id;

    /**
     * 文件组ID
     */
    private Integer fileGroupId;

    /**
     * 被分享的用户名
     */
    private String sharedWith;

    /**
     * 是否可读
     */
    private Boolean canRead;

    /**
     * 是否可新增
     */
    private Boolean canAdd;

    /**
     * 是否可修改
     */
    private Boolean canUpdate;

    /**
     * 是否可删除
     */
    private Boolean canDelete;

    /**
     * 分享人用户名
     */
    private String sharedBy;

    /**
     * 分享时间
     */
    private Date sharedAt;

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
        TRagFileGroupShare other = (TRagFileGroupShare) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileGroupId() == null ? other.getFileGroupId() == null : this.getFileGroupId().equals(other.getFileGroupId()))
            && (this.getSharedWith() == null ? other.getSharedWith() == null : this.getSharedWith().equals(other.getSharedWith()))
            && (this.getCanRead() == null ? other.getCanRead() == null : this.getCanRead().equals(other.getCanRead()))
            && (this.getCanAdd() == null ? other.getCanAdd() == null : this.getCanAdd().equals(other.getCanAdd()))
            && (this.getCanUpdate() == null ? other.getCanUpdate() == null : this.getCanUpdate().equals(other.getCanUpdate()))
            && (this.getCanDelete() == null ? other.getCanDelete() == null : this.getCanDelete().equals(other.getCanDelete()))
            && (this.getSharedBy() == null ? other.getSharedBy() == null : this.getSharedBy().equals(other.getSharedBy()))
            && (this.getSharedAt() == null ? other.getSharedAt() == null : this.getSharedAt().equals(other.getSharedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileGroupId() == null) ? 0 : getFileGroupId().hashCode());
        result = prime * result + ((getSharedWith() == null) ? 0 : getSharedWith().hashCode());
        result = prime * result + ((getCanRead() == null) ? 0 : getCanRead().hashCode());
        result = prime * result + ((getCanAdd() == null) ? 0 : getCanAdd().hashCode());
        result = prime * result + ((getCanUpdate() == null) ? 0 : getCanUpdate().hashCode());
        result = prime * result + ((getCanDelete() == null) ? 0 : getCanDelete().hashCode());
        result = prime * result + ((getSharedBy() == null) ? 0 : getSharedBy().hashCode());
        result = prime * result + ((getSharedAt() == null) ? 0 : getSharedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileGroupId=").append(fileGroupId);
        sb.append(", sharedWith=").append(sharedWith);
        sb.append(", canRead=").append(canRead);
        sb.append(", canAdd=").append(canAdd);
        sb.append(", canUpdate=").append(canUpdate);
        sb.append(", canDelete=").append(canDelete);
        sb.append(", sharedBy=").append(sharedBy);
        sb.append(", sharedAt=").append(sharedAt);
        sb.append("]");
        return sb.toString();
    }
}