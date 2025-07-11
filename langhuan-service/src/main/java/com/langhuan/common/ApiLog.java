package com.langhuan.common;

import java.lang.annotation.*;

/**
 * 接口调用日志统计注解
 * 用于标记需要记录调用日志的接口
 *
 * @author system
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {

    /**
     * 接口名称
     * 默认为空，如果为空则使用方法名
     */
    String apiName() default "";

    /**
     * 接口说明描述
     * 默认为空
     */
    String description() default "";

    /**
     * 是否记录接口出参
     * 默认为true，记录出参
     */
    boolean logResponse() default false;

    /**
     * 是否记录接口入参
     * 默认为true，记录入参
     */
    boolean logRequest() default true;
} 