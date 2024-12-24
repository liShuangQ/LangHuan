package com.shuangqi.aiagent7.filter;

import cn.hutool.json.JSONUtil;
import com.shuangqi.aiagent7.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//认证/授权

/**
 * 自定义的访问拒绝处理器，用于处理JWT认证中的访问拒绝情况
 * 该类实现了AccessDeniedHandler接口，用于定制化处理访问拒绝响应
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 处理访问拒绝的情况
     * 当用户请求的资源没有足够的权限访问时，此方法将被调用
     * 它负责向响应中写入禁止访问（403）的状态码，并以JSON格式返回错误信息
     *
     * @param httpServletRequest  用于获取请求信息的HttpServletRequest对象
     * @param httpServletResponse 用于设置响应信息的HttpServletResponse对象
     * @param e                   访问拒绝异常，包含错误的详细信息
     * @throws IOException      如果在处理响应期间发生I/O错误
     * @throws ServletException 如果在处理响应期间发生Servlet错误
     */
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        // 设置响应的内容类型和字符集
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        // 设置响应的状态码为403禁止访问
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 创建一个错误结果对象，包含错误信息
        Result<Object> result = Result.error(e.getMessage());

        // 获取响应的输出流，用于写入JSON格式的错误信息
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        // 将错误结果对象转换为JSON字符串，并写入响应中
        outputStream.write(JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8));
        // 刷新输出流，确保数据被写入响应中
        outputStream.flush();
        // 关闭输出流
        outputStream.close();
    }
}
