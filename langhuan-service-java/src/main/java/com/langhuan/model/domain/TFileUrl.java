package com.langhuan.model.domain;

import lombok.Data;

/**
 * 
 * @TableName t_file_url
 */
@Data
public class TFileUrl {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private Integer fileId;

    /**
     * 
     */
    private String fUrl;

    /**
     * 
     */
    private String fStatus;

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
        TFileUrl other = (TFileUrl) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getFUrl() == null ? other.getFUrl() == null : this.getFUrl().equals(other.getFUrl()))
            && (this.getFStatus() == null ? other.getFStatus() == null : this.getFStatus().equals(other.getFStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getFUrl() == null) ? 0 : getFUrl().hashCode());
        result = prime * result + ((getFStatus() == null) ? 0 : getFStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileId=").append(fileId);
        sb.append(", fUrl=").append(fUrl);
        sb.append(", fStatus=").append(fStatus);
        sb.append("]");
        return sb.toString();
    }
}