package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_rag_file
 */
@TableName(value ="t_rag_file")
@Data
public class TRagFile {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String fileName;

    /**
     * 
     */
    private String fileType;

    /**
     * 
     */
    private String fileSize;

    /**
     * 
     */
    private String documentNum;

    /**
     * 
     */
    private String fileDesc;

    /**
     * 
     */
    private String fileGroupId;

    /**
     * 
     */
    private String uploadedBy;

    /**
     * 
     */
    private Date uploadedAt;

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
        TRagFile other = (TRagFile) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getDocumentNum() == null ? other.getDocumentNum() == null : this.getDocumentNum().equals(other.getDocumentNum()))
            && (this.getFileDesc() == null ? other.getFileDesc() == null : this.getFileDesc().equals(other.getFileDesc()))
            && (this.getFileGroupId() == null ? other.getFileGroupId() == null : this.getFileGroupId().equals(other.getFileGroupId()))
            && (this.getUploadedBy() == null ? other.getUploadedBy() == null : this.getUploadedBy().equals(other.getUploadedBy()))
            && (this.getUploadedAt() == null ? other.getUploadedAt() == null : this.getUploadedAt().equals(other.getUploadedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getDocumentNum() == null) ? 0 : getDocumentNum().hashCode());
        result = prime * result + ((getFileDesc() == null) ? 0 : getFileDesc().hashCode());
        result = prime * result + ((getFileGroupId() == null) ? 0 : getFileGroupId().hashCode());
        result = prime * result + ((getUploadedBy() == null) ? 0 : getUploadedBy().hashCode());
        result = prime * result + ((getUploadedAt() == null) ? 0 : getUploadedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileType=").append(fileType);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", documentNum=").append(documentNum);
        sb.append(", fileDesc=").append(fileDesc);
        sb.append(", fileGroupId=").append(fileGroupId);
        sb.append(", uploadedBy=").append(uploadedBy);
        sb.append(", uploadedAt=").append(uploadedAt);
        sb.append("]");
        return sb.toString();
    }
}