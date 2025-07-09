package com.langhuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 接口调用日志统计实体类
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_api_log")
public class TApiLog {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口名称
     */
    @TableField("api_name")
    private String apiName;

    /**
     * 接口URL
     */
    @TableField("api_url")
    private String apiUrl;

    /**
     * 接口说明
     */
    @TableField("api_description")
    private String apiDescription;

    /**
     * HTTP请求方法
     */
    @TableField("http_method")
    private String httpMethod;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 请求IP地址
     */
    @TableField("request_ip")
    private String requestIp;

    /**
     * 接口入参JSON字符串
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 接口出参JSON字符串
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 接口执行时长(毫秒)
     */
    @TableField("execution_time")
    private Long executionTime;

    /**
     * 接口调用是否成功
     */
    @TableField("is_success")
    private Boolean isSuccess;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
} 