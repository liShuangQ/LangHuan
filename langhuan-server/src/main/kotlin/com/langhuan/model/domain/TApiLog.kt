package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

/**
 * 接口调用日志统计实体类
 *
 * @author system
 */
@TableName("t_api_log")
open class TApiLog {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Long? = null

    /**
     * 接口名称
     */
    @TableField("api_name")
    var apiName: String? = null

    /**
     * 接口URL
     */
    @TableField("api_url")
    var apiUrl: String? = null

    /**
     * 接口说明
     */
    @TableField("api_description")
    var apiDescription: String? = null

    /**
     * HTTP请求方法
     */
    @TableField("http_method")
    var httpMethod: String? = null

    /**
     * 用户ID
     */
    @TableField("user_id")
    var userId: String? = null

    /**
     * 用户名
     */
    @TableField("username")
    var username: String? = null

    /**
     * 请求IP地址
     */
    @TableField("request_ip")
    var requestIp: String? = null

    /**
     * 接口入参JSON字符串
     */
    @TableField("request_params")
    var requestParams: String? = null

    /**
     * 接口出参JSON字符串
     */
    @TableField("response_data")
    var responseData: String? = null

    /**
     * 接口执行时长(毫秒)
     */
    @TableField("execution_time")
    var executionTime: Long? = null

    /**
     * 接口调用是否成功
     */
    @TableField("is_success")
    var isSuccess: Boolean? = null

    /**
     * 错误信息
     */
    @TableField("error_message")
    var errorMessage: String? = null

    /**
     * 创建时间
     */
    @TableField("create_time")
    var createTime: LocalDateTime? = null

    /**
     * 更新时间
     */
    @TableField("update_time")
    var updateTime: LocalDateTime? = null
}
