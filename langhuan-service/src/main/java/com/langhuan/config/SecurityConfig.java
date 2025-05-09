package com.langhuan.config;

import com.langhuan.filter.*;
import com.langhuan.common.Constant;
import com.langhuan.service.AccountUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

// 配置类，启用Web安全性并激活方法级别的安全性
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // 白名单URL，不需要身份验证即可访问
    private static final String[] URL_WHITELIST = Constant.URL_WHITELIST;

    // 依赖注入：用户详细服务、JWT认证过滤器、JWT登出成功处理器、JWT访问拒绝处理器、登录成功处理器、登录失败处理器、JWT认证入口点
    private final AccountUserDetailsService accountUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // 构造函数进行依赖注入
    public SecurityConfig(AccountUserDetailsService accountUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter, JwtLogoutSuccessHandler jwtLogoutSuccessHandler, JwtAccessDeniedHandler jwtAccessDeniedHandler, LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.accountUserDetailsService = accountUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtLogoutSuccessHandler = jwtLogoutSuccessHandler;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    // [已注释]
    // @Bean public PasswordEncoder passwordEncoder() {
    // return new BCryptPasswordEncoder();
    // }

    /**
     * 跨域配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // 允许的源，可以根据需要修改
//        configuration.setAllowedMethods(Arrays.asList("*")); // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的 HTTP 方法

        configuration.setAllowedHeaders(Arrays.asList("*")); // 允许的请求头
        configuration.setAllowCredentials(false); // 是否允许发送 Cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 配置所有路径

        return source;
    }

    /**
     * 配置身份验证提供者
     *
     * @return 身份校验机制、身份验证提供程序
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // 创建一个用户认证提供者
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 设置用户相关信息，可以从数据库中读取、或者缓存、或者配置文件
        authProvider.setUserDetailsService(accountUserDetailsService);
        // 设置加密机制，用于对用户进行身份验证
        //authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 配置AuthenticationManager，用于处理身份验证请求
     * 基于用户名和密码或使用用户名和密码进行身份验证
     *
     * @param config
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置SecurityFilterChain，定制化安全过滤链
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http    // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 禁用csrf(防止跨站请求伪造攻击)
                .csrf(AbstractHttpConfigurer::disable)
                // 登录操作
                .formLogin(form -> form.successHandler(loginSuccessHandler).failureHandler(loginFailureHandler))
                // 登出操作
                .logout(logout -> logout.logoutSuccessHandler(jwtLogoutSuccessHandler))
                // 使用无状态session，即不使用session缓存数据
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 设置白名单
                .authorizeHttpRequests(auth -> auth.requestMatchers(URL_WHITELIST).permitAll().anyRequest().authenticated())
                // 异常处理器
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(jwtAccessDeniedHandler))
                // 添加身份验证提供者
                .authenticationProvider(authenticationProvider())
                // 添加jwt过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }


}
