package com.langhuan.config

import com.langhuan.common.GlobalExceptionHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

class WebLogInterceptor : HandlerInterceptor {
    companion object {
        private val log = LoggerFactory.getLogger(WebLogInterceptor::class.java)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestURI = request.requestURI
        val method = request.method
        log.info("Request: {} {}", method, requestURI)
        request.setAttribute("startTime", System.currentTimeMillis())
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        val startTime = request.getAttribute("startTime") as Long?
        val endTime = System.currentTimeMillis()
        log.info("Response: {} {} took {}ms", request.method, request.requestURI, endTime - (startTime ?: 0))
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (ex != null) {
            log.error("Request Error: {} {}", request.method, request.requestURI, ex)
        }
    }
}
