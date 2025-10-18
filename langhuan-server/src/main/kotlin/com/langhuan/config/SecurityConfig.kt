package com.langhuan.config

import com.langhuan.common.Constant
import com.langhuan.filter.*
import com.langhuan.service.AccountUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

// 配置类，启用Web安全性并激活方法级别的安全性
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    // 依赖注入：用户详细服务、JWT认证过滤器、JWT登出成功处理器、JWT访问拒绝处理器、登录成功处理器、登录失败处理器、JWT认证入口点
    private val accountUserDetailsService: AccountUserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtLogoutSuccessHandler: JwtLogoutSuccessHandler,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val loginSuccessHandler: LoginSuccessHandler,
    private val loginFailureHandler: LoginFailureHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {
    
    // 白名单URL，不需要身份验证即可访问
    private val urlWhitelist: Array<String> = Constant.URL_WHITELIST

    // [已注释]
    // @Bean fun passwordEncoder(): PasswordEncoder {
    //     return BCryptPasswordEncoder()
    // }

    /**
     * 跨域配置
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*") // 允许的源，可以根据需要修改
//        configuration.allowedMethods = listOf("*") // 允许的 HTTP 方法
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法

        configuration.allowedHeaders = listOf("*") // 允许的请求头
        configuration.allowCredentials = false // 是否允许发送 Cookie

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration) // 配置所有路径

        return source
    }

    /**
     * 配置身份验证提供者
     *
     * @return 身份校验机制、身份验证提供程序
     */
    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        // 创建一个用户认证提供者
        val authProvider = DaoAuthenticationProvider()
        // 设置用户相关信息，可以从数据库中读取、或者缓存、或者配置文件
        authProvider.setUserDetailsService(accountUserDetailsService)
        // 设置加密机制，用于对用户进行身份验证
        //authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
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
    @Throws(Exception::class)
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    /**
     * 配置SecurityFilterChain，定制化安全过滤链
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http    // 配置CORS
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            // 禁用csrf(防止跨站请求伪造攻击)
            .csrf { csrf -> csrf.disable() }
            // 登录操作
            .formLogin { form -> 
                form.successHandler(loginSuccessHandler)
                    .failureHandler(loginFailureHandler)
            }
            // 登出操作
            .logout { logout -> logout.logoutSuccessHandler(jwtLogoutSuccessHandler) }
            // 使用无状态session，即不使用session缓存数据
            .sessionManagement { session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // 设置白名单
            .authorizeHttpRequests { auth -> 
                auth.requestMatchers(*urlWhitelist).permitAll()
                    .anyRequest().authenticated()
            }
            // 异常处理器
            .exceptionHandling { exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            }
            // 添加身份验证提供者
            .authenticationProvider(authenticationProvider())
            // 添加jwt过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
