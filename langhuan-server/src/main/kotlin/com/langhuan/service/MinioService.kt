package com.langhuan.service

import com.langhuan.utils.minio.MinioUtils
import io.minio.MinioClient
import jakarta.annotation.Resource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream

/**
 * @author Afish
 * @date 2025/7/28 17:24
 */
@Service
class MinioService {
    
    companion object {
        private val log = LoggerFactory.getLogger(MinioService::class.java)
    }

    @Resource
    private var minioClient: MinioClient? = null

    @Value("\${minio.url}")
    private var minioUrl: String? = null

    private val minioUtils: MinioUtils

    // 使用构造函数注入
    constructor(minioClient: MinioClient, @Value("\${minio.url}") minioUrl: String) {
        this.minioClient = minioClient
        this.minioUrl = minioUrl
        this.minioUtils = MinioUtils(minioClient, minioUrl)
    }

    @Throws(Exception::class)
    fun handleUpload(objectName: String, inputStream: InputStream, size: Long, bucketName: String) {
        // 可以在此添加额外的业务逻辑
        minioUtils.uploadFile(objectName, inputStream, size, bucketName)
    }

    @Throws(Exception::class)
    fun handleDownload(objectName: String, bucketName: String): InputStream {
        // 可以在此添加额外的业务逻辑
        return minioUtils.downloadFile(objectName, bucketName) as InputStream
    }

    @Throws(Exception::class)
    fun handleDelete(objectName: String, bucketName: String) {
        // 可以在此添加额外的业务逻辑
        minioUtils.deleteFile(objectName, bucketName)
    }

    @Throws(Exception::class)
    fun ensureBucketExists(bucketName: String) {
        minioUtils.createBucket(bucketName)
    }

    fun extractObjectName(url: String, bucketName: String): String {
        return minioUtils.extractObjectName(url, bucketName) as String
    }

    fun fileExists(objectName: String, bucketName: String): Boolean {
        return minioUtils.fileExists(objectName, bucketName) as Boolean
    }

    // 生成minio访问链接
    fun generateMinioUrl(objectName: String, bucketName: String): String {
        return minioUtils.generateMinioUrl(objectName, bucketName) as String
    }
    
    // 删除文件夹下所有文件和文件夹本身
    @Throws(Exception::class)
    fun deleteFolder(folderName: String, bucketName: String) {
        minioUtils.deleteFolder(folderName, bucketName)
    }
}
