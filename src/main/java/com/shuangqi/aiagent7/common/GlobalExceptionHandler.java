package com.shuangqi.aiagent7.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("System Exception: uri={}", request.getRequestURI(), e);
        return Result.error(500, "System Error");
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("Business Exception: uri={}, code={}, message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public Result<?> handlerGlobalException(HttpServletResponse response, BaseException e) {
        log.error("BaseException请求异常：", e);
        response.setStatus(e.getResponseCode().getCode());

        return Result.error(e.getResponseCode().getCode(), e.getResponseCode().getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handlerGlobalException(Exception e) {
        log.error("AccessDeniedException请求异常：", e);
        return Result.error(401, e.getMessage());

    }


}
