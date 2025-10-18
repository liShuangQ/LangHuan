package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

/**
 * 角色-权限关联表
 * @TableName t_role_permission
 */
@TableName(value = "t_role_permission")
data class TRolePermission(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 
     */
    var roleId: Int? = null,

    /**
     * 
     */
    var permissionId: Int? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
