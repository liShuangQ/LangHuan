package com.langhuan.model.pojo;

import lombok.Data;

@Data
public class RagMetaData {
    private String filename;
    private String fileId;
    private String groupId; // HACK: 改动字段名注意搜索：filterExpression
}
