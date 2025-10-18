package com.langhuan.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Afish
 * @date 2025/7/28 17:23
 */
@Configuration
class MinioConfig {
    
    @Value("\${minio.url}")
    private lateinit var minioUrl: String

    @Value("\${minio.access-key}")
    private lateinit var accessKey: String

    @Value("\${minio.secret-key}")
    private lateinit var secretKey: String

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(minioUrl)
            .credentials(accessKey, secretKey)
            .build()
    }
}
