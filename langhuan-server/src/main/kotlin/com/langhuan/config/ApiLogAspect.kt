package com.langhuan.config

import cn.hutool.core.util.StrUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.langhuan.common.ApiLog
import com.langhuan.common.GlobalExceptionHandler
import com.langhuan.model.domain.TApiLog
import com.langhuan.service.TApiLogService
import com.langhuan.utils.date.DateTimeUtils
import jakarta.servlet.http.HttpServletRequest
import lombok.extern.slf4j.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * 接口调用日志AOP切面
 * 拦截带有@ApiLog注解的方法，记录接口调用日志
 * <p>
 * 使用示例：
 *
 * @author system
 * @ApiLog(apiName = "用户注册", description = "新用户注册接口", logResponse = false, logRequest = true)
 * public String register(String username, String password) {
 * // 业务逻辑
 * return "success";
 * }
 */
@Aspect
@Component
class ApiLogAspect(
    private val apiLogService: TApiLogService,
    objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(ApiLogAspect::class.java)
    }


    private val objectMapper: ObjectMapper

    init {
        this.objectMapper = objectMapper
        // 配置ObjectMapper忽略空的beans，避免序列化失败
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    }

    /**
     * 环绕通知，拦截带有@ApiLog注解的方法
     *
     * @param joinPoint 连接点
     * @param apiLog    注解信息
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(apiLog)")
    @Throws(Throwable::class)
    fun around(joinPoint: ProceedingJoinPoint, apiLog: ApiLog): Any? {
        val startTime = System.currentTimeMillis()

        // 获取请求信息
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val request = attributes?.request

        // 构建日志对象
        val logEntity = buildApiLog(joinPoint, apiLog, request)

        var result: Any? = null
        var isSuccess = true
        var errorMessage: String? = null

        try {
            // 执行目标方法
            result = joinPoint.proceed()
        } catch (e: Exception) {
            isSuccess = false
            errorMessage = e.message
            log.error("接口执行异常: {}", e.message, e)
            throw e
        } finally {
            // 计算执行时长
            val executionTime = System.currentTimeMillis() - startTime

            // 完善日志信息
            completeApiLog(logEntity, joinPoint, apiLog, result, executionTime, isSuccess, errorMessage)

            // 异步保存日志
            apiLogService.saveApiLogAsync(logEntity)
        }

        return result
    }

    /**
     * 构建基础接口日志对象
     *
     * @param joinPoint 连接点
     * @param apiLog    注解信息
     * @param request   HTTP请求
     * @return 日志对象
     */
    private fun buildApiLog(joinPoint: ProceedingJoinPoint, apiLog: ApiLog, request: HttpServletRequest?): TApiLog {
        val logEntity = TApiLog()

        // 设置接口名称
        val apiName = if (StrUtil.isNotBlank(apiLog.apiName)) apiLog.apiName else joinPoint.signature.name
        logEntity.apiName = apiName

        // 设置接口说明
        logEntity.apiDescription = apiLog.description

        // 设置请求信息
        request?.let {
            logEntity.apiUrl = it.requestURI
            logEntity.httpMethod = it.method
            logEntity.requestIp = getClientIpAddress(it)
        }

        // 设置用户信息
        setUserInfo(logEntity)

        // 设置请求参数
        if (apiLog.logRequest) {
            setRequestParams(logEntity, joinPoint)
        }

        logEntity.createTime = DateTimeUtils.now()
        logEntity.updateTime = DateTimeUtils.now()

        return logEntity
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
    private fun completeApiLog(
        logEntity: TApiLog,
        joinPoint: ProceedingJoinPoint,
        apiLog: ApiLog,
        result: Any?,
        executionTime: Long,
        isSuccess: Boolean,
        errorMessage: String?
    ) {
        // 设置执行结果
        logEntity.executionTime = executionTime
        logEntity.isSuccess = isSuccess
        logEntity.errorMessage = errorMessage
        logEntity.updateTime = DateTimeUtils.now()

        // 设置响应参数
        if (apiLog.logResponse && result != null) {
            setResponseData(logEntity, result)
        }
    }

    /**
     * 设置用户信息
     *
     * @param logEntity 日志对象
     */
    private fun setUserInfo(logEntity: TApiLog) {
        try {
            val authentication: Authentication = SecurityContextHolder.getContext().authentication
            logEntity.userId = authentication.name
            logEntity.username = authentication.name
        } catch (e: Exception) {
            log.warn("获取用户信息失败: {}", e.message)
        }
    }

    /**
     * 设置请求参数
     * HACK 注意屏蔽的类型
     * @param logEntity 日志对象
     * @param joinPoint 连接点
     */
    private fun setRequestParams(logEntity: TApiLog, joinPoint: ProceedingJoinPoint) {
        try {
            val args = joinPoint.args
            if (!args.isNullOrEmpty()) {
                // 过滤掉HttpServletRequest、HttpServletResponse等不需要记录的参数
                val filteredArgs = args.filter { arg ->
                    arg != null &&
                            arg !is HttpServletRequest &&
                            arg !is jakarta.servlet.http.HttpServletResponse &&
                            arg !is File &&
                            arg !is InputStream &&
                            arg !is OutputStream &&
                            arg !is Socket &&
                            arg !is MultipartFile &&
                            !(arg is Array<*> && arg.isArrayOf<MultipartFile>())
                }.toTypedArray()
                
                if (filteredArgs.isNotEmpty()) {
                    val requestParams = objectMapper.writeValueAsString(filteredArgs)
                    logEntity.requestParams = requestParams
                }
            }
        } catch (e: Exception) {
            log.warn("序列化请求参数失败: {}", e.message)
            logEntity.requestParams = "序列化失败: " + e.message
        }
    }

    /**
     * 设置响应数据
     *
     * @param logEntity 日志对象
     * @param result    方法执行结果
     */
    private fun setResponseData(logEntity: TApiLog, result: Any) {
        // 过滤掉不可序列化的对象
        if (result is File ||
            result is InputStream ||
            result is OutputStream ||
            result is Socket ||
            result is MultipartFile ||
            result is Array<*> && result.isArrayOf<MultipartFile>()
        ) {
            logEntity.responseData = "不可序列化的对象类型: " + result::class.java.name
            return
        }

        try {
            val responseData = objectMapper.writeValueAsString(result)
            logEntity.responseData = responseData
        } catch (e: Exception) {
            log.warn("序列化响应数据失败: {}", e.message)
            logEntity.responseData = "序列化失败: " + e.message
        }
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (StrUtil.isNotBlank(xForwardedFor) && "unknown" != xForwardedFor) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (StrUtil.isNotBlank(xRealIp) && "unknown" != xRealIp) {
            return xRealIp
        }

        val proxyClientIp = request.getHeader("Proxy-Client-IP")
        if (StrUtil.isNotBlank(proxyClientIp) && "unknown" != proxyClientIp) {
            return proxyClientIp
        }

        val wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP")
        if (StrUtil.isNotBlank(wlProxyClientIp) && "unknown" != wlProxyClientIp) {
            return wlProxyClientIp
        }

        return request.remoteAddr
    }
}
