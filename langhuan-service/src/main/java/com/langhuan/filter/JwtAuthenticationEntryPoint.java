package com.langhuan.filter;

import cn.hutool.json.JSONUtil;
import com.langhuan.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证入口点类，用于处理未授权的请求
 * 该类实现了AuthenticationEntryPoint接口，以定义当用户尝试访问受保护资源时的入口点行为
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 处理未授权的请求，向响应中写入错误信息
     * 当用户尝试访问受保护的资源但未提供有效的认证信息时，此方法将被调用
     * 它负责设置响应的内容类型、状态码，并将错误信息以JSON格式写入响应体中
     *
     * @param httpServletRequest 请求对象，包含请求信息
     * @param httpServletResponse 响应对象，用于向客户端发送响应
     * @param e 认证异常，提供有关认证失败的信息
     * @throws IOException 如果在处理响应期间发生I/O错误
     * @throws ServletException 如果在处理请求期间发生Servlet特定的错误
     */
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

        // 设置响应的内容类型和状态码
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 创建一个错误结果对象，用于向客户端指示认证失败
        Result result = Result.error("请先登录");

        // 获取响应的输出流，准备向客户端发送错误信息
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        // 将错误结果对象转换为JSON字符串，并以UTF-8编码写入响应体中
        outputStream.write(JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8));
        // 确保所有数据被写入并关闭输出流
        outputStream.flush();
        outputStream.close();
    }
}
