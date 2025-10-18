package com.langhuan.common

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {
    
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): Result<Any> {
        log.error("System Exception: uri={}", request.requestURI, e)
        return Result.error(500, "System Error")
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException, request: HttpServletRequest): Result<Any> {
        log.error("Business Exception: uri={}, code={}, message={}", request.requestURI, e.code, e.message)
        return Result.error(e.code, e.message)
    }

    @ExceptionHandler(BaseException::class)
    fun handlerGlobalException(response: HttpServletResponse, e: BaseException): Result<Any> {
        log.error("BaseException请求异常：", e)
        response.status = e.responseCode?.code ?: 500

        return Result.error(e.responseCode?.code ?: 500, e.responseCode?.message ?: "Unknown Error")
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handlerGlobalException(e: Exception): Result<Any> {
        log.error("AccessDeniedException请求异常：", e)
        return Result.error(401, e.message ?: "Access Denied")
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(ex: AuthorizationDeniedException): Result<Any> {
        log.error("AuthorizationDeniedException请求异常：", ex)
        return Result.error(HttpStatus.FORBIDDEN.value(), ex.message ?: "Authorization Denied")
    }

    @ExceptionHandler(TokenExpiredException::class)
    fun handleTokenExpiredException(ex: TokenExpiredException): Result<Any> {
        log.error("TokenExpiredException请求异常：", ex)
        return Result.error(ex.responseCode!!.code, ex.responseCode!!.message)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(ex: ExpiredJwtException): Result<Any> {
        log.error("ExpiredJwtException请求异常：", ex)
        return Result.error(ResponseCodeEnum.UNAUTHORIZED.code, "Token已过期，请重新登录")
    }
}
