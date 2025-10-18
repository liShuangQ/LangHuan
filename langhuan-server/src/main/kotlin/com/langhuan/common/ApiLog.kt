package com.langhuan.common

import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import java.lang.annotation.Documented

/**
 * 接口调用日志统计注解
 * 用于标记需要记录调用日志的接口
 *
 * @author system
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Documented
annotation class ApiLog(
    /**
     * 接口名称
     * 默认为空，如果为空则使用方法名
     */
    val apiName: String = "",

    /**
     * 接口说明描述
     * 默认为空
     */
    val description: String = "",

    /**
     * 是否记录接口出参
     * 默认为false，不记录出参
     */
    val logResponse: Boolean = false,

    /**
     * 是否记录接口入参
     * 默认为true，记录入参
     */
    val logRequest: Boolean = true
)
