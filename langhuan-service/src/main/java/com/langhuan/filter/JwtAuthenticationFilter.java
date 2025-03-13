package com.langhuan.filter;

import cn.hutool.core.util.StrUtil;
import com.langhuan.common.BaseException;
import com.langhuan.common.ResponseCodeEnum;
import com.langhuan.service.AccountUserDetailsService;
import com.langhuan.utils.other.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // JwtUtil工具类，用于处理JWT令牌
    private JwtUtil jwtUtil;

    // AccountUserDetailsService服务类，用于获取用户详细信息
    private final AccountUserDetailsService accountUserDetailsService;

    /**
     * 构造函数，初始化JwtAuthenticationFilter
     *
     * @param jwtUtil 用于处理JWT的工具类实例
     * @param accountUserDetailsService 用于获取用户详细信息的服务类实例
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, AccountUserDetailsService accountUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.accountUserDetailsService = accountUserDetailsService;
    }

    /**
     * 执行过滤器的主要方法，用于验证JWT令牌并设置身份验证信息
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链对象，用于将请求传递给下一个过滤器或目标资源
     * @throws IOException 如果发生I/O错误
     * @throws ServletException 如果处理过程中发生Servlet异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException, ServletException {
        // 从请求头中获取token
        String token = request.getHeader(JwtUtil.HEADER);
        // 未获取到token，继续往后走，因为后面还有鉴权管理器等去判断是否拥有身份凭证，所以可以放行
        // 没有token相当于匿名访问，若有一些接口是需要权限的，则不能访问这些接口
        if (StrUtil.isBlankOrUndefined(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 解析token获取claims信息
        Claims claims = jwtUtil.getClaimsByToken(token);
        // 如果claims为空，则抛出异常，表示token异常
        if (claims == null) {
            throw new BaseException(ResponseCodeEnum.BAD_REQUEST, "token异常");
        }
        // 检查token是否已过期
        if (jwtUtil.isTokenExpired(claims.getExpiration())) {
            throw new BaseException(ResponseCodeEnum.BAD_REQUEST, "token已过期");
        }

        // 从claims中获取用户名
        String username = claims.getSubject();

        // 构建UsernamePasswordAuthenticationToken，这里密码为null，是因为提供了正确的token，实现自动登录
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, accountUserDetailsService.getUserAuthority(username));
        // 设置详细信息
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // 将身份验证信息设置到SecurityContextHolder中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 继续传递请求给下一个过滤器或目标资源
        chain.doFilter(request, response);
    }
}
