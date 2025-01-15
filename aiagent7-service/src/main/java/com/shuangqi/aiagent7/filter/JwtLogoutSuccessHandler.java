package com.shuangqi.aiagent7.filter;

import cn.hutool.json.JSONUtil;
import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.utils.other.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JwtLogoutSuccessHandler 类实现了 LogoutSuccessHandler 接口，
 * 用于在用户成功登出后处理相关的逻辑，特别是与 JWT（JSON Web Token）相关的操作。
 * 该类被标记为 @Component，表示它是一个 Spring 管理的组件。
 */
@Component
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    /**
     * 在用户成功登出时调用的方法。
     * 该方法负责清理安全上下文，并向响应中写入成功登出的信息。
     *
     * @param httpServletRequest  HTTP 请求对象，可用于获取请求信息。
     * @param httpServletResponse HTTP 响应对象，用于向客户端发送响应。
     * @param authentication      用户认证信息，可能为 null，表示没有提供认证信息。
     * @throws IOException        如果在写入响应过程中发生 I/O 错误。
     * @throws ServletException 如果在处理登出过程中发生 Servlet 错误。
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        // 如果提供了认证信息，则调用 SecurityContextLogoutHandler 来处理登出逻辑。
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(httpServletRequest, httpServletResponse, authentication);
        }

        // 设置响应内容类型为 JSON，并清理 JWT Token。
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setHeader(JwtUtil.HEADER, "");
        SecurityContextHolder.clearContext();

        // 创建一个表示成功登出的结果对象。
        Result result = Result.success("SuccessLogout");

        // 获取响应的输出流，用于写入 JSON 格式的响应体。
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        outputStream.write(JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}
