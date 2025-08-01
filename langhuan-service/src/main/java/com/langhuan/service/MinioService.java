package com.langhuan.service;

import com.langhuan.utils.minio.MinioUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.minio.MinioClient;

import java.io.InputStream;


/**
 * @author Afish
 * @date 2025/7/28 17:24
 */
@Service
@Slf4j
public class MinioService {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.img-bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    private final MinioUtils minioUtils;

    // 使用构造函数注入
    public MinioService(MinioClient minioClient, @Value("${minio.url}") String minioUrl, @Value("${minio.img-bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.minioUrl = minioUrl;
        this.minioUtils = new MinioUtils(minioClient, bucketName, minioUrl);
    }

    public void handleUpload(String objectName, InputStream inputStream, long size) throws Exception {
        // 可以在此添加额外的业务逻辑
        minioUtils.uploadFile(objectName, inputStream, size);
    }

    public InputStream handleDownload(String objectName) throws Exception {
        // 可以在此添加额外的业务逻辑
        return minioUtils.downloadFile(objectName);
    }

    public void handleDelete(String objectName) throws Exception {
        // 可以在此添加额外的业务逻辑
        minioUtils.deleteFile(objectName);
    }

    public void ensureBucketExists() throws Exception {
        minioUtils.createBucket();
    }

    public String extractObjectName(String url){
        return minioUtils.extractObjectName(url);
    }
}
