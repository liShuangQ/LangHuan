package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.util.*

/**
 * 用户表
 * @TableName t_user
 */
@TableName(value = "t_user")
data class TUser(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 姓名
     */
    var name: String? = null,

    /**
     * 用户名
     */
    var username: String? = null,

    /**
     * 密码
     */
    var password: String? = null,

    /**
     * 手机号
     */
    var phone: String? = null,

    /**
     * 性别
     */
    var gender: Int? = null,

    /**
     * 是否启用（0-未启用；1-启用中）
     */
    var enabled: Int? = null,

    /**
     * 创建时间
     */
    var creationTime: Date? = null,

    /**
     * 上一次登录时间
     */
    var lastLoginTime: Date? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
