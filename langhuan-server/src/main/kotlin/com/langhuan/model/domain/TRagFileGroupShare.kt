package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.*

/**
 * 文件组分享表
 * @TableName t_rag_file_group_share
 */
@TableName(value = "t_rag_file_group_share")
data class TRagFileGroupShare(
    /**
     * 主键ID
     */
    @TableId
    var id: Int? = null,

    /**
     * 文件组ID
     */
    var fileGroupId: Int? = null,

    /**
     * 被分享的用户名
     */
    var sharedWith: String? = null,

    /**
     * 是否可读
     */
    var canRead: Boolean? = null,

    /**
     * 是否可新增
     */
    var canAdd: Boolean? = null,

    /**
     * 是否可修改
     */
    var canUpdate: Boolean? = null,

    /**
     * 是否可删除
     */
    var canDelete: Boolean? = null,

    /**
     * 分享人用户名
     */
    var sharedBy: String? = null,

    /**
     * 分享时间
     */
    var sharedAt: Date? = null
)
