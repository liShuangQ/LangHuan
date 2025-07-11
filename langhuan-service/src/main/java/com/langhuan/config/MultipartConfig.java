package com.langhuan.config;


import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 设置单个文件最大为10MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // 设置整个请求最大为10MB
        return factory.createMultipartConfig();
    }
}