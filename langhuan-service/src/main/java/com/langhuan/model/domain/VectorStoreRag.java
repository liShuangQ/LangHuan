package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName vector_store_rag
 */
@TableName(value ="vector_store_rag")
@Data
public class VectorStoreRag implements Serializable {
    /**
     * 
     */
    @TableId
    private Object id;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    private Object metadata;

    /**
     * 
     */
    private Object embedding;

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
        VectorStoreRag other = (VectorStoreRag) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getMetadata() == null ? other.getMetadata() == null : this.getMetadata().equals(other.getMetadata()))
            && (this.getEmbedding() == null ? other.getEmbedding() == null : this.getEmbedding().equals(other.getEmbedding()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getMetadata() == null) ? 0 : getMetadata().hashCode());
        result = prime * result + ((getEmbedding() == null) ? 0 : getEmbedding().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", content=").append(content);
        sb.append(", metadata=").append(metadata);
        sb.append(", embedding=").append(embedding);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}