package com.langhuan.filter

import cn.hutool.json.JSONUtil
import com.langhuan.common.Result
import jakarta.servlet.ServletException
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets

// 自定义登录失败处理类
@Component
class LoginFailureHandler : AuthenticationFailureHandler {

    /**
     * 当认证失败时调用的方法
     *
     * @param httpServletRequest 请求对象，包含本次请求的所有信息
     * @param httpServletResponse 响应对象，用于向客户端发送响应数据
     * @param e 认证失败的异常，可以获取详细的错误信息
     * @throws IOException 当输出流操作失败时抛出
     * @throws ServletException 当处理认证失败的过程中发生Servlet相关的错误时抛出
     */
    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        e: AuthenticationException
    ) {
        // 设置响应内容类型为JSON格式，并指定字符集为UTF-8
        httpServletResponse.contentType = "application/json;charset=UTF-8"

        // 创建一个错误结果对象，用于告知客户端登录失败的原因
        val result = Result.error<Any>("用户名或密码错误")

        // 获取响应的输出流，用于向客户端发送响应数据
        val outputStream: ServletOutputStream = httpServletResponse.outputStream
        // 将错误结果对象转换为JSON字符串，并写入输出流
        outputStream.write(JSONUtil.toJsonStr(result).toByteArray(StandardCharsets.UTF_8))
        // 刷新输出流，确保数据被发送到客户端
        outputStream.flush()
        // 关闭输出流，释放系统资源
        outputStream.close()
    }
}
