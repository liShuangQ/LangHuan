package com.shuangqi.aiagent7.filter;

import cn.hutool.core.util.StrUtil;
import com.shuangqi.aiagent7.common.BaseException;
import com.shuangqi.aiagent7.common.ResponseCodeEnum;
import com.shuangqi.aiagent7.service.AccountUserDetailsService;
import com.shuangqi.aiagent7.utils.JwtUtil;
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

    private JwtUtil jwtUtil;

    private final AccountUserDetailsService accountUserDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AccountUserDetailsService accountUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.accountUserDetailsService = accountUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException, ServletException {
        String token = request.getHeader(JwtUtil.HEADER);
        // 未获取到token，继续往后走，因为后面还有鉴权管理器等去判断是否拥有身份凭证，所以可以放行
        // 没有token相当于匿名访问，若有一些接口是需要权限的，则不能访问这些接口
        if (StrUtil.isBlankOrUndefined(token)) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtil.getClaimsByToken(token);
        if (claims == null) {
            throw new BaseException(ResponseCodeEnum.BAD_REQUEST, "token异常");
        }
        if (jwtUtil.isTokenExpired(claims.getExpiration())) {
            throw new BaseException(ResponseCodeEnum.BAD_REQUEST, "token已过期");
        }

        String username = claims.getSubject();

        // 构建UsernamePasswordAuthenticationToken，这里密码为null，是因为提供了正确的token，实现自动登录
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, accountUserDetailsService.getUserAuthority(username));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}