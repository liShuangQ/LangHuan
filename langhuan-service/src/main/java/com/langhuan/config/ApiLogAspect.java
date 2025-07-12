package com.langhuan.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.common.ApiLog;
import com.langhuan.model.domain.TApiLog;
import com.langhuan.model.pojo.AccountUser;
import com.langhuan.service.TApiLogService;
import com.langhuan.utils.date.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * 接口调用日志AOP切面
 * 拦截带有@ApiLog注解的方法，记录接口调用日志
 *
 * 使用示例：
 * @ApiLog(apiName = "用户注册", description = "新用户注册接口", logResponse = false, logRequest = true)
 * public String register(String username, String password) {
 *     // 业务逻辑
 *     return "success";
 * }
 * 
 * @author system
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    private final TApiLogService apiLogService;
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知，拦截带有@ApiLog注解的方法
     *
     * @param joinPoint 连接点
     * @param apiLog    注解信息
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(apiLog)")
    public Object around(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.nonNull(attributes) ? attributes.getRequest() : null;

        // 构建日志对象
        TApiLog logEntity = buildApiLog(joinPoint, apiLog, request);

        Object result = null;
        boolean isSuccess = true;
        String errorMessage = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Exception e) {
            isSuccess = false;
            errorMessage = e.getMessage();
            log.error("接口执行异常: {}", e.getMessage(), e);
            throw e;
        } finally {
            // 计算执行时长
            long executionTime = System.currentTimeMillis() - startTime;

            // 完善日志信息
            completeApiLog(logEntity, joinPoint, apiLog, result, executionTime, isSuccess, errorMessage);

            // 异步保存日志
            apiLogService.saveApiLogAsync(logEntity);
        }

        return result;
    }

    /**
     * 构建基础接口日志对象
     *
     * @param joinPoint 连接点
     * @param apiLog    注解信息
     * @param request   HTTP请求
     * @return 日志对象
     */
    private TApiLog buildApiLog(ProceedingJoinPoint joinPoint, ApiLog apiLog, HttpServletRequest request) {
        TApiLog logEntity = new TApiLog();

        // 设置接口名称
        String apiName = StrUtil.isNotBlank(apiLog.apiName()) ? apiLog.apiName() : joinPoint.getSignature().getName();
        logEntity.setApiName(apiName);

        // 设置接口说明
        logEntity.setApiDescription(apiLog.description());

        // 设置请求信息
        if (Objects.nonNull(request)) {
            logEntity.setApiUrl(request.getRequestURI());
            logEntity.setHttpMethod(request.getMethod());
            logEntity.setRequestIp(getClientIpAddress(request));
        }

        // 设置用户信息
        setUserInfo(logEntity);

        // 设置请求参数
        if (apiLog.logRequest()) {
            setRequestParams(logEntity, joinPoint);
        }

        logEntity.setCreateTime(DateTimeUtils.now());
        logEntity.setUpdateTime(DateTimeUtils.now());

        return logEntity;
    }

    /**
     * 完善接口日志信息
     *
     * @param logEntity     日志对象
     * @param joinPoint     连接点
     * @param apiLog        注解信息
     * @param result        方法执行结果
     * @param executionTime 执行时长
     * @param isSuccess     是否成功
     * @param errorMessage  错误信息
     */
    private void completeApiLog(TApiLog logEntity, ProceedingJoinPoint joinPoint, ApiLog apiLog,
            Object result, long executionTime, boolean isSuccess, String errorMessage) {
        // 设置执行结果
        logEntity.setExecutionTime(executionTime);
        logEntity.setIsSuccess(isSuccess);
        logEntity.setErrorMessage(errorMessage);
        logEntity.setUpdateTime(DateTimeUtils.now());

        // 设置响应参数
        if (apiLog.logResponse() && Objects.nonNull(result)) {
            setResponseData(logEntity, result);
        }
    }

    /**
     * 设置用户信息
     *
     * @param logEntity 日志对象
     */
    private void setUserInfo(TApiLog logEntity) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logEntity.setUserId(authentication.getName());
            logEntity.setUsername(authentication.getName());
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }
    }

    /**
     * 设置请求参数
     *
     * @param logEntity 日志对象
     * @param joinPoint 连接点
     */
    private void setRequestParams(TApiLog logEntity, ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (Objects.nonNull(args) && args.length > 0) {
                // 过滤掉HttpServletRequest、HttpServletResponse等不需要记录的参数
                Object[] filteredArgs = Arrays.stream(args)
                        .filter(arg -> Objects.nonNull(arg) &&
                                !(arg instanceof HttpServletRequest) &&
                                !(arg instanceof jakarta.servlet.http.HttpServletResponse))
                        .toArray();

                if (filteredArgs.length > 0) {
                    String requestParams = objectMapper.writeValueAsString(filteredArgs);
                    logEntity.setRequestParams(requestParams);
                }
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败: {}", e.getMessage());
            logEntity.setRequestParams("序列化失败: " + e.getMessage());
        }
    }

    /**
     * 设置响应数据
     *
     * @param logEntity 日志对象
     * @param result    方法执行结果
     */
    private void setResponseData(TApiLog logEntity, Object result) {
        try {
            String responseData = objectMapper.writeValueAsString(result);
            logEntity.setResponseData(responseData);
        } catch (Exception e) {
            log.warn("序列化响应数据失败: {}", e.getMessage());
            logEntity.setResponseData("序列化失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StrUtil.isNotBlank(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (StrUtil.isNotBlank(proxyClientIp) && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }

        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (StrUtil.isNotBlank(wlProxyClientIp) && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }

        return request.getRemoteAddr();
    }
}