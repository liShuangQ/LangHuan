package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_tool
 */
@TableName(value ="t_tool")
@Data
public class TTool implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String toolName;

    /**
     * 
     */
    private String toolType;

    /**
     * 
     */
    private String toolDesc;

    /**
     * 
     */
    private String toolGroupId;

    /**
     * 
     */
    private String toolBy;

    /**
     * 
     */
    private Date toolAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
        TTool other = (TTool) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getToolName() == null ? other.getToolName() == null : this.getToolName().equals(other.getToolName()))
            && (this.getToolType() == null ? other.getToolType() == null : this.getToolType().equals(other.getToolType()))
            && (this.getToolDesc() == null ? other.getToolDesc() == null : this.getToolDesc().equals(other.getToolDesc()))
            && (this.getToolGroupId() == null ? other.getToolGroupId() == null : this.getToolGroupId().equals(other.getToolGroupId()))
            && (this.getToolBy() == null ? other.getToolBy() == null : this.getToolBy().equals(other.getToolBy()))
            && (this.getToolAt() == null ? other.getToolAt() == null : this.getToolAt().equals(other.getToolAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getToolName() == null) ? 0 : getToolName().hashCode());
        result = prime * result + ((getToolType() == null) ? 0 : getToolType().hashCode());
        result = prime * result + ((getToolDesc() == null) ? 0 : getToolDesc().hashCode());
        result = prime * result + ((getToolGroupId() == null) ? 0 : getToolGroupId().hashCode());
        result = prime * result + ((getToolBy() == null) ? 0 : getToolBy().hashCode());
        result = prime * result + ((getToolAt() == null) ? 0 : getToolAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", toolName=").append(toolName);
        sb.append(", toolType=").append(toolType);
        sb.append(", toolDesc=").append(toolDesc);
        sb.append(", toolGroupId=").append(toolGroupId);
        sb.append(", toolBy=").append(toolBy);
        sb.append(", toolAt=").append(toolAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}