package com.langhuan.filter

import cn.hutool.json.JSONUtil
import com.langhuan.common.Result
import com.langhuan.utils.other.JwtUtil
import jakarta.annotation.Resource
import jakarta.servlet.ServletException
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * 自定义登录成功处理器
 * 在用户成功登录后，处理相关的业务逻辑
 */
@Component
class LoginSuccessHandler : AuthenticationSuccessHandler {

    /**
     * JwtUtil工具类，用于生成和解析JWT令牌
     */
    @Resource
    private lateinit var jwtUtil: JwtUtil

    /**
     * 当用户成功通过身份验证后调用此方法
     * 主要功能包括设置响应内容类型，生成JWT令牌，构建成功登录的响应结果，并将其写入到响应流中
     *
     * @param httpServletRequest 用于获取请求信息的HttpServletRequest对象
     * @param httpServletResponse 用于设置响应信息的HttpServletResponse对象
     * @param authentication 身份验证对象，包含用户详细信息
     * @throws IOException 当输出流操作失败时抛出
     * @throws ServletException 当处理过程中发生Servlet异常时抛出
     */
    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        authentication: Authentication
    ) {
        // 设置响应内容类型为JSON，并指定字符集为UTF-8
        httpServletResponse.contentType = "application/json;charset=UTF-8"

        // 生成token，并放置到请求头中
        val token = jwtUtil.generateToken(authentication.name)
        httpServletResponse.setHeader(JwtUtil.HEADER, token)

        // 构建登录成功的响应结果
        val result = Result.success("SuccessLogin")

        // 获取响应的输出流，用于写入响应数据
        val outputStream: ServletOutputStream = httpServletResponse.outputStream
        // 将结果对象转换为JSON字符串，并写入到输出流中
        outputStream.write(JSONUtil.toJsonStr(result).toByteArray(StandardCharsets.UTF_8))
        // 刷新输出流，确保数据被写入
        outputStream.flush()
        // 关闭输出流
        outputStream.close()
    }
}
